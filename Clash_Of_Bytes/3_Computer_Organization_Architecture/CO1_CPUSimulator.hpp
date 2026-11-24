#ifndef CO1_CPU_SIMULATOR_HPP
#define CO1_CPU_SIMULATOR_HPP

#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
#include <sstream>
#include <algorithm>
#include <stdexcept>
#include "CO1_RegisterBank.hpp"
#include "CO1_MemoryUnit.hpp"
#include "CO1_Prac01_80386_ALU_Simulator.hpp"
#include "CO4_Prac02_Control_Unit_Decoder.hpp"

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome 1 & 4 (CO1, CO4) | Complete Educational CPU Simulator
 * ==============================================================================
 * Implements a complete five-stage Instruction Execution Cycle:
 *   1. FETCH      : Instruction fetched via Instruction Pointer (RIP / EIP)
 *   2. DECODE     : Control Unit decodes Opcode, operands, and generates signals
 *   3. EXECUTE    : Reuses CO1 ALU_Simulator to compute arithmetic/logic & flags
 *   4. MEMORY     : Reads/writes memory data (LOAD / STORE) with bounds validation
 *   5. WRITE BACK : Updates destination register or memory location
 *
 * Supports sequential 5-stage simulation with full operand and flag inspection.
 */

struct PipelineStageInfo {
    std::string fetchDesc;
    std::string decodeDesc;
    std::string executeDesc;
    std::string memoryDesc;
    std::string writeBackDesc;
};

class CPUSimulator {
private:
    RegisterBank registers;
    MemoryUnit memory;
    std::vector<SimulatedInstruction> program;
    bool halted;
    size_t totalCycles;
    size_t totalInstructions;
    bool verbose;

    static std::string trim(const std::string& str) {
        size_t first = str.find_first_not_of(" \t\r\n");
        if (first == std::string::npos) return "";
        size_t last = str.find_last_not_of(" \t\r\n");
        return str.substr(first, (last - first + 1));
    }

    static std::string toUpper(std::string s) {
        for (char& c : s) c = static_cast<char>(::toupper(c));
        return s;
    }

public:
    explicit CPUSimulator(size_t memorySize = 65536, bool verboseOutput = true)
        : memory(memorySize), halted(false), totalCycles(0), totalInstructions(0), verbose(verboseOutput) {
        reset();
    }

    void reset() {
        registers.reset();
        memory.reset();
        program.clear();
        halted = false;
        totalCycles = 0;
        totalInstructions = 0;
    }

    RegisterBank& getRegisters() { return registers; }
    const RegisterBank& getRegisters() const { return registers; }

    MemoryUnit& getMemory() { return memory; }
    const MemoryUnit& getMemory() const { return memory; }

    bool isHalted() const { return halted; }
    size_t getTotalCycles() const { return totalCycles; }
    size_t getTotalInstructions() const { return totalInstructions; }

    // --------------------------------------------------------------------------
    // Assembly Program Parser
    // --------------------------------------------------------------------------
    bool loadProgram(const std::vector<std::string>& asmCode) {
        reset();
        for (size_t lineIdx = 0; lineIdx < asmCode.size(); ++lineIdx) {
            std::string line = trim(asmCode[lineIdx]);
            if (line.empty() || line[0] == ';' || line[0] == '#') continue;

            // Remove inline comments
            size_t commPos = line.find(';');
            if (commPos != std::string::npos) line = trim(line.substr(0, commPos));

            std::stringstream ss(line);
            std::string mnemonic;
            ss >> mnemonic;

            SimulatedInstruction inst;
            inst.rawMnemonic = line;
            inst.opcode = ControlUnitDecoder::parseOpcode(mnemonic);
            inst.dest = "";
            inst.src = "";
            inst.immediateVal = 0;
            inst.isImmediate = false;
            inst.targetAddress = 0;

            if (inst.opcode == Opcode::UNKNOWN) {
                std::cerr << "  [PARSER ERROR] Unknown instruction: " << mnemonic 
                          << " at line " << lineIdx + 1 << "\n";
                return false;
            }

            // Extract remaining operand string
            std::string rest;
            std::getline(ss, rest);
            rest = trim(rest);

            if (!rest.empty()) {
                size_t commaPos = rest.find(',');
                if (commaPos != std::string::npos) {
                    inst.dest = trim(rest.substr(0, commaPos));
                    inst.src  = trim(rest.substr(commaPos + 1));
                } else {
                    inst.dest = rest; // Single operand instruction (e.g. MUL RCX, INC RAX, JMP addr)
                }

                // Parse immediate values if present
                std::string targetForImm = inst.src.empty() ? inst.dest : inst.src;
                if (!targetForImm.empty()) {
                    char firstChar = targetForImm[0];
                    if (std::isdigit(firstChar) || firstChar == '-' || firstChar == '+') {
                        try {
                            inst.immediateVal = std::stoll(targetForImm, nullptr, 0);
                            inst.isImmediate = true;
                        } catch (...) {
                            inst.isImmediate = false;
                        }
                    }
                }
            }

            program.push_back(inst);
        }
        return true;
    }

    // --------------------------------------------------------------------------
    // Complete 5-Stage CPU Execution Step
    // --------------------------------------------------------------------------
    bool step() {
        if (halted) return false;

        uint64_t rip = registers.get64(RegId::RIP);
        if (rip >= program.size()) {
            halted = true;
            return false;
        }

        // ======================================================================
        // STAGE 1: INSTRUCTION FETCH (IF)
        // ======================================================================
        const SimulatedInstruction& inst = program[rip];
        registers.snapshot(); // Capture previous values for diff tracking
        PipelineStageInfo stage;
        stage.fetchDesc = "Fetched [" + std::to_string(rip) + "]: " + inst.rawMnemonic;

        // ======================================================================
        // STAGE 2: INSTRUCTION DECODE (ID) & CONTROL SIGNAL GENERATION
        // ======================================================================
        ControlSignals sigs = ControlUnitDecoder::generateSignals(inst.opcode, inst.isImmediate);
        stage.decodeDesc = "Opcode=" + std::to_string(static_cast<int>(inst.opcode)) + 
                           " | ALUOp=" + sigs.ALUOp + " | RegWrite=" + (sigs.RegWrite ? "1" : "0") +
                           " | MemRead=" + (sigs.MemRead ? "1" : "0") + " | MemWrite=" + (sigs.MemWrite ? "1" : "0");

        // Read source operands
        uint64_t srcVal = 0;
        bool destIsReg = false, srcIsReg = false;
        RegId destId = RegId::RAX, srcId = RegId::RBX;
        bool destIs32 = false, srcIs32 = false;

        destIsReg = registers.resolveRegisterName(inst.dest, destId, destIs32);
        srcIsReg  = registers.resolveRegisterName(inst.src,  srcId,  srcIs32);

        uint64_t destVal = destIsReg ? registers.get64(destId) : 0;

        if (inst.isImmediate) {
            srcVal = static_cast<uint64_t>(inst.immediateVal);
        } else if (srcIsReg) {
            srcVal = registers.get64(srcId);
        } else if (!inst.src.empty() && inst.src.front() == '[' && inst.src.back() == ']') {
            // Memory operand in src
            std::string addrStr = inst.src.substr(1, inst.src.size() - 2);
            uint64_t addr = std::stoull(addrStr, nullptr, 0);
            memory.read64(addr, srcVal);
        }

        // ======================================================================
        // STAGE 3: EXECUTE (EX) VIA CO1 ALU_SIMULATOR
        // ======================================================================
        uint64_t aluResult = 0;
        ALUFlags flags = registers.getALUFlags();
        bool branchTaken = false;
        uint64_t branchTarget = 0;

        switch (inst.opcode) {
            case Opcode::MOV:
                aluResult = srcVal;
                break;
            case Opcode::ADD:
                aluResult = ALU_Simulator::executeAdd(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::SUB:
                aluResult = ALU_Simulator::executeSub(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::MUL:
                // Single-operand MUL: RAX = RAX * Operand
                aluResult = ALU_Simulator::executeImul(registers.get64(RegId::RAX), 
                                                        destIsReg ? destVal : srcVal, flags);
                destId = RegId::RAX;
                destIsReg = true;
                registers.setALUFlags(flags);
                break;
            case Opcode::INC:
                aluResult = ALU_Simulator::executeInc(destVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::DEC:
                aluResult = ALU_Simulator::executeDec(destVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::AND:
                aluResult = ALU_Simulator::executeAnd(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::OR:
                aluResult = ALU_Simulator::executeOr(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::XOR:
                aluResult = ALU_Simulator::executeXor(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::NOT:
                aluResult = ALU_Simulator::executeNot(destVal, flags);
                break;
            case Opcode::CMP:
                ALU_Simulator::executeCmp(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::TEST:
                ALU_Simulator::executeTest(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::SHL:
                aluResult = ALU_Simulator::executeShl(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::SHR:
                aluResult = ALU_Simulator::executeShr(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::SAR:
                aluResult = ALU_Simulator::executeSar(destVal, srcVal, flags);
                registers.setALUFlags(flags);
                break;
            case Opcode::JMP:
                branchTaken = true;
                branchTarget = inst.immediateVal;
                break;
            case Opcode::JE: // JZ
                branchTaken = flags.ZF;
                branchTarget = inst.immediateVal;
                break;
            case Opcode::JNE: // JNZ
                branchTaken = !flags.ZF;
                branchTarget = inst.immediateVal;
                break;
            case Opcode::JG:
                branchTaken = (!flags.ZF && (flags.SF == flags.OF));
                branchTarget = inst.immediateVal;
                break;
            case Opcode::JL:
                branchTaken = (flags.SF != flags.OF);
                branchTarget = inst.immediateVal;
                break;
            case Opcode::JGE:
                branchTaken = (flags.SF == flags.OF);
                branchTarget = inst.immediateVal;
                break;
            case Opcode::JLE:
                branchTaken = (flags.ZF || (flags.SF != flags.OF));
                branchTarget = inst.immediateVal;
                break;
            case Opcode::HLT:
                halted = true;
                break;
            case Opcode::LOAD:
            case Opcode::STORE:
            case Opcode::NOP:
            default:
                break;
        }

        stage.executeDesc = "ALU Op=" + sigs.ALUOp + " | Result=0x" + 
                            std::to_string(aluResult) + " (" + std::to_string(aluResult) + ")" +
                            " | Flags:[ZF=" + (flags.ZF ? "1" : "0") + " CF=" + (flags.CF ? "1" : "0") +
                            " SF=" + (flags.SF ? "1" : "0") + " OF=" + (flags.OF ? "1" : "0") + "]";

        // ======================================================================
        // STAGE 4: MEMORY ACCESS (MEM)
        // ======================================================================
        stage.memoryDesc = "No Memory Access";
        if (inst.opcode == Opcode::LOAD) {
            // LOAD DestReg, [Address] or LOAD DestReg, Address
            uint64_t addr = 0;
            if (!inst.src.empty() && inst.src.front() == '[' && inst.src.back() == ']') {
                std::string addrStr = inst.src.substr(1, inst.src.size() - 2);
                addr = std::stoull(addrStr, nullptr, 0);
            } else if (inst.isImmediate) {
                addr = static_cast<uint64_t>(inst.immediateVal);
            } else if (srcIsReg) {
                addr = srcVal;
            } else if (!inst.src.empty()) {
                try {
                    addr = std::stoull(inst.src, nullptr, 0);
                } catch (...) {
                    addr = 0;
                }
            }

            if (memory.isValidAddress(addr, 8)) {
                memory.read64(addr, aluResult);
                stage.memoryDesc = "Read 64-bit value 0x" + std::to_string(aluResult) + " from Memory [0x" + std::to_string(addr) + "]";
            } else {
                std::cerr << "  [CPU ERROR] Memory LOAD out of bounds at 0x" << addr << "\n";
            }
        } else if (inst.opcode == Opcode::STORE) {
            // STORE [Address], SrcReg or STORE Address, SrcReg
            uint64_t addr = 0;
            if (!inst.dest.empty() && inst.dest.front() == '[' && inst.dest.back() == ']') {
                std::string addrStr = inst.dest.substr(1, inst.dest.size() - 2);
                addr = std::stoull(addrStr, nullptr, 0);
            } else if (inst.isImmediate) {
                addr = static_cast<uint64_t>(inst.immediateVal);
            } else if (destIsReg) {
                addr = destVal;
            } else if (!inst.dest.empty()) {
                try {
                    addr = std::stoull(inst.dest, nullptr, 0);
                } catch (...) {
                    addr = 0;
                }
            }

            if (memory.isValidAddress(addr, 8)) {
                memory.write64(addr, srcVal);
                stage.memoryDesc = "Wrote 64-bit value 0x" + std::to_string(srcVal) + " to Memory [0x" + std::to_string(addr) + "]";
            } else {
                std::cerr << "  [CPU ERROR] Memory STORE out of bounds at 0x" << addr << "\n";
            }
        }

        // ======================================================================
        // STAGE 5: WRITE BACK (WB)
        // ======================================================================
        stage.writeBackDesc = "No Register Write";
        if (sigs.RegWrite && destIsReg) {
            if (destIs32) {
                registers.set32(destId, static_cast<uint32_t>(aluResult));
            } else {
                registers.set64(destId, aluResult);
            }
            stage.writeBackDesc = "Updated Register " + inst.dest + " = 0x" + 
                                  std::to_string(aluResult) + " (" + std::to_string(aluResult) + ")";
        }

        // Update Instruction Pointer (RIP)
        if (branchTaken) {
            registers.set64(RegId::RIP, branchTarget);
        } else {
            registers.set64(RegId::RIP, rip + 1);
        }

        totalCycles += 5; // 5 pipeline stages per instruction
        totalInstructions++;

        // Detailed Instruction Execution Trace Display
        if (verbose) {
            std::cout << "  --------------------------------------------------------------\n";
            std::cout << "  CYCLE TRACE #" << totalInstructions << " | RIP: " << rip 
                      << " | INSTRUCTION: " << inst.rawMnemonic << "\n";
            std::cout << "    [1. FETCH]      " << stage.fetchDesc << "\n";
            std::cout << "    [2. DECODE]     " << stage.decodeDesc << "\n";
            std::cout << "    [3. EXECUTE]    " << stage.executeDesc << "\n";
            std::cout << "    [4. MEMORY]     " << stage.memoryDesc << "\n";
            std::cout << "    [5. WRITE BACK] " << stage.writeBackDesc << "\n";
            registers.displayChanges();
        }

        return !halted;
    }

    // Run entire program until HLT or EOF
    void run(size_t maxSteps = 1000) {
        size_t steps = 0;
        while (!halted && steps < maxSteps) {
            if (!step()) break;
            steps++;
        }
        if (steps >= maxSteps) {
            std::cerr << "  [WARNING] Execution stopped: Maximum cycle limit reached (" << maxSteps << ")\n";
        }
    }
};

#endif // CO1_CPU_SIMULATOR_HPP

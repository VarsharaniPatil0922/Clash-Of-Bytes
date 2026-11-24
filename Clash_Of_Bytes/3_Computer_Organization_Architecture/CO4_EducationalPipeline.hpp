#ifndef CO4_EDUCATIONAL_PIPELINE_HPP
#define CO4_EDUCATIONAL_PIPELINE_HPP

#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
#include <sstream>
#include "CO1_RegisterBank.hpp"
#include "CO1_MemoryUnit.hpp"
#include "CO1_Prac01_80386_ALU_Simulator.hpp"
#include "CO4_Prac02_Control_Unit_Decoder.hpp"

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome 4 (CO4) | Task 4: Complete 5-Stage Educational Processor Pipeline
 * ==============================================================================
 * Pipeline Stages:
 *   1. IF  (Instruction Fetch)   : Fetches instruction using Program Counter (PC / RIP)
 *   2. ID  (Instruction Decode)  : Decodes opcode, reads register operands, generates signals
 *   3. EX  (Execute)             : Dispatches to ALU, evaluates arithmetic/branch conditions
 *   4. MEM (Memory Access)       : Reads from or writes to primary memory (LOAD / STORE)
 *   5. WB  (Write Back)          : Writes final computation to destination register
 *
 * Inter-Stage Pipeline Latches:
 *   - IF_ID_Latch   : Passes raw instruction and PC to decode stage
 *   - ID_EX_Latch   : Passes decoded control signals, register values, and immediate data
 *   - EX_MEM_Latch  : Passes ALU computation result, memory write data, and branch decisions
 *   - MEM_WB_Latch  : Passes final data ready for register write-back
 *
 * Pipeline Hazard & Branch Control:
 *   - Control Hazard Handling: When a conditional branch is TAKEN in the EX stage,
 *     the pipeline generates a BRANCH FLUSH, discarding instructions in the IF and ID
 *     stages to prevent incorrect execution.
 *   - Structural Model: Sequentially clocked 5-stage pipeline simulation with
 *     explicit latch state transitions and hazard resolution.
 */

// ------------------------------------------------------------------------------
// Pipeline Inter-Stage Registers (Latches)
// ------------------------------------------------------------------------------
struct IF_ID_Latch {
    bool valid;
    uint64_t pc;
    SimulatedInstruction inst;
    IF_ID_Latch() : valid(false), pc(0) {}
};

struct ID_EX_Latch {
    bool valid;
    uint64_t pc;
    SimulatedInstruction inst;
    ControlSignals signals;
    uint64_t regValSrc1;
    uint64_t regValSrc2;
    uint64_t immediate;
    RegId destRegId;
    bool destIs32;
    ID_EX_Latch() : valid(false), pc(0), regValSrc1(0), regValSrc2(0), immediate(0), 
                    destRegId(RegId::RAX), destIs32(false) {}
};

struct EX_MEM_Latch {
    bool valid;
    uint64_t pc;
    SimulatedInstruction inst;
    ControlSignals signals;
    uint64_t aluResult;
    ALUFlags flags;
    bool branchTaken;
    uint64_t branchTarget;
    uint64_t memWriteData;
    RegId destRegId;
    bool destIs32;
    EX_MEM_Latch() : valid(false), pc(0), aluResult(0), branchTaken(false), 
                     branchTarget(0), memWriteData(0), destRegId(RegId::RAX), destIs32(false) {}
};

struct MEM_WB_Latch {
    bool valid;
    uint64_t pc;
    SimulatedInstruction inst;
    ControlSignals signals;
    uint64_t writeBackData;
    RegId destRegId;
    bool destIs32;
    MEM_WB_Latch() : valid(false), pc(0), writeBackData(0), 
                     destRegId(RegId::RAX), destIs32(false) {}
};

class EducationalPipeline {
private:
    RegisterBank registers;
    MemoryUnit memory;
    std::vector<SimulatedInstruction> instructionMemory;

    // Pipeline Latches
    IF_ID_Latch  if_id;
    ID_EX_Latch  id_ex;
    EX_MEM_Latch ex_mem;
    MEM_WB_Latch mem_wb;

    uint64_t pc;
    bool pipelineHalted;
    size_t clockCycle;
    size_t instructionsRetired;

public:
    explicit EducationalPipeline(size_t memSize = 65536)
        : memory(memSize), pc(0), pipelineHalted(false), clockCycle(0), instructionsRetired(0) {
        reset();
    }

    void reset() {
        registers.reset();
        memory.reset();
        instructionMemory.clear();
        if_id = IF_ID_Latch();
        id_ex = ID_EX_Latch();
        ex_mem = EX_MEM_Latch();
        mem_wb = MEM_WB_Latch();
        pc = 0;
        pipelineHalted = false;
        clockCycle = 0;
        instructionsRetired = 0;
    }

    RegisterBank& getRegisters() { return registers; }
    MemoryUnit& getMemory() { return memory; }

    void loadCombatProgram(const std::vector<std::string>& asmCode) {
        reset();
        for (const auto& line : asmCode) {
            std::string l = line;
            size_t cPos = l.find(';');
            if (cPos != std::string::npos) l = l.substr(0, cPos);
            // Trim
            size_t f = l.find_first_not_of(" \t\r\n");
            if (f == std::string::npos) continue;
            size_t la = l.find_last_not_of(" \t\r\n");
            l = l.substr(f, la - f + 1);

            std::stringstream ss(l);
            std::string mnemonic;
            ss >> mnemonic;

            SimulatedInstruction inst;
            inst.rawMnemonic = l;
            inst.opcode = ControlUnitDecoder::parseOpcode(mnemonic);
            inst.dest = "";
            inst.src = "";
            inst.immediateVal = 0;
            inst.isImmediate = false;
            inst.targetAddress = 0;

            std::string rest;
            std::getline(ss, rest);
            size_t cf = rest.find_first_not_of(" \t\r\n");
            if (cf != std::string::npos) {
                size_t cl = rest.find_last_not_of(" \t\r\n");
                rest = rest.substr(cf, cl - cf + 1);
            } else {
                rest = "";
            }

            if (!rest.empty()) {
                size_t comma = rest.find(',');
                if (comma != std::string::npos) {
                    inst.dest = rest.substr(0, comma);
                    inst.src  = rest.substr(comma + 1);
                    // Trim dest & src
                    size_t d1 = inst.dest.find_first_not_of(" \t");
                    size_t d2 = inst.dest.find_last_not_of(" \t");
                    if (d1 != std::string::npos) inst.dest = inst.dest.substr(d1, d2 - d1 + 1);
                    size_t s1 = inst.src.find_first_not_of(" \t");
                    size_t s2 = inst.src.find_last_not_of(" \t");
                    if (s1 != std::string::npos) inst.src = inst.src.substr(s1, s2 - s1 + 1);
                } else {
                    inst.dest = rest;
                }

                std::string targetForImm = inst.src.empty() ? inst.dest : inst.src;
                if (!targetForImm.empty()) {
                    char ch = targetForImm[0];
                    if (std::isdigit(ch) || ch == '-' || ch == '+') {
                        try {
                            inst.immediateVal = std::stoll(targetForImm, nullptr, 0);
                            inst.isImmediate = true;
                        } catch (...) {}
                    }
                }
            }

            instructionMemory.push_back(inst);
        }
    }

    // --------------------------------------------------------------------------
    // Single Complete Clock Cycle of 5-Stage Pipeline
    // --------------------------------------------------------------------------
    bool clock() {
        if (pipelineHalted && !if_id.valid && !id_ex.valid && !ex_mem.valid && !mem_wb.valid) {
            return false;
        }
        clockCycle++;

        std::cout << "\n========================================================================\n";
        std::cout << " [PIPELINE CLOCK CYCLE #" << clockCycle << "] PC: " << pc 
                  << " | Active Latches: [IF/ID=" << if_id.valid 
                  << " ID/EX=" << id_ex.valid 
                  << " EX/MEM=" << ex_mem.valid 
                  << " MEM/WB=" << mem_wb.valid << "]\n";
        std::cout << "========================================================================\n";

        // ----------------------------------------------------------------------
        // STAGE 5: WRITE BACK (WB)
        // ----------------------------------------------------------------------
        if (mem_wb.valid) {
            std::cout << "  [STAGE 5: WB] Instruction: " << mem_wb.inst.rawMnemonic << "\n";
            if (mem_wb.signals.RegWrite) {
                if (mem_wb.destIs32) {
                    registers.set32(mem_wb.destRegId, static_cast<uint32_t>(mem_wb.writeBackData));
                } else {
                    registers.set64(mem_wb.destRegId, mem_wb.writeBackData);
                }
                std::cout << "    -> Committed Result to RegId " << static_cast<int>(mem_wb.destRegId)
                          << " = 0x" << std::hex << mem_wb.writeBackData 
                          << " (" << std::dec << mem_wb.writeBackData << " decimal)\n";
            } else {
                std::cout << "    -> No register commit (Memory Write or Branch instruction)\n";
            }
            instructionsRetired++;
            mem_wb.valid = false;
        } else {
            std::cout << "  [STAGE 5: WB] Bubble / Stalled\n";
        }

        // ----------------------------------------------------------------------
        // STAGE 4: MEMORY ACCESS (MEM)
        // ----------------------------------------------------------------------
        if (ex_mem.valid) {
            std::cout << "  [STAGE 4: MEM] Instruction: " << ex_mem.inst.rawMnemonic << "\n";
            mem_wb.inst = ex_mem.inst;
            mem_wb.pc = ex_mem.pc;
            mem_wb.signals = ex_mem.signals;
            mem_wb.destRegId = ex_mem.destRegId;
            mem_wb.destIs32 = ex_mem.destIs32;
            mem_wb.writeBackData = ex_mem.aluResult;

            if (ex_mem.signals.MemRead) {
                uint64_t addr = ex_mem.aluResult;
                uint64_t val = 0;
                if (memory.read64(addr, val)) {
                    mem_wb.writeBackData = val;
                    std::cout << "    -> Read 64-bit value 0x" << std::hex << val 
                              << " from Memory[0x" << addr << "]\n" << std::dec;
                }
            } else if (ex_mem.signals.MemWrite) {
                // Address resolution
                uint64_t addr = 0x1000;
                if (ex_mem.inst.dest.front() == '[' && ex_mem.inst.dest.back() == ']') {
                    std::string aStr = ex_mem.inst.dest.substr(1, ex_mem.inst.dest.size() - 2);
                    addr = std::stoull(aStr, nullptr, 0);
                }
                memory.write64(addr, ex_mem.memWriteData);
                std::cout << "    -> Wrote 64-bit value 0x" << std::hex << ex_mem.memWriteData 
                          << " to Memory[0x" << addr << "]\n" << std::dec;
            } else {
                std::cout << "    -> Bypassed memory access (Register-to-Register operation)\n";
            }

            mem_wb.valid = true;
            ex_mem.valid = false;
        } else {
            std::cout << "  [STAGE 4: MEM] Bubble / Stalled\n";
            mem_wb.valid = false;
        }

        // ----------------------------------------------------------------------
        // STAGE 3: EXECUTE (EX)
        // ----------------------------------------------------------------------
        bool branchFlushed = false;
        if (id_ex.valid) {
            std::cout << "  [STAGE 3: EX]  Instruction: " << id_ex.inst.rawMnemonic << "\n";
            ex_mem.inst = id_ex.inst;
            ex_mem.pc = id_ex.pc;
            ex_mem.signals = id_ex.signals;
            ex_mem.destRegId = id_ex.destRegId;
            ex_mem.destIs32 = id_ex.destIs32;

            ALUFlags flags = registers.getALUFlags();
            uint64_t res = 0;
            uint64_t opA = id_ex.regValSrc1;
            uint64_t opB = id_ex.signals.ALUSrc ? id_ex.immediate : id_ex.regValSrc2;

            // Operand A forwarding if updated in MEM stage
            if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == id_ex.destRegId) {
                opA = ex_mem.aluResult;
            }
            // Operand B forwarding if updated in MEM stage
            RegId srcRegId;
            bool srcIs32;
            if (!id_ex.signals.ALUSrc && registers.resolveRegisterName(id_ex.inst.src, srcRegId, srcIs32)) {
                if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == srcRegId) {
                    opB = ex_mem.aluResult;
                }
            }

            // For STORE instruction (STORE [addr], Reg), src operand is in opB or regValSrc2
            ex_mem.memWriteData = id_ex.signals.ALUSrc ? id_ex.regValSrc1 : opB;

            switch (id_ex.inst.opcode) {
                case Opcode::MOV:
                    res = opB;
                    break;
                case Opcode::ADD:
                    res = ALU_Simulator::executeAdd(opA, opB, flags);
                    registers.setALUFlags(flags);
                    break;
                case Opcode::SUB:
                    res = ALU_Simulator::executeSub(opA, opB, flags);
                    registers.setALUFlags(flags);
                    break;
                case Opcode::MUL:
                    {
                        uint64_t raxVal = registers.get64(RegId::RAX);
                        if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == RegId::RAX) {
                            raxVal = ex_mem.aluResult;
                        } else if (mem_wb.valid && mem_wb.signals.RegWrite && mem_wb.destRegId == RegId::RAX) {
                            raxVal = mem_wb.writeBackData;
                        }

                        uint64_t multiplier = opB;
                        if (!id_ex.signals.ALUSrc) {
                            multiplier = id_ex.regValSrc1; // in single-operand MUL, dest is the register
                            if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == id_ex.destRegId) {
                                multiplier = ex_mem.aluResult;
                            } else if (mem_wb.valid && mem_wb.signals.RegWrite && mem_wb.destRegId == id_ex.destRegId) {
                                multiplier = mem_wb.writeBackData;
                            }
                        }

                        res = ALU_Simulator::executeImul(raxVal, multiplier, flags);
                        ex_mem.destRegId = RegId::RAX;
                        registers.setALUFlags(flags);
                    }
                    break;
                case Opcode::CMP:
                    ALU_Simulator::executeCmp(opA, opB, flags);
                    registers.setALUFlags(flags);
                    break;
                case Opcode::JMP:
                    ex_mem.branchTaken = true;
                    ex_mem.branchTarget = id_ex.immediate;
                    break;
                case Opcode::JE:
                    ex_mem.branchTaken = flags.ZF;
                    ex_mem.branchTarget = id_ex.immediate;
                    break;
                case Opcode::JLE:
                    ex_mem.branchTaken = (flags.ZF || (flags.SF != flags.OF));
                    ex_mem.branchTarget = id_ex.immediate;
                    break;
                case Opcode::HLT:
                    pipelineHalted = true;
                    break;
                default:
                    res = opB;
                    break;
            }

            ex_mem.aluResult = res;
            ex_mem.flags = flags;

            std::cout << "    -> ALU Operation: " << id_ex.signals.ALUOp 
                      << " | Result: " << res << " | Flags: [ZF=" << flags.ZF 
                      << " CF=" << flags.CF << " SF=" << flags.SF << " OF=" << flags.OF << "]\n";

            // Control Hazard / Branch Resolution
            if (ex_mem.branchTaken) {
                std::cout << "    -> [CONTROL HAZARD DETECTED] Branch TAKEN to Target PC: " 
                          << ex_mem.branchTarget << "! Flushing IF/ID Stage Latch!\n";
                pc = ex_mem.branchTarget;
                if_id.valid = false; // FLUSH IF STAGE
                branchFlushed = true;
                ex_mem.branchTaken = false; // Reset so branch is not re-triggered in subsequent cycles
            }

            ex_mem.valid = true;
            id_ex.valid = false;
        } else {
            std::cout << "  [STAGE 3: EX]  Bubble / Stalled\n";
            ex_mem.valid = false;
        }

        // ----------------------------------------------------------------------
        // STAGE 2: INSTRUCTION DECODE (ID)
        // ----------------------------------------------------------------------
        if (if_id.valid && !branchFlushed) {
            std::cout << "  [STAGE 2: ID]  Instruction: " << if_id.inst.rawMnemonic << "\n";
            id_ex.inst = if_id.inst;
            id_ex.pc = if_id.pc;
            id_ex.signals = ControlUnitDecoder::generateSignals(if_id.inst.opcode, if_id.inst.isImmediate);
            id_ex.immediate = static_cast<uint64_t>(if_id.inst.immediateVal);

            // Read Register Operands with RAW Data Forwarding
            RegId dId, sId;
            bool d32, s32;
            if (registers.resolveRegisterName(if_id.inst.dest, dId, d32)) {
                id_ex.destRegId = dId;
                id_ex.destIs32 = d32;
                uint64_t val = registers.get64(dId);
                // Check EX/MEM forward
                if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == dId) {
                    val = ex_mem.aluResult;
                } else if (mem_wb.valid && mem_wb.signals.RegWrite && mem_wb.destRegId == dId) {
                    val = mem_wb.writeBackData;
                }
                id_ex.regValSrc1 = val;
            }
            if (registers.resolveRegisterName(if_id.inst.src, sId, s32)) {
                uint64_t val = registers.get64(sId);
                // Check EX/MEM forward
                if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == sId) {
                    val = ex_mem.aluResult;
                } else if (mem_wb.valid && mem_wb.signals.RegWrite && mem_wb.destRegId == sId) {
                    val = mem_wb.writeBackData;
                }
                id_ex.regValSrc2 = val;
            }

            // In store instructions like STORE [0x2000], RAX: dest is memory syntax, src is register
            if (if_id.inst.opcode == Opcode::STORE) {
                if (registers.resolveRegisterName(if_id.inst.src, sId, s32)) {
                    uint64_t val = registers.get64(sId);
                    if (ex_mem.valid && ex_mem.signals.RegWrite && ex_mem.destRegId == sId) {
                        val = ex_mem.aluResult;
                    } else if (mem_wb.valid && mem_wb.signals.RegWrite && mem_wb.destRegId == sId) {
                        val = mem_wb.writeBackData;
                    }
                    id_ex.regValSrc1 = val;
                    id_ex.regValSrc2 = val;
                }
            }

            std::cout << "    -> Decoded Control Signals: ALUOp=" << id_ex.signals.ALUOp
                      << " | RegRead=" << id_ex.signals.RegRead 
                      << " | RegWrite=" << id_ex.signals.RegWrite 
                      << " | ALUSrc=" << id_ex.signals.ALUSrc << "\n";

            id_ex.valid = true;
            if_id.valid = false;
        } else {
            std::cout << "  [STAGE 2: ID]  Bubble / Flushed\n";
            id_ex.valid = false;
        }

        // ----------------------------------------------------------------------
        // STAGE 1: INSTRUCTION FETCH (IF)
        // ----------------------------------------------------------------------
        if (!pipelineHalted && pc < instructionMemory.size() && !branchFlushed) {
            if_id.inst = instructionMemory[pc];
            if_id.pc = pc;
            if_id.valid = true;
            std::cout << "  [STAGE 1: IF]  Fetched PC [" << pc << "]: " << if_id.inst.rawMnemonic << "\n";
            pc++;
        } else {
            std::cout << "  [STAGE 1: IF]  Stalled / End of Program\n";
            if_id.valid = false;
        }

        return true;
    }

    void runToCompletion(size_t maxCycles = 100) {
        size_t c = 0;
        while (c < maxCycles) {
            bool active = clock();
            if (!active) break;
            c++;
        }
        std::cout << "\n========================================================================\n";
        std::cout << " PIPELINE EXECUTION SUMMARY: Total Clock Cycles: " << clockCycle 
                  << " | Instructions Retired: " << instructionsRetired << "\n";
        std::cout << "========================================================================\n";
        registers.display();
    }
};

#endif // CO4_EDUCATIONAL_PIPELINE_HPP

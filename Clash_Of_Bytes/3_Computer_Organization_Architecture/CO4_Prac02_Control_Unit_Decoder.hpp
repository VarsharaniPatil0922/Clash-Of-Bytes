#ifndef CO4_PRAC02_CONTROL_UNIT_DECODER_HPP
#define CO4_PRAC02_CONTROL_UNIT_DECODER_HPP

#include <string>
#include <vector>
#include <iostream>
#include <iomanip>

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome: CO4 | Practical 02 & Task 3: Complete Control Unit
 * ==============================================================================
 * Responsible for:
 *   1. Machine Instruction Decoding (Opcode, Operands, Addressing Modes)
 *   2. Generation of Micro-Operation Control Signals:
 *      - RegRead, RegWrite, ALUSrc, ALUOp, MemRead, MemWrite, Branch
 *   3. Application-Level Action Translation:
 *      - Translates high-level fighting game actions (PUNCH, KICK, SPECIAL, BLOCK)
 *        into structured sequences of simulated CPU micro-instructions.
 *      - Clearly differentiates between application combat semantics and
 *        hardware CPU instruction opcodes.
 */

enum class Opcode {
    // Data Transfer
    MOV, LOAD, STORE,
    // Arithmetic
    ADD, SUB, MUL, INC, DEC,
    // Logical
    AND, OR, XOR, NOT,
    // Comparison
    CMP, TEST,
    // Bitwise Shifts
    SHL, SHR, SAR,
    // Control Flow
    JMP, JE, JNE, JZ, JNZ, JG, JL, JGE, JLE,
    // System / Halt
    HLT, NOP,
    UNKNOWN
};

struct ControlSignals {
    bool RegRead;
    bool RegWrite;
    bool ALUSrc;      // true: immediate operand; false: register operand
    std::string ALUOp; // Operation mnemonic dispatched to ALU
    bool MemRead;     // Read from primary memory
    bool MemWrite;    // Write to primary memory
    bool Branch;      // Control-flow branching active

    ControlSignals() 
        : RegRead(false), RegWrite(false), ALUSrc(false), ALUOp("NOP"), 
          MemRead(false), MemWrite(false), Branch(false) {}
};

struct SimulatedInstruction {
    std::string rawMnemonic;
    Opcode opcode;
    std::string dest;       // Destination register or memory address
    std::string src;        // Source register, memory address, or immediate
    int64_t immediateVal;   // If operand is an immediate
    bool isImmediate;       // True if src is an immediate value
    uint64_t targetAddress; // Target branch address / memory address
};

class ControlUnitDecoder {
public:
    // --------------------------------------------------------------------------
    // Backward-Compatible Application Action Decoder (Preserved)
    // --------------------------------------------------------------------------
    static ControlSignals decodeAction(const std::string& action) {
        ControlSignals sigs;
        if (action == "PUNCH") {
            sigs.RegRead = true;
            sigs.RegWrite = true;
            sigs.ALUSrc = true;
            sigs.ALUOp = "SUB_DAMAGE";
            sigs.MemRead = false;
            sigs.MemWrite = true;
            sigs.Branch = false;
        } else if (action == "KICK") {
            sigs.RegRead = true;
            sigs.RegWrite = true;
            sigs.ALUSrc = true;
            sigs.ALUOp = "SUB_DAMAGE";
            sigs.MemRead = false;
            sigs.MemWrite = true;
            sigs.Branch = false;
        } else if (action == "SPECIAL") {
            sigs.RegRead = true;
            sigs.RegWrite = true;
            sigs.ALUSrc = true;
            sigs.ALUOp = "IMUL_BURST";
            sigs.MemRead = true;
            sigs.MemWrite = true;
            sigs.Branch = true;
        } else { // BLOCK / IDLE
            sigs.RegRead = true;
            sigs.RegWrite = false;
            sigs.ALUSrc = false;
            sigs.ALUOp = "NOP";
            sigs.MemRead = false;
            sigs.MemWrite = false;
            sigs.Branch = false;
        }
        return sigs;
    }

    // --------------------------------------------------------------------------
    // Machine Instruction Opcode Decoder
    // --------------------------------------------------------------------------
    static Opcode parseOpcode(const std::string& mnemonic) {
        std::string m = mnemonic;
        for (char& c : m) c = static_cast<char>(::toupper(c));

        if (m == "MOV")   return Opcode::MOV;
        if (m == "LOAD")  return Opcode::LOAD;
        if (m == "STORE") return Opcode::STORE;
        if (m == "ADD")   return Opcode::ADD;
        if (m == "SUB")   return Opcode::SUB;
        if (m == "MUL" || m == "IMUL") return Opcode::MUL;
        if (m == "INC")   return Opcode::INC;
        if (m == "DEC")   return Opcode::DEC;
        if (m == "AND")   return Opcode::AND;
        if (m == "OR")    return Opcode::OR;
        if (m == "XOR")   return Opcode::XOR;
        if (m == "NOT")   return Opcode::NOT;
        if (m == "CMP")   return Opcode::CMP;
        if (m == "TEST")  return Opcode::TEST;
        if (m == "SHL")   return Opcode::SHL;
        if (m == "SHR")   return Opcode::SHR;
        if (m == "SAR")   return Opcode::SAR;
        if (m == "JMP")   return Opcode::JMP;
        if (m == "JE" || m == "JZ")   return Opcode::JE;
        if (m == "JNE" || m == "JNZ") return Opcode::JNE;
        if (m == "JG")    return Opcode::JG;
        if (m == "JL")    return Opcode::JL;
        if (m == "JGE")   return Opcode::JGE;
        if (m == "JLE")   return Opcode::JLE;
        if (m == "HLT")   return Opcode::HLT;
        if (m == "NOP")   return Opcode::NOP;
        return Opcode::UNKNOWN;
    }

    static ControlSignals generateSignals(Opcode op, bool isImmediateSrc = false) {
        ControlSignals s;
        s.ALUSrc = isImmediateSrc;

        switch (op) {
            case Opcode::MOV:
                s.RegRead = !isImmediateSrc;
                s.RegWrite = true;
                s.ALUOp = "PASS_SRC";
                break;
            case Opcode::LOAD:
                s.RegRead = false;
                s.RegWrite = true;
                s.MemRead = true;
                s.ALUOp = "ADDR_GEN";
                break;
            case Opcode::STORE:
                s.RegRead = true;
                s.RegWrite = false;
                s.MemWrite = true;
                s.ALUOp = "ADDR_GEN";
                break;
            case Opcode::ADD:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "ADD";
                break;
            case Opcode::SUB:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "SUB";
                break;
            case Opcode::MUL:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "MUL";
                break;
            case Opcode::INC:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "INC";
                break;
            case Opcode::DEC:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "DEC";
                break;
            case Opcode::AND:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "AND";
                break;
            case Opcode::OR:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "OR";
                break;
            case Opcode::XOR:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "XOR";
                break;
            case Opcode::NOT:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "NOT";
                break;
            case Opcode::CMP:
                s.RegRead = true;
                s.RegWrite = false; // Compare updates flags only
                s.ALUOp = "CMP";
                break;
            case Opcode::TEST:
                s.RegRead = true;
                s.RegWrite = false; // Test updates flags only
                s.ALUOp = "TEST";
                break;
            case Opcode::SHL:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "SHL";
                break;
            case Opcode::SHR:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "SHR";
                break;
            case Opcode::SAR:
                s.RegRead = true;
                s.RegWrite = true;
                s.ALUOp = "SAR";
                break;
            case Opcode::JMP:
            case Opcode::JE:
            case Opcode::JNE:
            case Opcode::JG:
            case Opcode::JL:
            case Opcode::JGE:
            case Opcode::JLE:
                s.RegRead = false;
                s.RegWrite = false;
                s.Branch = true;
                s.ALUOp = "BRANCH_EVAL";
                break;
            case Opcode::HLT:
            case Opcode::NOP:
            default:
                s.ALUOp = "NOP";
                break;
        }
        return s;
    }

    // --------------------------------------------------------------------------
    // Application Action to CPU Instruction Sequence Mapping
    // --------------------------------------------------------------------------
    static std::vector<std::string> translateCombatActionToAssembly(const std::string& action, 
                                                                    int atk, int def, int combo) {
        std::vector<std::string> asmProgram;
        if (action == "PUNCH") {
            // PUNCH: Quick Strike
            // 1. Load Attack into RAX
            // 2. Load Defense into RBX
            // 3. SUB RAX, RBX
            // 4. STORE into Opponent HP memory
            asmProgram.push_back("MOV RAX, " + std::to_string(atk));
            asmProgram.push_back("MOV RBX, " + std::to_string(def));
            asmProgram.push_back("SUB RAX, RBX");
            asmProgram.push_back("STORE [0x1000], RAX");
        } else if (action == "KICK") {
            // KICK: Heavy Strike with recovery delay
            asmProgram.push_back("MOV RAX, " + std::to_string(atk));
            asmProgram.push_back("MOV RBX, " + std::to_string(def));
            asmProgram.push_back("SUB RAX, RBX");
            asmProgram.push_back("STORE [0x1000], RAX");
        } else if (action == "SPECIAL") {
            // SPECIAL: Multiplied Burst
            // 1. Load Attack into RAX
            // 2. Load Defense into RBX
            // 3. SUB RAX, RBX
            // 4. Load Combo Multiplier into RCX
            // 5. MUL RCX
            // 6. STORE into Opponent HP memory
            asmProgram.push_back("MOV RAX, " + std::to_string(atk));
            asmProgram.push_back("MOV RBX, " + std::to_string(def));
            asmProgram.push_back("SUB RAX, RBX");
            asmProgram.push_back("MOV RCX, " + std::to_string(combo));
            asmProgram.push_back("MUL RCX");
            asmProgram.push_back("STORE [0x1000], RAX");
        } else { // BLOCK / DEFEND
            // BLOCK: Fortify armor rating
            asmProgram.push_back("MOV RBX, " + std::to_string(def * 2));
            asmProgram.push_back("STORE [0x1008], RBX");
        }
        asmProgram.push_back("HLT");
        return asmProgram;
    }
};

#endif // CO4_PRAC02_CONTROL_UNIT_DECODER_HPP

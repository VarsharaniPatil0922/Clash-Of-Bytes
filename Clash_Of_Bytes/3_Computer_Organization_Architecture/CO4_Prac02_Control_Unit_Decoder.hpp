#ifndef CO4_PRAC02_CONTROL_UNIT_DECODER_HPP
#define CO4_PRAC02_CONTROL_UNIT_DECODER_HPP

#include <string>

/*
 * Subject: Computer Organization & Architecture
 * Course Outcome: CO4 | Practical 02
 * Topic: Control Unit Instruction Decoder & Hardware Control Signal Generation
 */
struct ControlSignals {
    bool RegRead;
    bool RegWrite;
    bool ALUSrc;
    std::string ALUOp;
    bool MemRead;
    bool MemWrite;
    bool Branch;
};

class ControlUnitDecoder {
public:
    static ControlSignals decodeAction(const std::string& action) {
        ControlSignals sigs;
        if (action == "PUNCH" || action == "KICK") {
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
};

#endif // CO4_PRAC02_CONTROL_UNIT_DECODER_HPP

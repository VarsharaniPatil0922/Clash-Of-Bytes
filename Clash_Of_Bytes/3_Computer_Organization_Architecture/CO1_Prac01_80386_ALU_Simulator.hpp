#ifndef CO1_PRAC01_80386_ALU_SIMULATOR_HPP
#define CO1_PRAC01_80386_ALU_SIMULATOR_HPP

#include <cstdint>
#include <string>

/*
 * Subject: Computer Organization & Architecture
 * Course Outcome: CO1 | Practical 01
 * Topic: 80386 Microprocessor ALU Simulator & Hardware Status EFLAGS
 */
struct ALUFlags {
    bool ZF; // Zero Flag: Set if damage calculation result is zero (K.O.)
    bool CF; // Carry / Borrow Flag: Set if damage underflows health
    bool SF; // Sign Flag: Set if signed negative
    bool OF; // Overflow Flag: Set on arithmetic overflow
};

class ALU_Simulator {
public:
    static uint64_t executeSub(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a - b;
        flags.ZF = (result == 0);
        flags.CF = (a < b);
        flags.SF = ((int64_t)result < 0);
        flags.OF = ((int64_t)a > 0 && (int64_t)b < 0 && (int64_t)result < 0);
        return result;
    }

    static uint64_t executeImul(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a * b;
        flags.ZF = (result == 0);
        flags.CF = (b != 0 && (result / b != a));
        flags.SF = ((int64_t)result < 0);
        flags.OF = flags.CF;
        return result;
    }
};

#endif // CO1_PRAC01_80386_ALU_SIMULATOR_HPP

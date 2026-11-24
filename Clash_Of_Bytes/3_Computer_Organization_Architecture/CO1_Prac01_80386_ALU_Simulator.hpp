#ifndef CO1_PRAC01_80386_ALU_SIMULATOR_HPP
#define CO1_PRAC01_80386_ALU_SIMULATOR_HPP

#include <cstdint>
#include <string>

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome: CO1 | Practical 01 & Task 1/2: Complete ALU Simulator
 * ==============================================================================
 * Simulates the 80386 and AMD64 Arithmetic Logic Unit (ALU)
 * Computes exact mathematical hardware status flags:
 *   - ZF: Zero Flag (Result == 0; used as Knockout Detector in Clash of Bytes)
 *   - CF: Carry / Borrow Flag (Unsigned carry out / borrow; detects Health Underflow)
 *   - SF: Sign Flag (Result MSB set; signed negative combat impact / debuff)
 *   - OF: Overflow Flag (Two's complement signed overflow / arithmetic saturation)
 */

struct ALUFlags {
    bool ZF; // Zero Flag
    bool CF; // Carry / Borrow Flag
    bool SF; // Sign Flag
    bool OF; // Overflow Flag

    ALUFlags() : ZF(false), CF(false), SF(false), OF(false) {}
};

class ALU_Simulator {
public:
    // --------------------------------------------------------------------------
    // Arithmetic Operations
    // --------------------------------------------------------------------------

    // ADD: a + b
    static uint64_t executeAdd(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a + b;
        flags.ZF = (result == 0);
        flags.CF = (result < a); // Unsigned carry
        flags.SF = (static_cast<int64_t>(result) < 0);
        // Signed overflow: positive + positive = negative OR negative + negative = positive
        flags.OF = ((~(a ^ b) & (a ^ result) & 0x8000000000000000ULL) != 0);
        return result;
    }

    // SUB: a - b (Preserved from original CO1 practical)
    static uint64_t executeSub(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a - b;
        flags.ZF = (result == 0);
        flags.CF = (a < b); // Unsigned borrow (damage > current HP)
        flags.SF = (static_cast<int64_t>(result) < 0);
        // Signed overflow: positive - negative = negative OR negative - positive = positive
        flags.OF = (((a ^ b) & (a ^ result) & 0x8000000000000000ULL) != 0);
        return result;
    }

    // IMUL / MUL: a * b (Preserved from original CO1 practical)
    static uint64_t executeImul(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a * b;
        flags.ZF = (result == 0);
        flags.CF = (b != 0 && (result / b != a)); // Unsigned product overflow
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = flags.CF;
        return result;
    }

    // INC: a + 1 (In standard x86, INC does not alter CF!)
    static uint64_t executeInc(uint64_t a, ALUFlags& flags) {
        uint64_t result = a + 1;
        flags.ZF = (result == 0);
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = (a == 0x7FFFFFFFFFFFFFFFULL); // Signed MAX_INT to MIN_INT
        return result;
    }

    // DEC: a - 1 (In standard x86, DEC does not alter CF!)
    static uint64_t executeDec(uint64_t a, ALUFlags& flags) {
        uint64_t result = a - 1;
        flags.ZF = (result == 0);
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = (a == 0x8000000000000000ULL); // Signed MIN_INT to MAX_INT
        return result;
    }

    // --------------------------------------------------------------------------
    // Comparison Operations
    // --------------------------------------------------------------------------

    // CMP: a - b (Non-destructive comparison, returns comparison result, sets flags)
    static uint64_t executeCmp(uint64_t a, uint64_t b, ALUFlags& flags) {
        return executeSub(a, b, flags);
    }

    // TEST: a & b (Non-destructive logical test, clears CF and OF, sets ZF and SF)
    static uint64_t executeTest(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a & b;
        flags.ZF = (result == 0);
        flags.CF = false;
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = false;
        return result;
    }

    // --------------------------------------------------------------------------
    // Logical Operations
    // --------------------------------------------------------------------------

    // AND: a & b
    static uint64_t executeAnd(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a & b;
        flags.ZF = (result == 0);
        flags.CF = false;
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = false;
        return result;
    }

    // OR: a | b
    static uint64_t executeOr(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a | b;
        flags.ZF = (result == 0);
        flags.CF = false;
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = false;
        return result;
    }

    // XOR: a ^ b
    static uint64_t executeXor(uint64_t a, uint64_t b, ALUFlags& flags) {
        uint64_t result = a ^ b;
        flags.ZF = (result == 0);
        flags.CF = false;
        flags.SF = (static_cast<int64_t>(result) < 0);
        flags.OF = false;
        return result;
    }

    // NOT: ~a
    static uint64_t executeNot(uint64_t a, ALUFlags& /*flags*/) {
        // In standard x86, NOT does not affect flags
        return ~a;
    }

    // --------------------------------------------------------------------------
    // Bitwise Shift Operations
    // --------------------------------------------------------------------------

    // SHL: a << shiftCount
    static uint64_t executeShl(uint64_t a, uint64_t shiftCount, ALUFlags& flags) {
        uint64_t cnt = shiftCount & 63;
        if (cnt == 0) return a;
        flags.CF = ((a >> (64 - cnt)) & 1ULL) != 0;
        uint64_t result = a << cnt;
        flags.ZF = (result == 0);
        flags.SF = (static_cast<int64_t>(result) < 0);
        if (cnt == 1) {
            flags.OF = (((result ^ a) & 0x8000000000000000ULL) != 0);
        } else {
            flags.OF = false;
        }
        return result;
    }

    // SHR: a >> shiftCount (Logical Shift Right)
    static uint64_t executeShr(uint64_t a, uint64_t shiftCount, ALUFlags& flags) {
        uint64_t cnt = shiftCount & 63;
        if (cnt == 0) return a;
        flags.CF = ((a >> (cnt - 1)) & 1ULL) != 0;
        uint64_t result = a >> cnt;
        flags.ZF = (result == 0);
        flags.SF = (static_cast<int64_t>(result) < 0);
        if (cnt == 1) {
            flags.OF = (a & 0x8000000000000000ULL) != 0;
        } else {
            flags.OF = false;
        }
        return result;
    }

    // SAR: a >> shiftCount (Arithmetic Shift Right - preserves sign)
    static uint64_t executeSar(uint64_t a, uint64_t shiftCount, ALUFlags& flags) {
        uint64_t cnt = shiftCount & 63;
        if (cnt == 0) return a;
        int64_t sa = static_cast<int64_t>(a);
        flags.CF = ((sa >> (cnt - 1)) & 1LL) != 0;
        int64_t sres = sa >> cnt;
        uint64_t result = static_cast<uint64_t>(sres);
        flags.ZF = (result == 0);
        flags.SF = (sres < 0);
        flags.OF = false;
        return result;
    }
};

#endif // CO1_PRAC01_80386_ALU_SIMULATOR_HPP

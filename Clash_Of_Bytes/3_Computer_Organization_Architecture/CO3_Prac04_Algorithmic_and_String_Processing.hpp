#ifndef CO3_PRAC04_ALGORITHMIC_AND_STRING_PROCESSING_HPP
#define CO3_PRAC04_ALGORITHMIC_AND_STRING_PROCESSING_HPP

#include <cstdint>
#include <string>
#include <sstream>
#include <iomanip>
#include <iostream>
#include <vector>
#include <cstring>
#include <algorithm>
#include "CO3_Prac04_Hex_Packed_BCD_Converter.hpp"

/*
 * Subject: Computer Organization & Architecture
 * Course Outcome: CO3 | Practical 04 / Task 3
 * Topic: 64-bit Algorithmic Arithmetic & String Processing on Hardware Registers
 * Architecture: x86-64 (AMD64 / Intel 64)
 * Application: "Clash of Bytes" Combat Scaling, Log Analysis & Score Encoding Engine
 */

// ============================================================================
// 1. DATA STRUCTURES FOR PROCESSOR-LEVEL RESULTS & EXECUTION TRACE
// ============================================================================
struct ShiftAddALUResult {
    uint64_t multiplicand;      // Multiplicand (e.g., Base Damage)
    uint64_t multiplier;        // Multiplier (e.g., Combo Factor)
    uint64_t assemblyProduct;   // Product computed via 64-bit hardware assembly
    uint64_t referenceProduct;  // Independent C++ trusted reference calculation
    bool matchesReference;      // Verification status: assembly == reference
    int bitIterations;          // Processor bit-scan iterations
    uint64_t rflags;            // Raw 64-bit CPU RFLAGS register
    bool ZF;                    // Zero Flag (Bit 6)
    bool CF;                    // Carry Flag (Bit 0)
    bool SF;                    // Sign Flag (Bit 7)
    bool OF;                    // Overflow Flag (Bit 11)
    std::string executionTrace; // Human-readable register state trace
};

struct BoothALUResult {
    int64_t multiplicand;       // Multiplicand (M) - Signed 64-bit (e.g., Damage debuff / modifier)
    int64_t multiplier;         // Multiplier (Q) - Signed 64-bit (e.g., Multiplier / Duration)
    int64_t assemblyProduct;    // Product computed via 64-bit hardware Booth assembly (lower 64 bits)
    int64_t upperProduct;       // Upper 64 bits of 128-bit product (Accumulator A)
    int64_t referenceProduct;   // Safe reference calculation avoiding UB
    bool matchesReference;      // Validation status: assembly == reference
    int bitIterations;          // Processor bit-scan iterations (64 cycles)
    uint64_t rflags;            // Raw 64-bit CPU RFLAGS register
    bool ZF;                    // Zero Flag (Bit 6)
    bool CF;                    // Carry Flag (Bit 0)
    bool SF;                    // Sign Flag (Bit 7)
    bool OF;                    // Overflow Flag (Bit 11) - set if product exceeds 64-bit signed range
    std::string executionTrace; // Human-readable register state trace
};

struct StringAnalysisResult {
    std::string text;
    std::string token;
    uint64_t assemblyStringLength;
    uint64_t referenceStringLength;
    bool lengthMatches;
    uint64_t assemblyMatchCount;
    uint64_t referenceMatchCount;
    bool matchCountMatches;
    std::string analysisTrace;
};

// ============================================================================
// 2. 64-BIT SHIFT-AND-ADD MULTIPLICATION ALGORITHM (HARDWARE MULTIPLIER)
// ============================================================================
class AssemblyShiftAddMultiplier {
public:
    // Native x86-64 hardware Shift-and-Add binary multiplication algorithm
    static ShiftAddALUResult executeShiftAdd(uint64_t multiplicand, uint64_t multiplier) {
        ShiftAddALUResult res;
        res.multiplicand = multiplicand;
        res.multiplier = multiplier;
        res.referenceProduct = multiplicand * multiplier; // Independent reference

        uint64_t product = 0;
        uint64_t rflags = 0;
        uint64_t iterations = 0;

        /*
         * Processor-Level Micro-Architecture Logic:
         * Registers:
         *   %rax = Product Accumulator (Initialized to 0)
         *   %rdx = Multiplicand (Shifted left by 1 bit on each iteration: RDX <<= 1)
         *   %rbx = Multiplier (Examined bit-by-bit via LSB test; shifted right: RBX >>= 1)
         *   %rcx = Iteration Counter
         * Instructions:
         *   testq $1, %rbx : Tests bit 0 of multiplier (Sets ZF=1 if bit is 0, ZF=0 if 1)
         *   jz .skip_add   : If LSB is 0, skip addition
         *   addq %rdx, %rax: Accumulate shifted multiplicand into product
         *   shlq $1, %rdx  : Shift multiplicand left (multiply by 2)
         *   shrq $1, %rbx  : Shift multiplier right (divide by 2)
         *   jnz .mul_loop  : If multiplier != 0, continue processing remaining bits
         *   pushfq / popq  : Captures genuine hardware CPU RFLAGS
         */
        __asm__ volatile (
            "xorq %%rax, %%rax\n\t"         // RAX = 0 (Accumulator)
            "xorq %%rcx, %%rcx\n\t"         // RCX = 0 (Iteration counter)
            "movq %2, %%rdx\n\t"            // RDX = multiplicand
            "movq %3, %%rbx\n\t"            // RBX = multiplier
            "testq %%rbx, %%rbx\n\t"        // Test if multiplier is zero
            "jz 2f\n\t"                     // If zero, jump to end (product = 0)

            "1:\n\t"                        // Loop label
            "incq %%rcx\n\t"                // RCX++ (Count clock/iteration cycles)
            "testq $1, %%rbx\n\t"           // Test LSB of multiplier
            "jz 3f\n\t"                     // If LSB == 0, skip addition
            "addq %%rdx, %%rax\n\t"         // If LSB == 1, RAX += RDX

            "3:\n\t"
            "shlq $1, %%rdx\n\t"            // RDX <<= 1 (Shift multiplicand left)
            "shrq $1, %%rbx\n\t"            // RBX >>= 1 (Shift multiplier right)
            "jnz 1b\n\t"                    // If RBX != 0, repeat loop

            "2:\n\t"                        // Done label
            "pushfq\n\t"                    // Push 64-bit RFLAGS register to stack
            "popq %1\n\t"                   // Pop RFLAGS into rflags variable
            "movq %%rax, %0\n\t"            // Save computed product
            "movq %%rcx, %4\n\t"            // Save iteration count
            : "=r" (product), "=r" (rflags), "+r" (multiplicand), "+r" (multiplier), "=r" (iterations)
            :
            : "%rax", "%rbx", "%rcx", "%rdx", "cc"
        );

        res.assemblyProduct = product;
        res.rflags = rflags;
        res.bitIterations = static_cast<int>(iterations);
        res.matchesReference = (res.assemblyProduct == res.referenceProduct);

        // Parse CPU Hardware Flags
        res.CF = (rflags & (1ULL << 0)) != 0;
        res.ZF = (rflags & (1ULL << 6)) != 0;
        res.SF = (rflags & (1ULL << 7)) != 0;
        res.OF = (rflags & (1ULL << 11)) != 0;

        std::stringstream ss;
        ss << "RAX:0x" << std::hex << std::uppercase << res.assemblyProduct << std::dec
           << " (" << res.multiplicand << " x " << res.multiplier << " = " << res.assemblyProduct << ")"
           << " | Cycles: " << res.bitIterations
           << " | Flags: [ZF=" << res.ZF << " CF=" << res.CF << " SF=" << res.SF << " OF=" << res.OF << "]"
           << " | Match: " << (res.matchesReference ? "PASSED" : "FAILED");
        res.executionTrace = ss.str();

        return res;
    }
};

// ============================================================================
// 3. 64-BIT BOOTH'S MULTIPLICATION ALGORITHM (SIGNED HARDWARE MULTIPLIER)
// ============================================================================
class AssemblyBoothMultiplier {
public:
    // Safe reference product computation avoiding signed overflow undefined behavior in C++
    static int64_t computeSafeReference(int64_t multiplicand, int64_t multiplier) {
        // Unsigned arithmetic wrap-around is defined in C++, yielding exact 2's complement 64-bit result
        return static_cast<int64_t>(static_cast<uint64_t>(multiplicand) * static_cast<uint64_t>(multiplier));
    }

    // Native x86-64 hardware Booth's signed binary multiplication algorithm
    static BoothALUResult executeBooth(int64_t multiplicand, int64_t multiplier) {
        BoothALUResult res;
        res.multiplicand = multiplicand;
        res.multiplier = multiplier;
        res.referenceProduct = computeSafeReference(multiplicand, multiplier);
        res.bitIterations = 64; // 64-bit word size = 64 processor cycles

        int64_t productLower = 0;
        int64_t productUpper = 0;
        uint64_t rflags = 0;

        /*
         * Processor-Level Micro-Architecture Logic (Booth's Algorithm):
         *
         * Theoretical Foundation:
         *   Booth's Multiplication Algorithm operates directly on signed two's complement numbers.
         *   It examines adjacent bits of the multiplier (Q) in overlapping pairs: (Q0, Q_-1),
         *   where Q0 is the current least significant bit (LSB) and Q_-1 is the previous bit.
         *   It replaces multi-bit continuous sequences of 1s with a subtraction at the beginning
         *   and an addition at the end:
         *     - Pair 01 : Transition 1 to 0 (Starting block of 1s)  -> A = A + M
         *     - Pair 10 : Transition 0 to 1 (Ending block of 1s)    -> A = A - M
         *     - Pair 00 : Middle of a run of 0s                     -> No arithmetic operation
         *     - Pair 11 : Middle of a run of 1s                     -> No arithmetic operation
         *   Following the pair inspection, an Arithmetic Right Shift (ARS) is executed
         *   on the concatenated [A, Q, Q_-1] data path, preserving the sign bit in A.
         *
         * Hardware Register Mapping:
         *   %rax = Accumulator (A) [Upper 64 bits of 128-bit product, cleared to 0]
         *   %rbx = Multiplier (Q)  [Lower 64 bits of product, holds signed multiplier]
         *   %rdx = Multiplicand (M)[Signed 64-bit operand, loaded into RDX]
         *   %rsi = Q_-1            [Single-bit latch of previous cycle, cleared to 0]
         *   %rcx = Iteration Count [Initialized to 64 cycles for 64-bit word width]
         *   %r8  = Temporary register for Q0 bit isolation
         *
         * Instruction Sequence:
         *   1. movq %rbx, %r8 / andq $1, %r8 : Isolates Q0 (bit 0 of multiplier)
         *   2. cmpq %rsi, %r8 / je .booth_shift : If Q0 == Q_-1 (00 or 11), skip arithmetic
         *   3. testq %r8, %r8 / jz .booth_add   : If Q0 == 0 (pair 01), branch to addition
         *   4. subq %rdx, %rax                  : Pair 10 (Q0=1, Q_-1=0) -> A = A - M
         *   5. addq %rdx, %rax                  : Pair 01 (Q0=0, Q_-1=1) -> A = A + M
         *   6. movq %r8, %rsi                   : Update Q_-1 = previous Q0
         *   7. shrdq $1, %rax, %rbx             : Arithmetic multi-precision shift:
         *                                         shifts RBX right by 1, shifting LSB of RAX into MSB of RBX
         *   8. sarq $1, %rax                    : Arithmetic Right Shift on RAX (preserves sign bit)
         *   9. decq %rcx / jnz .booth_loop      : Decrements cycle counter and repeats for 64 iterations
         *  10. testq %rbx, %rbx / pushfq / popq : Evaluates final 64-bit product flags (ZF, SF)
         */
        __asm__ volatile (
            "xorq %%rax, %%rax\n\t"         // RAX = A = 0 (64-bit Accumulator)
            "xorq %%rsi, %%rsi\n\t"         // RSI = Q_-1 = 0
            "movq %2, %%rdx\n\t"            // RDX = M (Multiplicand)
            "movq %3, %%rbx\n\t"            // RBX = Q (Multiplier)
            "movq $64, %%rcx\n\t"           // RCX = 64 (Cycle counter for 64-bit word)

            "1:\n\t"                        // .booth_loop
            "movq %%rbx, %%r8\n\t"          // R8 = Q
            "andq $1, %%r8\n\t"             // R8 = Q0 (LSB of multiplier)
            "cmpq %%rsi, %%r8\n\t"          // Compare Q0 and Q_-1
            "je 2f\n\t"                     // If Q0 == Q_-1 (00 or 11), skip add/sub

            "testq %%r8, %%r8\n\t"          // Test Q0
            "jz 3f\n\t"                     // If Q0 == 0 (so Q_-1 == 1, pair 01): jump to add
            "subq %%rdx, %%rax\n\t"         // Pair 10 (Q0=1, Q_-1=0): A = A - M
            "jmp 2f\n\t"                    // Proceed to shift

            "3:\n\t"                        // .booth_add
            "addq %%rdx, %%rax\n\t"         // Pair 01 (Q0=0, Q_-1=1): A = A + M

            "2:\n\t"                        // .booth_shift
            "movq %%r8, %%rsi\n\t"          // Q_-1 = old Q0
            "shrdq $1, %%rax, %%rbx\n\t"    // Shift Q right by 1; LSB of A becomes MSB of Q
            "sarq $1, %%rax\n\t"            // Arithmetic Right Shift on A (preserves sign)
            "decq %%rcx\n\t"                // RCX--
            "jnz 1b\n\t"                    // Loop for all 64 bits

            // Evaluation and flag capture
            "testq %%rbx, %%rbx\n\t"        // Test lower 64-bit product to set ZF and SF
            "pushfq\n\t"                    // Push CPU RFLAGS to stack
            "popq %1\n\t"                   // Pop RFLAGS into rflags variable
            "movq %%rbx, %0\n\t"            // Return lower 64 bits of product (Q)
            "movq %%rax, %4\n\t"            // Return upper 64 bits of product (A)
            : "=r" (productLower), "=r" (rflags), "+r" (multiplicand), "+r" (multiplier), "=r" (productUpper)
            :
            : "%rax", "%rbx", "%rcx", "%rdx", "%rsi", "%r8", "cc"
        );

        res.assemblyProduct = productLower;
        res.upperProduct = productUpper;
        res.rflags = rflags;
        res.matchesReference = (res.assemblyProduct == res.referenceProduct);

        // Hardware CPU Flags
        res.CF = (rflags & (1ULL << 0)) != 0;
        res.ZF = (rflags & (1ULL << 6)) != 0;
        res.SF = (rflags & (1ULL << 7)) != 0;

        // Overflow condition: Upper 64 bits must be exact sign extension of lower 64 bits
        int64_t expectedUpper = (res.assemblyProduct < 0) ? -1LL : 0LL;
        res.OF = (res.upperProduct != expectedUpper);

        std::stringstream ss;
        ss << "Q:0x" << std::hex << std::uppercase << static_cast<uint64_t>(res.assemblyProduct) << std::dec
           << " (" << res.multiplicand << " x " << res.multiplier << " = " << res.assemblyProduct << ")"
           << " | High64(A):0x" << std::hex << static_cast<uint64_t>(res.upperProduct) << std::dec
           << " | Cycles: " << res.bitIterations
           << " | Flags: [ZF=" << res.ZF << " CF=" << res.CF << " SF=" << res.SF << " OF=" << res.OF << "]"
           << " | Match: " << (res.matchesReference ? "PASSED" : "FAILED");
        res.executionTrace = ss.str();

        return res;
    }
};

// ============================================================================
// 4. 64-BIT SUCCESSIVE ADDITION MULTIPLICATION ALGORITHM
// ============================================================================
class AssemblySuccessiveAddition {
public:
    // Native x86-64 hardware Successive Addition algorithm
    static ShiftAddALUResult executeSuccessiveAdd(uint64_t multiplicand, uint64_t multiplier) {
        ShiftAddALUResult res;
        res.multiplicand = multiplicand;
        res.multiplier = multiplier;
        res.referenceProduct = multiplicand * multiplier;

        uint64_t product = 0;
        uint64_t rflags = 0;
        uint64_t iterations = 0;

        /*
         * Successive Addition Assembly Logic:
         * Registers:
         *   %rax = Accumulator (Product)
         *   %rdx = Multiplicand (Value to add)
         *   %rcx = Counter (Multiplier decremented until 0)
         * Instructions:
         *   addq %rdx, %rax : Accumulator += Multiplicand
         *   decq %rcx       : Decrement counter (sets ZF if zero)
         *   jnz .loop       : Branch back until RCX == 0
         */
        __asm__ volatile (
            "xorq %%rax, %%rax\n\t"         // RAX = 0
            "xorq %%rsi, %%rsi\n\t"         // RSI = 0 (Loop counter)
            "movq %2, %%rdx\n\t"            // RDX = multiplicand
            "movq %3, %%rcx\n\t"            // RCX = multiplier (decrement counter)
            "testq %%rcx, %%rcx\n\t"        // Test if multiplier is zero
            "jz 2f\n\t"

            "1:\n\t"
            "incq %%rsi\n\t"                // RSI++ (Iteration count)
            "addq %%rdx, %%rax\n\t"         // RAX += RDX
            "decq %%rcx\n\t"                // RCX--
            "jnz 1b\n\t"                    // Loop while RCX != 0

            "2:\n\t"
            "pushfq\n\t"                    // Capture CPU RFLAGS
            "popq %1\n\t"
            "movq %%rax, %0\n\t"            // Output product
            "movq %%rsi, %4\n\t"            // Output iterations
            : "=r" (product), "=r" (rflags), "+r" (multiplicand), "+r" (multiplier), "=r" (iterations)
            :
            : "%rax", "%rcx", "%rdx", "%rsi", "cc"
        );

        res.assemblyProduct = product;
        res.rflags = rflags;
        res.bitIterations = static_cast<int>(iterations);
        res.matchesReference = (res.assemblyProduct == res.referenceProduct);

        res.CF = (rflags & (1ULL << 0)) != 0;
        res.ZF = (rflags & (1ULL << 6)) != 0;
        res.SF = (rflags & (1ULL << 7)) != 0;
        res.OF = (rflags & (1ULL << 11)) != 0;

        std::stringstream ss;
        ss << "RAX:0x" << std::hex << std::uppercase << res.assemblyProduct << std::dec
           << " (Added " << res.multiplicand << ", " << res.multiplier << " times = " << res.assemblyProduct << ")"
           << " | Steps: " << res.bitIterations
           << " | Flags: [ZF=" << res.ZF << " CF=" << res.CF << " SF=" << res.SF << " OF=" << res.OF << "]"
           << " | Match: " << (res.matchesReference ? "PASSED" : "FAILED");
        res.executionTrace = ss.str();

        return res;
    }
};

// ============================================================================
// 4. 64-BIT HARDWARE STRING PROCESSING ENGINE
// ============================================================================
class AssemblyStringProcessor {
public:
    // String Length calculation using native x86-64 repne scasb instruction
    static uint64_t computeStringLength(const char* str) {
        if (!str) return 0;
        uint64_t length = 0;

        /*
         * Native Hardware Instruction: repne scasb
         * Registers:
         *   %rdi = Address of input string buffer
         *   %al  = Byte value to match (0x00 null terminator)
         *   %rcx = Maximum scan limit (-1 = 0xFFFFFFFFFFFFFFFF)
         * Execution:
         *   cld        : Direction Flag DF = 0 (Scan forward from lower to higher memory)
         *   repne scasb: Scans byte at RDI, increments RDI, decrements RCX while Byte != AL
         *   notq %rcx  : Inverts RCX
         *   decq %rcx  : Yields precise string length in O(N) hardware bus cycles
         */
        __asm__ volatile (
            "movq %1, %%rdi\n\t"            // RDI = string address
            "xorq %%rax, %%rax\n\t"         // AL = 0x00 (Search for null-terminator)
            "movq $-1, %%rcx\n\t"           // RCX = 0xFFFFFFFFFFFFFFFF
            "cld\n\t"                       // Clear Direction Flag (Scan forward)
            "repne scasb\n\t"               // Scan string byte until byte == AL
            "notq %%rcx\n\t"                // Invert RCX bits
            "decq %%rcx\n\t"                // Length = (~RCX) - 1
            "movq %%rcx, %0\n\t"            // Store result
            : "=r" (length)
            : "r" (str)
            : "%rax", "%rcx", "%rdi", "memory", "cc"
        );

        return length;
    }

    // Token Occurrence Counter in 64-bit Assembly
    static uint64_t countTokenOccurrences(const std::string& text, const std::string& token) {
        if (text.empty() || token.empty() || token.length() > text.length()) {
            return 0;
        }

        const char* textPtr = text.c_str();
        const char* tokenPtr = token.c_str();
        uint64_t textLen = text.length();
        uint64_t tokenLen = token.length();
        uint64_t matchCount = 0;

        /*
         * 64-bit Assembly String Substring Search Algorithm:
         * Registers:
         *   %rsi = Pointer to source text buffer
         *   %rdi = Pointer to target token buffer
         *   %r8  = Text length
         *   %r9  = Token length
         *   %rax = Match Counter (Accumulator)
         *   %rcx = Outer loop index (0 to textLen - tokenLen)
         *   %rdx = Inner loop index (0 to tokenLen)
         *   %r10b, %r11b = Byte comparison registers
         */
        __asm__ volatile (
            "xorq %%rax, %%rax\n\t"         // RAX = 0 (Match count accumulator)
            "movq %1, %%rsi\n\t"            // RSI = textPtr
            "movq %2, %%rdi\n\t"            // RDI = tokenPtr
            "movq %3, %%r8\n\t"             // R8  = textLen
            "movq %4, %%r9\n\t"             // R9  = tokenLen
            "xorq %%rcx, %%rcx\n\t"         // RCX = 0 (Outer index i)

            "1:\n\t"                        // Outer loop: while (i <= textLen - tokenLen)
            "movq %%r8, %%r10\n\t"
            "subq %%r9, %%r10\n\t"          // R10 = textLen - tokenLen
            "cmpq %%r10, %%rcx\n\t"
            "ja 2f\n\t"                     // If i > textLen - tokenLen, exit outer loop

            // Inner comparison loop
            "xorq %%rdx, %%rdx\n\t"         // RDX = 0 (Inner index j)
            "3:\n\t"
            "cmpq %%r9, %%rdx\n\t"          // If j == tokenLen, full match found!
            "je 4f\n\t"

            // Compare text[i + j] with token[j]
            "movq %%rcx, %%r11\n\t"
            "addq %%rdx, %%r11\n\t"         // R11 = i + j
            "movb (%%rsi, %%r11), %%r10b\n\t" // R10b = text[i + j]
            "movb (%%rdi, %%rdx), %%r11b\n\t" // R11b = token[j]
            "cmpb %%r10b, %%r11b\n\t"
            "jne 5f\n\t"                    // Mismatch: break inner loop

            "incq %%rdx\n\t"                // j++
            "jmp 3b\n\t"

            "4:\n\t"                        // Match found!
            "incq %%rax\n\t"                // matchCount++

            "5:\n\t"                        // Advance outer index
            "incq %%rcx\n\t"                // i++
            "jmp 1b\n\t"

            "2:\n\t"                        // Finished
            "movq %%rax, %0\n\t"
            : "=r" (matchCount)
            : "r" (textPtr), "r" (tokenPtr), "r" (textLen), "r" (tokenLen)
            : "%rax", "%rcx", "%rdx", "%rsi", "%rdi", "%r8", "%r9", "%r10", "%r11", "cc"
        );

        return matchCount;
    }

    // Comprehensive analysis wrapper comparing assembly results with independent C++ references
    static StringAnalysisResult analyzeCombatLog(const std::string& combatLog, const std::string& token) {
        StringAnalysisResult res;
        res.text = combatLog;
        res.token = token;

        // Assembly String Length via repne scasb
        res.assemblyStringLength = computeStringLength(combatLog.c_str());
        res.referenceStringLength = combatLog.length(); // Trusted reference
        res.lengthMatches = (res.assemblyStringLength == res.referenceStringLength);

        // Assembly Token Count via 64-bit register loop
        res.assemblyMatchCount = countTokenOccurrences(combatLog, token);

        // Independent C++ trusted reference count
        uint64_t refCount = 0;
        if (!token.empty() && token.length() <= combatLog.length()) {
            size_t pos = 0;
            while ((pos = combatLog.find(token, pos)) != std::string::npos) {
                refCount++;
                pos++;
            }
        }
        res.referenceMatchCount = refCount;
        res.matchCountMatches = (res.assemblyMatchCount == res.referenceMatchCount);

        std::stringstream ss;
        ss << "Token: [" << token << "] -> Assembly Count: " << res.assemblyMatchCount 
           << " | Reference Count: " << res.referenceMatchCount 
           << " (" << (res.matchCountMatches ? "MATCH SUCCESS" : "MISMATCH") << ")"
           << " | Length: " << res.assemblyStringLength << " bytes ("
           << (res.lengthMatches ? "LEN SUCCESS" : "LEN MISMATCH") << ")";
        res.analysisTrace = ss.str();

        return res;
    }
};

// ============================================================================
// 5. 64-BIT HEXADECIMAL TO PACKED BCD ENCODING ALGORITHM
// Defined in CO3_Prac04_Hex_Packed_BCD_Converter.hpp
// ============================================================================

// ============================================================================
// 7. COMPREHENSIVE COA CO3 VERIFICATION & DEMONSTRATION SUITE
// ============================================================================
class COA_CO3_Engine {
public:
    // Executive College Demonstration Summary (matching syllabus review presentation)
    static void demonstrateCollegeExecutiveSummary() {
        std::cout << "========================================\n";
        std::cout << "       CLASH OF BYTES - COA CO3        \n";
        std::cout << "========================================\n\n";

        // [1] Shift-and-Add Multiplication
        ShiftAddALUResult sa = AssemblyShiftAddMultiplier::executeShiftAdd(28, 5);
        std::cout << "[1] SHIFT-AND-ADD MULTIPLICATION\n";
        std::cout << "Input: 28 x 5\n";
        std::cout << "Result: " << sa.assemblyProduct << "\n";
        std::cout << "Status: " << (sa.matchesReference ? "PASS" : "FAIL") << "\n\n";

        // [2] Booth's Multiplication Algorithm
        BoothALUResult bm = AssemblyBoothMultiplier::executeBooth(-5, 4);
        std::cout << "[2] BOOTH'S MULTIPLICATION ALGORITHM\n";
        std::cout << "Multiplicand: -5\n";
        std::cout << "Multiplier: 4\n";
        std::cout << "Result: " << bm.assemblyProduct << "\n";
        std::cout << "Status: " << (bm.matchesReference ? "PASS" : "FAIL") << "\n\n";

        // [3] String Processing
        const char* fighterStr = "FIGHTER";
        uint64_t fighterLen = AssemblyStringProcessor::computeStringLength(fighterStr);
        std::cout << "[3] STRING PROCESSING\n";
        std::cout << "String: " << fighterStr << "\n";
        std::cout << "Length: " << fighterLen << "\n";
        std::cout << "Status: " << (fighterLen == std::strlen(fighterStr) ? "PASS" : "FAIL") << "\n\n";

        // [4] Token Occurrence Search
        std::string sampleLog = "[COMBAT] CRITICAL HIT! ATTACK CONNECTED! CRITICAL COMBO!";
        std::string searchTok = "CRITICAL";
        uint64_t occurrences = AssemblyStringProcessor::countTokenOccurrences(sampleLog, searchTok);
        std::cout << "[4] TOKEN OCCURRENCE SEARCH\n";
        std::cout << "Search Token: " << searchTok << "\n";
        std::cout << "Occurrences: " << occurrences << "\n";
        std::cout << "Status: " << (occurrences == 2 ? "PASS" : "FAIL") << "\n\n";

        std::cout << "========================================\n";
        std::cout << "       CO3 DEMONSTRATION COMPLETE       \n";
        std::cout << "========================================\n\n";
    }

    static void demonstrateComputationalAlgorithms() {
        // First display the executive summary for college project demonstration
        demonstrateCollegeExecutiveSummary();

        std::cout << "===================================================================\n";
        std::cout << " [COA PRACTICAL 04 / TASK 3] ALGORITHMIC & STRING PROCESSING (CO3)\n";
        std::cout << " Subject: Computer Organization & Architecture (Course Code: 2310221L)\n";
        std::cout << " Architecture: 64-bit AMD64/x86-64 | CPU Registers: RAX, RBX, RCX, RDX\n";
        std::cout << "===================================================================\n\n";

        // --------------------------------------------------------------------
        // PHASE 1: SHIFT-AND-ADD MULTIPLICATION (UNSIGNED HARDWARE MULTIPLIER)
        // --------------------------------------------------------------------
        std::cout << "--- [PHASE 1] 64-BIT SHIFT-AND-ADD MULTIPLICATION ALGORITHM ---\n";
        std::cout << "  Simulating Combat Strike Multipliers via Hardware Micro-Architecture:\n";

        struct TestCase {
            uint64_t multiplicand;
            uint64_t multiplier;
            std::string label;
        };

        std::vector<TestCase> cases = {
            {28, 5, "Standard Strike Damage (28 base x 5 combo multiplier)"},
            {45, 3, "Special Art Burst (45 base x 3 rage factor)"},
            {12, 16, "Multi-hit Rush (12 base x 16 rapid strikes)"},
            {110, 2, "Shadow Ninja Finisher (110 base x 2 critical bonus)"},
            {250, 4, "Berserker Ultimate (250 base x 4 overdrive)"}
        };

        for (const auto& tc : cases) {
            ShiftAddALUResult r = AssemblyShiftAddMultiplier::executeShiftAdd(tc.multiplicand, tc.multiplier);
            std::cout << "  " << tc.label << "\n";
            std::cout << "   -> " << r.executionTrace << "\n";
            std::cout << "   -> Validation: " << (r.matchesReference ? "[PASSED: Assembly == Reference]" : "[FAILED]") << "\n\n";
        }

        // --------------------------------------------------------------------
        // PHASE 2: BOOTH'S MULTIPLICATION ALGORITHM (SIGNED COMBAT MODIFIERS)
        // --------------------------------------------------------------------
        std::cout << "--- [PHASE 2] 64-BIT BOOTH'S MULTIPLICATION ALGORITHM (SIGNED COMBAT MODIFIERS) ---\n";
        std::cout << "  Simulating Signed Health Adjustments & Damage Debuffs via Hardware Booth Micro-Architecture:\n\n";
        std::cout << "  [FIGHTING GAME SCENARIO] Debuff Scaling & Health Drain Calculation:\n";
        std::cout << "    In Clash of Bytes, a fighter affected by 'Curse of Vulnerability' receives\n";
        std::cout << "    a signed negative health adjustment (-5 HP per tick) across 4 turns.\n";
        std::cout << "    Multiplicand (Health Adjustment) = -5\n";
        std::cout << "    Multiplier   (Turn Duration)     =  4\n";
        std::cout << "    Expected Signed Product          = -20 HP\n";
        std::cout << "    Demonstrates why signed two's complement multiplication is essential in games.\n\n";

        struct BoothTestCase {
            int64_t multiplicand;
            int64_t multiplier;
            int64_t expected;
            std::string label;
        };

        std::vector<BoothTestCase> boothCases = {
            {-5,  4, -20, "Fighter Health Drain Debuff (-5 HP/tick x 4 turns)"},
            { 5,  4,  20, "Fighter Positive Health Regen (+5 HP/tick x 4 turns)"},
            {-5, -4,  20, "Inverted Debuff Reversal (-5 penalty x -4 inverse scaling)"},
            { 7,  3,  21, "Tactical Damage Buff (+7 bonus x 3 attack combo)"},
            { 0,  5,   0, "Neutral Combat State (0 base modifier x 5 turns)"},
            {12, -3, -36, "Armor Decay Penalty (+12 base penalty x -3 resistance reduction)"},
            {-8,  2, -16, "Poison Damage Over Time (-8 venom x 2 rounds)"}
        };

        for (const auto& tc : boothCases) {
            BoothALUResult r = AssemblyBoothMultiplier::executeBooth(tc.multiplicand, tc.multiplier);
            bool pass = (r.assemblyProduct == tc.expected) && r.matchesReference;
            std::cout << "  " << tc.label << "\n";
            std::cout << "   -> Multiplicand: " << tc.multiplicand << " | Multiplier: " << tc.multiplier << "\n";
            std::cout << "   -> Assembly Product: " << r.assemblyProduct << " | Expected: " << tc.expected << "\n";
            std::cout << "   -> Hardware Trace: " << r.executionTrace << "\n";
            std::cout << "   -> Validation: " << (pass ? "[PASS: Assembly == Reference]" : "[FAIL]") << "\n\n";
        }

        // Print Formatted Tabular Summary of Booth's Test Cases
        std::cout << "  =========================================================================\n";
        std::cout << "              BOOTH'S MULTIPLICATION ALGORITHM - VALIDATION TABLE          \n";
        std::cout << "  =========================================================================\n";
        std::cout << "  | Multiplicand | Multiplier | Expected Result | Assembly Result | Status |\n";
        std::cout << "  |--------------|------------|-----------------|-----------------|--------|\n";
        for (const auto& tc : boothCases) {
            BoothALUResult r = AssemblyBoothMultiplier::executeBooth(tc.multiplicand, tc.multiplier);
            bool pass = (r.assemblyProduct == tc.expected) && r.matchesReference;
            std::cout << "  | " << std::right << std::dec << std::setw(12) << tc.multiplicand
                      << " | " << std::setw(10) << tc.multiplier
                      << " | " << std::setw(15) << tc.expected
                      << " | " << std::setw(15) << r.assemblyProduct
                      << " |  " << (pass ? "PASS" : "FAIL") << "  |\n";
        }
        std::cout << "  =========================================================================\n\n";

        // --------------------------------------------------------------------
        // PHASE 3: SUCCESSIVE ADDITION (BENCHMARK REFERENCE)
        // --------------------------------------------------------------------
        std::cout << "--- [PHASE 3] 64-BIT SUCCESSIVE ADDITION MULTIPLICATION ALGORITHM ---\n";
        std::cout << "  Comparing Successive Addition Loop (DECQ RCX & JNZ) with Shift-and-Add & Booth:\n";

        for (const auto& tc : cases) {
            ShiftAddALUResult r = AssemblySuccessiveAddition::executeSuccessiveAdd(tc.multiplicand, tc.multiplier);
            std::cout << "  " << tc.label << "\n";
            std::cout << "   -> " << r.executionTrace << "\n";
            std::cout << "   -> Validation: " << (r.matchesReference ? "[PASSED: Assembly == Reference]" : "[FAILED]") << "\n\n";
        }
    }

    static void demonstrateStringProcessing() {
        std::cout << "--- [PHASE 4] 64-BIT HARDWARE STRING PROCESSING (repne scasb) ---\n";
        std::cout << "  Evaluating string length directly using CPU string scan hardware:\n";

        std::vector<std::string> sampleLogs = {
            "[P1] BLAZE_WARRIOR: DRAGON_UPPER HIT!",
            "[P2] SHADOW_NINJA: SHADOW_STEP DODGED ATTACK!",
            "[COMBAT_EVENT] CRITICAL HIT LANDED! COMBO X3 MULTIPLIER APPLIED! VICTORY IMMINENT!",
            "" // Empty string boundary
        };

        for (const auto& log : sampleLogs) {
            uint64_t asmLen = AssemblyStringProcessor::computeStringLength(log.c_str());
            uint64_t refLen = log.length();
            bool matches = (asmLen == refLen);
            std::cout << "  Log: \"" << (log.empty() ? "<EMPTY_STRING>" : log) << "\"\n";
            std::cout << "   -> Assembly (repne scasb): " << asmLen << " bytes"
                      << " | Reference: " << refLen << " bytes"
                      << " | Status: " << (matches ? "[PASSED]" : "[FAILED]") << "\n\n";
        }

        std::cout << "--- [PHASE 5] COMBAT LOG TOKEN OCCURRENCE COUNTING IN ASSEMBLY ---\n";
        std::cout << "  Analyzing combat logs for specific tactical action tokens:\n";

        std::string fullBattleLog = 
            "[ROUND 1] BLAZE_WARRIOR initiated ATTACK. SHADOW_NINJA raised BLOCK. "
            "BLAZE_WARRIOR executed SPECIAL_ATTACK! CRITICAL HIT! "
            "SHADOW_NINJA retaliated with ATTACK! CRITICAL HIT! "
            "BLAZE_WARRIOR activated DEFEND! BLOCK successful! "
            "SHADOW_NINJA unleashed SPECIAL_ATTACK! CRITICAL HIT! VICTORY!";

        std::vector<std::string> searchTokens = {"CRITICAL", "HIT", "ATTACK", "BLOCK", "SPECIAL_ATTACK", "DEFEND", "MISS"};

        for (const auto& tok : searchTokens) {
            StringAnalysisResult r = AssemblyStringProcessor::analyzeCombatLog(fullBattleLog, tok);
            std::cout << "  Search Token: \"" << std::left << std::setw(15) << tok << "\""
                      << " | Count: " << r.assemblyMatchCount
                      << " | Ref: " << r.referenceMatchCount
                      << " | " << (r.matchCountMatches ? "[PASSED]" : "[FAILED]") << "\n";
        }
        std::cout << "\n";
    }

    static void demonstratePackedBCD() {
        std::cout << "--- [PHASE 6] 64-BIT HEXADECIMAL TO PACKED BCD SCORE ENCODER ---\n";
        std::cout << "  Converting arcade damage outputs into packed decimal for HUD display:\n";

        std::vector<uint64_t> scores = {28, 140, 500, 1024, 9999, 123456};
        for (uint64_t sc : scores) {
            uint64_t bcd = HexPackedBCDConverter::convertHexToPackedBCD(sc);
            std::cout << "  Score: " << std::dec << std::setw(6) << sc 
                      << " (Hex: 0x" << std::hex << std::uppercase << sc << ")"
                      << " -> Packed BCD: 0x" << bcd << std::dec << "\n";
        }
        std::cout << "\n";
    }

    static void demonstrateEdgeCasesAndValidation() {
        std::cout << "--- [PHASE 7] EDGE CASES, ZERO VALUES & BOUNDARY VALIDATION ---\n";
        std::cout << "  Testing arithmetic and string algorithms against boundary constraints:\n\n";

        // 1. Zero values in Shift-and-Add
        std::cout << "  [TEST 1] Zero Multiplicand / Multiplier (Shift-and-Add):\n";
        ShiftAddALUResult z1 = AssemblyShiftAddMultiplier::executeShiftAdd(0, 100);
        std::cout << "   -> 0 x 100 = " << z1.assemblyProduct << " (ZF=" << z1.ZF << ") | "
                  << (z1.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        ShiftAddALUResult z2 = AssemblyShiftAddMultiplier::executeShiftAdd(100, 0);
        std::cout << "   -> 100 x 0 = " << z2.assemblyProduct << " (ZF=" << z2.ZF << ") | "
                  << (z2.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        ShiftAddALUResult z3 = AssemblyShiftAddMultiplier::executeShiftAdd(0, 0);
        std::cout << "   -> 0 x 0 = " << z3.assemblyProduct << " (ZF=" << z3.ZF << ") | "
                  << (z3.matchesReference ? "[PASSED]" : "[FAILED]") << "\n\n";

        // 2. Identity Multiplier (1)
        std::cout << "  [TEST 2] Identity Multiplier (x 1):\n";
        ShiftAddALUResult id = AssemblyShiftAddMultiplier::executeShiftAdd(42, 1);
        std::cout << "   -> 42 x 1 = " << id.assemblyProduct << " | "
                  << (id.matchesReference ? "[PASSED]" : "[FAILED]") << "\n\n";

        // 3. Powers of Two (bit shift efficiency)
        std::cout << "  [TEST 3] Powers of Two Multipliers (2, 4, 8, 16, 64):\n";
        uint64_t powers[] = {2, 4, 8, 16, 64};
        for (uint64_t p : powers) {
            ShiftAddALUResult r = AssemblyShiftAddMultiplier::executeShiftAdd(25, p);
            std::cout << "   -> 25 x " << p << " = " << r.assemblyProduct << " (Cycles: " << r.bitIterations << ") | "
                      << (r.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";
        }
        std::cout << "\n";

        // 4. Large 64-bit Values
        std::cout << "  [TEST 4] High-Magnitude 64-bit Arithmetic:\n";
        ShiftAddALUResult lg = AssemblyShiftAddMultiplier::executeShiftAdd(1000000ULL, 500000ULL);
        std::cout << "   -> 1,000,000 x 500,000 = " << lg.assemblyProduct << " | "
                  << (lg.matchesReference ? "[PASSED]" : "[FAILED]") << "\n\n";

        // 5. String Edge Cases
        std::cout << "  [TEST 5] String Edge Cases (Empty strings & non-existent tokens):\n";
        StringAnalysisResult se1 = AssemblyStringProcessor::analyzeCombatLog("", "TOKEN");
        std::cout << "   -> Search in empty string: Count=" << se1.assemblyMatchCount << " | "
                  << (se1.matchCountMatches ? "[PASSED]" : "[FAILED]") << "\n";

        StringAnalysisResult se2 = AssemblyStringProcessor::analyzeCombatLog("HELLO WORLD", "");
        std::cout << "   -> Search empty token in string: Count=" << se2.assemblyMatchCount << " | "
                  << (se2.matchCountMatches ? "[PASSED]" : "[FAILED]") << "\n";

        StringAnalysisResult se3 = AssemblyStringProcessor::analyzeCombatLog("PUNCH KICK", "MISSING_ACTION");
        std::cout << "   -> Search non-existent token: Count=" << se3.assemblyMatchCount << " | "
                  << (se3.matchCountMatches ? "[PASSED]" : "[FAILED]") << "\n\n";

        // 6. Booth's Algorithm Signed Boundary & Symmetry Tests
        std::cout << "  [TEST 6] Booth Signed Boundary & Symmetry Tests:\n";
        BoothALUResult b_z1 = AssemblyBoothMultiplier::executeBooth(0, -5);
        std::cout << "   -> Zero Multiplicand:  0 x (-5) = " << b_z1.assemblyProduct << " (ZF=" << b_z1.ZF << ") | "
                  << (b_z1.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        BoothALUResult b_z2 = AssemblyBoothMultiplier::executeBooth(-5, 0);
        std::cout << "   -> Zero Multiplier:   (-5) x 0 = " << b_z2.assemblyProduct << " (ZF=" << b_z2.ZF << ") | "
                  << (b_z2.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        BoothALUResult b_id1 = AssemblyBoothMultiplier::executeBooth(-42, 1);
        std::cout << "   -> Signed Identity:   (-42) x 1 = " << b_id1.assemblyProduct << " | "
                  << (b_id1.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        BoothALUResult b_id2 = AssemblyBoothMultiplier::executeBooth(42, -1);
        std::cout << "   -> Negative Identity:  42 x (-1) = " << b_id2.assemblyProduct << " | "
                  << (b_id2.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        BoothALUResult b_neg = AssemblyBoothMultiplier::executeBooth(-1, -1);
        std::cout << "   -> Negative Unity:    (-1) x (-1) = " << b_neg.assemblyProduct << " | "
                  << (b_neg.matchesReference ? "[PASSED]" : "[FAILED]") << "\n";

        BoothALUResult b_sym1 = AssemblyBoothMultiplier::executeBooth(-15, 6);
        BoothALUResult b_sym2 = AssemblyBoothMultiplier::executeBooth(15, -6);
        bool symPass = (b_sym1.assemblyProduct == b_sym2.assemblyProduct) && (b_sym1.assemblyProduct == -90);
        std::cout << "   -> Sign Symmetry:     (-15) x 6 = " << b_sym1.assemblyProduct 
                  << " vs 15 x (-6) = " << b_sym2.assemblyProduct << " | "
                  << (symPass ? "[PASSED: Commutative Sign Inversion]" : "[FAILED]") << "\n";

        BoothALUResult b_lg = AssemblyBoothMultiplier::executeBooth(-1000000LL, 500000LL);
        std::cout << "   -> Large Signed:      (-1,000,000) x 500,000 = " << b_lg.assemblyProduct << " | "
                  << (b_lg.matchesReference ? "[PASSED]" : "[FAILED]") << "\n\n";
    }

    static void displayProcessorLevelDocumentation() {
        std::cout << "===================================================================\n";
        std::cout << " [PROCESSOR-LEVEL DOCUMENTATION & CO3 ACADEMIC SPECIFICATION]\n";
        std::cout << "===================================================================\n";
        std::cout << " Course Outcome : CO3 - Algorithmic and String Processing in Assembly\n";
        std::cout << " Syllabus Topic : Microprocessor Assembly Programming (Arithmetic & Strings)\n";
        std::cout << " Target CPU     : Intel 64 / AMD64 (x86-64 Instruction Set Architecture)\n\n";
        std::cout << " Processor Register Allocation:\n";
        std::cout << "  - RAX : Primary 64-bit Accumulator (A) [Holds running product / upper 64 bits in Booth]\n";
        std::cout << "  - RBX : Multiplier Register (Q) [Shifted right in Shift-and-Add; holds lower product in Booth]\n";
        std::cout << "  - RCX : Counter Register [Cycle count for loops: 64 iterations in Booth; limit for repne scasb]\n";
        std::cout << "  - RDX : Multiplicand Register (M) [Shifted left in Shift-and-Add; constant operand in Booth]\n";
        std::cout << "  - RSI : Source Index / Q_-1 Latch [Holds previous multiplier bit in Booth; text pointer in strings]\n";
        std::cout << "  - RDI : Destination Index Register [Address pointer for string scan & token buffer]\n";
        std::cout << "  - R8  : Temporary Bit Inspection Register [Isolates Q0 bit for overlapping pair test in Booth]\n";
        std::cout << "  - RFLAGS : 64-bit CPU Hardware Status Flags (ZF, CF, SF, OF captured via pushfq)\n\n";
        std::cout << " Core Assembly Instructions Used:\n";
        std::cout << "  1. testq $1, %rbx    : Non-destructive logical AND to inspect multiplier LSB bit\n";
        std::cout << "  2. cmpq %rsi, %r8    : Compare current bit Q0 with previous bit Q_-1 in Booth algorithm\n";
        std::cout << "  3. jz / jnz / je     : Conditional branching on Zero Flag (ZF)\n";
        std::cout << "  4. addq %rdx, %rax   : 64-bit integer addition (A = A + M on pair 01 in Booth)\n";
        std::cout << "  5. subq %rdx, %rax   : 64-bit integer subtraction (A = A - M on pair 10 in Booth)\n";
        std::cout << "  6. shrdq $1, %rax, %rbx: Double-precision right shift: RBX >>= 1 with LSB of RAX into MSB of RBX\n";
        std::cout << "  7. sarq $1, %rax     : Arithmetic right shift on Accumulator A (preserves sign bit for signed 2's comp)\n";
        std::cout << "  8. shlq $1, %rdx     : Logical shift left (multiplies multiplicand by 2 in Shift-and-Add)\n";
        std::cout << "  9. shrq $1, %rbx     : Logical shift right (divides multiplier by 2 in Shift-and-Add)\n";
        std::cout << " 10. decq %rcx         : Decrement loop counter with automatic ZF condition code\n";
        std::cout << " 11. repne scasb       : Repeat string scan while not equal (searches for 0x00 null byte)\n";
        std::cout << " 12. cld               : Clear Direction Flag (DF=0 ensures autoincrement memory traversal)\n";
        std::cout << " 13. pushfq / popq     : Direct stack operations capturing live CPU hardware status flags\n\n";
        std::cout << " Booth's Recoding Principle:\n";
        std::cout << "  Booth's algorithm recodes runs of consecutive 1s into a single subtraction\n";
        std::cout << "  at the start of the run (pair 10) and an addition at the end of the run (pair 01).\n";
        std::cout << "  Isolated 00 and 11 pairs require no arithmetic, optimizing CPU cycles.\n";
        std::cout << "  Arithmetic Right Shift (sarq/shrdq) preserves two's complement sign throughout.\n";
        std::cout << "===================================================================\n\n";
    }

    static void runAll() {
        demonstrateComputationalAlgorithms();
        demonstrateStringProcessing();
        demonstratePackedBCD();
        demonstrateEdgeCasesAndValidation();
        displayProcessorLevelDocumentation();
    }
};

#endif // CO3_PRAC04_ALGORITHMIC_AND_STRING_PROCESSING_HPP

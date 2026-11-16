#ifndef CO2_PRAC03_X86_64_ASSEMBLY_DAMAGE_ENGINE_HPP
#define CO2_PRAC03_X86_64_ASSEMBLY_DAMAGE_ENGINE_HPP

#include <cstdint>
#include <string>
#include <sstream>
#include <iomanip>
#include "CO3_Prac04_Hex_Packed_BCD_Converter.hpp"

/*
 * Subject: Computer Organization & Architecture
 * Course Outcome: CO2 | Practical 03
 * Topic: 64-bit Hardware Assembly Language Execution (__asm__ on CPU Registers RAX, RBX, RDX)
 */
struct CombatALUResult {
    uint64_t finalDamage;
    uint64_t rawDifference;
    uint64_t bcdScore;
    bool ZF; // Zero Flag
    bool CF; // Carry Flag
    bool SF; // Sign Flag
    bool OF; // Overflow Flag
    std::string assemblyTrace;
};

class AssemblyDamageEngine {
public:
    static CombatALUResult calculateHitDamage(uint64_t attackPower, uint64_t defense, uint64_t comboMultiplier) {
        CombatALUResult result;
        uint64_t netAttack = 0;
        uint64_t product = 0;
        uint64_t rflags = 0;

        // 1. Native x86-64 Assembly: SUBQ (%rbx, %rax) & pushfq to capture CPU hardware EFLAGS
        __asm__ volatile (
            "movq %2, %%rax\n\t"        // Load Attack Power into RAX
            "movq %3, %%rbx\n\t"        // Load Defense into RBX
            "subq %%rbx, %%rax\n\t"     // RAX = RAX - RBX
            "pushfq\n\t"                // Push 64-bit RFLAGS to stack
            "popq %1\n\t"               // Pop RFLAGS into variable
            "movq %%rax, %0\n\t"        // Save result
            : "=r" (netAttack), "=r" (rflags)
            : "r" (attackPower), "r" (defense)
            : "%rax", "%rbx", "cc"
        );

        result.rawDifference = netAttack;

        // Clamp minimum base damage to 4
        if ((int64_t)netAttack < 4) netAttack = 4;

        // 2. Native x86-64 Assembly: IMULQ (%rbx, %rax) for Combo Multiplication
        __asm__ volatile (
            "movq %1, %%rax\n\t"        // Net attack in RAX
            "movq %2, %%rbx\n\t"        // Combo multiplier in RBX
            "imulq %%rbx, %%rax\n\t"    // RAX = RAX * RBX
            "movq %%rax, %0\n\t"        // Save final damage
            : "=r" (product)
            : "r" (netAttack), "r" (comboMultiplier)
            : "%rax", "%rbx", "cc"
        );

        result.finalDamage = product;

        // Parse Hardware EFLAGS (Bit 0: CF, Bit 6: ZF, Bit 7: SF, Bit 11: OF)
        result.CF = (rflags & (1 << 0)) != 0;
        result.ZF = (rflags & (1 << 6)) != 0;
        result.SF = (rflags & (1 << 7)) != 0;
        result.OF = (rflags & (1 << 11)) != 0;

        // Convert Hex to Packed BCD
        result.bcdScore = HexPackedBCDConverter::convertHexToPackedBCD(result.finalDamage);

        // Build live HUD trace string
        std::stringstream ss;
        ss << "RAX:0x" << std::hex << std::uppercase << result.finalDamage
           << " [SUB: " << std::dec << attackPower << "-" << defense 
           << " | IMUL: x" << comboMultiplier << "]"
           << " | FLAGS:[ZF=" << result.ZF << " CF=" << result.CF << " SF=" << result.SF << " OF=" << result.OF << "]"
           << " | BCD:0x" << std::hex << result.bcdScore;
        result.assemblyTrace = ss.str();

        return result;
    }
};

#endif // CO2_PRAC03_X86_64_ASSEMBLY_DAMAGE_ENGINE_HPP

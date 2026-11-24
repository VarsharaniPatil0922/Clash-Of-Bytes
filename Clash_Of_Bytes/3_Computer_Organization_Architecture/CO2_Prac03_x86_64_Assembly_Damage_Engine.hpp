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

// Link external GNU Assembler symbols when compiled and linked via GCC
extern "C" {
    uint64_t calculate_combat_damage_gas(uint64_t atk, uint64_t def, uint64_t combo);
    uint64_t calculate_combat_damage_with_flags_gas(uint64_t atk, uint64_t def, uint64_t combo, uint64_t* outRflags);
}

class AssemblyDamageEngine {
public:
    static CombatALUResult calculateHitDamage(uint64_t attackPower, uint64_t defense, uint64_t comboMultiplier) {
        CombatALUResult result;
        uint64_t finalDamage = 0;
        uint64_t rflags = 0;

#if defined(USE_GAS_ASSEMBLY)
        // Call standalone 64-bit GNU Assembler routine (CO2_Prac03_Combat_Damage_GAS.s)
        finalDamage = calculate_combat_damage_with_flags_gas(attackPower, defense, comboMultiplier, &rflags);
        uint64_t netDiff = (attackPower > defense) ? (attackPower - defense) : 4;
        result.rawDifference = netDiff;
        result.finalDamage = finalDamage;
#elif defined(__x86_64__) || defined(_M_X64)
        // Native x86-64 Inline Assembly Fallback (demonstrating RAX, RBX, CMP, JGE, SUB, IMUL)
        uint64_t netAttack = 0;
        __asm__ volatile (
            "movq %2, %%rax\n\t"        // Load Attack Power into RAX
            "movq %3, %%rbx\n\t"        // Load Defense into RBX
            "subq %%rbx, %%rax\n\t"     // RAX = RAX - RBX
            "pushfq\n\t"                // Push 64-bit RFLAGS to stack
            "popq %1\n\t"               // Pop RFLAGS into variable
            "cmpq $4, %%rax\n\t"        // Compare net damage to minimum clamp threshold
            "jge 1f\n\t"                // Conditional jump if RAX >= 4
            "movq $4, %%rax\n\t"        // Minimum damage clamp
            "1:\n\t"
            "imulq %4, %%rax\n\t"       // RAX = RAX * comboMultiplier
            "movq %%rax, %0\n\t"        // Save final damage
            : "=r" (finalDamage), "=r" (rflags)
            : "r" (attackPower), "r" (defense), "r" (comboMultiplier)
            : "%rax", "%rbx", "cc"
        );
        result.rawDifference = (attackPower > defense) ? (attackPower - defense) : 4;
        result.finalDamage = finalDamage;
#else
        // Deterministic C++ fallback for non-x86 platforms
        uint64_t netAttack = (attackPower > defense) ? (attackPower - defense) : 4;
        result.rawDifference = netAttack;
        result.finalDamage = netAttack * comboMultiplier;
        rflags = (result.finalDamage == 0) ? 0x40 : 0x00;
#endif

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

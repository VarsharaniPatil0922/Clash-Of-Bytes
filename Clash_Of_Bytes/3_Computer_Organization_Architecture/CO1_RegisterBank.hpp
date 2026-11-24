#ifndef CO1_REGISTER_BANK_HPP
#define CO1_REGISTER_BANK_HPP

#include <cstdint>
#include <string>
#include <iostream>
#include <iomanip>
#include <unordered_map>
#include <vector>
#include <algorithm>
#include "CO1_Prac01_80386_ALU_Simulator.hpp"

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome 1 (CO1) | Task 1 & 2: Dedicated Processor Register Bank
 * ==============================================================================
 * Architecture: Educational x86 / AMD64 Compatible Register Architecture
 * Supports dual 32-bit (80386 legacy) and 64-bit (x86-64 long mode) register access.
 * In x86-64, writes to 32-bit registers (e.g. EAX) automatically zero-extend into
 * the upper 32 bits of their 64-bit parent registers (e.g. RAX).
 */

enum class RegId {
    RAX = 0, RBX, RCX, RDX, RSI, RDI, RBP, RSP, RIP, RFLAGS,
    COUNT
};

class RegisterBank {
private:
    uint64_t regs[static_cast<size_t>(RegId::COUNT)];
    uint64_t prevRegs[static_cast<size_t>(RegId::COUNT)];

    static std::string toUpper(std::string s) {
        for (char& c : s) c = static_cast<char>(::toupper(c));
        return s;
    }

public:
    RegisterBank() {
        reset();
    }

    void reset() {
        for (size_t i = 0; i < static_cast<size_t>(RegId::COUNT); ++i) {
            regs[i] = 0;
            prevRegs[i] = 0;
        }
        // Initialize RSP to safe simulated stack base (e.g., 0x7FFF0000)
        regs[static_cast<size_t>(RegId::RSP)] = 0x7FFF0000;
        prevRegs[static_cast<size_t>(RegId::RSP)] = 0x7FFF0000;
        // Default RFLAGS with standard reserved bit 1 set (0x202 = IF, bit 1)
        regs[static_cast<size_t>(RegId::RFLAGS)] = 0x0002;
        prevRegs[static_cast<size_t>(RegId::RFLAGS)] = 0x0002;
    }

    void snapshot() {
        for (size_t i = 0; i < static_cast<size_t>(RegId::COUNT); ++i) {
            prevRegs[i] = regs[i];
        }
    }

    // --------------------------------------------------------------------------
    // 64-bit Register Accessors
    // --------------------------------------------------------------------------
    uint64_t get64(RegId id) const {
        size_t idx = static_cast<size_t>(id);
        return (idx < static_cast<size_t>(RegId::COUNT)) ? regs[idx] : 0;
    }

    void set64(RegId id, uint64_t val) {
        size_t idx = static_cast<size_t>(id);
        if (idx < static_cast<size_t>(RegId::COUNT)) {
            regs[idx] = val;
        }
    }

    // --------------------------------------------------------------------------
    // 32-bit Register Accessors (Zero-extends to 64-bit on write)
    // --------------------------------------------------------------------------
    uint32_t get32(RegId id) const {
        return static_cast<uint32_t>(get64(id) & 0xFFFFFFFFULL);
    }

    void set32(RegId id, uint32_t val) {
        // x86-64 hardware semantic: 32-bit operations zero-extend into 64-bit
        set64(id, static_cast<uint64_t>(val));
    }

    // --------------------------------------------------------------------------
    // String-based Name Lookup (Case-Insensitive)
    // --------------------------------------------------------------------------
    bool resolveRegisterName(const std::string& name, RegId& outId, bool& outIs32Bit) const {
        std::string n = toUpper(name);
        if (n == "RAX") { outId = RegId::RAX; outIs32Bit = false; return true; }
        if (n == "EAX") { outId = RegId::RAX; outIs32Bit = true;  return true; }
        if (n == "RBX") { outId = RegId::RBX; outIs32Bit = false; return true; }
        if (n == "EBX") { outId = RegId::RBX; outIs32Bit = true;  return true; }
        if (n == "RCX") { outId = RegId::RCX; outIs32Bit = false; return true; }
        if (n == "ECX") { outId = RegId::RCX; outIs32Bit = true;  return true; }
        if (n == "RDX") { outId = RegId::RDX; outIs32Bit = false; return true; }
        if (n == "EDX") { outId = RegId::RDX; outIs32Bit = true;  return true; }
        if (n == "RSI") { outId = RegId::RSI; outIs32Bit = false; return true; }
        if (n == "ESI") { outId = RegId::RSI; outIs32Bit = true;  return true; }
        if (n == "RDI") { outId = RegId::RDI; outIs32Bit = false; return true; }
        if (n == "EDI") { outId = RegId::RDI; outIs32Bit = true;  return true; }
        if (n == "RBP") { outId = RegId::RBP; outIs32Bit = false; return true; }
        if (n == "EBP") { outId = RegId::RBP; outIs32Bit = true;  return true; }
        if (n == "RSP") { outId = RegId::RSP; outIs32Bit = false; return true; }
        if (n == "ESP") { outId = RegId::RSP; outIs32Bit = true;  return true; }
        if (n == "RIP") { outId = RegId::RIP; outIs32Bit = false; return true; }
        if (n == "EIP") { outId = RegId::RIP; outIs32Bit = true;  return true; }
        if (n == "RFLAGS") { outId = RegId::RFLAGS; outIs32Bit = false; return true; }
        if (n == "EFLAGS") { outId = RegId::RFLAGS; outIs32Bit = true;  return true; }
        return false;
    }

    uint64_t readByName(const std::string& name, bool* success = nullptr) const {
        RegId id;
        bool is32;
        if (resolveRegisterName(name, id, is32)) {
            if (success) *success = true;
            return is32 ? get32(id) : get64(id);
        }
        if (success) *success = false;
        return 0;
    }

    bool writeByName(const std::string& name, uint64_t val) {
        RegId id;
        bool is32;
        if (resolveRegisterName(name, id, is32)) {
            if (is32) set32(id, static_cast<uint32_t>(val));
            else set64(id, val);
            return true;
        }
        return false;
    }

    // --------------------------------------------------------------------------
    // EFLAGS / RFLAGS Decoding & Updating
    // --------------------------------------------------------------------------
    ALUFlags getALUFlags() const {
        uint64_t f = get64(RegId::RFLAGS);
        ALUFlags af;
        af.CF = (f & 0x0001) != 0; // Bit 0
        af.ZF = (f & 0x0040) != 0; // Bit 6
        af.SF = (f & 0x0080) != 0; // Bit 7
        af.OF = (f & 0x0800) != 0; // Bit 11
        return af;
    }

    void setALUFlags(const ALUFlags& af) {
        uint64_t f = get64(RegId::RFLAGS);
        if (af.CF) f |= 0x0001; else f &= ~0x0001ULL;
        if (af.ZF) f |= 0x0040; else f &= ~0x0040ULL;
        if (af.SF) f |= 0x0080; else f &= ~0x0080ULL;
        if (af.OF) f |= 0x0800; else f &= ~0x0800ULL;
        set64(RegId::RFLAGS, f);
    }

    // --------------------------------------------------------------------------
    // Register Bank Display & Value Tracking
    // --------------------------------------------------------------------------
    void display(bool show64 = true) const {
        std::cout << "  +-------------------------------------------------------------+\n";
        std::cout << "  |          EDUCATIONAL PROCESSOR REGISTER BANK STATUS         |\n";
        std::cout << "  +--------+--------------------+--------+----------------------+\n";
        std::cout << "  | 64-bit | Value (Hex / Dec)  | 32-bit | Value (Hex / Dec)    |\n";
        std::cout << "  +--------+--------------------+--------+----------------------+\n";

        const char* r64Names[] = {"RAX", "RBX", "RCX", "RDX", "RSI", "RDI", "RBP", "RSP", "RIP", "RFLAGS"};
        const char* r32Names[] = {"EAX", "EBX", "ECX", "EDX", "ESI", "EDI", "EBP", "ESP", "EIP", "EFLAGS"};

        for (size_t i = 0; i < static_cast<size_t>(RegId::COUNT); ++i) {
            uint64_t val64 = regs[i];
            uint32_t val32 = static_cast<uint32_t>(val64 & 0xFFFFFFFFULL);

            std::cout << "  | " << std::setw(6) << r64Names[i] << " | 0x"
                      << std::hex << std::setw(16) << std::setfill('0') << val64 << std::dec << std::setfill(' ')
                      << " | " << std::setw(6) << r32Names[i] << " | 0x"
                      << std::hex << std::setw(8) << std::setfill('0') << val32 << std::dec << std::setfill(' ')
                      << " (" << std::setw(8) << val32 << ") |\n";
        }
        std::cout << "  +--------+--------------------+--------+----------------------+\n";

        ALUFlags flags = getALUFlags();
        std::cout << "  Hardware Flags: [ZF=" << flags.ZF 
                  << " (Zero/K.O.) | CF=" << flags.CF 
                  << " (Carry/Underflow) | SF=" << flags.SF 
                  << " (Sign/Negative) | OF=" << flags.OF << " (Overflow)]\n";
    }

    void displayChanges() const {
        const char* r64Names[] = {"RAX", "RBX", "RCX", "RDX", "RSI", "RDI", "RBP", "RSP", "RIP", "RFLAGS"};
        bool anyChange = false;
        std::cout << "  Modified Registers: ";
        for (size_t i = 0; i < static_cast<size_t>(RegId::COUNT); ++i) {
            if (regs[i] != prevRegs[i]) {
                std::cout << r64Names[i] << ": 0x" << std::hex << prevRegs[i] 
                          << " -> 0x" << regs[i] << std::dec << " (" << regs[i] << ")  ";
                anyChange = true;
            }
        }
        if (!anyChange) std::cout << "(None)";
        std::cout << "\n";
    }
};

#endif // CO1_REGISTER_BANK_HPP

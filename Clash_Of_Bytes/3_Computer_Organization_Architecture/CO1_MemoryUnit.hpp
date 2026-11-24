#ifndef CO1_MEMORY_UNIT_HPP
#define CO1_MEMORY_UNIT_HPP

#include <cstdint>
#include <vector>
#include <iostream>
#include <iomanip>
#include <string>
#include <stdexcept>

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome 1 (CO1) | Task 1: Educational Memory Unit
 * ==============================================================================
 * Simulates a bounded byte-addressable primary memory system with Little-Endian
 * byte order matching standard x86 / x86-64 microprocessors.
 * Features strict memory bounds checking and address validation.
 */

class MemoryUnit {
private:
    std::vector<uint8_t> memory;
    size_t memorySize;
    mutable std::string lastError;

public:
    explicit MemoryUnit(size_t sizeInBytes = 65536) // Default 64 KB
        : memory(sizeInBytes, 0), memorySize(sizeInBytes), lastError("") {}

    void reset() {
        std::fill(memory.begin(), memory.end(), 0);
        lastError = "";
    }

    size_t getSize() const { return memorySize; }
    std::string getLastError() const { return lastError; }

    bool isValidAddress(uint64_t addr, size_t accessSize = 1) const {
        if (addr + accessSize > memorySize || addr + accessSize < addr) {
            lastError = "Memory access out of bounds at 0x" + std::to_string(addr) + 
                        " (Capacity: " + std::to_string(memorySize) + " bytes)";
            return false;
        }
        return true;
    }

    // 8-bit byte read/write
    bool write8(uint64_t addr, uint8_t val) {
        if (!isValidAddress(addr, 1)) return false;
        memory[addr] = val;
        return true;
    }

    bool read8(uint64_t addr, uint8_t& outVal) const {
        if (!isValidAddress(addr, 1)) return false;
        outVal = memory[addr];
        return true;
    }

    // 16-bit word read/write (Little Endian)
    bool write16(uint64_t addr, uint16_t val) {
        if (!isValidAddress(addr, 2)) return false;
        memory[addr]     = static_cast<uint8_t>(val & 0xFF);
        memory[addr + 1] = static_cast<uint8_t>((val >> 8) & 0xFF);
        return true;
    }

    bool read16(uint64_t addr, uint16_t& outVal) const {
        if (!isValidAddress(addr, 2)) return false;
        outVal = static_cast<uint16_t>(memory[addr]) |
                 (static_cast<uint16_t>(memory[addr + 1]) << 8);
        return true;
    }

    // 32-bit dword read/write (Little Endian)
    bool write32(uint64_t addr, uint32_t val) {
        if (!isValidAddress(addr, 4)) return false;
        for (int i = 0; i < 4; ++i) {
            memory[addr + i] = static_cast<uint8_t>((val >> (i * 8)) & 0xFF);
        }
        return true;
    }

    bool read32(uint64_t addr, uint32_t& outVal) const {
        if (!isValidAddress(addr, 4)) return false;
        outVal = 0;
        for (int i = 0; i < 4; ++i) {
            outVal |= (static_cast<uint32_t>(memory[addr + i]) << (i * 8));
        }
        return true;
    }

    // 64-bit qword read/write (Little Endian)
    bool write64(uint64_t addr, uint64_t val) {
        if (!isValidAddress(addr, 8)) return false;
        for (int i = 0; i < 8; ++i) {
            memory[addr + i] = static_cast<uint8_t>((val >> (i * 8)) & 0xFFULL);
        }
        return true;
    }

    bool read64(uint64_t addr, uint64_t& outVal) const {
        if (!isValidAddress(addr, 8)) return false;
        outVal = 0;
        for (int i = 0; i < 8; ++i) {
            outVal |= (static_cast<uint64_t>(memory[addr + i]) << (i * 8));
        }
        return true;
    }

    // Memory Display / Formatted Hex Dump
    void displayMemory(uint64_t startAddr, size_t count = 32) const {
        if (startAddr >= memorySize) {
            std::cout << "  [MEMORY ERROR] Start address 0x" << std::hex << startAddr 
                      << " exceeds physical memory size.\n" << std::dec;
            return;
        }
        size_t endAddr = std::min(startAddr + count, static_cast<uint64_t>(memorySize));

        std::cout << "  +-------------------------------------------------------------+\n";
        std::cout << "  |             PRIMARY MEMORY HEX DUMP SEGMENT                 |\n";
        std::cout << "  +----------+--------------------------------------------------+\n";
        std::cout << "  | Address  | Hex Data Bytes (Little-Endian)                   |\n";
        std::cout << "  +----------+--------------------------------------------------+\n";

        for (uint64_t a = startAddr; a < endAddr; a += 16) {
            std::cout << "  | 0x" << std::hex << std::setw(6) << std::setfill('0') << a << std::dec << " | ";
            for (uint64_t b = 0; b < 16; ++b) {
                if (a + b < endAddr) {
                    std::cout << std::hex << std::setw(2) << std::setfill('0') 
                              << static_cast<int>(memory[a + b]) << " ";
                } else {
                    std::cout << "   ";
                }
            }
            std::cout << std::dec << "|\n";
        }
        std::cout << "  +----------+--------------------------------------------------+\n";
    }
};

#endif // CO1_MEMORY_UNIT_HPP

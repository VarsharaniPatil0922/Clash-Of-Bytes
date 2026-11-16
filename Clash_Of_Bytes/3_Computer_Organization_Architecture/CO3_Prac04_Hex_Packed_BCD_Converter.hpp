#ifndef CO3_PRAC04_HEX_PACKED_BCD_CONVERTER_HPP
#define CO3_PRAC04_HEX_PACKED_BCD_CONVERTER_HPP

#include <cstdint>
#include <sstream>
#include <iomanip>
#include <string>

/*
 * Subject: Computer Organization & Architecture
 * Course Outcome: CO3 | Practical 04
 * Topic: 64-bit Hexadecimal to Packed BCD Conversion Algorithm
 */
class HexPackedBCDConverter {
public:
    static uint64_t convertHexToPackedBCD(uint64_t hexVal) {
        uint64_t bcd = 0;
        int shift = 0;
        while (hexVal > 0 && shift < 64) {
            uint64_t digit = hexVal % 10;
            bcd |= (digit << shift);
            shift += 4;
            hexVal /= 10;
        }
        return bcd;
    }

    static std::string formatPackedBCD(uint64_t bcd) {
        std::stringstream ss;
        ss << "0x" << std::hex << std::uppercase << bcd;
        return ss.str();
    }
};

#endif // CO3_PRAC04_HEX_PACKED_BCD_CONVERTER_HPP

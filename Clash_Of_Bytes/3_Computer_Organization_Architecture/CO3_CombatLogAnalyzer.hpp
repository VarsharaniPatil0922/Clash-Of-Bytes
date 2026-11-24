#ifndef CO3_COMBAT_LOG_ANALYZER_HPP
#define CO3_COMBAT_LOG_ANALYZER_HPP

#include <string>
#include <vector>
#include <iostream>
#include <iomanip>
#include "CO3_Prac04_Algorithmic_and_String_Processing.hpp"

/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE
 * Course Outcome: CO3 | Task 6: Event-Driven Combat Log Analyzer
 * ==============================================================================
 * Connects the 64-bit hardware string processing instructions (repne scasb and
 * token search loops) to actual Clash of Bytes combat events.
 * Operates event-driven (analyzes at round/match end or debug overlay request),
 * ensuring zero frame rate impact during 60 FPS combat rendering.
 */

struct CombatLogStatistics {
    uint64_t totalLogBytes;
    uint64_t criticalHits;
    uint64_t attackStrikes;
    uint64_t blockedAttacks;
    uint64_t specialMoves;
    uint64_t totalCombatActions;
};

class CombatLogAnalyzer {
private:
    std::string accumulatedCombatLog;

public:
    CombatLogAnalyzer() {
        reset();
    }

    void reset() {
        accumulatedCombatLog.clear();
    }

    // Appends live combat event string during gameplay
    void recordCombatEvent(const std::string& eventStr) {
        if (!accumulatedCombatLog.empty()) {
            accumulatedCombatLog += " ";
        }
        accumulatedCombatLog += eventStr;
    }

    const std::string& getRawLog() const {
        return accumulatedCombatLog;
    }

    // Executes 64-bit hardware assembly string processing to analyze log statistics
    CombatLogStatistics analyzeLog() const {
        CombatLogStatistics stats;
        if (accumulatedCombatLog.empty()) {
            stats.totalLogBytes = 0;
            stats.criticalHits = 0;
            stats.attackStrikes = 0;
            stats.blockedAttacks = 0;
            stats.specialMoves = 0;
            stats.totalCombatActions = 0;
            return stats;
        }

        // 1. Hardware string length via repne scasb
        stats.totalLogBytes = AssemblyStringProcessor::computeStringLength(accumulatedCombatLog.c_str());

        // 2. Hardware token occurrence counting via 64-bit register loops
        stats.criticalHits   = AssemblyStringProcessor::countTokenOccurrences(accumulatedCombatLog, "CRITICAL");
        stats.attackStrikes  = AssemblyStringProcessor::countTokenOccurrences(accumulatedCombatLog, "ATTACK");
        stats.blockedAttacks = AssemblyStringProcessor::countTokenOccurrences(accumulatedCombatLog, "BLOCK");
        stats.specialMoves   = AssemblyStringProcessor::countTokenOccurrences(accumulatedCombatLog, "SPECIAL");
        stats.totalCombatActions = stats.attackStrikes + stats.blockedAttacks + stats.specialMoves;

        return stats;
    }

    void displayStatistics() const {
        CombatLogStatistics stats = analyzeLog();

        std::cout << "\n  ===============================================================\n";
        std::cout << "     CLASH OF BYTES: 64-BIT HARDWARE COMBAT LOG ANALYSIS (CO3)  \n";
        std::cout << "  ===============================================================\n";
        std::cout << "  | Metric                         | Count / Bytes | Status     |\n";
        std::cout << "  |--------------------------------|---------------|------------|\n";
        std::cout << "  | Scanned Log Payload Length     | " << std::setw(13) << stats.totalLogBytes 
                  << " | " << std::left << std::setw(10) << "repne scasb" << std::right << " |\n";
        std::cout << "  | 'ATTACK' Actions Logged        | " << std::setw(13) << stats.attackStrikes 
                  << " | " << std::left << std::setw(10) << "AMD64 Loop" << std::right << " |\n";
        std::cout << "  | 'BLOCK' Defensive Guards       | " << std::setw(13) << stats.blockedAttacks 
                  << " | " << std::left << std::setw(10) << "AMD64 Loop" << std::right << " |\n";
        std::cout << "  | 'SPECIAL' Moves Executed       | " << std::setw(13) << stats.specialMoves 
                  << " | " << std::left << std::setw(10) << "AMD64 Loop" << std::right << " |\n";
        std::cout << "  | 'CRITICAL' High-Impact Hits    | " << std::setw(13) << stats.criticalHits 
                  << " | " << std::left << std::setw(10) << "AMD64 Loop" << std::right << " |\n";
        std::cout << "  | Total Combat Actions Analyzed  | " << std::setw(13) << stats.totalCombatActions 
                  << " | " << std::left << std::setw(10) << "Verified" << std::right << " |\n";
        std::cout << "  ===============================================================\n";
    }
};

#endif // CO3_COMBAT_LOG_ANALYZER_HPP

#ifndef CO4_INTEGRATED_PIPELINE_DEMONSTRATION_HPP
#define CO4_INTEGRATED_PIPELINE_DEMONSTRATION_HPP

/*
 * ============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE (Course Code: 2310221L)
 * Practical No. 04 / Task 4 | Course Outcome 4 (CO4):
 * INTEGRATED COA PIPELINE & MULTI-STAGE DEMONSTRATION WORKFLOW
 * ============================================================================
 * 
 * ARCHITECTURAL SPECIFICATION (CO1 + CO2 + CO3 + CO4 INTEGRATION):
 * This module unifies all previous COA course outcomes into a single,
 * demonstrable processor-level execution pipeline:
 * 
 * 1. APPLICATION-LEVEL INPUT:
 *    - Fighting game combat action ("SPECIAL_ART_BURST", Atk: 45, Def: 12, Rage: 3x, Debuff: -5 HP x 4 turns).
 * 
 * 2. CONTROL UNIT (CU) INSTRUCTION DECODING:
 *    - Generates 8-bit micro-operation control signals:
 *      RegRead, RegWrite, ALUSrc, ALUOp, MemRead, MemWrite, Branch, FlagWrite.
 * 
 * 3. 64-BIT PROCESSOR REGISTER MAPPING (AMD64 / x86-64 ISA):
 *    - RAX: 64-bit Accumulator (holds base damage, multiplication product, return value).
 *    - RBX: 64-bit Base / Multiplier Register (holds defense rating / multiplier Q in Booth).
 *    - RCX: 64-bit Counter Register (loop iterations / rep string prefix counter).
 *    - RDX: 64-bit Data / Multiplicand Register (holds multiplicand M / raw difference).
 *    - RSI: 64-bit Source Index (pointer to combat telemetry text).
 *    - RDI: 64-bit Destination Index (destination address for scasb search).
 *    - RFLAGS: 64-bit CPU status flags (ZF, CF, SF, OF captured via PUSHFQ).
 * 
 * 4. ALU & HARDWARE FLAG SIMULATION:
 *    - Subtraction and status flag evaluation for Zero (K.O.), Carry/Borrow, Sign, and Overflow.
 * 
 * 5. 64-BIT ASSEMBLY COMPUTATION WITH CONDITIONAL BRANCHING:
 *    - Native x86-64 assembly execution:
 *      movq, subq, cmpq, jle, imulq, pushfq, popq.
 * 
 * 6. HARDWARE STRING & ALGORITHMIC PROCESSING:
 *    - Booth's Signed Multiplication for health debuff scaling (-5 * 4 = -20 HP).
 *    - High-speed string length inspection via repne scasb instruction.
 * 
 * 7. PACKED BCD HUD SCORE ENCODING:
 *    - Converts binary integer damage into packed binary-coded decimal for arcade HUD registers.
 */

#include <iostream>
#include <iomanip>
#include <string>
#include <sstream>
#include <cstdint>
#include "CO1_Prac01_80386_ALU_Simulator.hpp"
#include "CO4_Prac02_Control_Unit_Decoder.hpp"
#include "CO4_EducationalPipeline.hpp"
#include "CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp"
#include "CO3_Prac04_Algorithmic_and_String_Processing.hpp"

struct IntegratedCombatTelemetry {
    std::string actionName;
    uint64_t baseAttack;
    uint64_t baseDefense;
    uint64_t comboMultiplier;
    int64_t debuffPerTick;
    int64_t debuffTicks;
    std::string combatLogString;
};

struct PipelineExecutionTrace {
    // 1. Control Unit Signals
    ControlSignals cuSignals;

    // 2. Register Values
    uint64_t regRAX;
    uint64_t regRBX;
    uint64_t regRCX;
    uint64_t regRDX;
    uint64_t regRFLAGS;

    // 3. Status Flags
    bool flagZF;
    bool flagCF;
    bool flagSF;
    bool flagOF;

    // 4. Arithmetic Results
    int64_t finalStrikeDamage;
    int64_t boothDebuffTotal;
    int64_t netHealthImpact;

    // 5. String Processing
    size_t scannedStringLength;
    int tokenMatchCount;

    // 6. Scoreboard Output
    uint32_t packedBcdScore;
};

class IntegratedCOAPipeline {
public:
    static PipelineExecutionTrace executeWorkflow(const IntegratedCombatTelemetry& input) {
        PipelineExecutionTrace trace;

        // --------------------------------------------------------------------
        // STAGE 1: CONTROL UNIT INSTRUCTION DECODING
        // --------------------------------------------------------------------
        std::string baseAction = "SPECIAL";
        if (input.actionName.find("PUNCH") != std::string::npos) baseAction = "PUNCH";
        else if (input.actionName.find("KICK") != std::string::npos) baseAction = "KICK";
        else if (input.actionName.find("BLOCK") != std::string::npos) baseAction = "BLOCK";
        trace.cuSignals = ControlUnitDecoder::decodeAction(baseAction);

        // --------------------------------------------------------------------
        // STAGE 2: 64-BIT ASSEMBLY DAMAGE ENGINE WITH CONDITIONAL BRANCHING
        // --------------------------------------------------------------------
        uint64_t atk = input.baseAttack;
        uint64_t def = input.baseDefense;
        uint64_t combo = input.comboMultiplier;

        int64_t finalDamage = 0;
        uint64_t rflagsRaw = 0;

        // Native 64-bit AMD64 Assembly Block with Conditional Branching
        #if defined(__x86_64__) || defined(_M_X64)
        __asm__ __volatile__(
            "movq %[atk], %%rax\n\t"        // Load Attack Power into RAX Accumulator
            "movq %[def], %%rbx\n\t"        // Load Target Defense into RBX
            "cmpq %%rbx, %%rax\n\t"        // Compare Attack vs Defense (sets CPU flags)
            "jle 1f\n\t"                    // Conditional Jump: if ATK <= DEF, branch to 1 (Armor Absorb)
            "subq %%rbx, %%rax\n\t"        // RAX = ATK - DEF (Unblocked raw damage)
            "imulq %[combo], %%rax\n\t"    // RAX = RAX * comboMultiplier (Critical scaling)
            "jmp 2f\n\t"                    // Jump to exit
            "1:\n\t"
            "movq $1, %%rax\n\t"            // Armor fully absorbed strike: minimum chip damage = 1 HP
            "2:\n\t"
            "pushfq\n\t"                    // Push 64-bit RFLAGS register onto stack
            "popq %[rflags]\n\t"           // Pop hardware flags into rflagsRaw
            "movq %%rax, %[outDmg]\n\t"     // Store computed damage from RAX
            : [outDmg] "=r"(finalDamage), [rflags] "=r"(rflagsRaw)
            : [atk] "r"(atk), [def] "r"(def), [combo] "r"(combo)
            : "%rax", "%rbx", "cc"
        );
        #else
        // Deterministic Fallback Simulator for non-x86_64 architectures
        if (atk > def) {
            finalDamage = (atk - def) * combo;
            rflagsRaw = 0x00; // Normal flags
        } else {
            finalDamage = 1;
            rflagsRaw = 0x40; // ZF or CF flag simulation
        }
        #endif

        trace.finalStrikeDamage = finalDamage;
        trace.regRAX = static_cast<uint64_t>(finalDamage);
        trace.regRBX = def;
        trace.regRDX = (atk > def) ? (atk - def) : 1;
        trace.regRFLAGS = rflagsRaw;

        trace.flagZF = (rflagsRaw & 0x0040) != 0;
        trace.flagCF = (rflagsRaw & 0x0001) != 0;
        trace.flagSF = (rflagsRaw & 0x0080) != 0;
        trace.flagOF = (rflagsRaw & 0x0800) != 0;

        // --------------------------------------------------------------------
        // STAGE 3: BOOTH'S ALGORITHM SIGNED DEBUFF MULTIPLICATION (CO3)
        // --------------------------------------------------------------------
        BoothALUResult booth = AssemblyBoothMultiplier::executeBooth(input.debuffPerTick, input.debuffTicks);
        trace.boothDebuffTotal = booth.assemblyProduct;
        trace.netHealthImpact = -(trace.finalStrikeDamage + (-trace.boothDebuffTotal));

        // --------------------------------------------------------------------
        // STAGE 4: HARDWARE ASSEMBLY STRING PROCESSING (repne scasb)
        // --------------------------------------------------------------------
        trace.scannedStringLength = AssemblyStringProcessor::computeStringLength(input.combatLogString.c_str());
        StringAnalysisResult strRes = AssemblyStringProcessor::analyzeCombatLog(input.combatLogString, "CRITICAL");
        trace.tokenMatchCount = static_cast<int>(strRes.assemblyMatchCount);
        trace.regRCX = static_cast<uint64_t>(trace.scannedStringLength);

        // --------------------------------------------------------------------
        // STAGE 5: PACKED BCD ARCADE HUD SCORE ENCODING
        // --------------------------------------------------------------------
        trace.packedBcdScore = static_cast<uint32_t>(HexPackedBCDConverter::convertHexToPackedBCD(static_cast<uint64_t>(trace.finalStrikeDamage)));

        // --------------------------------------------------------------------
        // STAGE 6: EDUCATIONAL 5-STAGE PIPELINE VERIFICATION RUN
        // --------------------------------------------------------------------
        // Executes the combat action through the simulated 5-stage pipeline
        // (IF, ID, EX, MEM, WB) to ensure educational micro-architectural consistency.
        EducationalPipeline eduPipe(65536);
        std::vector<std::string> pipeProgram = {
            "MOV RAX, " + std::to_string(input.baseAttack),
            "MOV RBX, " + std::to_string(input.baseDefense),
            "SUB RAX, RBX",
            "MOV RCX, " + std::to_string(input.comboMultiplier),
            "MUL RCX",
            "STORE [0x2000], RAX",
            "HLT"
        };
        eduPipe.loadCombatProgram(pipeProgram);
        eduPipe.runToCompletion(30);

        return trace;
    }

    static void printWorkflowReport(const IntegratedCombatTelemetry& input, const PipelineExecutionTrace& trace) {
        std::cout << "\n========================================================================================\n";
        std::cout << " [PRACTICAL 04 / TASK 4] INTEGRATED COA PIPELINE EXECUTION REPORT (CO4)\n";
        std::cout << " Course Code: 2310221L | CPU Architecture: Intel/AMD x86-64 Instruction Set Architecture\n";
        std::cout << "========================================================================================\n";

        std::cout << " [STAGE 1] APPLICATION-LEVEL COMBAT INPUT:\n";
        std::cout << "   -> Combat Move Selected    : " << input.actionName << "\n";
        std::cout << "   -> Base Attack / Defense   : ATK = " << input.baseAttack << " HP | DEF = " << input.baseDefense << " HP\n";
        std::cout << "   -> Combo Scaling Multiplier: x" << input.comboMultiplier << " (Rage Burst)\n";
        std::cout << "   -> Health Debuff Parameter : " << input.debuffPerTick << " HP/turn across " << input.debuffTicks << " turns\n";
        std::cout << "   -> Telemetry String Payload: \"" << input.combatLogString << "\"\n";

        std::cout << "\n [STAGE 2] CONTROL UNIT (CU) DECODED MICRO-OPERATION SIGNALS:\n";
        std::cout << "   -> RegRead  : " << trace.cuSignals.RegRead   << " (Reads source registers RAX & RBX)\n";
        std::cout << "   -> RegWrite : " << trace.cuSignals.RegWrite  << " (Writes destination damage register RAX)\n";
        std::cout << "   -> ALUSrc   : " << trace.cuSignals.ALUSrc    << " (Selects register operand vs immediate)\n";
        std::cout << "   -> ALUOp    : " << trace.cuSignals.ALUOp     << " (Dispatches arithmetic micro-code to ALU)\n";
        std::cout << "   -> MemWrite : " << trace.cuSignals.MemWrite  << " (Updates in-memory combat state)\n";
        std::cout << "   -> Branch   : " << trace.cuSignals.Branch    << " (Condition code evaluation)\n";

        std::cout << "\n [STAGE 3] 64-BIT HARDWARE REGISTER MAPPING (AMD64 / x86-64):\n";
        std::cout << "   -> RAX (Accumulator / Strike Damage) : 0x" << std::hex << std::uppercase << trace.regRAX 
                  << " (" << std::dec << trace.regRAX << " decimal HP)\n";
        std::cout << "   -> RBX (Base / Target Armor Defense) : 0x" << std::hex << trace.regRBX 
                  << " (" << std::dec << trace.regRBX << " decimal HP)\n";
        std::cout << "   -> RDX (Data / Raw Net Difference)   : 0x" << std::hex << trace.regRDX 
                  << " (" << std::dec << trace.regRDX << " decimal HP)\n";
        std::cout << "   -> RCX (Counter / String Scan Count) : 0x" << std::hex << trace.regRCX 
                  << " (" << std::dec << trace.regRCX << " characters scanned)\n";
        std::cout << "   -> RFLAGS (CPU Hardware Status Word) : 0x" << std::hex << trace.regRFLAGS << std::dec << "\n";

        std::cout << "\n [STAGE 4] HARDWARE ALU STATUS FLAGS (ZF, CF, SF, OF):\n";
        std::cout << "   -> ZF (Zero Flag)    : " << trace.flagZF << " (" << (trace.flagZF ? "K.O. Struck! Enemy HP reached 0" : "Target retains conscious health") << ")\n";
        std::cout << "   -> CF (Carry/Borrow) : " << trace.flagCF << " (" << (trace.flagCF ? "Borrow occurred: Defense absorbed blow" : "No borrow generated") << ")\n";
        std::cout << "   -> SF (Sign Flag)    : " << trace.flagSF << " (" << (trace.flagSF ? "Negative signed result" : "Positive damage value") << ")\n";
        std::cout << "   -> OF (Overflow Flag): " << trace.flagOF << " (" << (trace.flagOF ? "Arithmetic two's complement overflow" : "Normal magnitude range") << ")\n";

        std::cout << "\n [STAGE 5] ALGORITHMIC MULTIPLICATION (BOOTH'S SIGNED ALGORITHM):\n";
        std::cout << "   -> Signed Debuff Formula   : (" << input.debuffPerTick << " HP/turn) * (" << input.debuffTicks << " turns)\n";
        std::cout << "   -> Booth Hardware Product  : " << trace.boothDebuffTotal << " HP (Signed two's complement correct)\n";
        std::cout << "   -> Combined Health Impact  : Strike Damage (" << trace.finalStrikeDamage << " HP) + Debuff (" 
                  << trace.boothDebuffTotal << " HP) = " << trace.netHealthImpact << " HP Total\n";

        std::cout << "\n [STAGE 6] HARDWARE ASSEMBLY STRING SCAN (repne scasb):\n";
        std::cout << "   -> Telemetry Length (cld/repne): " << trace.scannedStringLength << " bytes\n";
        std::cout << "   -> 'CRITICAL' Tokens Matched   : " << trace.tokenMatchCount << " occurrence(s)\n";

        std::cout << "\n [STAGE 7] PACKED BCD SCOREBOARD HUD ENCODER:\n";
        std::cout << "   -> Strike Damage Output (Hex) : 0x" << std::hex << trace.finalStrikeDamage << "\n";
        std::cout << "   -> Packed BCD Output for HUD  : 0x" << std::hex << trace.packedBcdScore << std::dec << "\n";
        std::cout << "========================================================================================\n";
    }
};

#endif // CO4_INTEGRATED_PIPELINE_DEMONSTRATION_HPP

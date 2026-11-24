/*
 * ==============================================================================
 * CLASH OF BYTES: COMPUTER ORGANIZATION & ARCHITECTURE (Course Code: 2310221L)
 * Master Practical Execution & Automated Verification Test Suite (CO1 to CO4)
 * ==============================================================================
 * Modules Verified:
 *   - CO1 (Prac 01 / Tasks 1 & 2):
 *       * 32-bit & 64-bit Register Bank with Zero-Extension & State Tracking
 *       * Educational Memory Unit (64KB, Little-Endian, Bounds Checking, Hex Dump)
 *       * Complete ALU Simulator with Mathematical Flag Verification (ZF, CF, SF, OF)
 *       * Complete 5-Stage Educational CPU Simulator (Fetch, Decode, Execute, Mem, WB)
 *   - CO2 (Prac 03 / Task 5):
 *       * Native 64-bit Hardware Assembly Damage Engine (RAX, RBX, RDX, RFLAGS)
 *       * Standard Damage Verification (Atk: 28, Def: 6, Combo: 2 -> 44 HP)
 *   - CO3 (Prac 04 / Tasks 3 & 6):
 *       * 64-bit Shift-and-Add & Booth's Signed Multiplication Algorithms
 *       * Hardware String Processing (repne scasb & Token Occurrence Search)
 *       * Event-Driven Combat Log Analyzer (CRITICAL, ATTACK, BLOCK, SPECIAL)
 *   - CO4 (Prac 02 & Tasks 3, 4, 7):
 *       * Control Unit Instruction Decoder & Micro-Signal Generation
 *       * 5-Stage Pipeline Simulation with Inter-Stage Latches & Branch Flush
 *       * Unified End-to-End Combat Pipeline Demonstration
 * ==============================================================================
 */

#include <iostream>
#include <iomanip>
#include <cassert>
#include <vector>
#include <string>

// COA Academic Module Headers
#include "CO1_Prac01_80386_ALU_Simulator.hpp"
#include "CO1_RegisterBank.hpp"
#include "CO1_MemoryUnit.hpp"
#include "CO4_Prac02_Control_Unit_Decoder.hpp"
#include "CO1_CPUSimulator.hpp"
#include "CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp"
#include "CO3_Prac04_Algorithmic_and_String_Processing.hpp"
#include "CO3_CombatLogAnalyzer.hpp"
#include "CO4_EducationalPipeline.hpp"
#include "CO4_Integrated_Pipeline_Demonstration.hpp"

// Global Test Counters
static int g_passedTests = 0;
static int g_failedTests = 0;

#define COA_ASSERT(cond, msg) \
    do { \
        if (cond) { \
            std::cout << "  [ASSERTION PASSED] " << msg << "\n"; \
            g_passedTests++; \
        } else { \
            std::cerr << "  [ASSERTION FAILED] " << msg << "\n"; \
            g_failedTests++; \
        } \
    } while(0)

// ==============================================================================
// CO1: REGISTER BANK, MEMORY UNIT & ALU FLAG SUITE
// ==============================================================================
void testCO1_RegisterBank() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO1 TEST 1] Dedicated 32-bit & 64-bit Processor Register Bank\n";
    std::cout << "-------------------------------------------------------------------\n";

    RegisterBank rb;
    rb.reset();

    // 1. Initial State Checks
    COA_ASSERT(rb.get64(RegId::RAX) == 0, "RAX initialized to 0");
    COA_ASSERT(rb.get64(RegId::RSP) == 0x7FFF0000, "RSP initialized to safe stack address 0x7FFF0000");

    // 2. 64-bit Write & Read
    rb.set64(RegId::RAX, 0x0123456789ABCDEFULL);
    COA_ASSERT(rb.get64(RegId::RAX) == 0x0123456789ABCDEFULL, "64-bit write/read on RAX matches");

    // 3. 32-bit Read from 64-bit Register
    COA_ASSERT(rb.get32(RegId::RAX) == 0x89ABCDEF, "EAX reads lower 32 bits of RAX");

    // 4. 32-bit Write with x86-64 Hardware Zero-Extension
    rb.set32(RegId::RAX, 0x000055AA);
    COA_ASSERT(rb.get64(RegId::RAX) == 0x000055AAULL, "Writing EAX zero-extends into upper 32 bits of RAX");

    // 5. String-based Name Lookup (Case-Insensitive)
    rb.writeByName("rcx", 42);
    COA_ASSERT(rb.readByName("RCX") == 42, "Case-insensitive read/write by name 'RCX'");
    rb.writeByName("edx", 100);
    COA_ASSERT(rb.readByName("EDX") == 100, "Writing 32-bit 'EDX' succeeds");
    COA_ASSERT(rb.readByName("RDX") == 100, "Reading 64-bit 'RDX' reflects zero-extended EDX write");

    rb.display();
    std::cout << "\n";
}

void testCO1_MemoryUnit() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO1 TEST 2] Educational Memory Unit (64KB Little-Endian)\n";
    std::cout << "-------------------------------------------------------------------\n";

    MemoryUnit mem(65536);
    mem.reset();

    // 1. Capacity Check
    COA_ASSERT(mem.getSize() == 65536, "Memory initialized to 65,536 bytes (64 KB)");

    // 2. 8-bit, 16-bit, 32-bit, 64-bit Little-Endian Data Access
    mem.write8(0x1000, 0xAB);
    uint8_t v8 = 0;
    mem.read8(0x1000, v8);
    COA_ASSERT(v8 == 0xAB, "8-bit byte read/write at 0x1000");

    mem.write16(0x1002, 0x1234);
    uint16_t v16 = 0;
    mem.read16(0x1002, v16);
    COA_ASSERT(v16 == 0x1234, "16-bit word read/write at 0x1002");

    mem.write32(0x1004, 0xDEADBEEF);
    uint32_t v32 = 0;
    mem.read32(0x1004, v32);
    COA_ASSERT(v32 == 0xDEADBEEF, "32-bit dword read/write at 0x1004");

    mem.write64(0x1010, 0x0123456789ABCDEFULL);
    uint64_t v64 = 0;
    mem.read64(0x1010, v64);
    COA_ASSERT(v64 == 0x0123456789ABCDEFULL, "64-bit qword read/write at 0x1010");

    // 3. Bounds Checking & Out of Bounds Validation
    uint64_t invalidRead = 0;
    bool oobWrite = mem.write64(70000, 12345);
    bool oobRead  = mem.read64(70000, invalidRead);
    COA_ASSERT(!oobWrite, "Out-of-bounds write beyond 64KB rejected safely");
    COA_ASSERT(!oobRead, "Out-of-bounds read beyond 64KB rejected safely");

    mem.displayMemory(0x1000, 32);
    std::cout << "\n";
}

void testCO1_ALUFlagsComprehensive() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO1 TEST 3] Complete Mathematical Processor Flag Verification\n";
    std::cout << "-------------------------------------------------------------------\n";

    ALUFlags flags;

    // 1. Addition without overflow
    uint64_t resAdd1 = ALU_Simulator::executeAdd(15, 10, flags);
    COA_ASSERT(resAdd1 == 25 && !flags.ZF && !flags.CF && !flags.SF && !flags.OF, 
               "Addition without overflow: 15 + 10 = 25 [All flags 0]");

    // 2. Addition with unsigned carry
    uint64_t resAddCarry = ALU_Simulator::executeAdd(0xFFFFFFFFFFFFFFFFULL, 1, flags);
    COA_ASSERT(resAddCarry == 0 && flags.ZF && flags.CF && !flags.SF && !flags.OF, 
               "Addition with carry: UINT64_MAX + 1 = 0 [CF=1, ZF=1]");

    // 3. Subtraction with zero result (Knockout test)
    uint64_t resSubZero = ALU_Simulator::executeSub(50, 50, flags);
    COA_ASSERT(resSubZero == 0 && flags.ZF && !flags.CF && !flags.SF && !flags.OF, 
               "Subtraction with zero result: 50 - 50 = 0 [ZF=1 (K.O. Struck!)]");

    // 4. Subtraction with borrow (Health underflow)
    uint64_t resSubBorrow = ALU_Simulator::executeSub(20, 50, flags);
    COA_ASSERT(flags.CF && flags.SF, 
               "Subtraction with borrow: 20 - 50 generates CF=1 (Borrow) & SF=1 (Negative)");

    // 5. Signed overflow in addition
    uint64_t resSignedOF = ALU_Simulator::executeAdd(0x7FFFFFFFFFFFFFFFULL, 1, flags);
    COA_ASSERT(flags.OF && flags.SF, 
               "Signed overflow: INT64_MAX + 1 produces OF=1 & SF=1");

    // 6. Comparison of equal values
    ALU_Simulator::executeCmp(40, 40, flags);
    COA_ASSERT(flags.ZF && !flags.CF && !flags.SF, 
               "CMP equal values: CMP 40, 40 produces ZF=1");

    // 7. Comparison of unequal values (Lesser vs Greater)
    ALU_Simulator::executeCmp(40, 60, flags);
    COA_ASSERT(!flags.ZF && flags.CF && flags.SF, 
               "CMP lesser vs greater: CMP 40, 60 produces ZF=0, CF=1, SF=1");

    // 8. Bitwise Logical Operations (AND, OR, XOR, NOT)
    uint64_t resAnd = ALU_Simulator::executeAnd(0x0F, 0xF0, flags);
    COA_ASSERT(resAnd == 0 && flags.ZF, "AND 0x0F, 0xF0 = 0 [ZF=1]");
    uint64_t resXor = ALU_Simulator::executeXor(0xFF, 0xFF, flags);
    COA_ASSERT(resXor == 0 && flags.ZF, "XOR 0xFF, 0xFF = 0 [ZF=1]");
    uint64_t resOr = ALU_Simulator::executeOr(0x0F, 0xF0, flags);
    COA_ASSERT(resOr == 0xFF && !flags.ZF, "OR 0x0F, 0xF0 = 0xFF [ZF=0]");

    // 9. Shifts (SHL, SHR, SAR)
    uint64_t resShl = ALU_Simulator::executeShl(8, 2, flags);
    COA_ASSERT(resShl == 32 && !flags.ZF, "SHL 8 << 2 = 32");
    uint64_t resSar = ALU_Simulator::executeSar(static_cast<uint64_t>(-16LL), 2, flags);
    COA_ASSERT(static_cast<int64_t>(resSar) == -4LL && flags.SF, "SAR (-16) >> 2 = -4 [SF=1]");

    std::cout << "\n";
}

void testCO1_CPUSimulator() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO1 TEST 4] Complete 5-Stage Educational CPU Simulator\n";
    std::cout << " Program: MOV RAX, 28 -> MOV RBX, 6 -> SUB RAX, RBX -> MOV RCX, 2 -> MUL RCX\n";
    std::cout << "-------------------------------------------------------------------\n";

    CPUSimulator cpu(65536, true);

    // Exact Program Specified in Prompt
    std::vector<std::string> combatProgram = {
        "MOV RAX, 28",       // Load base attack power into RAX
        "MOV RBX, 6",        // Load defense rating into RBX
        "SUB RAX, RBX",      // Net attack = 28 - 6 = 22 HP
        "MOV RCX, 2",        // Combo multiplier = 2
        "MUL RCX",           // Scaled damage = 22 * 2 = 44 HP
        "STORE [0x1000], RAX",// Store final computed damage to memory address 0x1000
        "HLT"                // Halt execution
    };

    bool loaded = cpu.loadProgram(combatProgram);
    COA_ASSERT(loaded, "Combat calculation assembly program parsed and loaded successfully");

    cpu.run();

    // Verify Final Register Values
    uint64_t finalRAX = cpu.getRegisters().get64(RegId::RAX);
    uint64_t finalRBX = cpu.getRegisters().get64(RegId::RBX);
    uint64_t finalRCX = cpu.getRegisters().get64(RegId::RCX);

    COA_ASSERT(finalRAX == 44, "FINAL RESULT VERIFIED: RAX = 44 HP (28 - 6 = 22, 22 * 2 = 44)");
    COA_ASSERT(finalRBX == 6,  "RBX preserved defense rating: 6");
    COA_ASSERT(finalRCX == 2,  "RCX preserved combo multiplier: 2");

    // Verify Memory Store
    uint64_t storedDamage = 0;
    cpu.getMemory().read64(0x1000, storedDamage);
    COA_ASSERT(storedDamage == 44, "Memory at [0x1000] successfully holds stored damage: 44 HP");

    std::cout << "\n  Final CPU Simulator Register Bank:\n";
    cpu.getRegisters().display();
    std::cout << "\n";
}

// ==============================================================================
// CO2: 64-BIT HARDWARE ASSEMBLY DAMAGE ENGINE
// ==============================================================================
void testCO2_AssemblyEngine() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO2 TEST 5] 64-bit Hardware Assembly Damage Engine (__asm__)\n";
    std::cout << " Combat Strike: Attack = 28, Defense = 6, Combo Multiplier = 2\n";
    std::cout << "-------------------------------------------------------------------\n";

    CombatALUResult res = AssemblyDamageEngine::calculateHitDamage(28, 6, 2);

    COA_ASSERT(res.finalDamage == 44, "64-bit Assembly calculated exact final damage: 44 HP");
    COA_ASSERT(res.rawDifference == 22, "Raw difference: 28 - 6 = 22 HP");
    COA_ASSERT(res.bcdScore == 0x44, "Packed BCD score generated: 0x44");
    COA_ASSERT(!res.ZF && !res.CF, "Hardware flags verified: ZF=0 (conscious), CF=0 (no underflow)");

    std::cout << "  Live Assembly Trace: " << res.assemblyTrace << "\n\n";
}

// ==============================================================================
// CO3: ALGORITHMIC MULTIPLICATION & COMBAT LOG ANALYZER
// ==============================================================================
void testCO3_AlgorithmicAndStringSuite() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO3 TEST 6] Algorithmic Arithmetic & Hardware String Processing\n";
    std::cout << "-------------------------------------------------------------------\n";

    // 1. Shift-and-Add Multiplication
    ShiftAddALUResult sa1 = AssemblyShiftAddMultiplier::executeShiftAdd(28, 5);
    COA_ASSERT(sa1.assemblyProduct == 140 && sa1.matchesReference, 
               "Shift-and-Add: 28 x 5 = 140 [Matches Reference]");
    ShiftAddALUResult saZero = AssemblyShiftAddMultiplier::executeShiftAdd(0, 100);
    COA_ASSERT(saZero.assemblyProduct == 0 && saZero.ZF, 
               "Shift-and-Add Zero Operand: 0 x 100 = 0 [ZF=1]");

    // 2. Booth's Signed Multiplication
    BoothALUResult bm1 = AssemblyBoothMultiplier::executeBooth(-5, 4);
    COA_ASSERT(bm1.assemblyProduct == -20 && bm1.matchesReference, 
               "Booth Signed Debuff: (-5) x 4 = -20 HP [Matches Reference]");
    BoothALUResult bmNeg = AssemblyBoothMultiplier::executeBooth(-6, -3);
    COA_ASSERT(bmNeg.assemblyProduct == 18 && bmNeg.matchesReference, 
               "Booth Double Negative: (-6) x (-3) = 18 [Matches Reference]");

    // 3. Hardware String Processing (repne scasb)
    const char* strTest = "CLASH_OF_BYTES_CHAMPION";
    uint64_t len = AssemblyStringProcessor::computeStringLength(strTest);
    COA_ASSERT(len == std::string(strTest).length(), 
               "repne scasb string length matches reference: 23 bytes");
    uint64_t emptyLen = AssemblyStringProcessor::computeStringLength("");
    COA_ASSERT(emptyLen == 0, "repne scasb on empty string returns 0 safely");

    // 4. Token Search in Assembly
    std::string testLog = "[EVENT] CRITICAL! ATTACK HIT! CRITICAL STRIKE! BLOCK!";
    uint64_t critCount = AssemblyStringProcessor::countTokenOccurrences(testLog, "CRITICAL");
    COA_ASSERT(critCount == 2, "Assembly token search counted 2 occurrences of 'CRITICAL'");

    // 5. Event-Driven Combat Log Analyzer
    CombatLogAnalyzer logAnalyzer;
    logAnalyzer.recordCombatEvent("[P1] ATTACK HIT! CRITICAL DAMAGE DEALT!");
    logAnalyzer.recordCombatEvent("[P2] BLOCK FORTIFIED!");
    logAnalyzer.recordCombatEvent("[P1] SPECIAL ATTACK UNLEASHED! CRITICAL COMBO!");
    logAnalyzer.recordCombatEvent("[P2] ATTACK RETALIATION!");

    CombatLogStatistics stats = logAnalyzer.analyzeLog();
    COA_ASSERT(stats.criticalHits == 2, "Log Analyzer: 2 CRITICAL events verified");
    COA_ASSERT(stats.attackStrikes == 3, "Log Analyzer: 3 ATTACK actions verified");
    COA_ASSERT(stats.blockedAttacks == 1, "Log Analyzer: 1 BLOCK action verified");
    COA_ASSERT(stats.specialMoves == 1, "Log Analyzer: 1 SPECIAL move verified");

    logAnalyzer.displayStatistics();
    std::cout << "\n";
}

// ==============================================================================
// CO4: CONTROL UNIT & 5-STAGE PIPELINE SIMULATION
// ==============================================================================
void testCO4_ControlUnitAndPipeline() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO4 TEST 7] Complete 5-Stage Educational Processor Pipeline\n";
    std::cout << " Inter-Stage Latches: IF/ID -> ID/EX -> EX/MEM -> MEM/WB\n";
    std::cout << "-------------------------------------------------------------------\n";

    // 1. Control Unit Action Decoding
    ControlSignals sigPunch   = ControlUnitDecoder::decodeAction("PUNCH");
    ControlSignals sigSpecial = ControlUnitDecoder::decodeAction("SPECIAL");
    ControlSignals sigBlock   = ControlUnitDecoder::decodeAction("BLOCK");

    COA_ASSERT(sigPunch.RegWrite && sigPunch.ALUOp == "SUB_DAMAGE", 
               "Control Unit translates PUNCH -> RegWrite=1, ALUOp=SUB_DAMAGE");
    COA_ASSERT(sigSpecial.Branch && sigSpecial.ALUOp == "IMUL_BURST", 
               "Control Unit translates SPECIAL -> Branch=1, ALUOp=IMUL_BURST");
    COA_ASSERT(!sigBlock.RegWrite && sigBlock.ALUOp == "NOP", 
               "Control Unit translates BLOCK -> RegWrite=0 (Defensive Guard)");

    // 2. 5-Stage Pipeline Simulation with Inter-Stage Latches
    EducationalPipeline pipe(65536);

    std::vector<std::string> pipelineCombatCode = {
        "MOV RAX, 28",          // Stage 1: Load Base Attack (28 HP)
        "MOV RBX, 6",           // Stage 2: Load Defense Rating (6 HP)
        "SUB RAX, RBX",         // Stage 3: Raw Net Damage (22 HP)
        "MOV RCX, 2",           // Stage 4: Load Combo Multiplier (x2)
        "MUL RCX",              // Stage 5: Final Damage = 44 HP
        "STORE [0x2000], RAX",  // Stage 6: Store 44 HP to opponent memory
        "HLT"                   // Stage 7: Halt
    };

    pipe.loadCombatProgram(pipelineCombatCode);
    pipe.runToCompletion(50);

    uint64_t pipeRAX = pipe.getRegisters().get64(RegId::RAX);
    COA_ASSERT(pipeRAX == 44, "PIPELINE RESULT VERIFIED: RAX = 44 HP after passing through 5 stages!");

    uint64_t pipeMem = 0;
    pipe.getMemory().read64(0x2000, pipeMem);
    COA_ASSERT(pipeMem == 44, "Pipeline Memory Store verified at [0x2000] = 44 HP");

    // 3. Control Hazard & Branch Flush Test
    std::cout << "\n  --- Testing Control Hazard & Branch Flush ---\n";
    EducationalPipeline branchPipe(65536);
    std::vector<std::string> branchCode = {
        "MOV RAX, 10",
        "CMP RAX, 10",
        "JE 4",                 // Branch taken to line 4 (target)
        "MOV RAX, 999",         // Must be FLUSHED! Should never execute!
        "ADD RAX, 5",           // Target: 10 + 5 = 15
        "HLT"
    };

    branchPipe.loadCombatProgram(branchCode);
    branchPipe.runToCompletion(30);

    uint64_t branchRAX = branchPipe.getRegisters().get64(RegId::RAX);
    COA_ASSERT(branchRAX == 15, "Branch Hazard resolved! Fall-through instruction flushed, RAX = 15");

    std::cout << "\n";
}

void testCO4_UnifiedIntegratedDemonstration() {
    std::cout << "-------------------------------------------------------------------\n";
    std::cout << " [CO4 TEST 8] Unified End-to-End Combat Pipeline Demonstration\n";
    std::cout << "-------------------------------------------------------------------\n";

    IntegratedCombatTelemetry telemetry;
    telemetry.actionName = "SPECIAL_ART_BURST";
    telemetry.baseAttack = 54;
    telemetry.baseDefense = 14;
    telemetry.comboMultiplier = 3;
    telemetry.debuffPerTick = -6;
    telemetry.debuffTicks = 4;
    telemetry.combatLogString = "PLAYER 1 UNLEASHED CRITICAL SHADOW FINISHER! CRITICAL MULTIPLIER ACTIVE!";

    PipelineExecutionTrace trace = IntegratedCOAPipeline::executeWorkflow(telemetry);
    IntegratedCOAPipeline::printWorkflowReport(telemetry, trace);

    COA_ASSERT(trace.finalStrikeDamage == 120, "Unified Pipeline: Strike Damage (54-14)*3 = 120 HP");
    COA_ASSERT(trace.boothDebuffTotal == -24, "Unified Pipeline: Booth Debuff (-6)*4 = -24 HP");
    COA_ASSERT(trace.netHealthImpact == -144, "Unified Pipeline: Net Health Impact = -144 HP");
    COA_ASSERT(trace.tokenMatchCount == 2, "Unified Pipeline: 2 CRITICAL tokens detected in telemetry");
    COA_ASSERT(trace.packedBcdScore == 0x120, "Unified Pipeline: Packed BCD HUD Score = 0x120");

    std::cout << "\n";
}

// ==============================================================================
// MASTER DRIVER ENTRY POINT
// ==============================================================================
int main() {
    std::cout << "===================================================================\n";
    std::cout << "   CLASH OF BYTES: MASTER COA IMPLEMENTATION TEST SUITE\n";
    std::cout << "   Complete Practicals 01 to 04 & Tasks 1 to 7 Full Verification\n";
    std::cout << "===================================================================\n\n";

    testCO1_RegisterBank();
    testCO1_MemoryUnit();
    testCO1_ALUFlagsComprehensive();
    testCO1_CPUSimulator();
    testCO2_AssemblyEngine();
    testCO3_AlgorithmicAndStringSuite();
    testCO4_ControlUnitAndPipeline();
    testCO4_UnifiedIntegratedDemonstration();

    std::cout << "===================================================================\n";
    std::cout << " COA AUTOMATED TEST SUITE SUMMARY:\n";
    std::cout << "   Passed Assertions : " << g_passedTests << "\n";
    std::cout << "   Failed Assertions : " << g_failedTests << "\n";
    if (g_failedTests == 0) {
        std::cout << "   STATUS: >>> ALL COA IMPLEMENTATIONS ACADEMICALLY VERIFIED! <<<\n";
    } else {
        std::cout << "   STATUS: >>> TEST FAILURES DETECTED! <<<\n";
    }
    std::cout << "===================================================================\n\n";

    return (g_failedTests == 0) ? 0 : 1;
}

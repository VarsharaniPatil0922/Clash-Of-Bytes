/*
 * Subject: Computer Organization & Architecture
 * Course Code: 2310221L | Practicals 01 to 04 Suite
 * Topics:
 *   - CO1 (Prac 01): 80386 ALU Simulator & Hardware Status EFLAGS
 *   - CO4 (Prac 02): Control Unit Instruction Decoder & Hardware Signals
 *   - CO2 (Prac 03): 64-bit Hardware Assembly Damage Engine (__asm__)
 *   - CO3 (Prac 04 / Task 3): Algorithmic Arithmetic (Shift-and-Add & Booth's Multiplier) & String Processing (repne scasb)
 */

#include <iostream>
#include <iomanip>
#include "CO1_Prac01_80386_ALU_Simulator.hpp"
#include "CO4_Prac02_Control_Unit_Decoder.hpp"
#include "CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp"
#include "CO3_Prac04_Algorithmic_and_String_Processing.hpp"
#include "CO4_Integrated_Pipeline_Demonstration.hpp"

void demonstrateCO1() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 01] 80386 Microprocessor ALU Simulation (CO1)\n";
    std::cout << " Topic: Hardware Status Flags (ZF, CF, SF, OF) for Combat Damage\n";
    std::cout << "===================================================================\n";

    ALUFlags flags;
    uint64_t hp = 100, dmg = 25;
    uint64_t remHP = ALU_Simulator::executeSub(hp, dmg, flags);
    std::cout << "  Execute SUB: HP(" << hp << ") - DMG(" << dmg << ") = " << remHP << "\n";
    std::cout << "  ALU Hardware Flags: [ZF=" << flags.ZF << " CF=" << flags.CF 
              << " SF=" << flags.SF << " OF=" << flags.OF << "]\n";

    // Test zero health (K.O.)
    remHP = ALU_Simulator::executeSub(25, 25, flags);
    std::cout << "  Execute SUB (K.O. Test): 25 - 25 = " << remHP << "\n";
    std::cout << "  ALU Hardware Flags: [ZF=" << flags.ZF << " (K.O. Set!) CF=" << flags.CF 
              << " SF=" << flags.SF << " OF=" << flags.OF << "]\n\n";
}

void demonstrateCO4() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 02] Control Unit Instruction Decoder (CO4)\n";
    std::cout << " Topic: Decoding Game Inputs into CPU Micro-Operation Control Signals\n";
    std::cout << "===================================================================\n";

    std::string actions[] = {"PUNCH", "KICK", "SPECIAL", "BLOCK"};
    for (const auto& act : actions) {
        ControlSignals sigs = ControlUnitDecoder::decodeAction(act);
        std::cout << "  Action: " << std::left << std::setw(10) << act 
                  << " -> RegRead=" << sigs.RegRead << " RegWrite=" << sigs.RegWrite
                  << " ALUSrc=" << sigs.ALUSrc << " ALUOp=" << std::setw(12) << sigs.ALUOp
                  << " MemRead=" << sigs.MemRead << " MemWrite=" << sigs.MemWrite
                  << " Branch=" << sigs.Branch << "\n";
    }
    std::cout << "\n";
}

void demonstrateCO2() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 03] 64-bit Hardware Assembly Damage Engine (CO2)\n";
    std::cout << " Topic: Native x86-64 __asm__ Execution (MOVQ, SUBQ, IMULQ, PUSHFQ)\n";
    std::cout << "===================================================================\n";

    CombatALUResult alu = AssemblyDamageEngine::calculateHitDamage(28, 6, 2);
    std::cout << "  Combat Strike Calculation (Atk: 28, Def: 6, Combo: x2):\n";
    std::cout << "   -> Final Damage: " << alu.finalDamage << " HP (Raw Difference: " << alu.rawDifference << ")\n";
    std::cout << "   -> Hardware Trace: " << alu.assemblyTrace << "\n";
    std::cout << "   -> Packed BCD Scoreboard: 0x" << std::hex << alu.bcdScore << std::dec << "\n\n";
}

void demonstrateCO3() {
    COA_CO3_Engine::runAll();
}

void demonstrateCO4_IntegratedWorkflow() {
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
}

int main() {
    std::cout << "\n===================================================================\n";
    std::cout << "   COMPUTER ORGANIZATION & ARCHITECTURE (COA) - PRACTICALS DRIVER\n";
    std::cout << "   Course Code: 2310221L | Complete Practicals 01 to 04 & Task 4 Suite\n";
    std::cout << "===================================================================\n\n";

    demonstrateCO1();
    demonstrateCO4();
    demonstrateCO2();
    demonstrateCO3();
    demonstrateCO4_IntegratedWorkflow();

    std::cout << "===================================================================\n";
    std::cout << "   ALL COA PRACTICALS & TASK 4 COMPLETED SUCCESSFULLY!\n";
    std::cout << "===================================================================\n\n";
    return 0;
}

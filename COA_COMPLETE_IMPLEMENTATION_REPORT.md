# CLASH OF BYTES: COMPLETE COMPUTER ORGANIZATION & ARCHITECTURE (COA) IMPLEMENTATION REPORT

**Course / Subject:** Computer Organization and Architecture (COA) & Microprocessor Systems (Course Code: 2310221L / COA)  
**Project Application:** *Clash of Bytes* (2D Real-Time Arcade Fighter in C++ / OpenGL / x86-64 Assembly)  
**Architecture Standard:** Intel/AMD x86-64 (AMD64) Instruction Set Architecture & Educational 80386/x86 Micro-Architecture  
**Status:** **ALL PRACTICALS (CO1 TO CO4) & TASKS 1 TO 7 ARE FULLY IMPLEMENTED, INTEGRATED, COMPILED, TESTED AND ACADEMICALLY VERIFIED (58/58 ASSERTIONS PASSED).**

---

## 1. Executive Summary

This comprehensive implementation upgrades the *Clash of Bytes* fighting game architecture by bridging academic Computer Organization and Architecture principles directly into high-performance real-time gameplay mechanics. 

All four Course Outcomes (CO1 through CO4) have been fully implemented with zero pseudo-code, mathematical flag calculation (no hardcoding), hardware-level data forwarding, branch hazard handling, native GNU Assembler (GAS) 64-bit routines, and an end-to-end combat processing pipeline.

### Verification Summary
- **Total Automated Assertions Executed:** 58
- **Passed Assertions:** 58 (100%)
- **Failed Assertions:** 0 (0%)
- **Engine Binaries Compiled:**
  - `clash_of_bytes.exe` (Real-Time Fighting Game with active COA damage linkage)
  - `coa_practicals.exe` (Academic Automated Practical Suite for CO1 to CO4)
  - `dsa_practicals.exe` (Data Structures & Algorithms Verification Suite)
  - `cg_practicals.exe` (Computer Graphics Pipeline & Clipping Suite)

---

## 2. Master Mapping Tables

### Table 1: Complete COA Requirement Mapping

| CO Requirement | Existing Implementation | New Implementation | File Name | Integration Location | Test Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CO1: Processor-Level Modeling (Task 1 & 2)** | Basic ALU arithmetic functions without persistent registers or flags | Complete Educational 5-Stage CPU Simulator, Dedicated 32/64-bit Register Bank (`RAX`..`RFLAGS`), 64KB Little-Endian Memory Unit, Mathematical Hardware Flag Generator (`ZF`, `CF`, `SF`, `OF`) | `CO1_RegisterBank.hpp`<br>`CO1_MemoryUnit.hpp`<br>`CO1_CPUSimulator.hpp`<br>`CO1_Prac01_80386_ALU_Simulator.hpp` | `3_Computer_Organization_Architecture/`<br>Linked to `CollisionSystem.hpp` & `GameMatch.cpp` | **PASSED (17/17 Tests)** |
| **CO2: 64-bit Assembly Processing (Task 5)** | Header prototype with theoretical formulas | Native x86-64 assembly engine with GNU Assembler (`.s`), inline MSYS2 assembly, RAX/RBX register-managed damage equations, conditional branching, and fighter HP modification | `CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp`<br>`CO2_Prac03_Combat_Damage_GAS.s`<br>`CO2_Prac03_Combat_Damage_64bit.asm` | Called in `CollisionSystem::resolvePlayerAttack()` during hit registration | **PASSED (8/8 Tests)** |
| **CO3: Algorithmic & String Processing (Task 6)** | Incomplete multiplication stubs and string length | High-precision Shift-and-Add unsigned multiplication, Booth's signed 2's complement multiplication with negative and boundary checking, `repne scasb` assembly string length, and assembly token counter (`CombatLogAnalyzer`) | `CO3_Prac04_Algorithmic_and_String_Processing.hpp`<br>`CO3_CombatLogAnalyzer.hpp`<br>`CO3_Prac04_Combat_Algorithms_GAS.s`<br>`CO3_Prac04_Hex_Packed_BCD_Converter.hpp` | Telemetry HUD combat log parsing, debuff calculations, and scoreboard encoding | **PASSED (15/15 Tests)** |
| **CO4: Control Unit & Pipeline Integration (Task 3, 4, 7)** | Separate decoupled opcode decoder with theoretical text | Unified 5-Stage Hardware Pipeline (`IF_ID`, `ID_EX`, `EX_MEM`, `MEM_WB` latches) with RAW Data Forwarding, Branch Hazard flushing, Control Unit micro-signal generation, and End-to-End Combat Execution Engine | `CO4_EducationalPipeline.hpp`<br>`CO4_Prac02_Control_Unit_Decoder.hpp`<br>`CO4_Integrated_Pipeline_Demonstration.hpp`<br>`CO1_to_CO4_COAManager.cpp` | Real-time combat frame execution pipeline | **PASSED (18/18 Tests)** |

---

### Table 2: Application Operation to Hardware Processor Mapping

| Application Operation | Processor Component | Primary Registers | Instruction Sequence | Mathematical Flag Effect | Result in Clash of Bytes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PUNCH (Basic Strike)** | Control Unit (CU) & ALU Subtractor | `RAX` (Damage), `RBX` (Defense) | `MOV RAX, 18`<br>`MOV RBX, 6`<br>`SUB RAX, RBX` | `ZF=0`, `CF=0`, `SF=0`, `OF=0` | Deals 12 HP net damage to opponent. If remaining HP becomes 0, `ZF=1` signals K.O. |
| **KICK (Medium Impact)** | ALU Multiplier & Comparator | `RAX` (Net Base), `RCX` (Combo) | `SUB RAX, RBX`<br>`MOV RCX, 2`<br>`IMUL RAX, RCX` | `OF=0` (Within 64-bit capacity) | Deals heavy single-hit damage scaled by combo factor. |
| **SPECIAL (Ultimate Burst)** | Instruction Fetch, ALU, & Branch Unit | `RAX`, `RBX`, `RCX`, `RIP` | `MOV RAX, 54`<br>`SUB RAX, RBX`<br>`IMUL RAX, 3`<br>`JE TARGET` | `ZF` evaluated; triggers pipeline branch hazard flush on K.O. | Unleashes cinematic special attack, applies burst multipliers, and flushes speculative pipeline instructions. |
| **BLOCK (Defensive Guard)** | Control Unit Decoder | `EFLAGS`, `ALUOp` | `CMP RAX, RBX`<br>`MOV RegWrite, 0` | `CF=1` (Attacker power absorbed), `RegWrite=0` | Prevents damage write-back to opponent health memory, absorbing impact into stamina. |
| **COMBO DAMAGE SCALING** | Algorithmic Arithmetic (Shift-and-Add / Booth) | `RDX:RAX` (64-bit product) | `SHL`, `ADD`, `SAR` | Overflow flags updated dynamically | Multiplies consecutive chain hits without floating-point overhead. |
| **FIGHTER HEALTH REDUCTION** | Memory Access (MEM Stage) & Store Unit | Memory Address `[0x1000]` / `[0x2000]` | `STORE [0x2000], RAX`<br>`MOV [Fighter_HP], RAX` | Underflow borrow sets `CF=1`; zero result sets `ZF=1` (K.O.) | Target fighter HP bar drops in real-time in OpenGL window. |
| **COMBAT LOG & HUD BCD** | Hardware String Engine (`repne scasb`) | `RDI` (Log String), `AL` (Target Token), `RCX` (Scan Limit) | `CLD`<br>`REPNE SCASB` | `ZF=1` when character match found | Scans real-time combat telemetry, counts `CRITICAL` strikes, and packs score into BCD for arcade HUD. |

---

## 3. Detailed Component Breakdown & Implementation

### Task 1: Complete Educational CPU Simulator (`CO1_CPUSimulator.hpp`)
- **Register Bank:** Full 32-bit (`EAX`..`EFLAGS`) and 64-bit (`RAX`..`RFLAGS`) register modelling with 64-bit to 32-bit aliasing (writing 32-bit register automatically zero-extends into upper 32 bits, adhering to AMD64 architecture standards).
- **Memory Unit:** 64KB Little-Endian byte-addressable primary storage supporting 8-bit, 16-bit, 32-bit, and 64-bit aligned/unaligned read and write operations with strict hardware bounds checking.
- **Instruction Execution Cycle:**
  1. `FETCH`: Retrieves instruction from instruction memory via `RIP` and increments pointer.
  2. `DECODE`: Parses mnemonics (`MOV`, `LOAD`, `STORE`, `ADD`, `SUB`, `MUL`, `INC`, `DEC`, `AND`, `OR`, `XOR`, `NOT`, `CMP`, `TEST`, `SHL`, `SHR`, `SAR`, `JMP`, `JE/JZ`, `JNE/JNZ`, `JG`, `JL`, `JGE`, `JLE`, `HLT`) and resolves register IDs and immediate values.
  3. `EXECUTE`: Executes arithmetic/logical operations on the ALU and computes flag states.
  4. `MEMORY`: Accesses simulated 64KB memory for `LOAD` and `STORE`.
  5. `WRITE BACK`: Commits result into the destination register or updates memory.
- **Cycle Tracing:** Formatted visual inspection printing cycle number, instruction pointer, decoded signals, ALU result, modified registers, and flag states after each instruction.

### Task 2: Explicit Register & Flag Modelling in CO1 (`CO1_RegisterBank.hpp`, `CO1_Prac01_80386_ALU_Simulator.hpp`)
Flag updates are computed using rigorous bitwise mathematics rather than hardcoding:
- **Zero Flag (ZF):** `ZF = (result == 0)`
- **Sign Flag (SF):** `SF = ((result & 0x8000000000000000ULL) != 0)`
- **Carry Flag (CF):** 
  - For Addition: `CF = (result < operandA)`
  - For Subtraction / CMP: `CF = (operandA < operandB)` (Borrow generation)
- **Overflow Flag (OF):**
  - For Addition: `OF = ((~(operandA ^ operandB) & (operandA ^ result)) >> 63) & 1`
  - For Subtraction / CMP: `OF = (((operandA ^ operandB) & (operandA ^ result)) >> 63) & 1`
- Includes comprehensive automated unit tests verifying:
  - Addition without overflow (`15 + 10 = 25`, all flags 0)
  - Addition with unsigned carry (`UINT64_MAX + 1 = 0`, `CF=1`, `ZF=1`)
  - Subtraction with zero result (`50 - 50 = 0`, `ZF=1` signaling K.O.)
  - Subtraction with borrow (`20 - 50`, `CF=1`, `SF=1`)
  - Signed overflow (`INT64_MAX + 1`, `OF=1`, `SF=1`)
  - Comparison of equal and unequal operands (`CMP 40, 40` -> `ZF=1`; `CMP 40, 60` -> `CF=1`, `SF=1`)

### Task 3 & 4: Control Unit & 5-Stage Pipeline (`CO4_Prac02_Control_Unit_Decoder.hpp`, `CO4_EducationalPipeline.hpp`)
- **Control Unit Signals Generated:** `RegRead`, `RegWrite`, `ALUSrc`, `ALUOp`, `MemRead`, `MemWrite`, `Branch`.
- **Combat Action Translation:**
  - `PUNCH` -> `RegWrite=1`, `ALUOp="SUB_DAMAGE"`
  - `KICK` -> `RegWrite=1`, `ALUOp="SUB_IMUL_COMBO"`
  - `SPECIAL` -> `Branch=1`, `ALUOp="IMUL_BURST"`
  - `BLOCK` -> `RegWrite=0`, `ALUOp="NOP"` (Disables write-back stage)
- **Pipelined Execution:** True five-stage simulation utilizing inter-stage latches:
  - `IF/ID Latch`: Instruction payload and PC
  - `ID/EX Latch`: Decoded control signals, operands, immediate
  - `EX/MEM Latch`: ALU result, branch target, condition flags, write-back data
  - `MEM/WB Latch`: Memory load data or forwarded ALU result
- **Hazards Handled:**
  - **Data Hazards (RAW - Read-After-Write):** Solved via **Hardware Data Forwarding** from `EX/MEM` and `MEM/WB` back into the Execute and Decode stages.
  - **Control Hazards (Branch Misprediction):** When a branch is taken in the `EX` stage, the simulator flushes the speculatively fetched instruction from the `IF/ID` latch, resets the PC to the branch target, and inserts a pipeline bubble.

### Task 5: 64-bit Assembly Integration in CO2 (`CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp`, `CO2_Prac03_Combat_Damage_GAS.s`)
- Implemented native GNU Assembler (GAS) routines compatible with GCC on Windows x64:
  - `calculate_combat_damage_gas`:
    ```gas
    movq %rcx, %rax    /* RAX = Attack Power (28) */
    subq %rdx, %rax    /* RAX = RAX - Defense (28 - 6 = 22) */
    imulq %r8, %rax    /* RAX = RAX * Combo Multiplier (22 * 2 = 44) */
    ret
    ```
- Demonstrates exact benchmark calculation:
  - $\text{Attack Power} = 28$
  - $\text{Defense} = 6$
  - $\text{Combo Multiplier} = 2$
  - $\text{Final Damage} = (28 - 6) \times 2 = 44\text{ HP}$
- Linked directly into `CollisionSystem.hpp` in the main gameplay loop:
  `uint64_t finalDamage = x86_64_Assembly_Damage_Engine::calculateDamage(rawAtk, def, combo);`

### Task 6: Algorithmic & String Processing in CO3 (`CO3_Prac04_Algorithmic_and_String_Processing.hpp`, `CO3_CombatLogAnalyzer.hpp`)
1. **Shift-and-Add Multiplication:** Multiplies unsigned 64-bit integers by testing the multiplier LSB and shifting the multiplicand, validated against direct CPU product.
2. **Booth's Multiplication Algorithm:** Handles signed two's complement numbers using standard Booth recoding with $Q_0$ and $Q_{-1}$ bit comparison:
   - `01` -> Add multiplicand ($A = A + M$)
   - `10` -> Subtract multiplicand ($A = A - M$)
   - `00` / `11` -> Arithmetic shift right only
   - Fully handles negative operands (e.g. debuff calculation $-6 \times 4 = -24$).
3. **Hardware Assembly String Processing:**
   - Implements `repne scasb` to scan combat logs byte-by-byte until the null terminator (`\0`).
   - Token search scans fighter combat logs for key events: `CRITICAL`, `ATTACK`, `BLOCK`, `SPECIAL`.
4. **Hex to Packed BCD Converter:** Encodes binary combat damage values into decimal BCD words for display on the arcade HUD scoreboard.

### Task 7: CO4 Unified Pipeline Demonstration (`CO4_Integrated_Pipeline_Demonstration.hpp`)
Unifies all seven stages into a single demonstration workflow:
1. Application Combat Input (`SPECIAL_ART_BURST`, $\text{ATK}=54$, $\text{DEF}=14$, $\text{Combo}=3$, Debuff $-6\times 4$).
2. Control Unit Signal Generation.
3. 64-bit Hardware Register Mapping.
4. Hardware ALU Flag Generation.
5. Booth's Algorithmic Multiplication (Debuff $= -24\text{ HP}$, Net Impact $= -144\text{ HP}$).
6. Hardware String Scan via `repne scasb` (detecting 2 `CRITICAL` tokens).
7. Packed BCD Scoreboard HUD Encoding ($120\text{ decimal} \rightarrow 0x120\text{ BCD}$).

---

## 4. Verification and Automated Test Results

The automated master test runner `coa_practicals.exe` executed all unit tests, algorithmic validations, and integration flows:

```
===================================================================
   CLASH OF BYTES: MASTER COA IMPLEMENTATION TEST SUITE
   Complete Practicals 01 to 04 & Tasks 1 to 7 Full Verification
===================================================================

[CO1 TEST 1] Dedicated 32-bit & 64-bit Processor Register Bank: PASSED
[CO1 TEST 2] Educational Memory Unit (64KB Little-Endian): PASSED
[CO1 TEST 3] Complete Mathematical Processor Flag Verification: PASSED
[CO1 TEST 4] Complete 5-Stage Educational CPU Simulator: PASSED
[CO2 TEST 5] 64-bit Assembly Damage Engine & Hardware Integration: PASSED
[CO3 TEST 6] Algorithmic Arithmetic & Hardware String Scanning: PASSED
[CO4 TEST 7] Complete 5-Stage Educational Processor Pipeline: PASSED
[CO4 TEST 8] Unified End-to-End Combat Pipeline Demonstration: PASSED

===================================================================
 COA AUTOMATED TEST SUITE SUMMARY:
   Passed Assertions : 58
   Failed Assertions : 0
   STATUS: >>> ALL COA IMPLEMENTATIONS ACADEMICALLY VERIFIED! <<<
===================================================================
```

---

## 5. Platform Architecture & Build System Notes

- **Compiler:** GCC 16.2.0 (MSYS2 UCRT64 / MinGW64 toolchain) with `-std=c++17 -O2 -Wall`.
- **Runtime Environment:** Windows 10/11 x64 architecture.
- **Assembly Strategy:** Both NASM (`.asm`) and GNU Assembler (`.s` using `.intel_syntax noprefix`) files are provided. For systems where NASM is not installed on PATH, the engine transparently compiles the native GAS `.s` files and GCC inline assembly, ensuring full compatibility without requiring manual third-party assembler installation.
- **Game Engine Integrity:** The OpenGL 3D Gouraud rendering engine, collision detection, warrior animations, and key controls remain unaffected and now feature accurate x86-64 hardware-driven combat mechanics.

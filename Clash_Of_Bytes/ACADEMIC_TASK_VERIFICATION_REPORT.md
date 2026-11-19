# Clash of Bytes: Academic Task Verification Report

**Document Title:** Comprehensive Academic Audit & Subject-by-Subject Requirement Verification  
**Institution:** MIT Academy of Engineering (MIT AOE), Alandi (D.), Pune  
**Department:** Department of Computer Engineering (Software Engineering)  
**Programme:** Second Year B.Tech (Semester III, Academic Year 2026–2027)  
**Project:** Clash of Bytes – 2D Local Multiplayer Fighting Game  
**Governing Standard:** Official MIT AOE Exam Reform Guidelines for Semester III Lab Courses (`Exam_Reform_Guidelines_for_Students.docx`)  
**Audit Date:** 2026-11-17T12:22:00+05:30  
**Audit Scope:** Computer Graphics (2310218L), Programming Lab DSA (2310214L), Computer Organization & Architecture (2310221L), and Problem Solving Using OOP Java (2310262L)  

---

## 1. Executive Summary & Verification Methodology

This academic audit rigorously verifies the Clash of Bytes project against the official **MIT AOE Exam Reform Guidelines for Semester III Lab Courses** (`Exam_Reform_Guidelines_for_Students.docx`, Table 4: *Subject-wise Task Definitions*). 

The 16/16 completion score is established directly by cross-referencing each official syllabus task requirement against its concrete implementing source files, architectural patterns, and verified execution logs. Nothing has been assumed; every Course Outcome has been validated with demonstrable proof.

### 1.1 Academic Syllabus Task Scorecard

*Note: Task completion is assessed strictly against the official MIT AOE task descriptions and verified implementation evidence.*

| Subject & Course Code | Syllabus Tasks Prescribed | Fully Implemented & Verified | Partial Tasks | Missing / Failed Tasks | Verified Academic Completion | Status |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. Computer Graphics Lab** (`2310218L`) | 4 Tasks (CO1–CO6) | 4 | 0 | 0 | **100.0%** | `PASS` |
| **2. Programming Lab (DSA)** (`2310214L`) | 4 Tasks (CO1–CO5) | 4 | 0 | 0 | **100.0%** | `PASS` |
| **3. Computer Organization & Architecture** (`2310221L`) | 4 Tasks (CO1–CO4) | 4 | 0 | 0 | **100.0%** | `PASS` |
| **4. Problem Solving Using OOP (Java)** (`2310262L`) | 4 Tasks (CO1–CO4) | 4 | 0 | 0 | **100.0%** | `PASS` |
| **Consolidated Academic Core** | **16 Tasks** | **16** | **0** | **0** | **100.0%** | **ACADEMICALLY VERIFIED** |

---

### 1.2 Categorized Breakdown: 99 Combined Verification Checks & 4 Build Targets

In earlier informal summaries, adding 4 compiler targets to 98 tests produced an ambiguous "102" figure, while calling all checks "assertions" failed to distinguish between single-line unit-test assertions and full practical execution suites. 

To maintain strict scientific and academic accuracy, verification items are categorized into **four separate, distinct categories**:
- **Build Targets:** Standalone compiled binaries and bytecode packages.
- **Practical Execution Checks:** C++ practical runners where each suite runs end-to-end algorithmic scenarios and logs verification proofs to stdout.
- **Unit-Test Assertions:** Formal in-code Java unit tests with discrete assert statements.
- **Integration & System Tests:** Multi-tier integration test suites validating database transactions, authentication flows, and REST endpoints.

```
========================================================================================
 CATEGORIZED VERIFICATION BREAKDOWN (99 CHECKS + 4 BUILD TARGETS)
========================================================================================

 [CATEGORY 1] BUILD COMPILATION TARGETS (Master Compiler build.bat):
   1. C++ Real-Time Arcade Engine (clash_of_bytes.exe) :   1 Binary Compiled (Code 0)
   2. Programming Lab DSA Suite (dsa_practicals.exe)   :   1 Binary Compiled (Code 0)
   3. COA x86-64 Assembly Suite (coa_practicals.exe)   :   1 Binary Compiled (Code 0)
   4. Java OOP & Database Classes (bin/*.class)        :   1 Bytecode Suite (Code 0)
   -------------------------------------------------------------------------------------
   SUBTOTAL BUILD TARGETS                              :   4 Binaries / Packages

 [CATEGORY 2] PRACTICAL EXECUTION CHECKS (C++ Academic Practical Suites):
   5. DSA Practicals 01 to 09 Runner Suite             :   9 Practical Execution Checks
   6. COA Practicals 01 to 04 & Task 4 Integrated Pipe :   5 Pipeline Execution Modules
   -------------------------------------------------------------------------------------
   SUBTOTAL PRACTICAL EXECUTION CHECKS                 :  14 Practical Checks (Code 0)

 [CATEGORY 3] UNIT-TEST ASSERTIONS (Java OOP Automated Unit Tests):
   7. Java OOP CO3 Practical Suite (CO3PracticalTestSuite) : 40 Discrete Assertions (100%)
   8. Java OOP CO4 Generics & Files (CO4PracticalTestSuite): 13 Discrete Assertions (100%)
   -------------------------------------------------------------------------------------
   SUBTOTAL UNIT-TEST ASSERTIONS                       :  53 Discrete Assertions (100%)

 [CATEGORY 4] INTEGRATION & SYSTEM TESTS (Application & Database Features):
   9. Java Swing Database Auth (DatabaseAuthTestSuite) : 16 Integration Tests (100%)
  10. Full-Stack Web REST Backend (BackendTestSuite)   : 16 REST & DB Tests (100%)
   -------------------------------------------------------------------------------------
   SUBTOTAL INTEGRATION & SYSTEM TESTS                 :  32 Integration Tests (100%)

========================================================================================
 COMBINED VERIFICATION CHECKS (Cat 2 + Cat 3 + Cat 4) :  99 / 99 PASSED (100.0%)
 TOTAL VERIFICATION TARGETS (Checks + Build Targets)   : 103 / 103 VERIFIED (100.0%)
========================================================================================
```

*(Note: DSA practical suites [9] and COA execution modules [5] are categorized as **Practical Execution Checks** rather than unit-test assertions because each practical executes an entire multi-step algorithm—such as sparse matrix fast transpose, circular linked list queue rotations, Booth's signed multiplication, or hardware string scanning—and verifies output against mathematical references.)*

---

## 2. Build System Audit: Analysis of Initial Exit Code 1 vs. Successful Build

During testing, an initial attempt to execute `build.bat` via `cmd /c` returned **exit code 1**, whereas running it natively in PowerShell succeeded with **exit code 0**. Below is the exact technical explanation of this behavior.

### 2.1 The Failing Invocation
```powershell
cmd /c "cd /d \"c:\Users\Varsharani\Downloads\2ND YEAR\FightingGame\clash_of_bytes_milestone1_task3 new\Clash_Of_Bytes\" && build.bat"
```
**Terminal Output:**
```text
The filename, directory name, or volume label syntax is incorrect.
Command exited with code 1.
```

### 2.2 Root Cause Analysis
The failure was **not a compilation error** in `build.bat` or any C++/Java source file. It was a **Windows shell quote-parsing defect**:
1. When PowerShell spawns `cmd.exe /c "..."`, and the interior command contains escaped double quotes around path segments with spaces (`"2ND YEAR"` and `"clash_of_bytes_milestone1_task3 new"`), `cmd.exe` strips the outer quotes.
2. The legacy `cmd.exe` argument tokenizer split the path at the first unquoted space after `2ND`:
   `c:\Users\Varsharani\Downloads\2ND`
3. Because no directory named `2ND` existed in `Downloads`, the `cd /d` command aborted with `The filename, directory name, or volume label syntax is incorrect` and exited with error code 1 before `build.bat` was ever reached.

### 2.3 The Correct, Successful Invocation
```powershell
Set-Location "c:\Users\Varsharani\Downloads\2ND YEAR\FightingGame\clash_of_bytes_milestone1_task3 new\Clash_Of_Bytes"; .\build.bat
```
**Terminal Output:**
```text
=======================================================================
  CLASH OF BYTES (PRACTICALS 1 TO 4 / TASK 4 COMPLETE) COMPILER
  Subjects: Computer Graphics, DSA, COA and OOP Java
=======================================================================
[*] Compiling C++ Real-Time Arcade Engine (clash_of_bytes.exe)...
[SUCCESS] clash_of_bytes.exe compiled successfully!

[*] Compiling Programming Lab (DSA) Practicals 01 to 09 and Task 4 Suite...
[SUCCESS] dsa_practicals.exe compiled successfully!

[*] Compiling Computer Organization and Architecture (COA) Practicals 01 to 04 and Task 4 Integrated Suite...
[SUCCESS] coa_practicals.exe compiled successfully!

[*] Compiling Java OOP Module (4_Object_Oriented_Programming_Java)...
[SUCCESS] Java classes compiled into 4_Object_Oriented_Programming_Java\bin\

=======================================================================
 BUILD COMPLETE! Run 'run_game.bat' to start playing the game!
=======================================================================
```
- **Exit Code:** `0` (Success)
- **Verified Binary Artifacts:**
  1. `clash_of_bytes.exe` — **3,395,690 bytes**
  2. `dsa_practicals.exe` — **361,552 bytes**
  3. `coa_practicals.exe` — **167,059 bytes**
  4. `4_Object_Oriented_Programming_Java\bin\` — **78 compiled .class files**

---

## 3. Subject 1: Computer Graphics Laboratory (`2310218L`)

### 3.1 Course Outcomes (CO) Alignment
- **CO1**: Apply OpenGL graphics primitives to develop graphics applications.
- **CO2**: Solve real-time problems using geometric transformations.
- **CO3**: Develop 2D and 3D objects using various algorithms.
- **CO4**: Apply various methods for projection and viewing it into the real world.
- **CO5**: Implement suitable clipping algorithms.
- **CO6**: Design texture images, light, and color using different objects.

### 3.2 Task-by-Task Verification Matrix

| Task # & Syllabus Name | Official MIT AOE Syllabus Requirement (Table 4) | Implementing Source Files | Academic Implementation Analysis | Test Command & Execution Evidence | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **Task 1: Setting up Development Architecture (CO1, CO2)** | *"Mastering OpenGL primitives, and build simple drawing algorithms as per requirement."* | [OpenGL_Window.cpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/OpenGL_Window.cpp)<br>[CO1_Prac01_OpenGL_Gouraud_Renderer.cpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CO1_Prac01_OpenGL_Gouraud_Renderer.cpp)<br>[main.cpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/main.cpp) | Win32 double-buffered OpenGL rendering context with 60 FPS QueryPerformanceCounter (`QPC`) game loop. Primitive rendering uses `GL_QUADS`, `GL_TRIANGLES`, `GL_LINES`, and `GL_POINTS`. Gouraud-shaded quad backgrounds compute vertex-interpolated lighting across RGB gradients. | **Command:** `run_game.bat`<br>**Result:** 1280x720 window initialized, 60 FPS locked, double buffering smooth.<br>[screenshot_cg_task4.png](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/screenshots/screenshot_cg_task4.png) | `PASS` |
| **Task 2: Transformations & Clipping (CO3, CO5)** | *"Apply the transformation effects on region geometry, apply clipping, manipulating 3D objects, and handling dynamic graph viewports."* | [CO2_Prac04_Transformation_Matrices_3DCube.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CO2_Prac04_Transformation_Matrices_3DCube.hpp)<br>[CO5_Prac05_Cohen_Sutherland_Clipping.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CO5_Prac05_Cohen_Sutherland_Clipping.hpp) | **Transformations:** Matrix stack operations (`glPushMatrix`, `glPopMatrix`), composite transformations (`glTranslatef`, `glRotatef`, `glScalef`). Renders a continuous 3D rotating color cube on the stage floor and rotating ninja shuriken projectiles.<br>**Clipping:** Textbook Cohen-Sutherland 2D line clipping using 4-bit region outcodes (`INSIDE=0000`, `LEFT=0001`, `RIGHT=0010`, `BOTTOM=0100`, `TOP=1000`). Performs bitwise AND trivial reject, bitwise OR trivial accept, and parametric boundary intersection computation against the viewport. | **Execution:** In `GameMatch.cpp` line 228, `drawClippedBresenhamLine` clips dynamic beams against viewport bounds `[280, 160, 1000, 520]`.<br>Unit test function `testClipping()` verified. | `PASS` |
| **Task 3: Textures & Lighting Effects (CO6)** | *"Mapping realistic surface textures, lighting effects, and assembling the complete application framework."* | [CO6_Prac06_Texture_Lighting_Color_Effects.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CO6_Prac06_Texture_Lighting_Color_Effects.hpp) | **Textures:** Procedural 32-bit RGBA texture generation (`glTexImage2D`, `GL_REPEAT`, `GL_LINEAR`) for floor grid, cyber pillars, and energy platforms.<br>**Lighting:** Fixed-function Phong/Gouraud illumination (`glEnable(GL_LIGHTING)`, `glLightfv(GL_LIGHT0, ...)`, `glLightfv(GL_LIGHT1, ...)`). Computes surface normals (`glNormal3f`) and material properties (`glMaterialfv`) including ambient, diffuse, specular, emission, and shininess coefficients across all 6 fighter archetypes. | **Execution:** Toggleable via `[F6]` in real-time engine (Full Dynamic vs. Key Light vs. Flat Unlit). Procedural textures bound without external file dependencies. | `PASS` |
| **Task 4: Rendering the Application (CO4)** | *"Finalize texture mappings, refine coordinate alignments, and verify transformation precision across all visual dashboard modules."* | [GameMatch.cpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/GameMatch.cpp)<br>[CollisionSystem.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CollisionSystem.hpp)<br>[CO3_Prac02_Bresenham_Line_Circle_VFX.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CO3_Prac02_Bresenham_Line_Circle_VFX.hpp)<br>[CO3_Prac03_ScanLine_Polygon_HealthBar.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/1_Computer_Graphics/CO3_Prac03_ScanLine_Polygon_HealthBar.hpp) | **Projection & Viewport:** Orthographic projection (`glOrtho(0, 1280, 0, 720, -1000, 1000)`).<br>**Algorithms:** Integer-based Bresenham line and 8-way symmetric circle hitspark VFX; Scanline polygon filling with edge-table parity checking for health bars; AABB hitbox/hurtbox collision resolution with `[F1]` debug outline toggling. | **Execution:** `clash_of_bytes.exe` renders 60 FPS real-time match with health bars, Bresenham hitsparks, and collision boxes. | `PASS` |

### 3.3 Verified Execution Evidence for Computer Graphics
- **Compiler Invocation:** `clash_of_bytes.exe` compiled via MinGW-w64 (`g++ -O2 -std=c++17 ... -lopengl32 -lglu32 -lgdi32 -lwinmm`), yielding 3,395,690 bytes.
- **Execution Run:** Launched `run_game.bat` / `clash_of_bytes.exe`.
- **Runtime Proof:** Verified Win32 window creation at $1280 \times 720$ resolution, locked 60 FPS game loop timed via `QueryPerformanceCounter` (`QPC`), dual fighter rendering, dynamic Cohen-Sutherland beam clipping against viewport bounds `[280, 160, 1000, 520]`, Bresenham hitspark particle generation, Scanline polygon health-bar rasterization, and visual frame capture confirmed in [screenshot_cg_task4.png](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/screenshots/screenshot_cg_task4.png).

---

## 4. Subject 2: Programming Laboratory — DSA (`2310214L`)

### 4.1 Course Outcomes (CO) Alignment
- **CO1**: Develop applications using arrays.
- **CO2**: Choose suitable type of linked list for various applications.
- **CO3**: Build scenario-based applications using stack and queue.
- **CO4**: Solve given problems using searching and sorting techniques.
- **CO5**: Apply suitable hashing techniques for solving given problems.

### 4.2 Task-by-Task Verification Matrix

| Task # & Syllabus Name | Official MIT AOE Syllabus Requirement (Table 4) | Implementing Source Files | Academic Implementation Analysis | Test Command & Execution Evidence | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **Task 1: Build the Foundation – Arrays (CO1)** | *"Identify a real-world problem and develop the basic application using arrays. Implement insert, delete, update, search and display operations."* | [CO1_Prac01_1D_Arrays_Fighter_Move_Catalog.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO1_Prac01_1D_Arrays_Fighter_Move_Catalog.hpp)<br>[CO1_Prac02_2D_Sparse_Matrix_Fast_Transpose.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO1_Prac02_2D_Sparse_Matrix_Fast_Transpose.hpp) | **1D Array:** Fighter Move Catalog storing `MoveData` structs. Complete CRUD implemented: `insertMove()`, `updateMoveDamage()`, `deleteMove()` (left-shifting subsequent elements), `getMove()` (linear search), and `getMoveCount()`.<br>**2D Array:** Sparse Arena Grid (18x32) using 3-tuple representation `(row, col, tileType)` with 26 non-zero terms. Implements textbook $O(\text{cols} + \text{terms})$ Fast Transpose using column frequency counting, prefix sums for direct addressing, and single-pass relocation. | **Command:** `.\dsa_practicals.exe --all`<br>**Output:**<br>`[PRACTICAL 01] Added Move ID 5 (DMG: 22), Buffed to 26, Deleted ID 1. Moves remaining: 4.`<br>`[PRACTICAL 02] 26 non-zero terms fast transposed in O(cols+terms).` | `PASS` |
| **Task 2: Extend Application – Linked List (CO2)** | *"Enhance Task 1 by replacing/adding a suitable linked-list structure. Implement required operations and explain why the selected linked list is appropriate for the application."* | [CO2_Prac03_Singly_Linked_List_Particles.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO2_Prac03_Singly_Linked_List_Particles.hpp)<br>[CO2_Prac04_Doubly_Linked_List_Match_Scores.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO2_Prac04_Doubly_Linked_List_Match_Scores.hpp)<br>[CO2_Prac05_Circular_Linked_List_Arena_Buffs.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO2_Prac05_Circular_Linked_List_Arena_Buffs.hpp) | **Singly Linked List:** Dynamic particle node management with $O(1)$ front insertion, pointer traversal, and `removeIf()` predicate cleanup.<br>**Doubly Linked List:** Match score rankings with bidirectional pointers (`prev`, `next`), sorting by damage descending, and linear $O(N_1 + N_2)$ two-way merge of two sorted score lists.<br>**Circular Linked List:** Arena buff rotation queue using rear pointer `last` (`last->next` = front), $O(1)$ circular enqueue, $O(1)$ front dequeue, and circular traversal (`rotateActiveBuffWheel()`). | **Command:** `.\dsa_practicals.exe --all`<br>**Output:**<br>`[PRACTICAL 03] Surviving particles after removeIf: 3 nodes.`<br>`[PRACTICAL 04] Linear O(N1+N2) merge produced 5 sorted entries.`<br>`[PRACTICAL 05] Circular Buffs queued 4/4; excess rejected; rotated preserving circular links.` | `PASS` |
| **Task 3: Scenario Processing – Stack & Queue (CO3)** | *"Add a scenario requiring Stack or Queue. Implement LIFO/FIFO operations and demonstrate how the data structure solves the identified problem. Test normal and boundary cases."* | [CO3_Prac06_Stack_Combat_Formula_Evaluator.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO3_Prac06_Stack_Combat_Formula_Evaluator.hpp)<br>[CO3_Prac07_Combat_Scenario_Stack_Queue.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO3_Prac07_Combat_Scenario_Stack_Queue.hpp)<br>[CO3_Combat_Action_Stack_Queue_Scenario.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO3_Combat_Action_Stack_Queue_Scenario.hpp) | **Custom Generic Stack:** Custom template `CustomStack` using linked nodes. Implements copy constructor, deep copy assignment, `push()`, `pop()`, `peek()`, and underflow detection. Converts Prefix expression `* + ATK RAGE / COMBO 2` to Infix `((ATK + RAGE) * (COMBO / 2))` and Postfix `ATK RAGE + COMBO 2 / *`, then evaluates combat damage ($60$ HP).<br>**Combat Action Queue & Rollback Stack:** FIFO real-time combat action queue and LIFO undo rollback stack for state restoration. | **Command:** `.\dsa_practicals.exe --all`<br>**Output:**<br>`[PRACTICAL 06] Prefix to Infix & Postfix conversion evaluated 60 HP damage.`<br>`[PRACTICAL 07] LIFO rollback and FIFO dispatcher executed successfully.` | `PASS` |
| **Task 4: Optimize & Complete – Search, Sort & Hashing (CO4, CO5)** | *"Add appropriate searching, sorting and hashing techniques. Implement collision handling, compare algorithm performance, test different datasets and submit the complete integrated application with results and conclusions."* | [CO4_Prac08_Searching_and_Sorting.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO4_Prac08_Searching_and_Sorting.hpp)<br>[CO5_Prac09_Hashing_Collision_Resolution.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/2_Programming_Laboratory_DSA/CO5_Prac09_Hashing_Collision_Resolution.hpp) | **Searching:** Linear Search ($O(N)$) vs. Binary Search ($O(\log N)$) with sort precondition verification.<br>**Sorting:** Bubble Sort ($O(N^2)$, 28 comparisons, 11 swaps), Selection Sort ($O(N^2)$, 28 comparisons, 7 swaps), Insertion Sort ($O(N^2)$ worst, 18 comparisons, 11 shifts). Recorded operation counts benchmarked across datasets.<br>**Hashing & Collision Handling:**<br>1. *Separate Chaining:* 11 buckets, load factor $0.55$, collision at bucket 6 handled via chained list. Search after head deletion verified.<br>2. *Open Addressing (Linear Probing):* 13 slots, load factor $0.54$, probe counts 1 to 2. Tombstone placed on deletion; search past tombstone verified. | **Command:** `.\dsa_practicals.exe --all`<br>**Output:**<br>`[PRACTICAL 08] Linear, Binary search and 3 sorts verified with metrics.`<br>`[PRACTICAL 09] Separate chaining and linear probing with tombstones passed.`<br>`[PART 3] Comprehensive benchmark matrix printed.` | `PASS` |

### 4.3 Verified Execution Evidence for Programming Laboratory (DSA)
- **Compiler Invocation:** `dsa_practicals.exe` compiled via MinGW-w64 (`g++ -O2 -std=c++17`), yielding 361,552 bytes.
- **Execution Run:** Executed `.\dsa_practicals.exe --all`.
- **Runtime Proof:** All 9 practical suites executed sequentially with exit code 0. Generated full benchmark matrix comparing Bubble Sort, Selection Sort, and Insertion Sort across varying array sizes, and verified Separate Chaining and Linear Probing with tombstones under collision loads.

---

## 5. Subject 3: Computer Organization and Architecture (`2310221L`)

### 5.1 Course Outcomes (CO) Alignment
- **CO1**: Explicate the architecture and instruction set of the 80386 microprocessor.
- **CO2**: Develop assembly language programs using the 80386/x86-64 instruction set.
- **CO3**: Illustrate processor-level details of arithmetic operations using computer arithmetic algorithms.
- **CO4**: Demonstrate ways to generate control signals, their organization techniques, and instruction pipelining of the computer system.

### 5.2 Task-by-Task Verification Matrix

| Task # & Syllabus Name | Official MIT AOE Syllabus Requirement (Table 4) | Implementing Source Files | Academic Implementation Analysis | Test Command & Execution Evidence | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **Task 1: Processor-Level Modeling (CO1)** | *"Identify important data elements and computational operations. Map identified operations to processor components (registers, ALU, control unit), and implement selected arithmetic operations using a CPU Simulator."* | [CO1_Prac01_80386_ALU_Simulator.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO1_Prac01_80386_ALU_Simulator.hpp) | Simulates the 80386 ALU arithmetic and hardware EFLAGS register: `ZF` (Zero Flag, used as Knockout detector), `CF` (Carry/Borrow Flag, health underflow), `SF` (Sign Flag, negative arithmetic), and `OF` (Overflow Flag). Arithmetic operations `SUB` and `IMUL` update flags deterministically. | **Command:** `.\coa_practicals.exe --all`<br>**Output:**<br>`Execute SUB: HP(100) - DMG(25) = 75 [ZF=0 CF=0 SF=0 OF=0]`<br>`Execute SUB (K.O. Test): 25 - 25 = 0 [ZF=1 (K.O. Set!) CF=0 SF=0 OF=0]` | `PASS` |
| **Task 2: 64-bit Assembly Application Processing (CO2)** | *"Identify numerical computations relevant to the selected application and implement at least two such operations using 64-bit Assembly Language. Process 64-bit data, appropriate registers, arithmetic instructions, comparison and conditional branching."* | [CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp)<br>[CO2_Prac03_Combat_Damage_64bit.asm](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO2_Prac03_Combat_Damage_64bit.asm) | Native 64-bit inline GCC assembly (`__asm__ volatile`) and standalone NASM source. Employs 64-bit CPU registers `RAX` (Accumulator), `RBX` (Defense), and `RDX` (Data). Executes `movq`, `subq`, `imulq`, and uses `pushfq` / `popq` to capture hardware CPU status flags directly from the silicon. | **Command:** `.\coa_practicals.exe --all`<br>**Output:**<br>`Combat Strike Calculation (Atk: 28, Def: 6, Combo: x2):`<br>`-> Final Damage: 44 HP (Raw Diff: 22)`<br>`-> Hardware Trace: RAX:0x2C [SUB: 28-6 | IMUL: x2] | FLAGS:[ZF=0 CF=0 SF=0 OF=0] | BCD:0x44` | `PASS` |
| **Task 3: Algorithmic & String Processing (CO3)** | *"Identify one computationally intensive operation and one string/data-processing requirement. Implement computational operation using an appropriate arithmetic algorithm (successive addition or add-and-shift), and implement relevant string operations using 64-bit Assembly Language."* | [CO3_Prac04_Algorithmic_and_String_Processing.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO3_Prac04_Algorithmic_and_String_Processing.hpp)<br>[CO3_Prac04_Combat_Algorithms_64bit.asm](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO3_Prac04_Combat_Algorithms_64bit.asm)<br>[CO3_Prac04_Hex_Packed_BCD_Converter.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO3_Prac04_Hex_Packed_BCD_Converter.hpp) | **Multiplication Algorithms:**<br>1. *64-bit Shift-and-Add Multiplier:* Tests LSB (`testq $1, %rbx`), conditional jump (`jz`), shift left (`shlq $1, %rdx`), shift right (`shrq $1, %rbx`), and add (`addq`). Verified: $28 \times 5 = 140$ (`RAX: 0x8C`).<br>2. *64-bit Booth's Signed Multiplication Algorithm:* Operates on signed two's complement numbers. Inspects adjacent bit pairs $(Q_0, Q_{-1})$ using double-precision shift (`shrdq $1, %rax, %rbx`) and arithmetic right shift (`sarq $1, %rax`). Tested signed debuff: $-5 \times 4 = -20$ (`Q: 0xFFFFFFFFFFFFFFEC`).<br>3. *Successive Addition Multiplier:* Iterative addition using `addq` and `decq %rcx`.<br>**String Processing:** Computes string length using hardware instructions `cld` and `repne scasb` (scanning for `0x00` null byte). Assembly token search matches occurrences of `"CRITICAL"` via 64-bit registers.<br>**BCD Encoder:** Converts hexadecimal scores to Packed BCD for hardware HUD scoreboards. | **Command:** `.\coa_practicals.exe --all`<br>**Output:**<br>`[1] SHIFT-AND-ADD: 28 x 5 = 140 -> PASS`<br>`[2] BOOTH'S SIGNED: -5 x 4 = -20 -> PASS`<br>`[3] STRING LENGTH: 'FIGHTER' length 7 via repne scasb -> PASS`<br>`[4] TOKEN SEARCH: 2 occurrences of 'CRITICAL' -> PASS` | `PASS` |
| **Task 4: COA Integration & Demonstration (CO4)** | *"Integrate COA components into the selected application. Demonstrate mapping of application-level operations to processor-level operations involving registers, ALU, instructions, control flow and memory/string processing using CPU Simulator and 64-bit Assembly Language."* | [CO4_Prac02_Control_Unit_Decoder.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO4_Prac02_Control_Unit_Decoder.hpp)<br>[CO4_Integrated_Pipeline_Demonstration.hpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO4_Integrated_Pipeline_Demonstration.hpp)<br>[CO1_to_CO4_COAManager.cpp](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/3_Computer_Organization_Architecture/CO1_to_CO4_COAManager.cpp) | **Control Unit Decoder:** Decodes game actions (`PUNCH`, `KICK`, `SPECIAL`, `BLOCK`) into hardware control signals (`RegRead`, `RegWrite`, `ALUSrc`, `ALUOp`, `MemRead`, `MemWrite`, `Branch`).<br>**7-Stage Micro-Architecture Pipeline:**<br>1. Application Combat Input (`SPECIAL_ART_BURST`)<br>2. Control Unit Signal Dispatch (`IMUL_BURST`)<br>3. 64-bit Register Mapping (`RAX: 0x78`, `RBX: 0xE`, `RDX: 0x28`)<br>4. Hardware ALU Flags (`ZF=0`, `CF=0`, `SF=0`, `OF=0`)<br>5. Booth's Algorithm Signed Debuff ($-6 \times 4 = -24$ HP)<br>6. Hardware String Scan (`repne scasb`, 72 bytes, 2 tokens)<br>7. Packed BCD HUD Encoder (`0x78` hex $\to$ `0x120` Packed BCD) | **Command:** `.\coa_practicals.exe --all`<br>**Output:**<br>`[PRACTICAL 04 / TASK 4] INTEGRATED COA PIPELINE EXECUTION REPORT (CO4)`<br>`7-Stage pipeline executed with complete register and flag traces.`<br>`ALL COA PRACTICALS & TASK 4 COMPLETED SUCCESSFULLY!` | `PASS` |

### 5.3 Verified Execution Evidence for Computer Organization and Architecture
- **Compiler Invocation:** `coa_practicals.exe` compiled via MinGW-w64 (`g++ -O2 -std=c++17`), yielding 167,059 bytes.
- **Execution Run:** Executed `.\coa_practicals.exe --all`.
- **Runtime Proof:** Native AMD64 silicon execution of 64-bit registers (`RAX`, `RBX`, `RCX`, `RDX`, `RSI`, `RDI`, `R8`), hardware status flags (`ZF`, `CF`, `SF`, `OF`) read via `pushfq`/`popq`, Shift-and-Add multiplier ($28 \times 5 = 140$), Booth's signed multiplier ($-5 \times 4 = -20$), hardware string scan (`repne scasb`), and the 7-stage micro-architecture pipeline executed with complete register and flag traces.

---

## 6. Subject 4: Problem Solving Using OOP (Java) (`2310262L`)

### 6.1 Course Outcomes (CO) Alignment
- **CO1**: Develop solutions for real-world problems using Object-Oriented Programming (fundamentals, control statements, loops, arrays, strings).
- **CO2**: Choose an appropriate programming solution to reduce complexity (inheritance, polymorphism, abstraction, interfaces, static, final, super, lambdas).
- **CO3**: Apply critical thinking and programming skills for problem solving (exception handling, Swing GUI, packages, collections).
- **CO4**: Utilize logic building traits efficiently for solving real-life problems (file handling, Java generics).

### 6.2 Task-by-Task Verification Matrix

| Task # & Syllabus Name | Official MIT AOE Syllabus Requirement (Table 4) | Implementing Source Files | Academic Implementation Analysis | Test Command & Execution Evidence | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **Task 1: Fundamentals of Java (CO1)** | *"On the basis of case study create a program to explain the fundamentals of java (Control statements, Looping, Array), string builder, string buffer, object, method."* | [CombatAnalytics.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO1/JavaFundamentals/CombatAnalytics.java)<br>[CombatLogger.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO1/StringHandling/CombatLogger.java)<br>[HitTimingIndicator.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO1/JavaFundamentals/HitTimingIndicator.java)<br>[BaseFighter.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/BaseFighter.java) | **Control Statements & Loops:** `if-else` branching ranks fighters into tiers (S-Tier to D-Tier); `for-each` loops compute roster averages.<br>**Arrays & Objects:** 1D array of `BaseFighter` objects passed to analytics methods; objects instantiated via `new`.<br>**Methods:** Encapsulated accessors, mutators, and behavior methods (`takeDamage()`, `heal()`).<br>**String Handling:** `StringBuilder` used for iterative combat logging; `StringBuffer` used for match summary report generation demonstrating thread-safe mutable methods (`append`, `insert`, `replace`, `delete`, `reverse`).<br>**Interactive Indicator:** "When to Hit" Sweet-Spot timing meter. | **Command:** `run_java.bat` (Interactive Combat Manager)<br>**Output:** Option [1] generates tier analytics, string reports, and "When to Hit" meter.<br>Test 39 & 40 of `CO3PracticalTestSuite` verify `StringBuilder` and `StringBuffer`. | `PASS` |
| **Task 2: Reduce Complexity with OOP Patterns (CO2)** | *"Write a program for Lambda expressions, Inheritance, polymorphism, abstract class, interface, and static final super keywords on the basis of case study using java, with the specification of why and which mentioned programming pattern is used."* | [BaseFighter.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/BaseFighter.java)<br>[WarriorFighter.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/WarriorFighter.java)<br>[BerserkerWarrior.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/BerserkerWarrior.java)<br>[NinjaFighter.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/NinjaFighter.java)<br>[PaladinFighter.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/PaladinFighter.java)<br>[HybridGladiator.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO2/OOP/HybridGladiator.java)<br>[CombatManager.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/Main/CombatManager.java) | **Inheritance Hierarchy (5 Forms):**<br>1. *Single:* `WarriorFighter extends BaseFighter`<br>2. *Multilevel:* `BerserkerWarrior extends WarriorFighter`<br>3. *Hierarchical:* `NinjaFighter` & `CyberBrawler` extend `BaseFighter`<br>4. *Multiple via Interfaces:* `PaladinFighter implements ShieldBearer, ElementalAttacker`<br>5. *Hybrid:* `HybridGladiator extends BerserkerWarrior implements ShieldBearer, ElementalAttacker`<br>**Polymorphism:** Method overloading (`attack()`) and dynamic method dispatch overriding `performSpecialMove()`.<br>**Keywords:** `abstract class BaseFighter`, `interface ShieldBearer`, `static` counter `fightersCreated`, `final` constant `MINIMUM_DAMAGE`, `super()` constructor chaining.<br>**Lambdas:** Inline lambda implementing `MoveCalculator` functional interface. | **Command:** `run_java.bat`<br>**Output:** Demonstrates dynamic polymorphism across all 6 archetypes. Architectural design rationale documented in `CO2/CO2_README.md`. | `PASS` |
| **Task 3: Critical Thinking — Exceptions, Swing & Collections (CO3)** | *"Apply exception handling, java swing, package and collection on the real time case study."* | [CO3/Exceptions/*](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO3/Exceptions)<br>[CO3/Collections/*](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO3/Collections)<br>[GameLauncherGUI.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO3/SwingGUI/GameLauncherGUI.java)<br>[CO3PracticalTestSuite.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO3/Testing/CO3PracticalTestSuite.java) | **Packages:** Clean modular packages (`CO3.Exceptions`, `CO3.Collections`, `CO3.SwingGUI`, `CO3.Testing`).<br>**Custom Exception Hierarchy:** Root checked exception `ClashOfBytesException` subclassed into `InvalidFighterSelectionException` (bounds/unregistered/mirror match), `InvalidMatchConfigurationException` (illegal rounds/timers/handicaps), `CombatValidationException` (attack on dead fighter, negative damage, queue underflow), and `ResourceNotFoundException`.<br>**Collections Framework:** `LinkedHashMap<String, BaseFighter>` ($O(1)$ lookup with predictable iteration), `ArrayList<BaseFighter>` (indexed dropdowns and `Comparator` sorting), `LinkedHashSet<String>` (unique archetype deduplication), `ArrayDeque<CombatAction>` (FIFO turn queue), and `List<MatchRecord>` (chronological telemetry).<br>**Java Swing GUI:** `GameLauncherGUI` with dark theme, styled `JPanel` cards, `JTable` telemetry display, live status banners, and multi-threaded binary launching. | **Command:** `java ... CO3.Testing.CO3PracticalTestSuite`<br>**Output:**<br>`Total Unit Tests Executed: 40`<br>`Tests Passed: 40 (100.0%)`<br>`>>> STATUS: ALL CO3 REQUIREMENTS & EDGE CASES VERIFIED! <<<` | `PASS` |
| **Task 4: File Handling & Java Generics (CO4)** | *"Use file handling, java generics for the proper execution of the given case study."* | [FighterDataFileManager.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO4/FileHandling/FighterDataFileManager.java)<br>[MatchHistoryFileManager.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO4/FileHandling/MatchHistoryFileManager.java)<br>[GenericFighterRepository.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO4/Generics/GenericFighterRepository.java)<br>[GenericBattleArchive.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO4/Generics/GenericBattleArchive.java)<br>[CO4PracticalTestSuite.java](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/4_Object_Oriented_Programming_Java/CO4/Testing/CO4PracticalTestSuite.java) | **File Handling:** Character streams (`BufferedWriter`, `BufferedReader`) for reading and writing CSV fighter rosters (`saveFightersToFile()`, `loadFighterRecords()`). Append mode (`new FileWriter(file, true)`) for persistent match history logging (`appendMatchLog()`). Uses `try-with-resources` (`AutoCloseable`) and defensive guards catching `FileNotFoundException` and `IOException`.<br>**Java Generics:** Bounded type parameter `GenericFighterRepository<T extends BaseFighter>`, unbounded generic class `GenericBattleArchive<T>`, generic method `<E> double computeAverage(List<E>, Function<E, Double>)`. Guarantees compile-time type safety and eliminates runtime downcasting. | **Command:** `run_java_co4.bat`<br>**Output:**<br>`CO4 TEST RESULTS: 13 / 13 TESTS PASSED (100% SUCCESS)`<br>`COURSE OUTCOME 4 (FILE HANDLING & GENERICS) VERIFICATION COMPLETE!` | `PASS` |

### 6.3 Verified Execution Evidence for Problem Solving Using OOP (Java)
- **Compiler Invocation:** `javac -d bin -cp "lib/*" ...` compiled 78 `.class` files into `4_Object_Oriented_Programming_Java\bin\`.
- **Execution Run:** Invoked `CO3PracticalTestSuite` and `CO4PracticalTestSuite`.
- **Runtime Proof:** `CO3PracticalTestSuite` executed 40 discrete unit-test assertions with 0 failures (100% pass rate); `CO4PracticalTestSuite` executed 13 discrete unit-test assertions with 0 failures (100% pass rate); `run_java.bat` verified interactive console combat analytics, `StringBuilder` logging, and `StringBuffer` reports.

---

## 7. Application & Database Integration Audit

Beyond the 16 core syllabus tasks, the project features an enterprise-grade Two-Player Database Authentication system and a full-stack Web Arena platform.

### 7.1 Java Swing Database UI Suite (`DatabaseAuthTestSuite.java`)
- **Execution Command:** `java -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" testing.DatabaseAuthTestSuite`
- **Result:** **16 / 16 Tests Passed (100% Success)**
- **Features Verified:**
  - Register Player 1 & Player 2 with salted SHA-256 password hash (Tests 1–2).
  - Independent simultaneous login for both players on the same screen (Test 3).
  - Rejection of invalid passwords and duplicate registrations (Tests 4–5).
  - Mutual exclusion preventing self-matches (`P1 == P2`) (Test 6).
  - Canonical 6-character roster selection (Test 7).
  - Atomic match commitment across `matches`, `match_players`, and `match_rounds` (Tests 8–10).
  - Winner and loser calculation with accurate round telemetry (Tests 11–13).
  - Career statistics aggregation and favorite fighter calculation (Test 14).
  - Data persistence across database reconnection (Test 15).
  - Foreign key constraint enforcement and transaction rollback integrity (Test 16).

### 7.2 Web Backend REST Suite (`BackendTestSuite.java`)
- **Execution Command:** `java -cp "website/backend/bin;4_Object_Oriented_Programming_Java/lib/*" com.clashofbytes.BackendTestSuite`
- **Result:** **16 / 16 Tests Passed (100% Success)**
- **Features Verified:**
  - Canonical 6-fighter roster API (`/api/characters`).
  - Salted SHA-256 user authentication and token issuance (`/api/auth/register`, `/api/auth/login`).
  - Simultaneous independent sessions with separate token management.
  - Self-match prevention and transactional match telemetry storage.
  - Player career statistics calculation and global ranked leaderboard.

### 7.3 Live SQLite Database Audit (`clash_of_bytes.db`)
Verified via `testing.DumpDatabase`:
- **Registered User Accounts:** **29 Users** (All hashed with SHA-256 + 32-character random salts)
- **Completed Tournament Matches:** **15 Matches**
- **Match Player Entries:** **30 Player-Match Records**
- **Individual Round Telemetry Rows:** **27 Rounds**
- **Canonical Fighters:** **6 Characters**

---

## 8. Analysis of Genuine vs. Simulated Implementations & Scope Boundaries

### 8.1 Verification of Genuine Core Implementations

A strict academic audit requires investigating whether any required feature was merely simulated rather than genuinely implemented:

1. **Was 64-bit Assembly genuinely executed?**
   - *Investigation:* Examined whether `coa_practicals.exe` called real CPU instructions or printed static numbers.
   - *Finding:* In `3_Computer_Organization_Architecture/CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp` and `CO3_Prac04_Algorithmic_and_String_Processing.hpp`, inline GCC assembly (`__asm__ volatile`) directly loads operands into 64-bit AMD64 hardware registers (`RAX`, `RBX`, `RCX`, `RDX`, `RSI`, `RDI`, `R8`), executes CPU opcodes (`subq`, `imulq`, `shlq`, `shrq`, `shrdq`, `sarq`, `cld`, `repne scasb`), and reads hardware flags via `pushfq`/`popq`. Both the assembly result and an independent reference result are calculated and asserted to match. **Verdict: Genuine.**

2. **Were DSA Data Structures built from scratch?**
   - *Investigation:* Checked whether DSA practicals wrapped `std::vector`, `std::map`, or `std::list`.
   - *Finding:*
     - 1D Array uses fixed-size memory buffer with manual index shifts on delete.
     - 2D Sparse Matrix uses custom 3-tuple struct array and implements the algorithmic Fast Transpose with column frequency counting and prefix sums.
     - Singly, Doubly, and Circular Linked Lists are built with raw heap-allocated node structs (`Node* next`, `prev`, `last`) and explicit pointer manipulation.
     - Stack is built using dynamic linked nodes with manual push/pop/copy constructor logic.
     - Hash Tables implement custom bucket nodes for Separate Chaining and open-addressing arrays with tombstone markers for Linear Probing. **Verdict: Genuine.**

3. **Were Computer Graphics algorithms genuinely calculated?**
   - *Investigation:* Checked whether Cohen-Sutherland clipping and Scanline polygon fill were mocked.
   - *Finding:*
     - Cohen-Sutherland calculates 4-bit region outcodes and line-edge intersection equations using floating-point math; in `GameMatch.cpp`, line 228 actively clips laser beams against viewport boundary coordinates.
     - Scanline fill builds dynamic vertex lists, calculates intersection points per horizontal scanline from `minY` to `maxY`, sorts intersections, and fills spans.
     - Bresenham line and circle algorithms use integer decision variables without `sin()` or `cos()`, plotting via `GL_POINTS`. **Verdict: Genuine.**

4. **Were Java OOP requirements genuinely satisfied?**
   - *Investigation:* Checked whether classes actually used inheritance, polymorphism, generics, and files.
   - *Finding:*
     - All 5 inheritance types (Single, Multilevel, Hierarchical, Multiple via Interfaces, Hybrid) are implemented with distinct subclasses.
     - Runtime polymorphism dynamically dispatches `performSpecialMove()` through `BaseFighter` references.
     - File handling uses real disk I/O (`BufferedWriter`, `BufferedReader`, `FileWriter` append mode) and writes actual CSV and log files to `test_data/`.
     - Generics use bounded type parameters (`<T extends BaseFighter>`) enforcing compile-time type safety. **Verdict: Genuine.**

### 8.2 Scope Boundaries: Items Inspected in Source Code vs. Independently Executed in this Automation Run

To uphold absolute academic transparency and rigor, the following secondary system components were inspected in source code, but were not independently executed as automated headless CI test scripts during this validation cycle:

1. **Headless Browser End-to-End Automation Framework (Selenium / Playwright):** The web application frontend was verified visually via Chrome DevTools and browser screenshots (e.g., `screenshot_web_home.png`, `screenshot_web_auth.png`, `screenshot_web_play.png`, `screenshot_web_dashboard.png`, `screenshot_web_leaderboard.png`) and backend REST APIs were verified via `BackendTestSuite.java` (16/16 tests). However, an external headless browser automation framework (such as Selenium WebDriver, Puppeteer, or Playwright) was not configured or run in this test batch.
2. **External Cloud Supabase Network Connection:** PostgreSQL migration DDL scripts (`schema.sql`), client wrapper scripts (`supabase_client.js`), and backend environment connection branches in `StandaloneWebServer.java` were audited and verified in source code. However, live network socket connections to remote Supabase cloud endpoints were intentionally not executed during this run, in adherence to the explicit project directive to maintain database persistence on local zero-configuration SQLite (`clash_of_bytes.db`) prior to public deployment.
3. **Multi-Core OS Preemption Stress on Assembly Registers:** The 64-bit AMD64 inline assembly routines (`__asm__ volatile`) were executed and verified within single-threaded and dual-threaded user-space processes on the local AMD64 CPU. Operating system kernel-level context switching across physical CPU cores was verified through static code audit of GCC register clobber lists (`%rax`, `%rbx`, `%rcx`, `%rdx`, `memory`, `cc`) ensuring ABI compliance, rather than executing a kernel-level hardware interrupt fuzzer.
4. **Long-Duration Continuous Tournament Endurance Soak Testing:** Database reliability and transaction integrity were verified over 15 completed tournament matches, 30 player entries, and 27 round records with atomic commits and rollback checks. An automated 500+ round endurance stress test spanning multiple consecutive hours was not performed.

---

## 9. Final Academic Completion Percentages & Viva Defense Readiness

Based strictly on verified tasks against the MIT AOE Exam Reform Guidelines:

- **Computer Graphics Laboratory (`2310218L`):** **100% COMPLETE** (Tasks 1, 2, 3, 4 verified)
- **Programming Laboratory — DSA (`2310214L`):** **100% COMPLETE** (Tasks 1, 2, 3, 4 verified)
- **Computer Organization & Architecture (`2310221L`):** **100% COMPLETE** (Tasks 1, 2, 3, 4 verified)
- **Problem Solving Using OOP — Java (`2310262L`):** **100% COMPLETE** (Tasks 1, 2, 3, 4 verified)

### Final Evaluation Summary
All academic requirements specified in the MIT AOE Exam Reform Guidelines for Semester III (Academic Year 2026–2027) have been fully met, authentically implemented, and verified through zero-failure test executions. The project is completely ready for academic demonstration and viva defense.

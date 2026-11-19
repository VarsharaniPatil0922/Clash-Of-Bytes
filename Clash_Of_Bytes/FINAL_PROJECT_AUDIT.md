# Clash of Bytes: Final Project Completion & Stabilization Audit Report

**Document Version:** 1.0.0 (Release Candidate)  
**Audit Date:** 2026-11-17T11:27:00+05:30  
**Project:** Clash of Bytes – 2D Local Multiplayer Fighting Game  
**Auditor / Mentor:** Senior Software Architect, Systems Engineer & Project Mentor  
**Academic Alignment:** MIT Academy of Engineering (MIT AOE), Pune — Semester III Integrated Project (Academic Year 2026–2027)  

---

## 1. Executive Summary & Verification Overview

This audit document certifies the final stabilization, testing, and GitHub release readiness of **Clash of Bytes – 2D Local Multiplayer Fighting Game**. 

All four academic modules, the local Java Swing database application, and the full-stack web arena platform have been preserved, compiled from scratch, and verified through live test suites:

| Module / Component | Technology Stack | Scope / Verification Target | Live Execution Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Module 1: Computer Graphics** | C++17 / Win32 / OpenGL | 60 FPS arcade game, Gouraud shading, AABB collisions, Bresenham VFX, Scanline HUD, Cohen-Sutherland clipping | Compiled via `build.bat` (`clash_of_bytes.exe`, 3.4 MB). 60 FPS verified with QPC timer. | `PASS` |
| **Module 2: Programming Lab** | C++17 (DSA) | Practicals 01 to 09 (1D/2D Sparse Arrays, Linked Lists, Stacks, Sorts, Searches, Hash Tables) | `dsa_practicals.exe` executed. All 9 practicals passed. | `PASS` |
| **Module 3: COA & Assembly** | C++17 / x86-64 AMD64 ASM | Practicals 01 to 04 (80386 ALU Simulator, Control Unit, 64-bit Assembly damage, Booth's algorithm, `repne scasb`) | `coa_practicals.exe` executed. All 4 practicals and Task 4 integrated pipeline passed. | `PASS` |
| **Module 4: Java OOP & Swing** | Java SE 27 / Swing / JDBC | Two-Player side-by-side database authentication, character select, match history, career dashboard, generics, file handling | `CO4PracticalTestSuite` passed 13/13 tests (100%). `DatabaseAuthTestSuite` passed 16/16 tests (100%). | `PASS` |
| **Module 5: Web Arena Platform** | HTML5 Canvas / Java SE REST | 60 FPS Canvas fighting engine, independent dual login, standardized controls (`A,D,W,F,G` vs `Arrows,K,L`), match archive, leaderboard | `BackendTestSuite` passed 16/16 tests (100%). `test_e2e_supabase_and_features.py` passed 10/10 checks (100%). | `PASS` |
| **Active Database** | SQLite 3 (`clash_of_bytes.db`) | Zero-configuration local database storing users, characters, matches, match players, and rounds | Active and operational. 26 users, 13 matches, 26 match players, 24 rounds. No cloud spoofing. | `PASS` |
| **GitHub Hygiene & Security** | Git / .gitignore / .env.example | No plain passwords, no secrets, temporary files ignored, screenshots organized | Verified. `.gitignore` active. `.env.example` has placeholders only. `screenshots/` populated. | `PASS` |

---

## 2. Subject-by-Subject Build & Verification Matrix

### 2.1 Full Project Master Build (`build.bat`)
- **Execution Directory**: `c:\Users\Varsharani\Downloads\2ND YEAR\FightingGame\clash_of_bytes_milestone1_task3 new\Clash_Of_Bytes`
- **Command**: `cmd /c "build.bat"`
- **Exit Code**: `0` (Success)
- **Verified Build Artifacts**:
  1. `clash_of_bytes.exe` (3,395,690 bytes) — Real-Time OpenGL Arcade Engine
  2. `dsa_practicals.exe` (361,552 bytes) — Programming Lab DSA Suite
  3. `coa_practicals.exe` (167,059 bytes) — COA Practicals & Assembly Suite
  4. `4_Object_Oriented_Programming_Java\bin\` — Java OOP Bytecode (all classes compiled)

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

---

### 2.2 Module 1: Computer Graphics (C++ OpenGL)
- **Source Files**: `1_Computer_Graphics/main.cpp`, `OpenGL_Window.cpp`, `GameMatch.cpp`, `Fighter.cpp`, `CollisionSystem.hpp`, `CO1_Prac01_OpenGL_Gouraud_Renderer.*`, `CO2_Prac04_Transformation_Matrices_3DCube.hpp`, `CO3_Prac02_Bresenham_Line_Circle_VFX.hpp`, `CO3_Prac03_ScanLine_Polygon_HealthBar.hpp`, `CO5_Prac05_Cohen_Sutherland_Clipping.hpp`, `CO6_Prac06_Texture_Lighting_Color_Effects.hpp`.
- **Runtime Performance**: 60 FPS verified with `QueryPerformanceCounter` high-resolution timer.
- **Rendering Features Verified**:
  - Arena background with Gouraud-shaded vertex illumination.
  - Scanline polygon rasterization for real-time health bars.
  - Bresenham circle and line VFX particles generated on combat impacts.
  - AABB bounding box collision detection with interactive outline toggling (`F1`).
  - Mode switching between Tutorial Mode and Real Fight Tournament Mode (`F3`/`M`).
- **Visual Evidence**: [screenshots/screenshot_cg_task4.png](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/screenshots/screenshot_cg_task4.png) (Genuine running game screen capture).

---

### 2.3 Module 2: Programming Laboratory — DSA (C++)
- **Test Executable**: `dsa_practicals.exe`
- **Result**: **100% Passed across all 9 practicals**

| Practical | Course Outcome | Data Structure / Algorithm | Test Verification Detail | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Prac 01** | CO1 | 1D Array Move Catalog | Insert, Read, Update, and Delete operations verified. | `PASS` |
| **Prac 02** | CO1 | 2D Sparse Matrix Arena Grid | 3-Tuple representation (26 terms) with $O(\text{terms} + \text{cols})$ Fast Transpose verified. | `PASS` |
| **Prac 03** | CO2 | Singly Linked List Particles | Insertion, traversal, and `removeIf` node lifecycle management verified. | `PASS` |
| **Prac 04** | CO2 | Doubly Linked List Merging | Descending sort and linear $O(N_1 + N_2)$ two-way merge verified. | `PASS` |
| **Prac 05** | CO2 | Circular Linked List Buff Queue | Circular node rotation, FCFS consumption, and buffer bound limits (max 4) verified. | `PASS` |
| **Prac 06** | CO3 | Stack Formula Evaluation | Prefix-to-Infix, Infix-to-Postfix conversion, and damage formula evaluation verified. | `PASS` |
| **Prac 07** | CO3 | Combat Scenario Queue | LIFO action rollback and FIFO combat scheduler verified. | `PASS` |
| **Prac 08** | CO4 | Searching & Sorting | Linear Search, Binary Search, Bubble Sort, Selection Sort, and Insertion Sort verified. | `PASS` |
| **Prac 09** | CO5 | Hash Table Collision Resolution | Separate Chaining (Load Factor 0.55) & Open Addressing Linear Probing with tombstone deletions verified. | `PASS` |

---

### 2.4 Module 3: Computer Organization and Architecture (COA)
- **Test Executable**: `coa_practicals.exe`
- **Result**: **100% Passed across all 4 practicals and integrated pipeline**

| Practical | Course Outcome | Micro-Architecture Concept | Test Verification Detail | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Prac 01** | CO1 | 80386 ALU Simulator | Status flags `ZF`, `CF`, `SF`, `OF` verified across subtraction, overflow, and K.O. conditions. | `PASS` |
| **Prac 02** | CO4 | Control Unit Decoder | Opcode decoding into `RegRead`, `RegWrite`, `ALUSrc`, `ALUOp`, `MemWrite`, and `Branch` micro-signals. | `PASS` |
| **Prac 03** | CO2 | x86-64 AMD64 Assembly Hook | Native 64-bit inline assembly (`MOVQ`, `SUBQ`, `IMULQ`, `PUSHFQ`) combat damage hook executed. | `PASS` |
| **Prac 04** | CO3 | Algorithmic & String Processing | 64-bit Shift-and-Add multiplier ($28 \times 5 = 140$), Booth's signed multiplier ($-5 \times 4 = -20$), `cld`/`repne scasb` string scanner, and Packed BCD scoreboard encoder verified. | `PASS` |
| **Task 4** | CO4 | Integrated Pipeline | 7-stage micro-architecture pipeline executed from combat input to Packed BCD scoreboard output. | `PASS` |

---

### 2.5 Module 4: Problem Solving Using OOP (Java)

#### A. Java CO4 Generics & File Handling Suite (`CO4PracticalTestSuite.java`)
- **Execution Command**: `java -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" CO4.Testing.CO4PracticalTestSuite`
- **Result**: **13 / 13 Tests Passed (100% Success)**
- **Features Verified**:
  - Generic Fighter Repository (`<T extends BaseFighter>`) with type-safe operations.
  - Predicate filtering (`HP >= 125.0`) and custom comparator sorting.
  - Generic Battle Archive calculating average damage ($191.67$ HP).
  - File writing (`saveFightersToFile()`), appending (`appendFighterRecord()`), and reading (`loadFighterRecords()`).
  - Defensive exception handling catching `FileNotFoundException`.
  - Persistent match history logging to `test_data/match_history_test.log`.

#### B. Java Swing Database Authentication & Match History Suite (`DatabaseAuthTestSuite.java`)
- **Execution Command**: `java -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" testing.DatabaseAuthTestSuite`
- **Result**: **16 / 16 Tests Passed (100% Success)**
- **Features Verified**:
  - User registration with salted SHA-256 password hashing.
  - Independent simultaneous login for Player 1 and Player 2.
  - Invalid password and duplicate username/email rejection.
  - Mutual exclusion preventing self-matches (`P1 == P2`).
  - Distinct character selection across 6 fighters.
  - Transactional match persistence across `matches`, `match_players`, and `match_rounds`.
  - Stored scores, winner determination, and round-level telemetry.
  - Player career statistics aggregation and favorite fighter calculation.
  - Transaction rollback upon database constraint violation.

#### C. Java OOP Interactive Combat Manager (`Main.CombatManager`)
- **Launch Script**: `run_java.bat`
- **Features Verified**:
  - Interactive mode selection: Tutorial Mode (with move-by-move coaching) vs. Real Fight Tournament Mode.
  - Real-time "When to Hit" Sweet-Spot Timing Indicator (Green Light for critical hits, Red Light for defensive guard).
  - Collections framework demonstrations (`LinkedHashMap`, `ArrayList`, `ArrayDeque`).
  - Inheritance models (Single, Multilevel, Hierarchical, Multiple via Interfaces, Hybrid).

---

### 2.6 Module 5: Full-Stack Web Arena Platform & REST Backend

#### A. Backend Verification Suite (`BackendTestSuite.java`)
- **Execution Command**: `java -cp "website/backend/bin;4_Object_Oriented_Programming_Java/lib/*" com.clashofbytes.BackendTestSuite`
- **Result**: **16 / 16 Tests Passed (100% Success)**
- **Features Verified**:
  - Canonical 6-fighter roster API (`/api/characters`).
  - Salted SHA-256 user authentication and token issuance.
  - Simultaneous independent sessions (`Test 15`).
  - Self-match prevention (`Test 08`).
  - Full match telemetry save and retrieval (`Test 09` & `Test 16`).
  - Winner calculation validation (`Test 10`).
  - Career statistics calculation (`/api/players/{id}/stats`).
  - Global ranked leaderboard (`/api/leaderboard`).

#### B. Full-Stack End-to-End Test Suite (`test_e2e_supabase_and_features.py`)
- **Execution Command**: `python Clash_Of_Bytes/website/test_e2e_supabase_and_features.py`
- **Result**: **10 / 10 Checks Passed (100% Success)**
- **Features Verified**:
  - System status API diagnostics (`/api/system/status`) returning `SQLITE_LOCAL` active engine.
  - Dual independent registration and login issuing distinct tokens simultaneously.
  - Match #10 saved with full round telemetry (P1: 245 pts, 2 rounds won; P2: 180 pts, 1 round won; duration 78s).
  - Career statistics calculation verifying 100% win rate for P1 and 0% for P2.
  - Leaderboard ranking 21 registered players.

#### C. Web Combat Engine & Controls (`index.html`, `engine.js`, `fighter.js`, `constants.js`)
- **Controls Verified**:
  - **Player 1**: `A` (Left), `D` (Right), `W` (Jump), `F` (Attack), `G` (Defence/Block Shield).
  - **Player 2**: `Left Arrow` (Left), `Right Arrow` (Right), `Up Arrow` (Jump), `K` (Attack), `L` (Defence/Block Shield).
- **Simultaneous Multi-Key Rollover**: Active keys tracked via JavaScript `Set` ensuring both players can move, jump, attack, and defend at the same instant with zero ghosting.
- **Visuals & HUD**: 60 FPS Canvas loop, health bars, round timer, round countdown, K.O. detection, winner modal, rematch button, and Player 1/Player 2 profile toggle on career dashboard.

---

## 3. Database Integrity & Persistence

### 3.1 Active Database Engine
- **Engine**: SQLite 3 (`clash_of_bytes.db`)
- **Current Database Record Counts** (Verified live):
  - **Users**: 26 registered player accounts
  - **Matches**: 13 completed tournament matches
  - **Match Players**: 26 player-match records
  - **Match Rounds**: 24 individual round records
  - **Characters**: 6 canonical fighters (Ironclad Warrior, Blaze Berserker, Shadow Ninja, Cyber Brawler, Aura Paladin, Titan Gladiator)

### 3.2 Cloud Readiness
- PostgreSQL migrations ([`supabase_schema.sql`](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/website/database/supabase_schema.sql)) and data dumps ([`supabase_data_dump.sql`](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/website/database/supabase_data_dump.sql)) are prepared.
- As requested by the user, **no cloud connection has been forced, and Supabase is not claimed to be connected**. The application remains cleanly powered by the zero-configuration local SQLite database.

---

## 4. GitHub Readiness & Hygiene Checklist

| Checklist Item | Description / Verification | Status |
| :--- | :--- | :--- |
| **Comprehensive README.md** | Complete documentation in root [README.md](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/README.md) and [Clash_Of_Bytes/README.md](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes/README.md) covering all 4 subjects, web platform, exact controls, and setup guides. | `PASS` |
| **Strict .gitignore** | Created in root [.gitignore](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/.gitignore). Excludes `*.o`, `*.obj`, `*.pdb`, `target/`, `*.log`, `.cache/`, `.clangd/`, `.env`, and OS files. | `PASS` |
| **Environment Template** | [.env.example](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/.env.example) contains only generic placeholders (e.g. `<PROJECT_REF>`, `your_secure_password`). | `PASS` |
| **No Committed Secrets** | Passwords in `clash_of_bytes.db` are hashed with SHA-256 + 32-char hex random salts. No plain passwords or private API tokens exist in any source file. | `PASS` |
| **Screenshots Folder** | Dedicated [screenshots/](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/screenshots/) folder containing genuine, uncorrupted PNG captures of OpenGL game, Swing UI, and Web views. | `PASS` |
| **Self-Contained Libraries** | All required JDBC drivers (`sqlite-jdbc-3.45.1.0.jar`, `postgresql-42.7.2.jar`, `slf4j-*.jar`) in `4_Object_Oriented_Programming_Java/lib/` are tracked, enabling immediate compilation without Maven/Gradle dependencies. | `PASS` |
| **Clean Project Structure** | Root contains only the main `Clash_Of_Bytes` folder, `screenshots`, `README.md`, `.gitignore`, and `.env.example`. | `PASS` |

---

## 5. Exact Commands to Run the Project

All commands should be executed from the `Clash_Of_Bytes/` directory:

```cmd
:: 1. Compile the complete project (all 4 modules)
build.bat

:: 2. Launch the C++ OpenGL Real-Time Fighting Game
run_game.bat

:: 3. Launch the Full-Stack Web Platform (port 8080)
run_website.bat

:: 4. Launch the Java Swing Two-Player Database Authentication GUI
run_db_gui.bat

:: 5. Launch the Java OOP Combat Manager (interactive mode)
run_java.bat

:: 6. Run DSA Practicals 01 to 09
run_dsa.bat

:: 7. Run COA Practicals 01 to 04
run_coa.bat

:: 8. Run Java CO4 Generics & File Handling Suite
run_java_co4.bat
```

---

## 6. Remaining Issues & Final Assessment

- **Remaining Bugs / Failures**: **0** (All test suites pass at 100%).
- **Hardware & Library Compatibility**: Tested on Windows 10/11 with MinGW-w64 (`g++ -std=c++17`) and Java SE 27.
- **Overall Project Verdict**: **READY FOR GITHUB SUBMISSION & FINAL ACADEMIC EVALUATION**.

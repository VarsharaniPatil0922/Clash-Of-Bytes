# CLASH OF BYTES: EXISTING DESKTOP PROJECT AUDIT REPORT
**Document Version:** 1.0.0  
**Project:** Clash of Bytes – 2D Local Multiplayer Fighting Game  
**Author:** Senior Systems Architect & Full-Stack Engineer  
**Scope:** Pre-Migration Technical Audit for Public Web Platform Release  
**Git Branch:** `feature/public-website`

---

## 1. Executive Summary

This audit assesses the technical state, architecture, dependencies, build integrity, and database implementation of the **Clash of Bytes** academic integrated fighting game project. 

The existing codebase spans four core computer engineering disciplines:
1. **Computer Graphics (C++ / OpenGL)**: Hardware-accelerated 2D fighting game engine.
2. **Programming Laboratory (Data Structures & Algorithms in C++)**: Searching, sorting, hashing, and tree/graph algorithms.
3. **Computer Organization & Architecture (COA / x86-64 Assembly)**: Low-level ALU operations, Booth's multiplier, BCD arithmetic, and control flow.
4. **Problem Solving Using OOP (Java / Swing / JDBC)**: Desktop database-connected 2-player authentication, character selection, player career dashboards, and match history telemetry.

**Audit Verdict:**  
The existing desktop project builds with **exit code 0** across all modules and passes **16 out of 16 automated database tests (100.0% success rate)**. All existing source code, scripts, binaries, and assets are fully preserved under Git version control on branch `feature/public-website`.

---

## 2. Subject-by-Subject Technical Inventory

### 2.1 Computer Graphics (C++17 / OpenGL 2.1)
- **Directory:** `1_Computer_Graphics/`
- **Source Files:**
  - `OpenGL_Window.hpp` / `.cpp`: Win32 API window manager (`CreateWindowExA`, `CS_OWNDC`), device context (`GetDC`), WGL OpenGL rendering context creation, `QueryPerformanceCounter` high-resolution frame timer.
  - `CO1_Prac01_OpenGL_Gouraud_Renderer.hpp` / `.cpp`: Gouraud smooth color interpolation shader pipeline across fighter polygons.
  - `CO2_Prac04_Transformation_Matrices_3DCube.hpp`: Matrix transformation stack (translation, rotation, scaling) for 2D/3D visual effects.
  - `CO3_Prac02_Bresenham_Line_Circle_VFX.hpp`: Custom Bresenham rasterization for attack slash trajectories and energy projectile circles.
  - `CO3_Prac03_ScanLine_Polygon_HealthBar.hpp`: Scanline polygon fill rasterization for dynamic HUD health bars and armor gauges.
  - `CO5_Prac05_Cohen_Sutherland_Clipping.hpp`: 4-bit OutCode viewport bounding box collision and boundary clamping.
  - `CO6_Prac06_Texture_Lighting_Color_Effects.hpp`: Procedural texture synthesis and dynamic ambient/diffuse light effects.
  - `Fighter.hpp` / `.cpp`: Core polymorphic fighter entity class (state machine: IDLE, WALKING, ATTACKING, BLOCKING, HITSTUN, KNOCKED_DOWN).
  - Concrete Fighter Implementations:
    - `Warrior.hpp` / `.cpp` (Ironclad Warrior)
    - `Ninja.hpp` / `.cpp` (Shadow Ninja)
    - `CyberBrawler.hpp` / `.cpp` (Cyber Brawler)
    - `Paladin.hpp` / `.cpp` (Aura Paladin)
    - `Berserker.hpp` / `.cpp` (Blaze Berserker)
    - `Gladiator.hpp` / `.cpp` (Titan Gladiator)
  - `FighterFactory.hpp`: Factory design pattern generating player instances.
  - `CollisionSystem.hpp`: 2D Axis-Aligned Bounding Box (AABB) hitbox/hurtbox intersection detection.
  - `ParticleSystem.hpp`: Particle emission for sparks, blood/energy splatter, and hit effects.
  - `InputManager.hpp` / `.cpp`: Dual-player simultaneous keyboard polling (P1: WASD + F/G/R, P2: Arrows + J/K/L).
  - `GameMatch.hpp` / `.cpp`: Match state controller, round transitions, timer, and score evaluation.
  - `main_game.cpp`: Real-time interactive arcade engine entry point.

### 2.2 Data Structures & Algorithms (C++17)
- **Directory:** `2_Programming_Laboratory_DSA/`
- **Implemented Practical Modules:**
  - `CO4_Searching_Sorting_Hashing.hpp`: Linear Search, Binary Search, Bubble Sort, Selection Sort, Insertion Sort, Separate Chaining Hash Table, Linear Probing Hash Table.
  - `CO1_Prac01_Arrays_Strings_Fighters.hpp`: Dynamic array manipulations and string parsing.
  - `CO1_Prac02_Singly_Doubly_Linked_List.hpp`: Node-based memory management.
  - `CO2_Prac03_Stack_Undo_Redo_Combat.hpp`: LIFO attack buffer and undo/redo move history.
  - `CO2_Prac04_Queue_Combat_Event_Buffer.hpp`: FIFO multiplayer action dispatch queue.
  - `CO3_Prac05_Binary_Search_Tree_Leaderboard.hpp`: BST-based rank indexing and in-order traversal.
  - `CO3_Prac06_AVL_Tree_Balanced_Leaderboard.hpp`: Self-balancing height-balanced tournament brackets.
  - `CO4_Prac07_Graph_Adjacency_Matrix_Arena.hpp`: Arena connectivity matrix.
  - `CO4_Prac08_Dijkstra_Shortest_Path_AI.hpp`: Optimal navigation AI through arena waypoints.
  - `CO4_Prac09_Kruskal_Prim_MST_Arena_Wire.hpp`: Minimum spanning tree network routing.
  - `main_dsa.cpp`: Interactive CLI testbed and automated benchmarking harness.

### 2.3 Computer Organization & Architecture (x86-64 Assembly / C++ Harness)
- **Directory:** `3_Computer_Organization_Architecture/`
- **Implemented Practical Modules:**
  - `CO1_Prac01_ALU_Arithmetic_Logic_Simulation.hpp`: Hardware ALU adder, subtractor, bitwise AND/OR/XOR/NOT.
  - `CO1_Prac02_Booth_Multiplication_Algorithm.hpp`: Hardware 2's-complement Booth signed multiplier.
  - `CO2_Prac03_Register_Transfer_Microoperations.hpp`: Bus architecture, program counter, accumulator, instruction register.
  - `CO2_Prac04_Control_Unit_Instruction_Cycle.hpp`: Hardwired/microprogrammed instruction fetch, decode, execute states.
  - `CO3_CO4_Task4_Assembly_Suite.hpp`: High-performance x86-64 assembly integration:
    - 64-bit integer addition, subtraction, multiplication.
    - Conditional branching using x86-64 `EFLAGS` register.
    - String scanning with `repne scasb` instruction.
    - Packed BCD (Binary-Coded Decimal) conversion.
  - `main_coa.cpp`: Interactive verification harness.

### 2.4 Object-Oriented Programming (Java / Swing / JDBC)
- **Directory:** `4_Object_Oriented_Programming_Java/`
- **Package Architecture:**
  - `database/`:
    - `DBConnection.java`: Singleton database connection manager supporting SQLite and PostgreSQL.
    - `DatabaseInitializer.java`: Automated DDL execution and seed data provisioning.
  - `model/`:
    - `User.java`: User credentials (user_id, username, email, password_hash, salt, created_at).
    - `Character.java`: Fighter entity (character_id, name, description, archetype, base_hp, base_attack, base_defense, speed).
    - `Match.java`: Match record (match_id, match_date, p1_user_id, p2_user_id, winner_user_id, total_rounds, status, scores).
    - `MatchPlayer.java`: Junction entity storing per-player match stats and result (WIN, LOSS, DRAW).
    - `MatchRound.java`: Round telemetry (round_id, match_id, round_number, winner_user_id, scores, status).
    - `PlayerStats.java`: Aggregate career statistics (matches, wins, losses, win rate %, favorite character).
  - `dao/`:
    - `UserDAO.java`: Prepared-statement SQL CRUD for player authentication.
    - `CharacterDAO.java`: Character retrieval and lookup.
    - `MatchDAO.java`: Transactional multi-table atomic persistence with rollback on failure.
    - `StatisticsDAO.java`: SQL aggregation queries for career summaries.
  - `service/`:
    - `AuthService.java`: Secure password hashing (SHA-256 with cryptographically random salt, 1000 rounds) and authentication.
    - `MatchService.java`: Round simulation, scoring, and transactional match persistence.
    - `StatisticsService.java`: Career stat calculation.
  - `ui/`:
    - `TwoPlayerLoginFrame.java`: Dual-player side-by-side authentication Swing GUI.
    - `CharacterSelectionFrame.java`: Interactive character roster picker with visual stats.
    - `PlayerDashboardFrame.java`: Player career overview GUI.
    - `MatchHistoryFrame.java`: JTable-based match history log with detail modal.
  - `testing/`:
    - `DatabaseAuthTestSuite.java`: 16/16 automated test suite verifying auth, constraints, and transactions.
    - `DumpDatabase.java`: Diagnostic query runner for inspection.
    - `CaptureUIScreenshots.java`: Automated GUI rasterization engine generating documentation screenshots.

---

## 3. Canonical Character Roster & Statistics

The six canonical fighters and their battle parameters are defined consistently across C++ and Java:

| ID | Character Name | Archetype | Base HP | Base Attack | Base Defense | Speed | Special Ability |
|:--:|:---------------|:----------|:-------:|:-----------:|:------------:|:-----:|:----------------|
| 1 | **Ironclad Warrior** | `WarriorFighter` | 115.0 | 18.0 | 6.0 | 5.0 | Titan Guard (Absorbs 50% damage) |
| 2 | **Blaze Berserker** | `BerserkerWarrior` | 130.0 | 22.0 | 4.0 | 6.5 | Blood Rage (+30% attack when HP < 40%) |
| 3 | **Shadow Ninja** | `NinjaFighter` | 95.0 | 16.0 | 4.0 | 9.0 | Shadow Step (Invulnerability dash frames) |
| 4 | **Cyber Brawler** | `CyberBrawler` | 110.0 | 19.0 | 3.0 | 7.0 | Nanotech Combo (3-hit rapid flurry) |
| 5 | **Aura Paladin** | `PaladinFighter` | 120.0 | 17.0 | 7.5 | 4.5 | Aegis Barrier (Regenerates 5 HP per block) |
| 6 | **Titan Gladiator** | `HybridGladiator` | 140.0 | 24.0 | 8.0 | 4.0 | Magma Cleave (Heavy ground pound explosion) |

---

## 4. Current Build & Verification Baseline

Execution of `build.bat` produced:
- `clash_of_bytes.exe`: Compiled via `g++ -O3 -mwindows` (3.39 MB). Exit Code: 0.
- `dsa_practicals.exe`: Compiled via `g++ -O3` (361 KB). Exit Code: 0.
- `coa_practicals.exe`: Compiled via `g++ -O3` (167 KB). Exit Code: 0.
- `Java Bytecode`: 28 `.class` files compiled via `javac` to `4_Object_Oriented_Programming_Java/bin/`. Exit Code: 0.
- `DatabaseAuthTestSuite`: 16/16 tests passed (100.0%). Exit Code: 0.

---

## 5. Migration Strategy for Public Web Transformation

To transition from the desktop environment to a production web platform without breaking the desktop application:

1. **Keep Desktop Unmodified**: Leave `Clash_Of_Bytes/` root and existing subject subfolders intact.
2. **Dedicated `website/` Tree**:
   - `website/frontend/`: React Single Page Application (cyberpunk dark theme, responsive layout).
   - `website/backend/`: Java Spring Boot REST API (Spring Web, Spring Security, Spring Data JPA/JDBC, BCrypt hashing).
   - `website/database/`: PostgreSQL production schema, indexes, seed data, and SQLite migration script.
   - `website/game-client/`: High-performance HTML5 Canvas 2D Fighting Game Engine replicating the exact C++ physics, hitboxes, character speeds, and keyboard inputs.
   - `website/deployment/`: Dockerfiles, docker-compose, environment configs, and execution scripts.
3. **Upgrade Password Security**: Upgrade from desktop SHA-256 + salt to **BCrypt (cost factor 12)** for web production security standards.
4. **Stateless REST & JWT**: Provide clean token-based or secure session authentication for all endpoints.
5. **Bidirectional Match Reporting**: Connect the browser game client directly to backend REST endpoints (`POST /api/matches`) so gameplay automatically updates player career statistics and the public leaderboard.

# Clash of Bytes – 2D Local Multiplayer Fighting Game

[![MIT AOE Exam Reform](https://img.shields.io/badge/MIT%20AOE-Exam%20Reform%20Guidelines-blue.svg)](https://mitaoe.ac.in)
[![Semester](https://img.shields.io/badge/Semester-III%20Integrated%20Project-orange.svg)](#)
[![C++17](https://img.shields.io/badge/Language-C%2B%2B17-blue.svg)](#)
[![Java 27](https://img.shields.io/badge/Java-JDK%2027%20%2F%20JDBC-red.svg)](#)
[![OpenGL](https://img.shields.io/badge/Graphics-OpenGL%20%2F%20Win32-green.svg)](#)
[![x86-64 Assembly](https://img.shields.io/badge/Assembly-x86--64%20AMD64-yellow.svg)](#)
[![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL-3ECF8E.svg?logo=supabase&logoColor=white)](https://supabase.com)
[![Tests Passing](https://img.shields.io/badge/Tests-100%25%20Passing-brightgreen.svg)](#)

> **Clash of Bytes** is a 2D Local Multiplayer Fighting Game engineered as an academic integrated capstone project according to the **MIT Academy of Engineering Exam Reform Guidelines for Semester III, Academic Year 2026–2027**.
>
> The project unifies four core computer science disciplines into one cohesive, interactive software system:
> 1. **Computer Graphics Laboratory** (C++ / Win32 / OpenGL)
> 2. **Programming Laboratory / DSA** (C++17)
> 3. **Computer Organization and Architecture** (COA / x86-64 Assembly)
> 4. **Problem Solving Using OOP** (Java SE 27 / Swing / JDBC / Supabase PostgreSQL)
>
> It also features a **Full-Stack Web Arena Platform** with a 60 FPS Canvas fighting engine, standalone Java REST backend, and database persistence.

---

## Table of Contents
1. [Architectural Overview by Subject](#1-architectural-overview-by-subject)
2. [Exact Controls Guide](#2-exact-controls-guide)
3. [Project Directory Structure](#3-project-directory-structure)
4. [Prerequisites & Environment Setup](#4-prerequisites--environment-setup)
5. [Build & Run Instructions](#5-build--run-instructions)
6. [Database Architecture & Setup](#6-database-architecture--setup)
7. [Visual Gallery & Screenshots](#7-visual-gallery--screenshots)
8. [Automated Verification & Test Suites](#8-automated-verification--test-suites)
9. [GitHub Security & Hygiene](#9-github-security--hygiene)

---

## 1. Architectural Overview by Subject

```
                                 CLASH OF BYTES
                        +------------------------------+
                        |  Master Launcher / build.bat |
                        +--------------+---------------+
                                       |
       +-------------------------------+-------------------------------+
       |                               |                               |
       v                               v                               v
+--------------+               +---------------+               +---------------+
| 1. OpenGL CG |               |  2. DSA C++   |               |   3. COA ASM  |
| 60 FPS Win32 |               | Practicals    |               | x86-64 AMD64  |
| Arcade Game  |               |  01 to 09     |               | ALU & Booth   |
+--------------+               +---------------+               +---------------+
       |                               |                               |
       +-------------------------------+-------------------------------+
                                       |
       +-------------------------------+-------------------------------+
       |                                                               |
       v                                                               v
+-------------------------------+               +-------------------------------+
|  4. Java OOP & Swing / JDBC   |               |    5. Full-Stack Web Arena    |
|  Two-Player Authentication    |               |    60 FPS Canvas Engine &     |
|  Match History & Dashboards   |               |    Java SE REST Server (:8080)|
+-------------------------------+               +-------------------------------+
```

### Module 1: Computer Graphics (C++ OpenGL)
- **Course Code**: 2310213L
- **Entry Point**: `clash_of_bytes.exe` (via `run_game.bat`)
- **Key Concepts**:
  - Double-buffered Win32 OpenGL rendering context with 60 FPS QueryPerformanceCounter (QPC) timing.
  - **CO1 (Prac 01)**: Gouraud-shaded illumination pipeline for dynamic arena backdrops.
  - **CO2 (Prac 04)**: 3D Transformation matrices (Model, View, Projection) and camera rotation.
  - **CO3 (Prac 02 & 03)**: Bresenham Line & Circle VFX algorithms; Scanline polygon rasterizer for real-time health bars.
  - **CO5 (Prac 05)**: Cohen-Sutherland line clipping against arena boundaries.
  - **CO6 (Prac 06)**: Hardware texture mapping, dynamic blending, and lighting effects.
  - Axis-Aligned Bounding Box (AABB) hitbox/hurtbox collision detection.

### Module 2: Programming Laboratory — DSA (C++)
- **Course Code**: 2310214L
- **Entry Point**: `dsa_practicals.exe` (via `run_dsa.bat`)
- **Key Concepts**:
  - **CO1 (Prac 01 & 02)**: 1D Array Fighter Move Catalog CRUD; 2D Sparse Matrix Arena Grid with $O(\text{terms} + \text{cols})$ Fast Transpose.
  - **CO2 (Prac 03, 04, 05)**: Singly Linked List dynamic particles; Doubly Linked List $O(N_1 + N_2)$ linear merge; Circular Linked List arena buff rotation wheel.
  - **CO3 (Prac 06 & 07)**: Prefix-to-Infix, Infix-to-Postfix Stack expressions; LIFO Undo and FIFO Action Scheduling Queue.
  - **CO4 (Prac 08)**: Linear Search vs. Binary Search; Bubble, Selection, and Insertion Sort with operation counts.
  - **CO5 (Prac 09)**: Hash Table Collision Resolution — Separate Chaining vs. Open Addressing (Linear Probing with Tombstone Deletions).

### Module 3: Computer Organization and Architecture (COA)
- **Course Code**: 2310221L
- **Entry Point**: `coa_practicals.exe` (via `run_coa.bat`)
- **Key Concepts**:
  - **CO1 (Prac 01)**: 80386 Microprocessor ALU simulator tracking CPU status flags (`ZF`, `CF`, `SF`, `OF`).
  - **CO4 (Prac 02)**: Control Unit (CU) instruction decoder generating register and memory micro-operations.
  - **CO2 (Prac 03)**: Inline 64-bit AMD64 assembly damage hook (`MOVQ`, `SUBQ`, `IMULQ`, `PUSHFQ`).
  - **CO3 (Prac 04)**: 64-bit Shift-and-Add and Booth's signed multiplication algorithm for debuff modifiers; string token scanning via `cld` and `repne scasb`; Packed BCD scoreboard encoder.

### Module 4: Problem Solving Using OOP (Java)
- **Course Code**: 2310222L
- **Entry Points**: `run_db_gui.bat` (Swing Two-Player Auth), `run_java.bat` (OOP Manager), `run_java_co4.bat` (Generics & Files)
- **Key Concepts**:
  - **OOP Hierarchy**: 6 Canonical Fighters modeling Single, Multilevel, Hierarchical, Multiple (via Interfaces), and Hybrid Inheritance.
  - **Polymorphism**: Compile-time method overloading and dynamic parent-reference method dispatch.
  - **CO3 Collections**: `LinkedHashMap` $O(1)$ lookups, `ArrayList` custom comparator sorting, `ArrayDeque` move queues.
  - **CO4 File Handling & Generics**: Generic Fighter Repository (`<T extends BaseFighter>`), Generic Battle Archive, and CSV/log file persistence with defensive exception handling.
  - **Database Integration**: JDBC connection to SQLite (`clash_of_bytes.db`), salted SHA-256 password security, prepared statements, and transactional match persistence.

### Module 5: Full-Stack Web Arena Platform
- **Entry Point**: `run_website.bat` (Server at `http://localhost:8080`)
- **Key Concepts**:
  - High-performance, zero-dependency Java SE REST API server (`StandaloneWebServer.java`).
  - Native HTML5 Canvas 60 FPS deterministic game engine with multi-key simultaneous input.
  - Dual simultaneous player logins on the same screen with independent sessions.
  - Real database queries for Player Career Dashboards and Global Ranked Leaderboards.

---

## 2. Exact Controls Guide

Both players can press keys simultaneously on the same keyboard with full multi-key rollover (no ghosting or input lag).

### Web Platform Controls (Task 3 Standardized)
| Action | Player 1 (Left / Cyan) | Player 2 (Right / Pink) |
| :--- | :--- | :--- |
| **Move Left** | `A` | `Left Arrow (←)` |
| **Move Right** | `D` | `Right Arrow (→)` |
| **Jump** | `W` | `Up Arrow (↑)` |
| **Attack** | `F` | `K` |
| **Defence (Block Shield)** | `G` | `L` |

### Desktop C++ OpenGL Arcade Controls
| Action | Player 1 (Left Fighter) | Player 2 (Right Fighter) |
| :--- | :--- | :--- |
| **Movement & Jump** | `W`, `A`, `S`, `D` | `Up`, `Left`, `Down`, `Right` Arrows |
| **Light Punch** | `F` | `J` or `NumPad 1` |
| **Heavy Kick** | `G` | `K` or `NumPad 2` |
| **Block Shield** | `H` | `L` or `NumPad 3` |
| **Special Ability** | `R` | `U` or `NumPad 5` |

**Desktop In-Game Hotkeys**:
- `[1]` / `[T]`: Tutorial Mode (Coaching & Sweet-Spot Indicators)
- `[2]` / `[ENTER]`: Real Fight Tournament Mode
- `[M]` / `[F3]`: Toggle Tutorial vs Real Fight Mode anytime
- `[F4]`: Cycle Player 1 Fighter
- `[F5]`: Cycle Player 2 Fighter
- `[F1]`: Toggle Hitbox & Hurtbox Debug Outlines
- `[ESC]`: Restart Match

---

## 3. Project Directory Structure

```text
Clash_Of_Bytes/
├── 1_Computer_Graphics/                      # OpenGL C++ 2D Arcade Engine
│   ├── main.cpp                              # WinMain entry point, 60 FPS QPC game loop
│   ├── OpenGL_Window.cpp / .hpp              # Win32 double-buffered OpenGL context
│   ├── CollisionSystem.hpp                   # AABB hitbox/hurtbox collision system
│   ├── Fighter.cpp / .hpp                    # Base fighter entity and state machine
│   ├── GameMatch.cpp / .hpp                  # Real-time combat state and HUD
│   └── [Fighter Subclasses].(cpp/hpp)        # 6 canonical fighter archetypes
├── 2_Programming_Laboratory_DSA/             # Data Structures & Algorithms (C++)
│   ├── CO1_to_CO3_DSAManager.cpp             # Practicals 01 to 09 test runner
│   ├── CO1_Prac01_1D_Arrays_*.hpp            # 1D Array Move Catalog CRUD
│   ├── CO1_Prac02_2D_Sparse_Matrix_*.hpp     # 2D Sparse Matrix Fast Transpose
│   ├── CO2_Prac03_to_05_*.hpp                # Singly, Doubly, Circular Linked Lists
│   ├── CO3_Prac06_to_07_*.hpp                # Stacks, Queues, Prefix/Postfix
│   ├── CO4_Prac08_Searching_and_Sorting.hpp  # Search & Sort algorithms with metrics
│   └── CO5_Prac09_Hashing_*.hpp              # Separate Chaining & Linear Probing
├── 3_Computer_Organization_Architecture/     # COA & Assembly (x86-64 AMD64)
│   ├── CO1_to_CO4_COAManager.cpp             # Unified COA test runner (CO1 to CO4)
│   ├── CO1_Prac01_80386_ALU_Simulator.hpp    # 80386 ALU Simulator & status flags
│   ├── CO4_Prac02_Control_Unit_Decoder.hpp   # Control Unit micro-operation decoder
│   ├── CO2_Prac03_x86_64_Assembly_*.hpp      # Inline x86-64 Assembly damage engine
│   └── CO3_Prac04_Algorithmic_*.hpp          # Booth's Algorithm & repne scasb scanner
├── 4_Object_Oriented_Programming_Java/       # Java OOP, Swing & JDBC Database
│   ├── bin/                                  # Compiled .class bytecode
│   ├── lib/                                  # Bundled JDBC Drivers (SQLite & PostgreSQL)
│   ├── model/, dao/, service/, ui/, testing/ # Full MVC & Database Architecture
│   ├── Main/CombatManager.java               # OOP match manager & mode switcher
│   ├── ui/TwoPlayerLoginFrame.java           # Swing dual-player login interface
│   └── testing/DatabaseAuthTestSuite.java    # Automated Swing/JDBC test suite
├── website/                                  # Full-Stack Web Platform
│   ├── backend/                              # Java REST backend (StandaloneWebServer.java)
│   ├── game-client/                          # HTML5 Canvas 60 FPS fighting game client
│   ├── database/                             # SQLite, PostgreSQL & Supabase migrations
│   └── deployment/                           # Dockerfiles, docker-compose, .env.example
├── screenshots/                              # Genuine screenshots of all modules
├── scripts/schema.sql                        # Canonical database schema
├── clash_of_bytes.db                         # Local SQLite database (26 users, 13 matches)
├── build.bat                                 # Master compiler for all 4 academic modules
├── run_game.bat                              # Launches C++ OpenGL arcade game
├── run_website.bat                           # Launches Web platform on port 8080
├── run_db_gui.bat                            # Launches Java Swing Two-Player DB GUI
├── run_java.bat                              # Launches Java OOP Combat Manager
├── run_dsa.bat                               # Launches DSA Practicals runner
├── run_coa.bat                               # Launches COA Practicals runner
├── run_java_co4.bat                          # Launches Java CO4 Generics & Files Suite
├── .gitignore                                # Git ignore configuration
└── README.md                                 # This documentation file
```

---

## 4. Prerequisites & Environment Setup

To compile and run all components locally on Windows:

1. **C++ Compiler (MinGW-w64 / MSYS2)**:
   - Must support C++17 (`g++ -std=c++17`).
   - Default search path: `C:\msys64\ucrt64\bin;C:\msys64\mingw64\bin`.
2. **Java Development Kit**:
   - JDK 17 or higher (tested with JDK 27).
   - Ensure `java` and `javac` are available in system `PATH`.
3. **Python (Optional for Automated Test Runners)**:
   - Python 3.8+ for running full-stack API verification scripts.
4. **OpenGL**:
   - Standard graphics drivers with OpenGL 2.0+ support (included in all modern Windows drivers).

---

## 5. Build & Run Instructions

### Step 1: Compile the Whole Project
Open Command Prompt or PowerShell in `Clash_Of_Bytes/` and run:
```cmd
build.bat
```
This single command compiles:
- `clash_of_bytes.exe` (Computer Graphics C++ OpenGL)
- `dsa_practicals.exe` (Programming Lab DSA C++)
- `coa_practicals.exe` (Computer Organization & Architecture C++/ASM)
- All Java OOP & Database classes into `4_Object_Oriented_Programming_Java\bin\`

### Step 2: Running the Applications

#### 1. Play the C++ OpenGL Fighting Game
```cmd
run_game.bat
```

#### 2. Run the Full-Stack Web Platform
```cmd
run_website.bat
```
Then open your browser to **`http://localhost:8080/`**.

#### 3. Run the Java Swing Two-Player Database Interface
```cmd
run_db_gui.bat
```

#### 4. Run the Java OOP Combat Manager
```cmd
run_java.bat
```

#### 5. Run the Academic Practicals
```cmd
run_dsa.bat       # Runs DSA Practicals 01 to 09
run_coa.bat       # Runs COA Practicals 01 to 04
run_java_co4.bat   # Runs Java CO4 Generics & File Handling Suite
```

---

## 6. Database Architecture & Setup

The permanent database architecture for **Clash of Bytes** uses **Supabase PostgreSQL** as the sole, unified database engine across both the **HTML5 Canvas Web Platform** and the **Java Swing Desktop Application**.

### Unified Cloud Architecture
* **Single Shared Database**: Both frontends communicate with the same Supabase project and PostgreSQL instance. User registrations, player career statistics, tournament matches, and leaderboard rankings are universally synchronized across web and desktop clients.
* **Security & Credential Isolation**: Public clients use the public anonymous key (`SUPABASE_ANON_KEY`) governed by PostgreSQL Row Level Security (RLS) policies. Service-role keys and database passwords are never exposed in client code or Git.
* **Local Development Transition**: During local development, the backend supports connecting to a local PostgreSQL instance or the managed Supabase pooler via environment variables in `.env` (derived from [`.env.example`](.env.example)).

### Canonical Database Schema (5 Core Tables)
Defined in [`Clash_Of_Bytes/website/database/supabase_schema.sql`](Clash_Of_Bytes/website/database/supabase_schema.sql):
1. **`users`**: User accounts with salted SHA-256 / BCrypt password hashes.
2. **`characters`**: Canonical 6-fighter roster with base HP, attack, defense, and speed.
3. **`matches`**: Full battle telemetry (Player 1, Player 2, scores, rounds won, winner, loser, duration).
4. **`match_players`**: Bridge table linking each player's chosen character, score, and result to the match.
5. **`match_rounds`**: Individual round outcomes, scores, and duration.

---

## 7. Visual Gallery & Screenshots

All genuine screenshots are preserved in the [`screenshots/`](screenshots/) directory:

| Module / Screen | Preview File | Description |
| :--- | :--- | :--- |
| **C++ OpenGL Game** | [`screenshot_cg_task4.png`](screenshots/screenshot_cg_task4.png) | 60 FPS Win32 OpenGL combat arena with Gouraud lighting and health bars |
| **Swing Main Menu** | [`screenshot_ui_main_menu.png`](screenshots/screenshot_ui_main_menu.png) | Central command hub linking all 6 UI screens, 3 game engines, and test suites |
| **Swing Dual Login** | [`screenshot_ui_login.png`](screenshots/screenshot_ui_login.png) | Two-Player side-by-side database authentication window with quick fills |
| **Swing Character Select** | [`screenshot_ui_character_select.png`](screenshots/screenshot_ui_character_select.png) | 6-character roster selection with stats comparison and top navigation |
| **Swing Player Dashboard** | [`screenshot_ui_player_dashboard.png`](screenshots/screenshot_ui_player_dashboard.png) | Player career record, win rate, and interactive player switcher dropdown |
| **Swing Match History** | [`screenshot_ui_match_history.png`](screenshots/screenshot_ui_match_history.png) | JTable match archive with database telemetry, search filter, and no truncation |
| **Swing Leaderboard** | [`screenshot_ui_leaderboard.png`](screenshots/screenshot_ui_leaderboard.png) | Tournament standings with Gold, Silver, and Bronze podium medal highlights |
| **Web Arena Home** | [`screenshot_web_home.png`](screenshots/screenshot_web_home.png) | Public web landing page and canonical fighter cards |
| **Web Dual Authentication**| [`screenshot_web_auth.png`](screenshots/screenshot_web_auth.png) | Side-by-side web login panels with mutual exclusion and quick-fill buttons |
| **Web Character Select** | [`screenshot_web_select.png`](screenshots/screenshot_web_select.png) | 6-fighter roster cards, stat gauges, and lock-in readiness badges |
| **Web Combat Arena** | [`screenshot_web_play.png`](screenshots/screenshot_web_play.png) | 60 FPS HTML5 Canvas combat with standardized controls, shields, and lag HP |
| **Web Career Dashboard** | [`screenshot_web_dashboard.png`](screenshots/screenshot_web_dashboard.png) | 8-metric career statistics and character usage breakdown |
| **Web Match History** | [`screenshot_web_history.png`](screenshots/screenshot_web_history.png) | Full battle archive with round results and telemetry |
| **Web Global Leaderboard** | [`screenshot_web_leaderboard.png`](screenshots/screenshot_web_leaderboard.png) | Ranked player standings sorted by victories and score |

---

## 8. Automated Verification & Test Suites

The project features automated regression test suites verifying 100% functionality across all modules:

| Test Suite | File / Command | Scope & Tests Executed | Success Rate |
| :--- | :--- | :--- | :--- |
| **Full Project Build** | `build.bat` | Compiles C++ OpenGL, DSA, COA, and Java OOP modules | **100% (Exit Code 0)** |
| **DSA Practicals Suite** | `dsa_practicals.exe` | Practicals 01 to 09 (Arrays, Linked Lists, Stacks, Searches, Sorts, Hashing) | **100% Passed** |
| **COA Integrated Suite** | `coa_practicals.exe` | Practicals 01 to 04 (ALU, Control Unit, 64-bit Assembly, Booth, String Scan) | **100% Passed** |
| **Java CO4 Generics Suite** | `run_java_co4.bat` | 13 test cases (Generic Repositories, Predicates, File Read/Write/Append) | **13 / 13 (100%)** |
| **Desktop Swing/JDBC Suite**| `DatabaseAuthTestSuite.java`| 16 test cases (Dual Auth, Password Hashing, Match Persistence, Rollback) | **16 / 16 (100%)** |
| **Web REST Backend Suite** | `BackendTestSuite.java` | 16 test cases (API Contracts, Dual Sessions, Telemetry Completeness, RLS) | **16 / 16 (100%)** |
| **Full-Stack E2E Web Suite**| `test_e2e_supabase_and_features.py` | 10 end-to-end integration checks (REST API, Dual Login, Matches, Stats) | **10 / 10 (100%)** |

---

## 9. GitHub Security & Hygiene

- **No Passwords or Secrets Committed**: Password hashes use SHA-256 with random salts. All configuration files use placeholder templates ([`.env.example`](.env.example)).
- **Clean Repository**: Temporary object files (`*.o`, `*.obj`), compiler debug files (`*.pdb`, `*.ilk`), build target directories (`target/`), and server runtime logs (`*.log`) are excluded by [`.gitignore`](.gitignore).
- **Offline Self-Contained Libraries**: JDBC driver `.jar` files in `4_Object_Oriented_Programming_Java/lib/` are tracked so any developer cloning the repository can immediately build without Maven/Gradle container dependencies.

---

## Academic Verification & Mentorship
- **Institution**: MIT Academy of Engineering (MIT AOE), Alandi (D.), Pune
- **Programme**: Bachelor of Technology (Second Year)
- **Academic Year**: 2026–2027 (Semester III)
- **Evaluation Status**: **Task 1 to Task 4 Complete & Verified**

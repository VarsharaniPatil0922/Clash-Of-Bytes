# Clash of Bytes – 2D Local Multiplayer Fighting Game
## Integrated College Project – Milestone 1 (Task 1 to Task 4 Complete)
### MIT Academy of Engineering – Semester III

All project components across all four subjects have been consolidated into the clean primary folder: **[`Clash_Of_Bytes`](file:///c:/Users/Varsharani/Downloads/2ND%20YEAR/FightingGame/clash_of_bytes_milestone1_task3%20new/Clash_Of_Bytes)**.

---

## 📁 Complete Project Directory Tree

```text
clash_of_bytes_milestone1_task3 new/
├── .vscode/                                      # Root IDE IntelliSense configuration
├── README.md                                     # Unified project execution guide
└── Clash_Of_Bytes/                               # 🎯 Main Project Folder
    ├── 1_Computer_Graphics/                      # OpenGL C++ 2D/3D Arcade Game Engine
    │   ├── main.cpp                              # WinMain entry point, 60 FPS QPC game loop
    │   ├── OpenGL_Window.cpp / .hpp              # Win32 double-buffered OpenGL context
    │   ├── CO1_Prac01_OpenGL_Gouraud_Renderer.*  # Gouraud illumination & 2D rendering pipeline
    │   ├── CO2_Prac04_Transformation_Matrices_3DCube.hpp # 3D cube model & camera matrix
    │   ├── CO3_Prac02_Bresenham_Line_Circle_VFX.hpp      # Bresenham rasterization VFX
    │   ├── CO3_Prac03_ScanLine_Polygon_HealthBar.hpp     # Scanline polygon rasterizer
    │   ├── CO5_Prac05_Cohen_Sutherland_Clipping.hpp      # Cohen-Sutherland line clipping
    │   ├── CO6_Prac06_Texture_Lighting_Color_Effects.hpp # Texture mapping & hardware lighting
    │   ├── CollisionSystem.hpp                   # AABB hitbox detection & assembly damage hooks
    │   ├── Fighter.cpp / .hpp                    # Base fighter physics, states, and hurtboxes
    │   ├── FighterFactory.hpp                    # Factory pattern character instantiator
    │   ├── GameMatch.cpp / .hpp                  # Real-time combat state, HUD, and mode manager
    │   ├── InputManager.cpp / .hpp               # Dual-player keyboard polling
    │   ├── ParticleSystem.hpp                    # Dynamic VFX particle emitter
    │   └── [Warrior, Ninja, CyberBrawler, Paladin, Berserker, Gladiator].(cpp/hpp)
    ├── 2_Programming_Laboratory_DSA/             # Data Structures & Algorithms (C++)
    │   ├── CO1_Prac01_1D_Arrays_Fighter_Move_Catalog.hpp    # 1D Array CRUD move catalog
    │   ├── CO1_Prac02_2D_Sparse_Matrix_Fast_Transpose.hpp   # 2D Sparse matrix arena grid
    │   ├── CO2_Prac03_Singly_Linked_List_Particles.hpp      # Singly linked list node manager
    │   ├── CO2_Prac04_Doubly_Linked_List_Match_Scores.hpp   # Doubly linked list O(N1+N2) merge
    │   ├── CO2_Prac05_Circular_Linked_List_Arena_Buffs.hpp  # Circular buffer power-up queue
    │   ├── CO3_Prac06_Stack_Combat_Formula_Evaluator.hpp    # Prefix/Infix/Postfix stack parser
    │   ├── CO3_Combat_Action_Stack_Queue_Scenario.hpp       # LIFO undo & FIFO combat scheduler
    │   ├── CO3_Prac07_Combat_Scenario_Stack_Queue.hpp       # Verification suite wrapper
    │   ├── CO4_Prac08_Searching_and_Sorting.hpp             # Linear/Binary Search, Bubble/Select/Insert Sort
    │   ├── CO5_Prac09_Hashing_Collision_Resolution.hpp      # Hash Table (Separate Chaining & Linear Probing)
    │   ├── CO1_to_CO3_DSAManager.cpp                        # Test driver for DSA Practicals 01 to 09
    │   └── Vector2D.hpp                                     # 2D Vector mathematics
    ├── 3_Computer_Organization_Architecture/     # Computer Organization & Architecture
    │   ├── CO1_Prac01_80386_ALU_Simulator.hpp               # 80386 Microprocessor ALU & flags
    │   ├── CO4_Prac02_Control_Unit_Decoder.hpp              # Micro-op control unit decoder
    │   ├── CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp     # Inline x86-64 assembly damage hook
    │   ├── CO2_Prac03_Combat_Damage_64bit.asm               # Standalone 64-bit assembly driver
    │   ├── CO3_Prac04_Algorithmic_and_String_Processing.hpp # Booth's multiplier & string scanner
    │   ├── CO3_Prac04_Combat_Algorithms_64bit.asm           # 64-bit Booth & ASM routines
    │   ├── CO3_Prac04_Hex_Packed_BCD_Converter.hpp          # Binary to Packed BCD encoder
    │   ├── CO4_Integrated_Pipeline_Demonstration.hpp        # Unified COA multi-stage execution pipeline
    │   └── CO1_to_CO4_COAManager.cpp                        # Unified COA test runner (CO1 to CO4)
    ├── 4_Object_Oriented_Programming_Java/       # Java OOP Combat Architecture
    │   ├── bin/                                  # Compiled .class bytecode
    │   ├── CO1/                                  # Fundamentals & String Handling
    │   │   ├── JavaFundamentals/CombatAnalytics.java, HitTimingIndicator.java
    │   │   └── StringHandling/CombatLogger.java
    │   ├── CO2/OOP/                              # Core OOP Hierarchy
    │   │   ├── BaseFighter.java, WarriorFighter.java, NinjaFighter.java, CyberBrawler.java
    │   │   ├── PaladinFighter.java, BerserkerWarrior.java, HybridGladiator.java
    │   │   └── Interfaces: ElementalAttacker.java, ShieldBearer.java
    │   ├── CO3/                                  # Collections, Exceptions & GUI
    │   │   ├── Collections/CombatAction.java, CombatActionQueue.java, MatchRecord.java, ...
    │   │   ├── Exceptions/ClashOfBytesException.java, InvalidFighterSelectionException.java, ...
    │   │   ├── SwingGUI/GameLauncherGUI.java
    │   │   └── Testing/CO3PracticalTestSuite.java
    │   ├── CO4/                                  # Task 4: File Handling & Java Generics
    │   │   ├── FileHandling/FighterDataFileManager.java     # CSV fighter persistence & error handling
    │   │   ├── FileHandling/MatchHistoryFileManager.java    # Append-mode match logging & history viewer
    │   │   ├── Generics/GenericFighterRepository.java       # Type-safe repository <T extends BaseFighter>
    │   │   ├── Generics/GenericBattleArchive.java           # Unbounded archive & generic average utility
    │   │   ├── Testing/CO4PracticalTestSuite.java           # 13/13 Automated verification test suite
    │   │   └── CO4_README.md                                # Concept notes and academic mapping
    │   └── Main/CombatManager.java               # Master OOP Console Combat Manager & Launcher
    ├── .vscode/                                  # C/C++ IntelliSense & compiler settings
    ├── .clangd                                   # Clangd language server configuration
    ├── compile_commands.json                     # Clang compilation database
    ├── compile_flags.txt                         # Compiler flags specification
    ├── clash_of_bytes.exe                        # Compiled Real-Time Arcade Game executable
    ├── coa_practicals.exe                        # Compiled COA Practicals test runner
    ├── dsa_practicals.exe                        # Compiled DSA Practicals test runner
    ├── screenshot_cg_task3.bmp                   # Graphics Task 3 visual verification
    ├── build.bat                                 # Master build batch script
    ├── run_game.bat                              # Launcher for the arcade game
    ├── run_gui.bat                               # Launcher for the Java Swing GUI
    ├── run_coa.bat                               # Launcher for COA test suite
    ├── run_dsa.bat                               # Launcher for DSA test suite
    ├── run_java.bat                              # Launcher for Java OOP Combat Manager
    ├── run_java_co4.bat                          # Launcher for Java CO4 File Handling & Generics
    └── run_db_gui.bat                            # Launcher for Two-Player Database Auth & Match History GUI
```

---

## 🚀 How to Run the Applications

From inside `Clash_Of_Bytes/`:

1. **`build.bat`**: Rebuild all C++ engines, COA drivers, DSA suites, and Java classes.
2. **`run_game.bat`**: Launch the C++ 60 FPS OpenGL Real-Time Arcade Game (`clash_of_bytes.exe`).
3. **`run_db_gui.bat`**: Launch the Two-Player Relational Database Authentication, Character Select & Match History GUI.
4. **`run_gui.bat`**: Launch the Cyberpunk Java Swing GUI Dashboard.
5. **`run_dsa.bat`**: Run DSA practicals (Arrays, Linked Lists, Stacks, Queues, Searching, Sorting, Hashing).
6. **`run_coa.bat`**: Run COA practicals (ALU status flags, Booth's multiplier, 64-bit Assembly damage engine, and Task 4 Integrated Pipeline).
7. **`run_java.bat`**: Launch the Java OOP Combat Manager (Console + GUI selector).
8. **`run_java_co4.bat`**: Run the automated Java CO4 test suite (File Handling & Java Generics).

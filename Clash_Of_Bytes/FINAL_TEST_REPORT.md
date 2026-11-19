# Clash of Bytes: Final Comprehensive Test Report

**Document Version:** 1.0.0 (Release Candidate)  
**Execution Date:** 2026-11-17T11:48:30+05:30  
**Project:** Clash of Bytes – 2D Local Multiplayer Fighting Game  
**Course Code Alignment:** MIT Academy of Engineering (MIT AOE), Pune — Semester III Integrated Project (AY 2026–2027)  
**Modules Tested:** Computer Graphics (OpenGL), Programming Lab (DSA), COA (x86-64 ASM), OOP (Java SE 27), and Web Platform  

---

## 1. Executive Test Summary

| Test Domain / Suite | Harness / Runner | Tests Executed | Passed | Failed | Pass Rate | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Full Project Build** | `build.bat` (MinGW-w64 + Java 27) | 4 modules compiled | 4 | 0 | **100.0%** | `PASS` |
| **DSA Practicals 01–09 & Task 4** | `dsa_practicals.exe --all` | 9 practicals + benchmarks | 9 | 0 | **100.0%** | `PASS` |
| **COA Practicals 01–04 & Task 4** | `coa_practicals.exe --all` | 4 practicals + 7-stage pipe | 5 | 0 | **100.0%** | `PASS` |
| **Java CO4 Generics & File I/O** | `CO4PracticalTestSuite.java` | 13 test cases | 13 | 0 | **100.0%** | `PASS` |
| **Java Database & Auth Suite** | `DatabaseAuthTestSuite.java` | 16 test cases | 16 | 0 | **100.0%** | `PASS` |
| **Web REST API Backend Suite** | `BackendTestSuite.java` | 16 test cases | 16 | 0 | **100.0%** | `PASS` |
| **Full-Stack REST E2E Suite** | `test_e2e_supabase_and_features.py` | 10 verification steps | 10 | 0 | **100.0%** | `PASS` |
| **UI Automated Captures** | `CaptureUIScreenshots.java` | 6 Swing frames rendered | 6 | 0 | **100.0%** | `PASS` |
| **Total Test Assertions** | **Consolidated Multi-Stack** | **74 Assertions** | **74** | **0** | **100.0%** | `VERIFIED` |

---

## 2. Full Project Build Verification (`build.bat`)

- **Execution Command**: `cmd /c "build.bat"`
- **Working Directory**: `c:\Users\Varsharani\Downloads\2ND YEAR\FightingGame\clash_of_bytes_milestone1_task3 new\Clash_Of_Bytes`
- **Exit Code**: `0`

### Binary Artifacts Produced:
1. `clash_of_bytes.exe` — **3,395,690 bytes** (C++17 Win32/OpenGL Real-Time Fighting Engine)
2. `dsa_practicals.exe` — **361,552 bytes** (Programming Laboratory DSA Test Suite)
3. `coa_practicals.exe` — **167,059 bytes** (Computer Organization & Assembly Test Suite)
4. `4_Object_Oriented_Programming_Java\bin\` — **78 compiled .class files** (Java OOP, UI, Models, DAOs, Services)

---

## 3. Programming Laboratory (DSA) Test Execution (`dsa_practicals.exe`)

Command: `.\dsa_practicals.exe --all`  
Result: **All 9 Practicals and Task 4 Benchmark Completed Successfully (Code 0)**

### 3.1 Practical Breakdown & Verified Output

```text
[PRACTICAL 01] 1D Arrays: Fighter Move Catalog CRUD (CO1)
  - Insert: Added Move ID 5 (Dragon Upper, DMG: 22, Startup: 5f) -> PASS
  - Read: Found Move ID 5 -> PASS
  - Update: Buffed Move ID 5 damage to 26 -> PASS
  - Delete: Removed Move ID 1. Moves remaining: 4 -> PASS

[PRACTICAL 02] 2D Arrays: Sparse Matrix Arena Grid & Fast Transpose (CO1)
  - 3-Tuple Representation: 26 non-zero grid elements captured.
  - Fast Transpose: O(terms + cols) computed in single pass. Transposed terms: 26 -> PASS

[PRACTICAL 03] Singly Linked List: Dynamic Particle Node Management (CO2)
  - Initial nodes: 5. Traversal and removeIf(lifetime <= 0) executed.
  - Active surviving particles: 3 (IDs #5, #3, #1) -> PASS

[PRACTICAL 04] Doubly Linked List: Sort & Linear O(N1 + N2) Merge (CO2)
  - Pool A sorted by damage dealt descending: Shadow Ninja (110) > Blaze Warrior (85) > Cyber Brawler (65).
  - Merged with Pool B in linear O(N1 + N2) time -> PASS

[PRACTICAL 05] Circular Linked List: Arena Power-Up & Buff Queue (CO2)
  - Buffs queued: 4/4 slots. Over-capacity buff #5 rejected safely -> PASS
  - Consumed Front Buff (FCFS): Dragon's Might applied -> PASS
  - Circular traversal rotates queue preserving node links -> PASS

[PRACTICAL 06] Stack Operations: Real-Time Combat Formula Engine (CO3)
  - Prefix Input: * + ATK RAGE / COMBO 2
  - Prefix to Infix: ((ATK + RAGE) * (COMBO / 2))
  - Infix to Postfix: ATK RAGE + COMBO 2 / *
  - Evaluated Combat Damage: ATK=24, RAGE=6, COMBO=4 => 60 HP Damage -> PASS

[PRACTICAL 07] Queue Operations: Combat Scenario Scheduler (CO3)
  - Combat Action History: LIFO rollback executed successfully -> PASS
  - Real-Time Action Dispatcher: FIFO command processing verified -> PASS

[PRACTICAL 08] Searching & Sorting Algorithms Benchmark (CO4)
  - Linear Search: Found key at index 3 in 4 comparisons -> PASS
  - Binary Search: Found key at sorted index 5 in 3 comparisons -> PASS
  - Bubble Sort: Sorted 8 fighters by HP in 28 comparisons, 11 swaps -> PASS
  - Selection Sort: Sorted 8 fighters by HP in 28 comparisons, 7 swaps -> PASS
  - Insertion Sort: Sorted 8 fighters by HP in 18 comparisons, 11 shifts -> PASS

[PRACTICAL 09] Hash Table with Separate Chaining & Linear Probing (CO5)
  - Separate Chaining (11 buckets, load factor 0.55): Inserted 6 elements, collision on bucket 6 handled via chained list. Search after head deletion verified -> PASS
  - Linear Probing (13 slots, load factor 0.54): Inserted 7 elements with probe counts 1 to 2. Tombstone placed on delete. Search past tombstone verified -> PASS
```

### 3.2 Search Benchmark Comparison Matrix
| Search Technique | Precondition | Best Case | Average Case | Worst Case | Typical Probes (N=8) | Space Complexity |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Linear Search** | Unordered array | $O(1)$ | $O(N)$ | $O(N)$ | 4 to 8 checks | $O(1)$ |
| **Binary Search** | Sorted array required | $O(1)$ | $O(\log N)$ | $O(\log N)$ | 2 to 3 checks | $O(1)$ |
| **Hash Table** | Hash function + table | $O(1)$ | $O(1)$ | $O(N)$ | 1 to 2 probes | $O(M)$ |

---

## 4. Computer Organization and Architecture (COA) Test Execution (`coa_practicals.exe`)

Command: `.\coa_practicals.exe --all`  
Result: **All 4 Practicals and Task 4 Integrated Pipeline Completed Successfully (Code 0)**

### 4.1 Micro-Architecture Pipeline Trace

```text
[PRACTICAL 01] 80386 Microprocessor ALU Simulation (CO1)
  - Subtraction: HP(100) - DMG(25) = 75 | Flags: ZF=0 CF=0 SF=0 OF=0 -> PASS
  - Knockout Test: 25 - 25 = 0 | Flags: ZF=1 (K.O. Active!) CF=0 SF=0 OF=0 -> PASS

[PRACTICAL 02] Control Unit Instruction Decoder (CO4)
  - Action PUNCH   -> RegRead=1 RegWrite=1 ALUSrc=1 ALUOp=SUB_DAMAGE MemRead=0 MemWrite=1 Branch=0 -> PASS
  - Action SPECIAL -> RegRead=1 RegWrite=1 ALUSrc=1 ALUOp=IMUL_BURST MemRead=1 MemWrite=1 Branch=1 -> PASS
  - Action BLOCK   -> RegRead=1 RegWrite=0 ALUSrc=0 ALUOp=NOP        MemRead=0 MemWrite=0 Branch=0 -> PASS

[PRACTICAL 03] 64-bit Hardware Assembly Damage Engine (CO2)
  - Native x86-64 __asm__ Hook: MOVQ, SUBQ, IMULQ, PUSHFQ
  - Calculated: Atk 28, Def 6, Combo x2 => 44 HP Damage | RAX: 0x2C | Packed BCD: 0x44 -> PASS

[PRACTICAL 04] Algorithmic & String Processing (CO3)
  - 64-Bit Shift-and-Add Multiplication: 28 x 5 = 140 (RAX: 0x8C) -> PASS (Assembly == Reference)
  - 64-Bit Booth's Signed Multiplication: -5 x 4 = -20 (Q: 0xFFFFFFFFFFFFFFEC) -> PASS (Assembly == Reference)
  - String Length (cld / repne scasb): "FIGHTER" length = 7 bytes -> PASS
  - Hardware Token Search: Found 2 occurrences of "CRITICAL" -> PASS

[TASK 4] INTEGRATED 7-STAGE COA PIPELINE EXECUTION (CO4)
  Stage 1 [Application Input]  : SPECIAL_ART_BURST (ATK=54, DEF=14, Combo x3, Debuff -6x4)
  Stage 2 [Control Unit]       : Opcode decoded to IMUL_BURST, RegRead=1, RegWrite=1, MemWrite=1
  Stage 3 [Register Mapping]   : RAX=0x78 (120 HP), RBX=0xE (14 HP), RDX=0x28 (40 HP diff)
  Stage 4 [ALU Flags]          : ZF=0, CF=0, SF=0, OF=0 (Target conscious)
  Stage 5 [Booth's Product]    : (-6 HP/turn) * 4 turns = -24 HP. Net Impact = -144 HP
  Stage 6 [repne scasb Scan]   : 72 characters scanned. 2 'CRITICAL' tokens matched
  Stage 7 [Packed BCD HUD]     : Hex 0x78 converted to Packed BCD 0x120 for hardware display
```

---

## 5. Java OOP & Generics Test Execution (`CO4PracticalTestSuite.java`)

Command: `java -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" CO4.Testing.CO4PracticalTestSuite`  
Result: **13 / 13 Tests Passed (100% Success)**

```text
[PASS] Test 01: Generic Repository Size Verification (5 Fighters)
[PASS] Test 02: Generic Search findByName('Shadow Ninja')
[PASS] Test 03: Generic Predicate Filter (HP >= 125.0 yields 2 fighters)
[PASS] Test 04: Generic Comparator Sorting (Top ATK = Blood Berserker with 35 ATK)
[PASS] Test 05: Subclass Type Parameter Specialization <WarriorFighter>
[PASS] Test 06: Generic Battle Archive Add Entries
[PASS] Test 07: Generic Method Average Damage Computation (avg = 191.67 HP)
[PASS] Test 08: File Creation & Write: saveFightersToFile()
[PASS] Test 09: File Append: appendFighterRecord()
[PASS] Test 10: File Read & Parse: loadFighterRecords() count == 6
[PASS] Test 11: Defensive Exception Guard: FileNotFoundException caught properly
[PASS] Test 12: Match History Append Logs (3 matches written)
[PASS] Test 13: Match History Read Lines Verification (Count == 3)
```

---

## 6. Java Swing Database Authentication Test Execution (`DatabaseAuthTestSuite.java`)

Command: `java -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" testing.DatabaseAuthTestSuite`  
Result: **16 / 16 Tests Passed (100% Success)**

```text
[*] Database Schema Initialized at: jdbc:sqlite:clash_of_bytes.db
[PASS] Test 01: Register Player 1                  | User ID: 25, Username: shadow_master_8658
[PASS] Test 02: Register Player 2                  | User ID: 26, Username: blaze_warrior_8658
[PASS] Test 03: Independent Login for Both Players | P1 verified by username; P2 verified by email
[PASS] Test 04: Reject Invalid Password            | Rejected incorrect password attempt with authentication error
[PASS] Test 05: Reject Duplicate Registrations     | Caught duplicate username collision & duplicate email collision
[PASS] Test 06: Prevent Self-Match (P1 == P2)      | Successfully guarded against player 1 playing against self
[PASS] Test 07: Select Valid Distinct Characters   | Characters loaded: 6 | P1: Ironclad Warrior, P2: Shadow Ninja
[PASS] Test 08: Simulate Match Rounds              | Rounds: 3 | P1 Score: 220 | P2 Score: 195
[PASS] Test 09: Complete Match Transactional Save  | Match, 2 MatchPlayers, and 3 Rounds committed atomically
[PASS] Test 10: Verify Stored Final Scores         | Match #13 -> P1 Score: 220, P2 Score: 195
[PASS] Test 11: Winner & Result Calculation        | P1 Result: WIN | P2 Result: LOSS | Winner: shadow_master_8658
[PASS] Test 12: Individual Round Telemetry Stored  | Retrieved 3 rounds with exact round-level scores
[PASS] Test 13: Character Selections Persisted     | P1 Pick: Ironclad Warrior vs P2 Pick: Shadow Ninja
[PASS] Test 14: Player Career Stats Aggregation    | P1 WinRate: 100.0%, Total Score: 220, Fav: Ironclad Warrior
[PASS] Test 15: Data Persistence Across Reconnect  | Connection re-established cleanly; user account and history intact
[PASS] Test 16: Transaction Rollback on DB Error   | Corrupt match safely rolled back; table row count unchanged
```

---

## 7. Web Backend REST API Test Execution (`BackendTestSuite.java` & `test_e2e_supabase_and_features.py`)

Harness: `BackendTestSuite.java` & `test_e2e_supabase_and_features.py`  
Result: **26 / 26 End-to-End REST Checks Passed (100% Success)**

| Endpoint | Method | Test Verification | Status |
| :--- | :--- | :--- | :--- |
| `/api/system/status` | `GET` | Returns active engine `SQLITE_LOCAL`, Supabase flag `false` | `PASS` |
| `/api/characters` | `GET` | Returns 6 canonical fighters with stats and image URLs | `PASS` |
| `/api/auth/register` | `POST` | Registers dual players with salted SHA-256 password hash | `PASS` |
| `/api/auth/login` | `POST` | Issues distinct base64 session tokens for P1 and P2 | `PASS` |
| `/api/matches` | `POST` | Rejects self-match payload (`P1 == P2`) with code 400 | `PASS` |
| `/api/matches` | `POST` | Saves 3-round tournament match with round scores atomically | `PASS` |
| `/api/matches/{id}` | `GET` | Retrieves full match telemetry matching submitted scores | `PASS` |
| `/api/players/{id}/stats` | `GET` | Computes accurate win rate %, total score, matches played | `PASS` |
| `/api/players/{id}/matches` | `GET` | Retrieves player's individual combat match history log | `PASS` |
| `/api/leaderboard` | `GET` | Ranks players by wins/win-rate/score with favorite fighter | `PASS` |

---

## 8. Current SQLite Database Live Audit

Database File: `clash_of_bytes.db` (Verified via `testing.DumpDatabase`)

- **Registered User Accounts**: **26 Users** (All hashed with SHA-256 + 32-char salt)
- **Completed Matches**: **13 Tournament Matches**
- **Match Player Entries**: **26 Player-Match Rows**
- **Individual Round Telemetry Rows**: **24 Rounds**
- **Canonical Character Roster**: **6 Fighters** (Ironclad Warrior, Blaze Berserker, Shadow Ninja, Cyber Brawler, Aura Paladin, Titan Gladiator)

---

## 9. Final Test Certification

All four academic subject modules and the full-stack web arena platform have been compiled, executed, and verified against all criteria specified in the MIT AOE Exam Reform Guidelines.

**Final Test Verdict: 100% PASSED (Zero Failures / Zero Warnings)**

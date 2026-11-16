# Course Outcome 4 (CO4): File Handling & Java Generics

**Course**: Problem Solving Using Object-Oriented Programming (Java) (2310217L)  
**Project**: Clash of Bytes – 2D Local Multiplayer Fighting Game  
**Milestone**: Task 4 Implementation  

---

## 1. Overview of Task 4 Implementation

Task 4 introduces enterprise-grade persistence and parametric polymorphism to the fighting game architecture:

1. **File Handling (`CO4.FileHandling`)**:
   - `FighterDataFileManager`: Provides persistent storage for fighter statistics in CSV format with both overwrite and append support.
   - `MatchHistoryFileManager`: Manages persistent logging of match records in append mode with timestamped telemetry.
   - Defensive error handling: Automatic directory generation, `try-with-resources` construct (automatic `AutoCloseable` stream closing), explicit `FileNotFoundException` and `IOException` guards.

2. **Java Generics (`CO4.Generics`)**:
   - `GenericFighterRepository<T extends BaseFighter>`: Type-safe repository restricting elements to `BaseFighter` and its derived classes (`WarriorFighter`, `NinjaFighter`, `CyberBrawler`, `PaladinFighter`, `BerserkerWarrior`, `HybridGladiator`).
   - `GenericBattleArchive<T>`: Unbounded generic archive demonstrating generic class design, generic methods (`computeAverage()`), and functional lambdas.
   - Compile-Time Safety: Eliminates manual runtime downcasting (`(WarriorFighter) obj`) and eliminates `ClassCastException`.

---

## 2. Academic Mapping & Key Concepts

| Concept | Class / Method | Implementation Details | Academic Value / Why Used |
| :--- | :--- | :--- | :--- |
| **Character Stream Writer** | `FighterDataFileManager.saveFightersToFile()` | Uses `BufferedWriter` wrapping `FileWriter(file, false)`. Writes CSV headers and fighter stats. | High-throughput buffered I/O; overwrites stale data safely. |
| **File Append Mode** | `MatchHistoryFileManager.appendMatchLog()` | Uses `BufferedWriter(new FileWriter(file, true))`. Appends match history records. | Preserves past match history without overwriting previous sessions. |
| **Character Stream Reader** | `FighterDataFileManager.loadFighterRecords()` | Uses `BufferedReader.readLine()` to parse lines. | Efficient line-by-line reading with minimal memory consumption. |
| **Defensive Exception Guards** | `loadFighterRecords()` & `readAllMatchLogs()` | Throws `FileNotFoundException` when target file is missing; handles `IOException`. | Prevents abrupt application crash and enables graceful recovery. |
| **Bounded Type Parameter** | `GenericFighterRepository<T extends BaseFighter>` | Declares `<T extends BaseFighter>`. | Enforces that only valid fighter entities can be added; enables calling `getName()`, `getHealth()`, `getBaseAttack()` directly. |
| **Type Safety Proof** | `CO4PracticalTestSuite` | Compares generic repository behavior against specialized `<WarriorFighter>` sub-repositories. | Eliminates runtime casting errors; compiler flags invalid types at compile-time. |
| **Generic Method** | `GenericBattleArchive.computeAverage()` | `<E> double computeAverage(List<E>, Function<E, Double>)` | Reusable mathematical utility applicable across any data model. |

---

## 3. How to Run the Tests

To run the automated CO4 test suite directly:
```cmd
cd Clash_Of_Bytes
run_java_co4.bat
```

Or via the unified interactive `CombatManager`:
```cmd
run_java.bat
# Select option [5] for CO4 test suite
# Select option [6] to export fighter roster to file
# Select option [7] to view stored match logs
```

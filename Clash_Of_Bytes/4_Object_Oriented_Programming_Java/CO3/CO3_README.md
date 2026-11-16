# Course Outcome 3 (CO3): Java Swing GUI, Exception Handling, Packages & Collections

**Subject**: Problem Solving Using OOP (Java)  
**Course Outcome**: CO3 | Task 3 Practical Implementation  
**Project**: Clash of Bytes — 2D Fighting Game & Cyberpunk Launcher  

---

## 1. CO3 Practical Syllabus Mapping

| Category | Concept | File | Implementation | Why Used & Architectural Benefit |
| :--- | :--- | :--- | :--- | :--- |
| **Packages** | Modular Package Hierarchy | `CO3/Exceptions/*`, `CO3/Collections/*`, `CO3/SwingGUI/*`, `CO3/Testing/*` | Structured sub-packages organizing domain errors, data structures, graphical UI, and unit tests. | Decouples concerns, prevents namespace collision, and maintains backward compatibility with existing launch scripts. |
| **Exceptions** | Base Checked Exception | `CO3/Exceptions/ClashOfBytesException.java` | Extends `Exception` with `errorCode`, `timestamp`, and `details`. | Provides a unified, structured root for all domain-specific errors with consistent formatting and error telemetry. |
| **Exceptions** | Fighter Selection Error | `CO3/Exceptions/InvalidFighterSelectionException.java` | Extends `ClashOfBytesException`; handles negative/out-of-bounds indices, missing keys, and disallowed mirror matches. | Prevents crashes when invalid roster indices or unknown character names are queried. |
| **Exceptions** | Match Configuration Error | `CO3/Exceptions/InvalidMatchConfigurationException.java` | Validates rounds `[1, 9]`, round timers `[10s, 300s]`, and handicap factors `[0.25x, 5.0x]`. | Enforces strict domain limits on match parameters before arcade or simulation execution starts. |
| **Exceptions** | Combat State Violation | `CO3/Exceptions/CombatValidationException.java` | Validates moves on fainted fighters (HP <= 0), negative damage/healing, and queue underflow. | Ensures the game combat engine remains mathematically consistent and deterministic. |
| **Exceptions** | Missing Binary / Asset | `CO3/Exceptions/ResourceNotFoundException.java` | Checks for `clash_of_bytes.exe` presence before attempting execution. | Gives user clear, actionable compilation instructions (`build.bat`) rather than an opaque OS process error. |
| **Collections** | Map (`LinkedHashMap`) | `CO3/Collections/FighterRosterRegistry.java` | `LinkedHashMap<String, BaseFighter>` for character registration and lookup. | Delivers fast $O(1)$ lookup by fighter name while preserving deterministic insertion order for menus and UI. |
| **Collections** | List (`ArrayList`) | `CO3/Collections/FighterRosterRegistry.java` | `ArrayList<BaseFighter>` for indexed dropdown binding and stat sorting. | Provides instantaneous $O(1)$ index access matching Swing `JComboBox` models and supports `Collections.sort` with custom `Comparator`. |
| **Collections** | Set (`LinkedHashSet`) | `CO3/Collections/FighterRosterRegistry.java` | `LinkedHashSet<String>` tracking distinct fighter archetypes. | Eliminates duplicate archetype classifications while preserving discovery order for class analytics. |
| **Collections** | Queue (`ArrayDeque`) | `CO3/Collections/CombatActionQueue.java` | `Queue<CombatAction>` backed by `ArrayDeque<CombatAction>` for turn processing. | Strictly enforces FIFO (First-In-First-Out) sequential turn ordering without the node-allocation overhead of `LinkedList`. |
| **Collections** | Chronological History List | `CO3/Collections/MatchHistoryManager.java` | `List<MatchRecord>` storing complete match telemetry and timestamps. | Maintains immutable chronological records of completed bouts, feedable into Swing `JTable`. |
| **Collections** | Win Leaderboard Map | `CO3/Collections/MatchHistoryManager.java` | `Map<String, Integer>` aggregating win counts per fighter. | Delivers instantaneous tally updates and leaderboard analytics. |
| **Swing UI** | Top-Level Container | `CO3/SwingGUI/GameLauncherGUI.java` | `GameLauncherGUI extends JFrame` with dark cyberpunk theme. | Serves as the primary desktop window hosting all controls and sub-panels. |
| **Swing UI** | Intermediate Containers | `CO3/SwingGUI/GameLauncherGUI.java` | Modular `JPanel` cards with `BorderFactory` compound and titled borders. | Groups player stats, match controls, and game modes into clean cyber panels. |
| **Swing UI** | Interactive Table | `CO3/SwingGUI/GameLauncherGUI.java` | `JTable` inside `JScrollPane` displaying match telemetry. | Presents tabular match history with formatted columns and center-aligned data. |
| **Swing UI** | Real-Time Input Validation | `CO3/SwingGUI/GameLauncherGUI.java` | Live status banner updating dynamically on combo-box or checkbox changes. | Gives instant visual feedback (green checkmark or red warning) before match launch. |
| **Swing UI** | Concurrency & Threading | `CO3/SwingGUI/GameLauncherGUI.java` | Swing Event Dispatch Thread (`SwingUtilities.invokeLater`) + async `Thread` launch. | Prevents GUI freezing when launching external arcade binaries or executing simulations. |

---

## 2. Deep Dive: Collections Rationale

1. **`LinkedHashMap<String, BaseFighter>` in `FighterRosterRegistry`**:
   - *Why*: Combines the lightning-fast $O(1)$ search capability of a hash table with the predictable iteration order of a doubly linked list. Looking up a fighter by name (e.g. `"Ironclad Warrior"`) does not require iterating through arrays.
2. **`ArrayList<BaseFighter>` in `FighterRosterRegistry`**:
   - *Why*: Direct zero-based index access ($O(1)$) directly synchronizes with Swing `JComboBox.getSelectedIndex()`. Allows sorting using `Comparator.comparingDouble(BaseFighter::getHealth).reversed()` without modifying the primary registry map.
3. **`ArrayDeque<CombatAction>` in `CombatActionQueue`**:
   - *Why*: Fighting game turns require strict sequential FIFO execution (Move 1 executes before Move 2). `ArrayDeque` is backed by a resizable circular array, which is significantly more cache-friendly and consumes less heap memory than pointer-heavy `LinkedList`.
4. **`LinkedHashSet<String>` in `FighterRosterRegistry`**:
   - *Why*: Automatically deduplicates character classes when multiple fighters share common class prototypes, while preserving display order.
5. **`List<MatchRecord>` & `Map<String, Integer>` in `MatchHistoryManager`**:
   - *Why*: Decouples match history logging into an unmodifiable list of chronological records (`MatchRecord`) and a high-speed win-loss accumulator map.

---

## 3. Custom Exception Hierarchy

```
java.lang.Exception
   │
   └── CO3.Exceptions.ClashOfBytesException (Base Checked Exception)
         ├── InvalidFighterSelectionException (Index out-of-bounds, unmapped names, mirror match violations)
         ├── InvalidMatchConfigurationException (Illegal rounds, invalid timers, out-of-range handicaps)
         ├── CombatValidationException (Attacking fainted fighter, negative damage/heal, action queue underflow)
         └── ResourceNotFoundException (Missing clash_of_bytes.exe binary or corrupted configs)
```

### Exception Recovery Model:
- **Zero Silent Failures**: All validation failures are intercepted by domain-specific `catch` blocks.
- **GUI Continuity**: In `GameLauncherGUI`, caught exceptions trigger styled `JOptionPane` alert modals and update the bottom status bar without terminating or hanging the application.
- **Console Resilience**: In `CombatManager`, invalid numeric or string selections output helpful hints and prompt the user again without aborting the program.

---

## 4. Test Suite Execution & Results

The automated test suite `CO3.Testing.CO3PracticalTestSuite` verifies 32 test cases across all requirements:

| # | Test Category | Specific Assertion | Status |
| :-: | :--- | :--- | :---: |
| 1 | Valid Fighter Selection | Valid index 0 returns "Ironclad Warrior" | **PASS** |
| 2 | Valid Fighter Selection | Valid name query returns "Shadow Ninja" | **PASS** |
| 3 | Valid Fighter Selection | Roster registry size matches available fighters (>= 6) | **PASS** |
| 4 | Index Bounds Exception | Negative index (`-1`) throws `InvalidFighterSelectionException` (`ERR_FIGHTER_INDEX_OUT_OF_BOUNDS`) | **PASS** |
| 5 | Index Bounds Exception | Out-of-bounds index (`999`) throws `InvalidFighterSelectionException` | **PASS** |
| 6 | Index Bounds Exception | Both boundary extremes caught and handled | **PASS** |
| 7 | Name Lookup Exception | Unregistered character name throws `ERR_FIGHTER_LOOKUP_FAILED` | **PASS** |
| 8 | Name Lookup Exception | Null or empty name string throws `ERR_FIGHTER_LOOKUP_FAILED` | **PASS** |
| 9 | Selection Validation | Distinct fighter pairs (Warrior vs Ninja) permitted | **PASS** |
| 10 | Selection Validation | Disallowed mirror match (Warrior vs Warrior) throws exception | **PASS** |
| 11 | Match Config Validation | Zero rounds (`0`) throws `InvalidMatchConfigurationException` | **PASS** |
| 12 | Match Config Validation | Negative round timer (`-10s`) throws `InvalidMatchConfigurationException` | **PASS** |
| 13 | Match Config Validation | Excessive handicap factor (`10.5x`) throws `InvalidMatchConfigurationException` | **PASS** |
| 14 | Collections Queue | `CombatActionQueue` enqueues 3 moves successfully | **PASS** |
| 15 | Collections Queue | `ArrayDeque` enforces strict FIFO ordering (Turn 1 -> Turn 2 -> Turn 3) | **PASS** |
| 16 | Collections Queue | Queue is correctly emptied after sequential dequeue | **PASS** |
| 17 | Queue Underflow Exception| Dequeueing on empty action queue throws `CombatValidationException` | **PASS** |
| 18 | Queue Underflow Exception| Peeking into empty action queue throws `CombatValidationException` | **PASS** |
| 19 | Combat State Exception | Attack attempted on fainted target (HP = 0) throws `CombatValidationException` | **PASS** |
| 20 | Combat State Exception | Negative damage value (`-25.0`) throws `CombatValidationException` | **PASS** |
| 21 | Collections Sorting | `ArrayList<BaseFighter>` sorted by Health descending via `Comparator` | **PASS** |
| 22 | Collections Sorting | `ArrayList<BaseFighter>` sorted by Base Attack descending | **PASS** |
| 23 | Collections Set | `LinkedHashSet<String>` collects unique archetypes without duplication | **PASS** |
| 24 | Match History Safety | Empty history query safely returns `null` rather than throwing `IndexOutOfBoundsException` | **PASS** |
| 25 | Match History Safety | Initial match tally is 0 | **PASS** |
| 26 | Match History Data | Individual match records contain valid IDs (1, 2, 3) | **PASS** |
| 27 | Match History List | Matches recorded in chronological `List<MatchRecord>` | **PASS** |
| 28 | Match History List | Latest match record retrieved accurately | **PASS** |
| 29 | Match History Map | Win tally map correctly counts Warrior wins (2) | **PASS** |
| 30 | Match History Map | Win tally map correctly counts Ninja wins (1) | **PASS** |
| 31 | Match History Map | Win tally map correctly handles zero-win fighters (Cyber Brawler: 0) | **PASS** |
| 32 | Error Recovery & State | Normal match simulation succeeds immediately after multiple caught errors | **PASS** |

**Summary**: **32 / 32 Unit Tests Passed (100.0% Pass Rate)** with **0 compiler warnings** under `javac -Xlint:all`.

---

## 5. Build and Run Commands

### Compile All Modules:
```cmd
build.bat
```

### Run Cyberpunk Swing GUI Launcher:
```cmd
run_gui.bat
```
*(Or directly via Java)*:
```cmd
java -cp 4_Object_Oriented_Programming_Java\bin CO3.SwingGUI.GameLauncherGUI
```

### Run Master OOP Combat Manager (Console):
```cmd
run_java.bat
```
*(Or directly via Java)*:
```cmd
java -cp 4_Object_Oriented_Programming_Java\bin Main.CombatManager
```

### Run Automated CO3 Test Suite:
```cmd
java -cp 4_Object_Oriented_Programming_Java\bin CO3.Testing.CO3PracticalTestSuite
```

# Clash of Bytes — Milestone 1 (Complete Academic Edition)

> **2D Retro Arcade Fighting Game & Academic Practical Integration Engine**  
> Unified across **Computer Graphics (CG)**, **Data Structures & Algorithms (DSA)**, **Computer Organization & Architecture (COA)**, and **Object-Oriented Programming (Java OOP)**.

---

## 🚀 Quick Start (1-Click Run)

All dependencies and runtimes (including embedded portable OpenJDK 21) are bundled inside. No extra installations required!

| Script | Purpose | Description |
| :--- | :--- | :--- |
| **`run_gui.bat`** | **Java Swing Arcade Launcher & Auth** | Dual-player login, character selection lobby, search bar, match history slots, standings table & syllabus map. |
| **`run_game.bat`** | **C++ OpenGL 2D Arcade Engine** | Launches the main fighting arena (Victorian Mansion / Dojo Practice). |
| **`run_java_co4.bat`** | **Java OOP CO4 Practical Test Suite** | 1-Click launcher for CO4: Parametric Generics & Persistent File Handling (13/13 Tests Passing). |
| **`run_java.bat`** | **Java OOP Combat Manager Console** | Interactive console combat simulation and practical runner (Practicals 1 to 4). |
| **`run_auth.bat`** | **Supabase Auth Portal** | Direct launcher for Player 1 & Player 2 authentication and profile sync. |
| **`run_db_gui.bat`** | **Match History & Database Dossier** | Direct launcher for match slot telemetry and leaderboard viewer. |
| **`build.bat`** | **Full Clean Rebuild** | Compiles all C++ game engines and Java OOP test suites. |

---

## 🎮 Game Controls & Hotkeys

### Combat Controls (Local 2-Player)
- **Player 1 (Left / Cyan)**:
  - **Move / Jump / Crouch**: `W` (Jump), `A` (Left), `S` (Crouch), `D` (Right)
  - **Attacks**: `F` (Punch), `G` (Kick), `H` (Block), `R` (Special Attack / Hadoken / Cyclone)
- **Player 2 (Right / Magenta)**:
  - **Move / Jump / Crouch**: `↑` (Jump), `←` (Left), `↓` (Crouch), `→` (Right)
  - **Attacks**: `J` / `Num 1` (Punch), `K` / `Num 2` (Kick), `L` / `Num 3` (Block), `U` / `Num 5` (Ultra Special / Beam)

### In-Game Shortcuts & Academic Telemetry
- `[M]` / `[F3]` : Switch instantly between **Tutorial Practice Mode** and **Real Fight Mode**
- `[1]` / `[T]` : Select Tutorial Practice Mode (action overlays & combo guides)
- `[2]` / `[ENTER]` : Select Real Fight Mode (clean authentic arcade combat)
- `[F1]` : Toggle Real-Time Hitbox Outlines (AABB Collision System)
- `[F2]` : Toggle In-Game Modes & Guidelines Menu
- `[F4]` : Cycle Player 1 Fighter Class
- `[F5]` : Cycle Player 2 Fighter Class
- `[TAB]` : Toggle Live 4-Course Syllabus Alignment Telemetry Overlay
- `[ESC]` : Reset / Restart Current Match

---

## ⚔️ Characters & Fighter Roster
1. **Elena (Agile Combatant)** — Rapid strikes, Hadoken energy wave, Cyclone Spin Kick.
2. **Cyber Brawler (Cybernetic Enforcer)** — Heavy titanium plating, Plasma Overdrive Beam, high-impact defense.
3. **Ironclad Warrior (Tank / Knight)** — High health pool, devastating overhead slash, shield absorption.
4. **Shadow Ninja (Speed Assassin)** — Shuriken projectile, aerial dash, high critical strike chance.
5. **Holy Paladin (Hybrid Guard)** — Radiant aura, holy hammer smash, defensive recovery.
6. **Gladiator (Arena Champion)** — Trident thrust, net immobilization, relentless combo pressure.

---

## 🏛️ Arenas & Visual Backgrounds
- **Victorian Mansion Battle Arena**: Procedural arched gothic windows, night sky gradient, chandelier candelabras with flickering flame VFX, red velvet drapes, and hanging portrait galleries.
- **Dojo Practice Arena**: Traditional wooden floor, sliding shoji screens, sparring banners, continuous health/meter regen, and zero-death training loop.
- **Cyberpunk Arcade Launcher**: Dark neon theme, high-contrast fighter cards, live match telemetry, and dual account badges.

---

## 🔐 Dual Local Player Authentication & Supabase Cloud Sync
- **Side-by-Side Dual Login**: Both Player 1 and Player 2 log into their respective accounts on the same screen before the match starts.
- **Supabase Cloud 2-Row Match History**:
  - Every match produces **two linked rows** in `public.match_history` (one record for Player 1, one record for Player 2) tracking username, chosen character, result (WIN/LOSS), rounds won, and timestamp.
  - Automatically updates player records in `public.profiles` (`+1 win`, `+1 loss`).
- **Single Players Standings Table**:
  - Lists every individual player ranked one below the other with Total Matches, Wins, Losses, Win Rate %, Peak Damage, and Favorite Character.
- **Match Slots History & Details Viewer**:
  - Interactive GUI allows browsing all previous match slots. Clicking **"🔍 View Slot Details"** opens full telemetry showing who won, who lost, total damage dealt, and round breakdowns.
- **Offline Resilient**: Fully mirrored in local SQLite database (`clash_of_bytes.db`) if internet is unavailable.

---

## 📚 4-Course Academic Mapping & Practical Coverage
- **Computer Graphics (CG)**: Bresenham Line & Circle algorithms, Gouraud shading, Cohen-Sutherland line clipping, scanline polygon health rendering, 2D/3D affine matrix transformations.
- **Data Structures & Algorithms (DSA)**: 1D Move Array catalog, 2D Sparse Matrix fast transpose, Singly Linked List particle systems, Doubly Linked List match scores, Circular Linked List arena buffs, Stack/Queue combat simulator.
- **Computer Organization & Architecture (COA)**: Real 80386 ALU arithmetic/logic pipeline, inline x86-64 assembly damage calculation engine, Hex to Packed-BCD score converter, Control Unit micro-instruction decoder.
- **Object-Oriented Programming (Java OOP)**:
  - **CO1 (Fundamentals & Strings)**: Primitive types, arrays, conditionals, `StringBuilder` & `StringBuffer` performance logging, stance/timing evaluation.
  - **CO2 (Inheritance & Polymorphism)**: All 5 inheritance models (Single, Multilevel, Hierarchical, Multiple via Interfaces, Hybrid), abstract classes, dynamic method dispatch.
  - **CO3 (Collections & Exception Handling)**: Custom exception hierarchy (`ClashOfBytesException`), input validation guards, Java Collections (`LinkedHashMap`, `ArrayList`, `ArrayDeque`, `LinkedHashSet`, `HashMap`), Swing Event Dispatch Thread (EDT) GUI architecture, SQLite JDBC / Supabase REST cloud synchronization (40/40 Unit Tests Passing).
  - **CO4 (Generics & File Handling)**: Parametric polymorphism (`GenericFighterRepository<T extends BaseFighter>`), generic methods (`GenericBattleArchive<T>.computeAverage()`), character stream I/O (`BufferedWriter` & `BufferedReader`), append-mode match history logging (`FileWriter(file, true)`), `try-with-resources` resource management, defensive `FileNotFoundException` & `IOException` recovery (13/13 Unit Tests Passing). Total Automated Test Suite: 53/53 Tests (100% Success).

package Main;

import CO1.JavaFundamentals.CombatAnalytics;
import CO1.JavaFundamentals.HitTimingIndicator;
import CO1.JavaFundamentals.HitTimingIndicator.FighterStance;
import CO1.JavaFundamentals.HitTimingIndicator.HitTimingRating;
import CO1.StringHandling.CombatLogger;
import CO2.OOP.BaseFighter;
import CO2.OOP.BerserkerWarrior;
import CO2.OOP.CyberBrawler;
import CO2.OOP.HybridGladiator;
import CO2.OOP.NinjaFighter;
import CO2.OOP.PaladinFighter;
import CO2.OOP.WarriorFighter;
import CO3.Collections.CombatAction;
import CO3.Collections.CombatActionQueue;
import CO3.Collections.FighterRosterRegistry;
import CO3.Collections.MatchHistoryManager;
import CO3.Collections.MatchRecord;
import CO3.Exceptions.CombatValidationException;
import CO3.Exceptions.InvalidFighterSelectionException;
import CO3.Exceptions.InvalidMatchConfigurationException;
import CO3.Testing.CO3PracticalTestSuite;
import CO4.FileHandling.FighterDataFileManager;
import CO4.FileHandling.MatchHistoryFileManager;
import CO4.Generics.GenericBattleArchive;
import CO4.Generics.GenericFighterRepository;
import CO4.Testing.CO4PracticalTestSuite;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

@FunctionalInterface
interface MoveCalculator {
    double execute(double attack, double defense, double multiplier);
}

/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcomes: CO1, CO2, CO3, CO4 | Practicals 01 to 04
 * Topic: Master OOP Combat Manager & Interprocess Game Launcher
 * 
 * CO3 Enhancements:
 * - Collections Framework: LinkedHashMap, ArrayList, ArrayDeque (Queue), LinkedHashSet, HashMap
 * - Custom Exceptions: ClashOfBytesException hierarchy with full edge-case recovery
 * - Input Validation: Robust validation for match configurations, fighters, and combat actions
 * - Java Swing GUI: Cyberpunk Dashboard running on Event Dispatch Thread (EDT)
 */
public class CombatManager {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Display Guidelines ("How to Play" screen) & Select Mode
        HitTimingIndicator.displayGameGuidelines();
        HitTimingIndicator.GameMode selectedMode = HitTimingIndicator.selectGameMode(scanner);
        if (selectedMode == HitTimingIndicator.GameMode.TUTORIAL_MODE) {
            HitTimingIndicator.renderTutorialMoveGuide(scanner);
        } else {
            HitTimingIndicator.waitForEnter(scanner);
        }

        System.out.println("=======================================================================");
        System.out.println("  SUBJECT: PROBLEM SOLVING USING OOP (JAVA)                            ");
        System.out.println("  CLASH OF BYTES - PRACTICALS 1 TO 4 GAME MATCH MANAGER                ");
        System.out.println("=======================================================================");

        // =======================================================================
        // PRACTICAL 04 & 03: Instantiating Characters across All 5 Inheritance Models
        // =======================================================================
        // 1. Single Inheritance: BaseFighter -> WarriorFighter
        WarriorFighter warrior = 
                new WarriorFighter("Ironclad Warrior", 115.0, 18.0, 6.0);

        // 2. Multilevel Inheritance: BaseFighter -> WarriorFighter -> BerserkerWarrior
        BerserkerWarrior berserker = 
                new BerserkerWarrior("Blaze Berserker", 130.0, 22.0, 4.0, 2.0);

        // 3. Hierarchical Inheritance: BaseFighter -> NinjaFighter & CyberBrawler
        NinjaFighter ninja = 
                new NinjaFighter("Shadow Ninja", 95.0, 16.0, 4);
        CyberBrawler cyber = 
                new CyberBrawler("Cyber Brawler", 110.0, 19.0, 3);

        // 4. Multiple Inheritance (via Interfaces): implements ShieldBearer, ElementalAttacker
        PaladinFighter paladin = 
                new PaladinFighter("Aura Paladin", 120.0, 17.0, "Holy Radiance", 7.5);

        // 5. Hybrid Inheritance: Multilevel + Multiple Interface Contracts
        HybridGladiator gladiator = 
                new HybridGladiator("Titan Gladiator", 140.0, 24.0, 8.0, 2.2, "Colosseum of Bytes");

        BaseFighter[] roster = {warrior, berserker, ninja, cyber, paladin, gladiator};

        // =======================================================================
        // CO3 COLLECTIONS FRAMEWORK: Central Fighter Roster Registry
        // =======================================================================
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();
        MatchHistoryManager historyManager = MatchHistoryManager.getInstance();

        System.out.println("\n=======================================================================");
        System.out.println("  CO3 COLLECTIONS FRAMEWORK DEMONSTRATION                              ");
        System.out.println("=======================================================================");
        System.out.println("  1. LinkedHashMap<String, BaseFighter> (O(1) Keyed Retrieval):");
        try {
            BaseFighter retrieved = registry.getFighterByName("Blaze Berserker");
            System.out.printf("     - Successfully looked up: %s | Base HP: %.1f | Class: %s%n",
                    retrieved.getName(), retrieved.getHealth(), retrieved.getClass().getSimpleName());
        } catch (InvalidFighterSelectionException ex) {
            System.out.println("     - [ERROR] " + ex.getFormattedMessage());
        }

        System.out.println("\n  2. ArrayList<BaseFighter> with Custom Comparators (Sorting):");
        List<BaseFighter> sortedByAtk = registry.getFightersSortedByAttack();
        System.out.println("     - Top Strikers (Sorted by Attack Descending):");
        for (int i = 0; i < Math.min(3, sortedByAtk.size()); i++) {
            System.out.printf("       [%d] %-18s -> %4.1f ATK%n", (i + 1), sortedByAtk.get(i).getName(), sortedByAtk.get(i).getBaseAttack());
        }

        System.out.println("\n  3. LinkedHashSet<String> (Unique Archetype Classifications):");
        Set<String> uniqueArchetypes = registry.getUniqueArchetypes();
        System.out.println("     - Registered Archetypes: " + String.join(", ", uniqueArchetypes));

        // =======================================================================
        // PRACTICAL 01: Arrays, Loops, Statistical Analytics & Control Flow
        // =======================================================================
        CombatAnalytics.demonstrateRosterAnalytics(roster);

        // =======================================================================
        // PRACTICAL 02: StringBuilder High-Performance Logging Benchmark
        // =======================================================================
        CombatLogger.demonstratePerformanceBenchmark();
        
        CombatLogger logger = new CombatLogger();
        logger.demonstrateStringBufferOperations(warrior.getName());

        // =======================================================================
        // PRACTICAL 04: Live Demonstration of All 5 Inheritance Models
        // =======================================================================
        System.out.println("\n=======================================================================");
        System.out.println("  PRACTICAL 04: ALL 5 TYPES OF JAVA INHERITANCE DEMONSTRATION          ");
        System.out.println("=======================================================================");
        System.out.println("  1. SINGLE INHERITANCE:");
        System.out.println("     - Hierarchy: BaseFighter -> WarriorFighter");
        System.out.printf("     - Concrete Object: %s | Base HP: %.1f | Shield Defense: %.1f%n",
                warrior.getName(), warrior.getHealth(), warrior.getShieldDefense());

        System.out.println("\n  2. MULTILEVEL INHERITANCE:");
        System.out.println("     - Hierarchy: BaseFighter -> WarriorFighter -> BerserkerWarrior");
        System.out.printf("     - Concrete Object: %s | Rage Multiplier: %.1fx | Shield Defense: %.1f%n",
                berserker.getName(), berserker.getRageMultiplier(), berserker.getShieldDefense());

        System.out.println("\n  3. HIERARCHICAL INHERITANCE:");
        System.out.println("     - Hierarchy: BaseFighter extended concurrently by distinct subclasses");
        System.out.printf("     - Subclass Branch A: %s (Stealth Duration: %d sec)%n", ninja.getName(), ninja.getStealthDuration());
        System.out.printf("     - Subclass Branch B: %s (Cyber Core Energy: %d units)%n", cyber.getName(), cyber.getCyberCoreEnergy());

        System.out.println("\n  4. MULTIPLE INHERITANCE (via Multiple Interfaces):");
        System.out.println("     - Hierarchy: PaladinFighter implements ShieldBearer, ElementalAttacker");
        System.out.println("     - (Resolves the Diamond Problem in Java via interface contracts)");
        paladin.deployAegisBarrier();
        paladin.blockIncomingDamage(32.0);
        paladin.castElementalStrike("Holy Radiance", paladin.getBaseAttack());

        System.out.println("\n  5. HYBRID INHERITANCE (Multilevel Class + Multiple Interfaces):");
        System.out.println("     - Hierarchy: HybridGladiator extends BerserkerWarrior implements ShieldBearer, ElementalAttacker");
        System.out.println("     - (Combines Class Inheritance Hierarchy with Interface Contracts)");
        gladiator.deployAegisBarrier();
        gladiator.blockIncomingDamage(48.0);
        gladiator.castElementalStrike("Chaos-Magma", gladiator.getBaseAttack());

        // =======================================================================
        // COMPILE-TIME POLYMORPHISM (Method Overloading)
        // =======================================================================
        System.out.println("\n=======================================================================");
        System.out.println("  COMPILE-TIME POLYMORPHISM (Method Overloading)                      ");
        System.out.println("=======================================================================");
        warrior.attack(); // Basic attack
        warrior.attack(1.5); // Attack with multiplier
        warrior.attack(45.0, berserker.getShieldDefense()); // Attack bypassing defense

        // =======================================================================
        // PRACTICAL 03: Parent-Reference Runtime Polymorphism (Dynamic Method Dispatch)
        // =======================================================================
        System.out.println("\n=======================================================================");
        System.out.println("  RUNTIME POLYMORPHISM (Parent-Reference Dynamic Method Dispatch)     ");
        System.out.println("=======================================================================");
        BaseFighter polymorphicRef;

        // Parent reference pointing to WarriorFighter instance
        polymorphicRef = warrior;
        System.out.print("  [Parent Ref -> WarriorFighter] : ");
        polymorphicRef.performSpecialMove();

        // Parent reference pointing to NinjaFighter instance
        polymorphicRef = ninja;
        System.out.print("  [Parent Ref -> NinjaFighter]   : ");
        polymorphicRef.performSpecialMove();

        // =======================================================================
        // PRACTICAL 03: Lambda Expressions & Functional Interfaces
        // =======================================================================
        System.out.println("\n=======================================================================");
        System.out.println("  PRACTICAL 03: FUNCTIONAL INTERFACES & LAMBDA EXPRESSIONS             ");
        System.out.println("=======================================================================");
        MoveCalculator lightAttack = (atk, def, mult) -> Math.max(BaseFighter.MINIMUM_DAMAGE, (atk - def) * mult);
        MoveCalculator heavyAttack = (atk, def, mult) -> Math.max(BaseFighter.MINIMUM_DAMAGE * 1.5, (atk * 1.5 - def) * mult);
        MoveCalculator criticalArt = (atk, def, mult) -> Math.max(BaseFighter.MINIMUM_DAMAGE * 2.0, ((atk + 12.0) - def) * mult * 1.8);

        double sampleLightDmg = lightAttack.execute(warrior.getBaseAttack(), 4.0, 1.0);
        System.out.printf("  [Lambda Execution] Calculated Light Attack Output: %.1f dmg%n", sampleLightDmg);

        // =======================================================================
        // CO3 COLLECTIONS: ArrayDeque CombatActionQueue Turn Simulation
        // =======================================================================
        System.out.println("\n=======================================================================");
        System.out.println("  CO3 COLLECTIONS: QUEUE<COMBATACTION> (FIFO MOVE PROCESSING)          ");
        System.out.println("=======================================================================");
        CombatActionQueue combatQueue = new CombatActionQueue();
        try {
            combatQueue.enqueueAction(new CombatAction(1, warrior.getName(), ninja.getName(), 
                CombatAction.ActionType.FAST_PUNCH, warrior.getBaseAttack(), 1.0, "Tactical Opener"));
            combatQueue.enqueueAction(new CombatAction(2, ninja.getName(), warrior.getName(), 
                CombatAction.ActionType.HEAVY_KICK, ninja.getBaseAttack(), 1.5, "Evasive Counter"));
            combatQueue.enqueueAction(new CombatAction(3, warrior.getName(), ninja.getName(), 
                CombatAction.ActionType.SPECIAL_ART, warrior.getBaseAttack() + 8.0, 1.8, "Titan Shield Impact"));

            System.out.printf("  Action Queue Initialized with %d moves (ArrayDeque FIFO):%n", combatQueue.size());
            while (!combatQueue.isEmpty()) {
                CombatAction action = combatQueue.dequeueNextAction();
                System.out.println("  -> Dequeued & Executed: " + action.toString());
            }
        } catch (CombatValidationException ex) {
            System.out.println("  [ERROR] " + ex.getFormattedMessage());
        }

        // =======================================================================
        // LIVE FIRST-TIME PLAYER "WHEN TO HIT" INDICATOR DEMONSTRATION
        // =======================================================================
        System.out.println("\n=======================================================================");
        System.out.println("  LIVE MATCH COMBAT: FIRST-TIME PLAYER \"WHEN TO HIT\" INDICATOR         ");
        System.out.println("=======================================================================");

        // Turn 1: Opponent (Shadow Ninja) whiffs an attack and drops into RECOVERY
        System.out.println("\n[*] Turn 1: Shadow Ninja whiffs a jump kick and enters Recovery Stance!");
        HitTimingIndicator.renderHitIndicator(ninja, FighterStance.RECOVERY_VULNERABLE);

        // Player strikes during the sweet spot!
        HitTimingRating rating1 = HitTimingIndicator.evaluateStrikeTiming(FighterStance.RECOVERY_VULNERABLE, 2);
        System.out.println("  Strike Rating   : " + rating1.getDescription());

        // Practical 02 Logger in Action
        logger.logBanner("ROUND 1: SWEET SPOT EXPLOITATION");

        // Warrior attacks Ninja with sweet-spot critical boost
        double dmg1 = heavyAttack.execute(warrior.getBaseAttack(), 4.0, rating1.getDamageMultiplier());
        ninja.takeDamage(dmg1);
        logger.logAction(warrior.getName(), ninja.getName(), "Heavy Greatsword Slash (Sweet Spot Critical)", dmg1, ninja.getHealth());

        // Turn 2: Ironclad Warrior raises Shield Guard; Opponent attempts strike
        System.out.println("\n[*] Turn 2: Ironclad Warrior raises Titan Shield, entering Fortified Guard!");
        HitTimingIndicator.renderHitIndicator(warrior, FighterStance.GUARD_FORTIFIED);

        HitTimingRating rating2 = HitTimingIndicator.evaluateStrikeTiming(FighterStance.GUARD_FORTIFIED, 1);
        System.out.println("  Strike Rating   : " + rating2.getDescription());

        // Ninja counters into fortified guard - mitigated
        double dmg2 = criticalArt.execute(ninja.getBaseAttack(), warrior.getShieldDefense() * 2.0, rating2.getDamageMultiplier());
        warrior.takeDamage(dmg2);
        logger.logAction(ninja.getName(), warrior.getName(), "Shadow Shuriken (Deflected by Fortified Guard)", dmg2, warrior.getHealth());

        // Record in Collections MatchHistoryManager
        historyManager.recordMatch(
            warrior.getName(), ninja.getName(), warrior.getName(), "TUTORIAL", 1, 
            warrior.getHealth(), ninja.getHealth(), (dmg1 + dmg2), "Console When-To-Hit Demo"
        );

        System.out.println("\n" + logger.getFormattedLog());

        // =======================================================================
        // INTERACTIVE MENU LOOP WITH INPUT VALIDATION (CO3 & CO4 RUNNER)
        // =======================================================================
        boolean running = true;
        while (running) {
            System.out.println("=======================================================================");
            System.out.println("  MATCH ACTIONS:");
            System.out.println("  [1] Launch C++ 60 FPS Real-Time Arcade Game (clash_of_bytes.exe)");
            System.out.println("  [2] View Complete Match Summary Log & Collections History (CO1-CO3)");
            System.out.println("  [3] Open Java Swing Game Launcher & Fighter Selection Dashboard (CO3 GUI)");
            System.out.println("  [4] Run Automated CO3 Practical Test Suite (Collections & Exceptions - 40 Tests)");
            System.out.println("  [5] Run Automated CO4 Practical Test Suite (File Handling & Generics - 13 Tests)");
            System.out.println("  [6] Export Fighter Roster to File (CO4 File Handling - FighterDataFileManager)");
            System.out.println("  [7] View Stored Match Logs from Disk (CO4 File Handling - MatchHistoryFileManager)");
            System.out.println("  [8] Exit");
            System.out.println("=======================================================================");
            System.out.print("Enter choice [1-8]: ");

            int choice = -1;
            if (!scanner.hasNextLine()) {
                System.out.println("\nNo further input detected. Exiting.");
                break;
            }
            String inputLine = scanner.nextLine().trim();
            try {
                choice = Integer.parseInt(inputLine);
                if (choice < 1 || choice > 8) {
                    throw new InvalidMatchConfigurationException("MenuChoice", choice, "Integer between 1 and 8");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("\n[VALIDATION WARNING] Please enter a valid numerical option [1-8].\n");
                continue;
            } catch (InvalidMatchConfigurationException ex) {
                System.out.println("\n[VALIDATION WARNING] " + ex.getFormattedMessage() + "\n");
                continue;
            }

            if (choice == 1) {
                try {
                    System.out.println("\n[*] Launching C++ Real-Time OpenGL Game Engine...");
                    ProcessBuilder pb = new ProcessBuilder(".\\clash_of_bytes.exe");
                    pb.start();
                    System.out.println("[SUCCESS] C++ Arcade Game launched successfully!");
                } catch (Exception ex) {
                    System.out.println("[ERROR] Could not launch clash_of_bytes.exe: " + ex.getMessage());
                    System.out.println("Make sure to run 'build.bat' first to compile the C++ engine.");
                }
            } else if (choice == 2) {
                System.out.println("\n" + logger.getFormattedLog());
                System.out.println("--- Recorded Matches in MatchHistoryManager (List<MatchRecord>) ---");
                for (MatchRecord mr : historyManager.getAllRecords()) {
                    System.out.println("  " + mr.toString());
                }
                System.out.println();
            } else if (choice == 3) {
                System.out.println("\n[*] Opening Cyberpunk Java Swing GUI Dashboard on Event Dispatch Thread...");
                javax.swing.SwingUtilities.invokeLater(() -> {
                    CO3.SwingGUI.GameLauncherGUI launcher = new CO3.SwingGUI.GameLauncherGUI();
                    launcher.setVisible(true);
                });
                System.out.println("[SUCCESS] Java Swing GUI opened on desktop!\n");
            } else if (choice == 4) {
                System.out.println("\n[*] Executing Automated CO3 Practical Test Suite (40 Tests)...\n");
                CO3PracticalTestSuite suite = new CO3PracticalTestSuite();
                suite.runAllTests();
            } else if (choice == 5) {
                System.out.println("\n[*] Executing Automated CO4 Practical Test Suite (File Handling & Generics - 13 Tests)...\n");
                CO4PracticalTestSuite.runAllTests();
            } else if (choice == 6) {
                System.out.println("\n[*] Exporting Active Fighter Roster to File using FighterDataFileManager (CO4)...");
                String rosterFilePath = "fighters_database.csv";
                FighterDataFileManager fileMgr = new FighterDataFileManager(rosterFilePath);
                boolean saved = fileMgr.saveFightersToFile(registry.getAllFighters());
                if (saved) {
                    System.out.println("[SUCCESS] Exported " + registry.getFighterCount() + " fighters to: " + rosterFilePath);
                    try {
                        List<String[]> rows = fileMgr.loadFighterRecords();
                        System.out.println("--- Verified Loaded Records from " + rosterFilePath + " (" + rows.size() + " fighters) ---");
                        for (String[] r : rows) {
                            System.out.println("  -> " + String.join(" | ", r));
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Failed reading back exported file: " + e.getMessage());
                    }
                } else {
                    System.out.println("[ERROR] Failed to export fighters to file.");
                }
                System.out.println();
            } else if (choice == 7) {
                System.out.println("\n[*] Reading Persistent Match Logs from Disk via MatchHistoryFileManager (CO4)...");
                String logPath = "match_history.log";
                MatchHistoryFileManager matchFileMgr = new MatchHistoryFileManager(logPath);
                try {
                    List<String> logs = matchFileMgr.readAllMatchLogs();
                    if (logs.isEmpty() && !historyManager.getAllRecords().isEmpty()) {
                        for (MatchRecord mr : historyManager.getAllRecords()) {
                            matchFileMgr.appendMatchLog(mr);
                        }
                    }
                } catch (Exception ex) {
                    for (MatchRecord mr : historyManager.getAllRecords()) {
                        matchFileMgr.appendMatchLog(mr);
                    }
                }
                matchFileMgr.displayStoredLogs();
                System.out.println();
            } else if (choice == 8) {
                System.out.println("\nSession concluded. Exiting Clash of Bytes.");
                running = false;
            }
        }
    }
}

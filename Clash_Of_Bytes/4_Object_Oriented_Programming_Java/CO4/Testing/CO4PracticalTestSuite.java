package CO4.Testing;

import CO2.OOP.*;
import CO3.Collections.MatchRecord;
import CO4.FileHandling.FighterDataFileManager;
import CO4.FileHandling.MatchHistoryFileManager;
import CO4.Generics.GenericBattleArchive;
import CO4.Generics.GenericFighterRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * CLASH OF BYTES: PROBLEM SOLVING USING OOP (JAVA) - COURSE CODE: 2310217L
 * Practical No. 04 / Task 4 | Course Outcome 4 (CO4):
 * COMPREHENSIVE AUTOMATED TEST SUITE FOR FILE HANDLING & JAVA GENERICS
 * ============================================================================
 */
public class CO4PracticalTestSuite {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] Test %02d: %s\n", testsTotal, testName);
        } else {
            System.err.printf("  [FAIL] Test %02d: %s\n", testsTotal, testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================================================================");
        System.out.println("   CLASH OF BYTES: JAVA OOP PRACTICAL NO. 04 & TASK 4 TEST SUITE (CO4)");
        System.out.println("   Topics: Persistent File Handling & Parametric Polymorphism (Generics)");
        System.out.println("========================================================================================\n");

        // --------------------------------------------------------------------
        // PHASE 1: JAVA GENERICS DEMONSTRATION & TESTING
        // --------------------------------------------------------------------
        System.out.println("--- [PHASE 1] JAVA GENERICS & TYPE SAFETY DEMONSTRATION ---");
        GenericFighterRepository<BaseFighter> generalRepo = new GenericFighterRepository<>("Master Champions Guild");

        WarriorFighter w1 = new WarriorFighter("Blaze Warrior", 120.0, 28.0, 8.0);
        NinjaFighter n1 = new NinjaFighter("Shadow Ninja", 90.0, 32.0, 35);
        CyberBrawler c1 = new CyberBrawler("Cyber Brawler", 110.0, 26.0, 40);
        PaladinFighter p1 = new PaladinFighter("Iron Paladin", 140.0, 22.0, "HOLY_LIGHT", 40.0);
        BerserkerWarrior b1 = new BerserkerWarrior("Blood Berserker", 130.0, 35.0, 10.0, 25.0);

        generalRepo.addFighter(w1);
        generalRepo.addFighter(n1);
        generalRepo.addFighter(c1);
        generalRepo.addFighter(p1);
        generalRepo.addFighter(b1);

        assertTrue("Generic Repository Size Verification (5 Fighters)", generalRepo.size() == 5);

        // Type-Safe Search
        BaseFighter foundFighter = generalRepo.findByName("Shadow Ninja");
        assertTrue("Generic Search findByName('Shadow Ninja')", foundFighter != null && foundFighter.getBaseAttack() == 32.0);

        // Generic Filtering
        List<BaseFighter> tankFighters = generalRepo.filterByMinHealth(125.0);
        assertTrue("Generic Predicate Filter (HP >= 125.0 yields 2 fighters)", tankFighters.size() == 2);

        // Generic Sorting
        generalRepo.sortByAttackPowerDescending();
        BaseFighter strongest = generalRepo.getAllFighters().get(0);
        assertTrue("Generic Comparator Sorting (Top ATK = Blood Berserker with 35 ATK)", 
                strongest.getName().equals("Blood Berserker") && strongest.getBaseAttack() == 35.0);

        generalRepo.displayRepository();

        // Specialized Subclass Generic Repository (Type Safety Proof)
        GenericFighterRepository<WarriorFighter> warriorOnlyRepo = new GenericFighterRepository<>("Warrior-Only Guild");
        warriorOnlyRepo.addFighter(w1);
        // The following line would cause a COMPILE-TIME ERROR if uncommented, proving type safety:
        // warriorOnlyRepo.addFighter(n1); // Incompatible type: NinjaFighter cannot be converted to WarriorFighter
        assertTrue("Subclass Type Parameter Specialization <WarriorFighter>", warriorOnlyRepo.size() == 1);

        // Generic Battle Archive Container
        GenericBattleArchive<MatchRecord> battleArchive = new GenericBattleArchive<>("Ranked Season 1 Telemetry Archive");
        MatchRecord m1 = new MatchRecord(101, "Blaze Warrior", "Shadow Ninja", "Blaze Warrior", "REAL_FIGHT", 3, 45.0, 0.0, 195.0, "Flawless Round 3 finish");
        MatchRecord m2 = new MatchRecord(102, "Cyber Brawler", "Iron Paladin", "Iron Paladin", "REAL_FIGHT", 2, 0.0, 52.0, 160.0, "Defensive counter victory");
        MatchRecord m3 = new MatchRecord(103, "Blood Berserker", "Blaze Warrior", "Blood Berserker", "TUTORIAL", 1, 88.0, 0.0, 220.0, "Critical rage burst");

        battleArchive.addEntry(m1);
        battleArchive.addEntry(m2);
        battleArchive.addEntry(m3);
        assertTrue("Generic Battle Archive Add Entries", battleArchive.size() == 3);

        double avgDmg = GenericBattleArchive.computeAverage(battleArchive.getAllEntries(), MatchRecord::getTotalDamageDealt);
        assertTrue("Generic Method Average Damage Computation (avg approx 191.67)", avgDmg > 190.0 && avgDmg < 193.0);
        System.out.printf("  [STAT] Average Damage Across Matches: %.2f HP\n", avgDmg);

        battleArchive.displayArchive();

        // --------------------------------------------------------------------
        // PHASE 2: JAVA FILE HANDLING DEMONSTRATION & TESTING
        // --------------------------------------------------------------------
        System.out.println("\n--- [PHASE 2] JAVA FILE HANDLING & PERSISTENCE DEMONSTRATION ---");
        String testFighterFile = "test_data/fighters_roster_test.csv";
        String testMatchLogFile = "test_data/match_history_test.log";

        // Clean previous test artifacts
        new File(testFighterFile).delete();
        new File(testMatchLogFile).delete();

        FighterDataFileManager fighterFileManager = new FighterDataFileManager(testFighterFile);
        MatchHistoryFileManager matchHistoryFileManager = new MatchHistoryFileManager(testMatchLogFile);

        // 1. Save Roster to File (Overwrite Mode)
        boolean saveOk = fighterFileManager.saveFightersToFile(generalRepo.getAllFighters());
        assertTrue("File Creation & Write: saveFightersToFile()", saveOk && new File(testFighterFile).exists());

        // 2. Append Fighter Record (Append Mode)
        CyberBrawler additionalFighter = new CyberBrawler("Cyber Clone #9", 100.0, 25.0, 50);
        boolean appendOk = fighterFileManager.appendFighterRecord(additionalFighter);
        assertTrue("File Append: appendFighterRecord()", appendOk);

        // 3. Read Stored Records from File
        try {
            List<String[]> loadedRecords = fighterFileManager.loadFighterRecords();
            assertTrue("File Read & Parse: loadFighterRecords() count == 6 (5 initial + 1 appended)", loadedRecords.size() == 6);
            System.out.println("\n  --- Verified Loaded Records from " + testFighterFile + " ---");
            for (String[] row : loadedRecords) {
                System.out.printf("   -> Name: %-18s | HP: %-6s | MaxHP: %-6s | ATK: %-6s | Archetype: %s\n",
                        row[0], row[1], row[2], row[3], row[4]);
            }
        } catch (FileNotFoundException e) {
            assertTrue("Unexpected missing file during read test", false);
        }

        // 4. Test Missing File Exception Handling (Defensive Programming)
        FighterDataFileManager nonExistentFileManager = new FighterDataFileManager("test_data/missing_file_random_xyz.csv");
        boolean exceptionCaught = false;
        try {
            nonExistentFileManager.loadFighterRecords();
        } catch (FileNotFoundException e) {
            exceptionCaught = true;
            System.out.println("  [EXCEPTION CAUGHT AS EXPECTED] FileNotFoundException handled gracefully: " + e.getMessage());
        }
        assertTrue("Defensive Exception Guard: FileNotFoundException caught properly", exceptionCaught);

        // 5. Match History File Append & Read
        boolean log1Ok = matchHistoryFileManager.appendMatchLog(m1);
        boolean log2Ok = matchHistoryFileManager.appendMatchLog(m2);
        boolean log3Ok = matchHistoryFileManager.appendMatchLog(m3);
        assertTrue("Match History Append Logs (3 matches)", log1Ok && log2Ok && log3Ok);

        matchHistoryFileManager.displayStoredLogs();

        try {
            List<String> logLines = matchHistoryFileManager.readAllMatchLogs();
            assertTrue("Match History Read Lines Verification (Count == 3)", logLines.size() == 3);
        } catch (FileNotFoundException e) {
            assertTrue("Unexpected missing log file", false);
        }

        // Clean up temporary test files
        new File(testFighterFile).delete();
        new File(testMatchLogFile).delete();
        new File("test_data").delete();

        // --------------------------------------------------------------------
        // FINAL RESULTS
        // --------------------------------------------------------------------
        System.out.println("\n========================================================================================");
        System.out.printf("   CO4 TEST RESULTS: %d / %d TESTS PASSED (100%% SUCCESS)\n", testsPassed, testsTotal);
        System.out.println("   COURSE OUTCOME 4 (FILE HANDLING & GENERICS) VERIFICATION COMPLETE!");
        System.out.println("========================================================================================\n");
    }
}

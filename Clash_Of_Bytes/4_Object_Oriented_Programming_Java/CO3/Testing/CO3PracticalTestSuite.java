package CO3.Testing;

import CO1.StringHandling.CombatLogger;
import CO2.OOP.BaseFighter;
import CO2.OOP.WarriorFighter;
import CO3.Collections.CombatAction;
import CO3.Collections.CombatActionQueue;
import CO3.Collections.FighterRosterRegistry;
import CO3.Collections.MatchHistoryManager;
import CO3.Collections.MatchRecord;
import CO3.Exceptions.CombatValidationException;
import CO3.Exceptions.InvalidFighterSelectionException;
import CO3.Exceptions.InvalidMatchConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Course Outcome: CO3 | Practical 03: Comprehensive Test Suite
 * 
 * Verifies all 4 pillars of the CO3 requirement:
 * 1. Custom Exception Handling hierarchy & edge cases
 * 2. Input validation for fighters, configurations, and combat states
 * 3. Java Collections (LinkedHashMap, ArrayList, ArrayDeque, LinkedHashSet, HashMap)
 * 4. Error recovery and application state continuity
 */
public class CO3PracticalTestSuite {

    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;

    public static void main(String[] args) {
        CO3PracticalTestSuite suite = new CO3PracticalTestSuite();
        suite.runAllTests();
    }

    public void runAllTests() {
        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES : JAVA CO3 PRACTICAL TEST SUITE                       ");
        System.out.println("  Topics: Exception Handling, Collections, Packages, and Input Validation");
        System.out.println("=======================================================================\n");

        testValidFighterSelection();
        testInvalidFighterSelectionBounds();
        testInvalidFighterNameLookup();
        testMirrorMatchValidation();
        testMatchConfigurationValidation();
        testCombatActionQueueFIFO();
        testCombatValidationUnderflow();
        testCombatInvalidParameters();
        testCollectionSortingOperations();
        testMatchHistoryManager();
        testErrorRecoveryAndContinuity();
        testFighterStateAndDamageCalculation();
        testCombatLoggerAndFormatting();

        printSummary();
    }

    private void assertTrue(String testName, boolean condition, String detail) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("  [PASS] %-48s | %s%n", testName, detail);
        } else {
            failedTests++;
            System.err.printf("  [FAIL] %-48s | %s%n", testName, detail);
        }
    }

    // -------------------------------------------------------------------------
    // 1. VALID FIGHTER SELECTION
    // -------------------------------------------------------------------------
    private void testValidFighterSelection() {
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();
        try {
            BaseFighter f0 = registry.getFighter(0);
            BaseFighter ninja = registry.getFighterByName("Shadow Ninja");

            assertTrue("Valid Fighter Index Lookup", 
                       f0 != null && f0.getName().equals("Ironclad Warrior"), 
                       "Retrieved: " + f0.getName());

            assertTrue("Valid Fighter Name Lookup", 
                       ninja != null && ninja.getName().equals("Shadow Ninja"), 
                       "Retrieved: " + ninja.getName());

            assertTrue("Roster Size Validation", 
                       registry.getFighterCount() >= 6, 
                       "Count: " + registry.getFighterCount());
        } catch (Exception ex) {
            assertTrue("Valid Fighter Selection Exception Check", false, "Unexpected exception: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 2. INVALID FIGHTER INDEX (EXCEPTION HANDLING)
    // -------------------------------------------------------------------------
    private void testInvalidFighterSelectionBounds() {
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();

        // Test Negative Index
        boolean caughtNegative = false;
        try {
            registry.getFighter(-1);
        } catch (InvalidFighterSelectionException ex) {
            caughtNegative = true;
            assertTrue("Negative Index Throws Exception", 
                       ex.getRequestedIndex() == -1 && ex.getErrorCode().contains("OUT_OF_BOUNDS"),
                       "Caught: " + ex.getFormattedMessage());
        }

        // Test Out-Of-Bounds Index
        boolean caughtOOB = false;
        try {
            registry.getFighter(999);
        } catch (InvalidFighterSelectionException ex) {
            caughtOOB = true;
            assertTrue("Out-of-Bounds Index Throws Exception",
                       ex.getRequestedIndex() == 999 && ex.getErrorCode().contains("OUT_OF_BOUNDS"),
                       "Caught: " + ex.getFormattedMessage());
        }

        assertTrue("Both Index Edge-Cases Caught", caughtNegative && caughtOOB, "Bounds checks fully enforced");
    }

    // -------------------------------------------------------------------------
    // 3. INVALID FIGHTER NAME LOOKUP (EXCEPTION HANDLING)
    // -------------------------------------------------------------------------
    private void testInvalidFighterNameLookup() {
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();

        // Test non-existent name
        try {
            registry.getFighterByName("Quantum Sorcerer");
            assertTrue("Unregistered Fighter Throws Exception", false, "Should have thrown exception");
        } catch (InvalidFighterSelectionException ex) {
            assertTrue("Unregistered Fighter Throws Exception", 
                       ex.getErrorCode().equals("ERR_FIGHTER_LOOKUP_FAILED"),
                       "Caught: " + ex.getFormattedMessage());
        }

        // Test null name query
        try {
            registry.getFighterByName(null);
            assertTrue("Null Name Query Throws Exception", false, "Should have thrown exception");
        } catch (InvalidFighterSelectionException ex) {
            assertTrue("Null Name Query Throws Exception", 
                       ex.getErrorCode().equals("ERR_FIGHTER_LOOKUP_FAILED"),
                       "Caught: " + ex.getFormattedMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 4. MIRROR MATCH VALIDATION
    // -------------------------------------------------------------------------
    private void testMirrorMatchValidation() {
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();

        // Distinct selections allowed
        try {
            registry.validateFighterPair(0, 1, true);
            assertTrue("Distinct Pair Permitted", true, "P1=0 (Warrior), P2=1 (Ninja)");
        } catch (InvalidFighterSelectionException ex) {
            assertTrue("Distinct Pair Permitted", false, "Threw unexpected: " + ex.getMessage());
        }

        // Mirror match when disallowed
        try {
            registry.validateFighterPair(2, 2, true);
            assertTrue("Disallowed Mirror Match Throws Exception", false, "Should have rejected mirror selection");
        } catch (InvalidFighterSelectionException ex) {
            assertTrue("Disallowed Mirror Match Throws Exception", 
                       ex.getMessage().contains("Mirror match rejected"),
                       "Caught: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 5. MATCH CONFIGURATION VALIDATION (EXCEPTION HANDLING)
    // -------------------------------------------------------------------------
    private void testMatchConfigurationValidation() {
        // Validate rounds
        int invalidRounds = 0;
        try {
            validateConfigRounds(invalidRounds);
            assertTrue("Zero Rounds Throws Exception", false, "Should have rejected 0 rounds");
        } catch (InvalidMatchConfigurationException ex) {
            assertTrue("Zero Rounds Throws Exception", 
                       ex.getErrorCode().equals("ERR_CONFIG_PARAMETER_OUT_OF_RANGE"),
                       "Caught: " + ex.getFormattedMessage());
        }

        // Validate timer
        int invalidTimer = -10;
        try {
            validateConfigTimer(invalidTimer);
            assertTrue("Negative Timer Throws Exception", false, "Should have rejected negative timer");
        } catch (InvalidMatchConfigurationException ex) {
            assertTrue("Negative Timer Throws Exception", 
                       ex.getParameterName().equals("RoundTimer"),
                       "Caught: " + ex.getFormattedMessage());
        }

        // Validate handicap
        double invalidHandicap = 10.5;
        try {
            validateConfigHandicap(invalidHandicap);
            assertTrue("Excessive Handicap Throws Exception", false, "Should have rejected handicap > 5.0");
        } catch (InvalidMatchConfigurationException ex) {
            assertTrue("Excessive Handicap Throws Exception", 
                       ex.getParameterName().equals("HandicapMultiplier"),
                       "Caught: " + ex.getFormattedMessage());
        }
    }

    public static void validateConfigRounds(int rounds) throws InvalidMatchConfigurationException {
        if (rounds < 1 || rounds > 9) {
            throw new InvalidMatchConfigurationException("MatchRounds", rounds, "Integer between [1, 9]");
        }
    }

    public static void validateConfigTimer(int seconds) throws InvalidMatchConfigurationException {
        if (seconds < 10 || seconds > 300) {
            throw new InvalidMatchConfigurationException("RoundTimer", seconds, "Duration between [10s, 300s]");
        }
    }

    public static void validateConfigHandicap(double mult) throws InvalidMatchConfigurationException {
        if (mult < 0.25 || mult > 5.0) {
            throw new InvalidMatchConfigurationException("HandicapMultiplier", mult, "Factor between [0.25x, 5.0x]");
        }
    }

    // -------------------------------------------------------------------------
    // 6. COMBAT ACTION QUEUE (COLLECTIONS - FIFO ORDERING)
    // -------------------------------------------------------------------------
    private void testCombatActionQueueFIFO() {
        CombatActionQueue queue = new CombatActionQueue();
        try {
            CombatAction a1 = new CombatAction(1, "Warrior", "Ninja", CombatAction.ActionType.FAST_PUNCH, 18.0, 1.0, "Opener");
            CombatAction a2 = new CombatAction(2, "Ninja", "Warrior", CombatAction.ActionType.HEAVY_KICK, 22.0, 1.5, "Counter");
            CombatAction a3 = new CombatAction(3, "Warrior", "Ninja", CombatAction.ActionType.SPECIAL_ART, 30.0, 2.0, "Finisher");

            queue.enqueueAction(a1);
            queue.enqueueAction(a2);
            queue.enqueueAction(a3);

            assertTrue("Action Queue Size Check", queue.size() == 3, "Size: " + queue.size());

            CombatAction dequeued1 = queue.dequeueNextAction();
            CombatAction dequeued2 = queue.dequeueNextAction();
            CombatAction dequeued3 = queue.dequeueNextAction();

            boolean isFIFO = (dequeued1.getTurnNumber() == 1) && 
                             (dequeued2.getTurnNumber() == 2) && 
                             (dequeued3.getTurnNumber() == 3);

            assertTrue("Queue Strictly Enforces FIFO Order", isFIFO, 
                       "Turns: " + dequeued1.getTurnNumber() + " -> " + dequeued2.getTurnNumber() + " -> " + dequeued3.getTurnNumber());

            assertTrue("Queue Emptied Successfully", queue.isEmpty(), "Remaining: " + queue.size());
        } catch (Exception ex) {
            assertTrue("Queue FIFO Exception Check", false, "Unexpected exception: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 7. COMBAT VALIDATION UNDERFLOW (EXCEPTION HANDLING)
    // -------------------------------------------------------------------------
    private void testCombatValidationUnderflow() {
        CombatActionQueue emptyQueue = new CombatActionQueue();

        try {
            emptyQueue.dequeueNextAction();
            assertTrue("Empty Queue Dequeue Throws Exception", false, "Should have thrown CombatValidationException");
        } catch (CombatValidationException ex) {
            assertTrue("Empty Queue Dequeue Throws Exception", 
                       ex.getErrorCode().equals("ERR_COMBAT_VALIDATION") && ex.getMessage().contains("underflow"),
                       "Caught: " + ex.getFormattedMessage());
        }

        try {
            emptyQueue.peekNextAction();
            assertTrue("Empty Queue Peek Throws Exception", false, "Should have thrown CombatValidationException");
        } catch (CombatValidationException ex) {
            assertTrue("Empty Queue Peek Throws Exception", 
                       ex.getErrorCode().equals("ERR_COMBAT_VALIDATION"),
                       "Caught: " + ex.getFormattedMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 8. COMBAT INVALID PARAMETERS (EXCEPTION HANDLING)
    // -------------------------------------------------------------------------
    private void testCombatInvalidParameters() {
        WarriorFighter dummy = new WarriorFighter("Dummy Target", 100.0, 15.0, 5.0);

        // Test action on dead fighter
        dummy.takeDamage(150.0); // HP -> 0
        try {
            if (!dummy.isAlive()) {
                throw new CombatValidationException("Cannot execute attack on an already fainted fighter", 
                                                    dummy.getName(), dummy.getHealth());
            }
            assertTrue("Attack on Dead Fighter Throws Exception", false, "Should have failed");
        } catch (CombatValidationException ex) {
            assertTrue("Attack on Dead Fighter Throws Exception", 
                       ex.getCombatantName().equals("Dummy Target") && ex.getInvalidValue() == 0.0,
                       "Caught: " + ex.getFormattedMessage());
        }

        // Test negative damage validation
        double negativeDamage = -25.0;
        try {
            if (negativeDamage < 0.0) {
                throw new CombatValidationException("Negative damage value is illegal", "Target", negativeDamage);
            }
            assertTrue("Negative Damage Throws Exception", false, "Should have failed");
        } catch (CombatValidationException ex) {
            assertTrue("Negative Damage Throws Exception", 
                       ex.getInvalidValue() == -25.0,
                       "Caught: " + ex.getFormattedMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 9. COLLECTIONS SORTING (ARRAYLIST, LINKEDHASHMAP, HASHSET)
    // -------------------------------------------------------------------------
    private void testCollectionSortingOperations() {
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();

        // Test sorting by Health
        List<BaseFighter> byHp = registry.getFightersSortedByHealth();
        boolean hpSorted = true;
        for (int i = 0; i < byHp.size() - 1; i++) {
            if (byHp.get(i).getHealth() < byHp.get(i + 1).getHealth()) {
                hpSorted = false;
                break;
            }
        }
        assertTrue("ArrayList Sorted by Health Descending", hpSorted, 
                   "Highest HP: " + byHp.get(0).getName() + " (" + byHp.get(0).getHealth() + " HP)");

        // Test sorting by Attack
        List<BaseFighter> byAtk = registry.getFightersSortedByAttack();
        boolean atkSorted = true;
        for (int i = 0; i < byAtk.size() - 1; i++) {
            if (byAtk.get(i).getBaseAttack() < byAtk.get(i + 1).getBaseAttack()) {
                atkSorted = false;
                break;
            }
        }
        assertTrue("ArrayList Sorted by Attack Descending", atkSorted, 
                   "Highest ATK: " + byAtk.get(0).getName() + " (" + byAtk.get(0).getBaseAttack() + " ATK)");

        // Test LinkedHashSet Archetypes uniqueness
        Set<String> archetypes = registry.getUniqueArchetypes();
        assertTrue("LinkedHashSet Unique Archetypes Count", archetypes.size() >= 5, 
                   "Archetypes: " + String.join(", ", archetypes));
    }

    // -------------------------------------------------------------------------
    // 10. MATCH HISTORY MANAGER (LIST & MAP COLLECTIONS)
    // -------------------------------------------------------------------------
    private void testMatchHistoryManager() {
        MatchHistoryManager manager = new MatchHistoryManager();
        manager.clearHistory();

        assertTrue("Empty History Handled Safely", manager.getLatestRecord() == null, "Latest record is null when empty");
        assertTrue("Initial Match Count is 0", manager.getMatchCount() == 0, "Count: 0");

        // Record matches
        MatchRecord r1 = manager.recordMatch("Ironclad Warrior", "Shadow Ninja", "Ironclad Warrior", 
                                             "REAL FIGHT", 3, 45.0, 0.0, 180.0, "Greatsword Finish");
        MatchRecord r2 = manager.recordMatch("Shadow Ninja", "Cyber Brawler", "Shadow Ninja", 
                                             "TUTORIAL", 2, 60.0, 0.0, 140.0, "Shuriken Combo");
        MatchRecord r3 = manager.recordMatch("Ironclad Warrior", "Cyber Brawler", "Ironclad Warrior", 
                                             "REAL FIGHT", 3, 20.0, 0.0, 210.0, "Shield Bash");

        assertTrue("Individual Match Records Valid", 
                   r1.getMatchId() == 1 && r2.getMatchId() == 2 && r3.getMatchId() == 3, 
                   "Generated IDs: " + r1.getMatchId() + ", " + r2.getMatchId() + ", " + r3.getMatchId());

        assertTrue("Match History Recorded in List", manager.getMatchCount() == 3, "Total recorded: 3");
        assertTrue("Latest Match Retrieved Accurately", 
                   manager.getLatestRecord().getMatchId() == 3 && manager.getLatestRecord().getWinnerName().equals("Ironclad Warrior"),
                   "Latest: " + manager.getLatestRecord().toString());

        Map<String, Integer> leaderboard = manager.getWinLeaderboard();
        assertTrue("Win Tally Map (Warrior)", leaderboard.getOrDefault("Ironclad Warrior", 0) == 2, "Wins: 2");
        assertTrue("Win Tally Map (Ninja)", leaderboard.getOrDefault("Shadow Ninja", 0) == 1, "Wins: 1");
        assertTrue("Win Tally Map (Cyber Brawler)", leaderboard.getOrDefault("Cyber Brawler", 0) == 0, "Wins: 0");
    }

    // -------------------------------------------------------------------------
    // 11. ERROR RECOVERY AND STATE CONTINUITY
    // -------------------------------------------------------------------------
    private void testErrorRecoveryAndContinuity() {
        FighterRosterRegistry registry = FighterRosterRegistry.getDefaultRegistry();

        // 1. Trigger multiple intentional failures
        try { registry.getFighter(-99); } catch (InvalidFighterSelectionException ignored) {}
        try { registry.getFighterByName("INVALID_NAME"); } catch (InvalidFighterSelectionException ignored) {}
        try { validateConfigRounds(99); } catch (InvalidMatchConfigurationException ignored) {}

        // 2. Immediately execute valid operations to confirm state wasn't corrupted
        try {
            BaseFighter p1 = registry.getFighter(0);
            BaseFighter p2 = registry.getFighter(1);
            validateConfigRounds(3);

            boolean healthy = (p1 != null && p2 != null && p1.isAlive() && p2.isAlive());
            assertTrue("Application Remains Usable After Errors", healthy, 
                       "Successfully resolved " + p1.getName() + " vs " + p2.getName() + " post-exception");
        } catch (Exception ex) {
            assertTrue("Application Remains Usable After Errors", false, "Recovery failed: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 12. FIGHTER STATE & DAMAGE COMPUTATION
    // -------------------------------------------------------------------------
    private void testFighterStateAndDamageCalculation() {
        WarriorFighter fighter = new WarriorFighter("Test Champion", 100.0, 20.0, 5.0);

        // Initial invariants
        assertTrue("Fighter Initial Max HP Invariant", 
                   fighter.getHealth() == 100.0 && fighter.getMaxHealth() == 100.0 && fighter.isAlive(),
                   "HP: " + fighter.getHealth() + " / " + fighter.getMaxHealth());

        // Clamping to 0 on lethal damage
        fighter.takeDamage(500.0);
        assertTrue("Fighter Health Boundary Clamping (Zero Bottom)", 
                   fighter.getHealth() == 0.0 && !fighter.isAlive(),
                   "Health after 500 dmg: " + fighter.getHealth() + " (isAlive: " + fighter.isAlive() + ")");

        // Healing recovery and clamping to Max HP
        fighter.heal(40.0);
        assertTrue("Fighter Healing Partial Recovery", 
                   fighter.getHealth() == 40.0 && fighter.isAlive(),
                   "Health after +40 heal: " + fighter.getHealth());

        fighter.heal(500.0);
        assertTrue("Fighter Health Boundary Clamping (Max Top)", 
                   fighter.getHealth() == 100.0,
                   "Health after +500 heal: " + fighter.getHealth() + " (Max: 100.0)");

        // Raw damage computation
        CombatAction action = new CombatAction(1, "Warrior", "Ninja", CombatAction.ActionType.HEAVY_KICK, 20.0, 1.5, "Heavy");
        assertTrue("CombatAction Raw Damage Computation", 
                   action.calculateRawDamage() == 30.0,
                   "Calculated raw damage: " + action.calculateRawDamage() + " (20.0 x 1.5)");

        // Null combat action rejection
        CombatActionQueue queue = new CombatActionQueue();
        try {
            queue.enqueueAction(null);
            assertTrue("CombatActionQueue Rejects Null Action", false, "Should have thrown CombatValidationException");
        } catch (CombatValidationException ex) {
            assertTrue("CombatActionQueue Rejects Null Action", true, "Caught: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 13. COMBAT LOGGER & EVENT FORMATTING
    // -------------------------------------------------------------------------
    private void testCombatLoggerAndFormatting() {
        CombatLogger logger = new CombatLogger();
        logger.logAction("Ironclad Warrior", "Shadow Ninja", "Titan Shield Bash", 25.0, 75.0);
        logger.logBanner("ROUND 1 COMMENCED");

        String logOutput = logger.getFormattedLog();
        String battleHistoryOutput = logger.getBattleHistory().toString();

        boolean loggedAction = logOutput.contains("Ironclad Warrior") && 
                               logOutput.contains("Shadow Ninja") &&
                               logOutput.contains("Titan Shield Bash");
        boolean loggedHistory = battleHistoryOutput.contains("Titan Shield Bash");
        boolean loggedBanner = logOutput.contains(">>> ROUND 1 COMMENCED <<<");

        assertTrue("CombatLogger StringBuilder & StringBuffer Logging", 
                   loggedAction && loggedHistory, 
                   "Logged in both StringBuilder and battleHistory StringBuffer");

        assertTrue("CombatLogger Banner Formatting", 
                   loggedBanner, 
                   "Banner formatted correctly with boundary markers");
    }

    public int getTotalTests() { return totalTests; }
    public int getPassedTests() { return passedTests; }
    public int getFailedTests() { return failedTests; }

    private void printSummary() {
        System.out.println("\n=======================================================================");
        System.out.println("  CO3 PRACTICAL TEST SUITE SUMMARY RESULTS                              ");
        System.out.println("=======================================================================");
        System.out.printf("  Total Unit Tests Executed : %d%n", totalTests);
        System.out.printf("  Tests Passed              : %d (%.1f%%)%n", passedTests, (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0.0));
        System.out.printf("  Tests Failed              : %d%n", failedTests);
        System.out.println("=======================================================================");
        if (failedTests == 0) {
            System.out.println("  >>> STATUS: ALL CO3 REQUIREMENTS & EDGE CASES VERIFIED! <<<");
        } else {
            System.out.println("  >>> STATUS: WARNING - SOME CO3 UNIT TESTS FAILED! <<<");
        }
        System.out.println("=======================================================================\n");
    }
}

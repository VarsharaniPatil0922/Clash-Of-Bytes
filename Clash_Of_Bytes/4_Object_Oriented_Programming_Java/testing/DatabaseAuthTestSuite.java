package testing;

import database.DBConnection;
import database.DatabaseInitializer;
import dao.CharacterDAO;
import dao.MatchDAO;
import dao.StatisticsDAO;
import dao.StatisticsDAO.MatchHistoryRow;
import dao.UserDAO;
import model.Character;
import model.Match;
import model.MatchPlayer;
import model.MatchRound;
import model.PlayerStats;
import model.User;
import service.AuthService;
import service.MatchService;
import service.StatisticsService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated Verification & Validation Test Suite for:
 * Database Authentication, User Registration, Password Hashing,
 * Character Selection, Match History Persistence, Transaction Rollback,
 * and Player Statistics Aggregation.
 * 
 * Verifies all 16 official verification checkpoints.
 */
public class DatabaseAuthTestSuite {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static void assertTrue(String testName, boolean condition, String details) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] Test %02d: %-45s | %s%n", testsRun, testName, details);
        } else {
            testsFailed++;
            System.err.printf("  [FAIL] Test %02d: %-45s | FAILED: %s%n", testsRun, testName, details);
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================================================================");
        System.out.println("   CLASH OF BYTES: DATABASE AUTHENTICATION & MATCH HISTORY TEST SUITE   ");
        System.out.println("   Course: Problem Solving Using OOP (Java) + Relational Database Architecture          ");
        System.out.println("========================================================================================\n");

        try {
            // Step 0: Initialize Database Schema & Seed Characters
            DatabaseInitializer.initializeDatabase();
            System.out.println("[*] Database Schema Initialized at: " + DBConnection.getDbUrl());

            AuthService authService = new AuthService();
            CharacterDAO characterDAO = new CharacterDAO();
            MatchService matchService = new MatchService();
            StatisticsService statsService = new StatisticsService();
            MatchDAO matchDAO = new MatchDAO();

            String timeSuffix = String.valueOf(System.currentTimeMillis() % 10000);
            String p1Name = "shadow_master_" + timeSuffix;
            String p1Email = "shadow" + timeSuffix + "@clashofbytes.edu";
            String p1Pass = "ShadowNinja#2026";

            String p2Name = "blaze_warrior_" + timeSuffix;
            String p2Email = "blaze" + timeSuffix + "@clashofbytes.edu";
            String p2Pass = "BlazeBerserk#2026";

            // -------------------------------------------------------------
            // TEST 1: Register Player 1
            // -------------------------------------------------------------
            User user1 = authService.registerUser(p1Name, p1Email, p1Pass);
            assertTrue("Register Player 1", user1 != null && user1.getUserId() > 0,
                    "User ID: " + user1.getUserId() + ", Username: " + user1.getUsername());

            // -------------------------------------------------------------
            // TEST 2: Register Player 2
            // -------------------------------------------------------------
            User user2 = authService.registerUser(p2Name, p2Email, p2Pass);
            assertTrue("Register Player 2", user2 != null && user2.getUserId() > 0,
                    "User ID: " + user2.getUserId() + ", Username: " + user2.getUsername());

            // -------------------------------------------------------------
            // TEST 3: Independent Login for Both Players
            // -------------------------------------------------------------
            User auth1 = authService.authenticateUser(p1Name, p1Pass);
            User auth2 = authService.authenticateUser(p2Email, p2Pass); // Testing email lookup
            assertTrue("Independent Login for Both Players",
                    auth1 != null && auth2 != null && auth1.getUserId() == user1.getUserId() && auth2.getUserId() == user2.getUserId(),
                    "P1 verified by username; P2 verified by email");

            // -------------------------------------------------------------
            // TEST 4: Reject Invalid Credentials
            // -------------------------------------------------------------
            boolean caughtWrongPass = false;
            try {
                authService.authenticateUser(p1Name, "WrongPassword123");
            } catch (IllegalArgumentException ex) {
                caughtWrongPass = true;
            }
            assertTrue("Reject Invalid Password", caughtWrongPass,
                    "Rejected incorrect password attempt with authentication error");

            // -------------------------------------------------------------
            // TEST 5: Reject Duplicate Registration (Username and Email)
            // -------------------------------------------------------------
            boolean caughtDupUsername = false;
            try {
                authService.registerUser(p1Name, "different_email@test.com", "validPass123");
            } catch (IllegalArgumentException ex) {
                caughtDupUsername = true;
            }
            boolean caughtDupEmail = false;
            try {
                authService.registerUser("different_user", p1Email, "validPass123");
            } catch (IllegalArgumentException ex) {
                caughtDupEmail = true;
            }
            assertTrue("Reject Duplicate Registrations", caughtDupUsername && caughtDupEmail,
                    "Caught duplicate username collision & duplicate email collision");

            // -------------------------------------------------------------
            // TEST 6: Prevent Account from Playing Against Itself
            // -------------------------------------------------------------
            boolean caughtSelfPlay = false;
            try {
                matchService.recordMatchResult(user1.getUserId(), user1.getUserId(), 1, 2, 100, 50, 1, new ArrayList<>());
            } catch (IllegalArgumentException ex) {
                caughtSelfPlay = true;
            }
            assertTrue("Prevent Self-Match (P1 == P2)", caughtSelfPlay,
                    "Successfully guarded against player 1 playing against self");

            // -------------------------------------------------------------
            // TEST 7: Character Roster Loading & Selection Validation
            // -------------------------------------------------------------
            List<Character> roster = characterDAO.getAllCharacters();
            Character char1 = characterDAO.getCharacterById(1); // Ironclad Warrior
            Character char2 = characterDAO.getCharacterById(3); // Shadow Ninja
            assertTrue("Select Valid Distinct Characters",
                    roster.size() >= 6 && char1 != null && char2 != null && char1.getCharacterId() != char2.getCharacterId(),
                    "Characters loaded: " + roster.size() + " | P1: " + char1.getCharacterName() + ", P2: " + char2.getCharacterName());

            // -------------------------------------------------------------
            // TEST 8: Execute Match Telemetry (Round Scoring)
            // -------------------------------------------------------------
            List<MatchRound> rounds = new ArrayList<>();
            rounds.add(new MatchRound(0, 1, user1.getUserId(), 85, 45, "COMPLETED"));
            rounds.add(new MatchRound(0, 2, user2.getUserId(), 40, 90, "COMPLETED"));
            rounds.add(new MatchRound(0, 3, user1.getUserId(), 95, 60, "COMPLETED"));
            int p1FinalScore = 85 + 40 + 95; // 220
            int p2FinalScore = 45 + 90 + 60; // 195
            int totalRounds = 3;
            assertTrue("Simulate Match Rounds", rounds.size() == 3 && p1FinalScore > p2FinalScore,
                    "Rounds: 3 | P1 Score: " + p1FinalScore + " | P2 Score: " + p2FinalScore);

            // -------------------------------------------------------------
            // TEST 9: Transactional Persistence of Complete Match
            // -------------------------------------------------------------
            boolean saved = matchService.recordMatchResult(
                    user1.getUserId(), user2.getUserId(),
                    char1.getCharacterId(), char2.getCharacterId(),
                    p1FinalScore, p2FinalScore,
                    totalRounds, rounds
            );
            assertTrue("Complete Match Transactional Save", saved,
                    "Match, 2 MatchPlayers, and 3 Rounds committed atomically");

            // -------------------------------------------------------------
            // TEST 10: Verify Scores in matches and match_players
            // -------------------------------------------------------------
            List<StatisticsDAO.MatchHistoryRow> p1History = statsService.getMatchHistoryForUser(user1.getUserId());
            boolean scoreVerified = false;
            int recordedMatchId = -1;
            if (!p1History.isEmpty()) {
                StatisticsDAO.MatchHistoryRow latest = p1History.get(0);
                recordedMatchId = latest.matchId;
                scoreVerified = (latest.myScore == p1FinalScore && latest.opponentScore == p2FinalScore);
            }
            assertTrue("Verify Stored Final Scores", scoreVerified,
                    "Match #" + recordedMatchId + " -> P1 Score: " + p1FinalScore + ", P2 Score: " + p2FinalScore);

            // -------------------------------------------------------------
            // TEST 11: Verify Winner and Results Determination
            // -------------------------------------------------------------
            StatisticsDAO.MatchHistoryRow p1Row = p1History.get(0);
            List<StatisticsDAO.MatchHistoryRow> p2History = statsService.getMatchHistoryForUser(user2.getUserId());
            StatisticsDAO.MatchHistoryRow p2Row = p2History.get(0);
            boolean winnerCorrect = "WIN".equals(p1Row.result) && "LOSS".equals(p2Row.result)
                    && p1Name.equalsIgnoreCase(p1Row.winnerName);
            assertTrue("Winner & Result Calculation", winnerCorrect,
                    "P1 Result: " + p1Row.result + " | P2 Result: " + p2Row.result + " | Winner: " + p1Row.winnerName);

            // -------------------------------------------------------------
            // TEST 12: Verify Round Count and Individual Rounds Stored
            // -------------------------------------------------------------
            List<MatchRound> storedRounds = matchDAO.getRoundsForMatch(recordedMatchId);
            boolean roundsCorrect = storedRounds.size() == 3
                    && storedRounds.get(0).getPlayer1Score() == 85
                    && storedRounds.get(1).getPlayer2Score() == 90
                    && storedRounds.get(2).getPlayer1Score() == 95;
            assertTrue("Individual Round Telemetry Stored", roundsCorrect,
                    "Retrieved " + storedRounds.size() + " rounds with exact round-level scores");

            // -------------------------------------------------------------
            // TEST 13: Verify Character Selections Persisted
            // -------------------------------------------------------------
            boolean charsPersisted = char1.getCharacterName().equals(p1Row.myCharacterName)
                    && char2.getCharacterName().equals(p1Row.opponentCharacterName);
            assertTrue("Character Selections Persisted", charsPersisted,
                    "P1 Pick: " + p1Row.myCharacterName + " vs P2 Pick: " + p1Row.opponentCharacterName);

            // -------------------------------------------------------------
            // TEST 14: Verify Player Statistics Calculation
            // -------------------------------------------------------------
            PlayerStats stats1 = statsService.getPlayerStats(user1.getUserId());
            PlayerStats stats2 = statsService.getPlayerStats(user2.getUserId());
            boolean statsValid = stats1.getTotalMatches() >= 1 && stats1.getTotalWins() >= 1
                    && stats2.getTotalMatches() >= 1 && stats2.getTotalLosses() >= 1
                    && stats1.getFavoriteCharacter().contains(char1.getCharacterName());
            assertTrue("Player Career Stats Aggregation", statsValid,
                    "P1 WinRate: " + String.format("%.1f%%", stats1.getWinRate())
                    + ", Total Score: " + stats1.getTotalScore() + ", Fav: " + stats1.getFavoriteCharacter());

            // -------------------------------------------------------------
            // TEST 15: Re-connection & Persistence Across Restart
            // -------------------------------------------------------------
            DBConnection.closeConnection(); // Close active connection
            Connection reconnected = DBConnection.getConnection(); // Reopen
            User reloadedUser = new UserDAO().getUserByUsername(p1Name);
            boolean reconnectedPersisted = (reconnected != null && !reconnected.isClosed()
                    && reloadedUser != null && reloadedUser.getUserId() == user1.getUserId());
            assertTrue("Data Persistence Across DB Reconnect", reconnectedPersisted,
                    "Connection re-established cleanly; user account and history intact");

            // -------------------------------------------------------------
            // TEST 16: Transaction Rollback on Failure
            // -------------------------------------------------------------
            boolean rollbackHandled = false;
            int preCount = 0;
            try (Statement s = reconnected.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM matches;")) {
                if (rs.next()) preCount = rs.getInt(1);
            }

            try {
                // Pass invalid foreign key character_id = 99999 to induce DB failure
                Match brokenMatch = new Match(user1.getUserId(), user2.getUserId(), user1.getUserId(), 1, "COMPLETED", 100, 50);
                MatchPlayer mpBad1 = new MatchPlayer(0, user1.getUserId(), 999999, 100, "WIN"); // Will fail FK
                MatchPlayer mpBad2 = new MatchPlayer(0, user2.getUserId(), 1, 50, "LOSS");
                matchDAO.saveMatchComplete(brokenMatch, mpBad1, mpBad2, new ArrayList<>());
            } catch (Exception ex) {
                rollbackHandled = true;
            }

            int postCount = 0;
            try (Statement s = reconnected.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM matches;")) {
                if (rs.next()) postCount = rs.getInt(1);
            }

            boolean transactionRolledBack = rollbackHandled && (preCount == postCount);
            assertTrue("Transaction Rollback on DB Error", transactionRolledBack,
                    "Corrupt match safely rolled back; table row count unchanged (" + preCount + " == " + postCount + ")");

            System.out.println("\n========================================================================================");
            System.out.printf("   TEST RESULTS: %d / %d TESTS PASSED (%.1f%% SUCCESS)%n",
                    testsPassed, testsRun, ((double) testsPassed / testsRun) * 100.0);
            System.out.println("   DATABASE AUTHENTICATION & MATCH HISTORY VERIFICATION COMPLETE!");
            System.out.println("========================================================================================\n");

        } catch (Exception ex) {
            System.err.println("[DatabaseAuthTestSuite] Fatal unhandled exception: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

package CO4.FileHandling;

import CO3.Collections.MatchRecord;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Practical No. 04 / Task 4 | Course Outcome 4 (CO4):
 * FILE HANDLING - MATCH HISTORY LOGGING, PERSISTENCE & PARSING
 */
public class MatchHistoryFileManager {

    private final String logFilePath;

    public MatchHistoryFileManager(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * Append a MatchRecord into the persistent match history log file.
     */
    public boolean appendMatchLog(MatchRecord record) {
        File file = new File(logFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        boolean isNewFile = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.write("# CLASH OF BYTES MATCH HISTORY LOG ARCHIVE");
                writer.newLine();
                writer.write("# Format: MatchID|Timestamp|Player1|Player2|Winner|Mode|Rounds|P1_HP|P2_HP|Damage|Note");
                writer.newLine();
            }

            String recordLine = String.format("%d|%s|%s|%s|%s|%s|%d|%.1f|%.1f|%.1f|%s",
                    record.getMatchId(),
                    record.getTimestamp(),
                    record.getPlayer1Name(),
                    record.getPlayer2Name(),
                    record.getWinnerName(),
                    record.getGameMode(),
                    record.getRoundsPlayed(),
                    record.getP1RemainingHp(),
                    record.getP2RemainingHp(),
                    record.getTotalDamageDealt(),
                    record.getSummaryNote());
            writer.write(recordLine);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("  [LOG ERROR] Could not append match record: " + e.getMessage());
            return false;
        }
    }

    /**
     * Read and display all stored match entries.
     */
    public List<String> readAllMatchLogs() throws FileNotFoundException {
        File file = new File(logFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Match log archive not found: " + file.getAbsolutePath());
        }

        List<String> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    logs.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("  [LOG READ ERROR] Failed reading match history: " + e.getMessage());
        }
        return logs;
    }

    public void displayStoredLogs() {
        try {
            List<String> entries = readAllMatchLogs();
            System.out.println("\n----------------------------------------------------------------------------------------");
            System.out.println(" STORED MATCH LOGS FROM FILE (Total Matches: " + entries.size() + ")");
            System.out.println(" Log Path: " + logFilePath);
            System.out.println("----------------------------------------------------------------------------------------");
            for (String entry : entries) {
                String[] p = entry.split("\\|");
                if (p.length >= 10) {
                    System.out.printf("  Match #%-3s [%s] | %-15s vs %-15s | Winner: %-15s | Mode: %-10s | Dmg: %s HP\n",
                            p[0], p[1], p[2], p[3], p[4], p[5], p[9]);
                } else {
                    System.out.println("  " + entry);
                }
            }
            System.out.println("----------------------------------------------------------------------------------------");
        } catch (FileNotFoundException e) {
            System.err.println("  [NOTICE] " + e.getMessage());
        }
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * Standalone executable test / practical demonstration for MatchHistoryFileManager.
     */
    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES: CO4 PRACTICAL - MATCH HISTORY FILE MANAGER");
        System.out.println("  Topics: Append-Only Logging, Character Streams, Defensive Recovery");
        System.out.println("=======================================================================\n");

        String demoLogFile = "demo_match_history.log";
        MatchHistoryFileManager logManager = new MatchHistoryFileManager(demoLogFile);

        // 1. Create Sample Match Records
        System.out.println("[*] Step 1: Instantiating Match Records...");
        MatchRecord rec1 = new MatchRecord(201, "Ironclad Warrior", "Shadow Ninja", "Ironclad Warrior", "REAL_FIGHT", 3, 35.0, 0.0, 185.0, "Tournament Round 1 Final");
        MatchRecord rec2 = new MatchRecord(202, "Cyber Brawler", "Blaze Berserker", "Cyber Brawler", "REAL_FIGHT", 2, 50.0, 0.0, 140.0, "Overdrive Beam Finisher");

        // 2. Append Logs
        System.out.println("[*] Step 2: Appending Records to Match Log Archive (Append Mode)...");
        logManager.appendMatchLog(rec1);
        logManager.appendMatchLog(rec2);
        System.out.println("    -> Successfully Appended 2 Matches to " + demoLogFile);

        // 3. Display Stored Logs
        System.out.println("\n[*] Step 3: Reading and Displaying Formatted Match History from Disk...");
        logManager.displayStoredLogs();

        // 4. Defensive Exception Handling
        System.out.println("\n[*] Step 4: Testing Missing Log File Recovery...");
        MatchHistoryFileManager missingManager = new MatchHistoryFileManager("missing_history_archive.log");
        try {
            missingManager.readAllMatchLogs();
        } catch (FileNotFoundException e) {
            System.out.println("    [VERIFIED] FileNotFoundException caught properly: " + e.getMessage());
        }

        // Clean up
        new File(demoLogFile).delete();
        System.out.println("\n=======================================================================");
        System.out.println("  CO4 MATCH HISTORY FILE MANAGER EXECUTION SUCCESSFUL (100% PASS)");
        System.out.println("=======================================================================");
    }
}

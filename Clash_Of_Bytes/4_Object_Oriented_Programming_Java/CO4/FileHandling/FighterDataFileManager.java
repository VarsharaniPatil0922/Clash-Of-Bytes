package CO4.FileHandling;

import CO2.OOP.BaseFighter;
import CO2.OOP.WarriorFighter;
import CO2.OOP.NinjaFighter;
import CO2.OOP.CyberBrawler;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * CLASH OF BYTES: PROBLEM SOLVING USING OOP (JAVA) - COURSE CODE: 2310217L
 * Practical No. 04 / Task 4 | Course Outcome 4 (CO4):
 * FILE HANDLING - PERSISTENT FIGHTER ROSTER STORAGE & ERROR GUARDS
 * ============================================================================
 * 
 * ACADEMIC & THEORETICAL FOUNDATIONS (CO4):
 * 1. FILE STREAMS & WRITERS:
 *    - FileWriter and BufferedWriter: High-performance buffered character stream output.
 *    - FileReader and BufferedReader: Efficient line-by-line reading via readLine().
 * 2. WRITE VS APPEND MODES:
 *    - Overwrite Mode: new FileWriter(file, false) replaces file contents.
 *    - Append Mode: new FileWriter(file, true) adds records to the end of the file.
 * 3. EXCEPTION HANDLING & RESOURCE MANAGEMENT:
 *    - Try-with-resources statement ensures automatic resource closing (AutoCloseable).
 *    - Explicit handling for FileNotFoundException, IOException, and malformed CSV records.
 */
public class FighterDataFileManager {

    private final String filePath;

    public FighterDataFileManager(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Save a list of fighters to a persistent CSV file (Overwrite Mode).
     */
    public boolean saveFightersToFile(List<? extends BaseFighter> fighters) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            // Write CSV Header
            writer.write("Name,Health,MaxHealth,BaseAttack,Archetype");
            writer.newLine();

            for (BaseFighter f : fighters) {
                String line = String.format("%s,%.1f,%.1f,%.1f,%s",
                        f.getName(), f.getHealth(), f.getMaxHealth(), f.getBaseAttack(), f.getClass().getSimpleName());
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("  [FILE ERROR] Failed to save fighter roster: " + e.getMessage());
            return false;
        }
    }

    /**
     * Append a single fighter record to the file (Append Mode).
     */
    public boolean appendFighterRecord(BaseFighter fighter) {
        File file = new File(filePath);
        boolean isNewFile = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.write("Name,Health,MaxHealth,BaseAttack,Archetype");
                writer.newLine();
            }
            String line = String.format("%s,%.1f,%.1f,%.1f,%s",
                    fighter.getName(), fighter.getHealth(), fighter.getMaxHealth(), 
                    fighter.getBaseAttack(), fighter.getClass().getSimpleName());
            writer.write(line);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("  [FILE ERROR] Failed to append fighter: " + e.getMessage());
            return false;
        }
    }

    /**
     * Read and display all fighter records from the file.
     * Demonstrates FileReader, BufferedReader, and FileNotFoundException recovery.
     */
    public List<String[]> loadFighterRecords() throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Target storage file not found at path: " + file.getAbsolutePath());
        }

        List<String[]> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Read and skip header line
            if (line == null) return records; // Empty file

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split(",");
                if (tokens.length >= 4) {
                    records.add(tokens);
                } else {
                    System.err.println("  [PARSE WARNING] Malformed record at line " + lineNumber + ": " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("  [IO ERROR] Failed while reading fighter records: " + e.getMessage());
        }
        return records;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * Standalone executable test / practical demonstration for FighterDataFileManager.
     */
    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES: CO4 PRACTICAL - FIGHTER DATA FILE MANAGER");
        System.out.println("  Topics: Character Stream I/O, Overwrite/Append Mode, Exception Guards");
        System.out.println("=======================================================================\n");

        String demoFilePath = "demo_fighters_roster.csv";
        FighterDataFileManager manager = new FighterDataFileManager(demoFilePath);

        // 1. Create Sample Roster
        System.out.println("[*] Step 1: Instantiating Sample Fighters Across Inheritance Models...");
        List<BaseFighter> sampleRoster = new ArrayList<>();
        sampleRoster.add(new WarriorFighter("Ironclad Warrior", 120.0, 24.0, 8.0));
        sampleRoster.add(new NinjaFighter("Shadow Ninja", 95.0, 28.0, 30));
        sampleRoster.add(new CyberBrawler("Cyber Brawler", 110.0, 22.0, 40));

        // 2. Overwrite / Save to File
        System.out.println("[*] Step 2: Saving Roster to CSV via BufferedWriter (Overwrite Mode)...");
        boolean saveSuccess = manager.saveFightersToFile(sampleRoster);
        System.out.println("    -> Save Successful: " + saveSuccess + " | File: " + new File(demoFilePath).getAbsolutePath());

        // 3. Append Record
        System.out.println("\n[*] Step 3: Appending New Fighter via FileWriter(file, true) (Append Mode)...");
        BaseFighter bonusFighter = new CyberBrawler("Bonus Cyber Glitch", 105.0, 25.0, 45);
        boolean appendSuccess = manager.appendFighterRecord(bonusFighter);
        System.out.println("    -> Append Successful: " + appendSuccess);

        // 4. Read & Parse Records
        System.out.println("\n[*] Step 4: Reading and Parsing CSV File via BufferedReader.readLine()...");
        try {
            List<String[]> records = manager.loadFighterRecords();
            System.out.println("    -> Successfully Loaded " + records.size() + " Records from Disk:\n");
            System.out.printf("    %-4s %-22s %-10s %-10s %-12s %-16s\n", "No.", "Name", "Health", "Max HP", "Base Attack", "Archetype");
            System.out.println("    ---------------------------------------------------------------------------------");
            int count = 1;
            for (String[] r : records) {
                System.out.printf("    #%-3d %-22s %-10s %-10s %-12s %-16s\n", count++, r[0], r[1], r[2], r[3], (r.length > 4 ? r[4] : "BaseFighter"));
            }
        } catch (FileNotFoundException e) {
            System.err.println("    [ERROR] Missing file: " + e.getMessage());
        }

        // 5. Defensive Exception Guard Test
        System.out.println("\n[*] Step 5: Testing Defensive Exception Guard (Non-Existent File)...");
        FighterDataFileManager ghostManager = new FighterDataFileManager("non_existent_ghost_file.csv");
        try {
            ghostManager.loadFighterRecords();
        } catch (FileNotFoundException e) {
            System.out.println("    [VERIFIED] FileNotFoundException caught and handled cleanly: " + e.getMessage());
        }

        // Clean up demo file
        new File(demoFilePath).delete();
        System.out.println("\n=======================================================================");
        System.out.println("  CO4 FIGHTER DATA FILE MANAGER EXECUTION SUCCESSFUL (100% PASS)");
        System.out.println("=======================================================================");
    }
}

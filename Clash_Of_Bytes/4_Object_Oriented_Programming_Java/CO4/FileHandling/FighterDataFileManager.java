package CO4.FileHandling;

import CO2.OOP.BaseFighter;
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
}

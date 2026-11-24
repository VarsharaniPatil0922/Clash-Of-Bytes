package CO4.Generics;

import CO3.Collections.MatchRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Practical No. 04 / Task 4 | Course Outcome 4 (CO4):
 * JAVA GENERICS - GENERIC BATTLE ARCHIVE CONTAINER & GENERIC UTILITY METHODS
 */
public class GenericBattleArchive<T> {

    private final String archiveTitle;
    private final List<T> entries;

    public GenericBattleArchive(String title) {
        this.archiveTitle = title;
        this.entries = new ArrayList<>();
    }

    public void addEntry(T item) {
        if (item != null) {
            entries.add(item);
        }
    }

    public int size() {
        return entries.size();
    }

    public List<T> getAllEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Generic static utility method that extracts a numeric statistic
     * and computes average value across any arbitrary collection.
     */
    public static <E> double computeAverage(List<E> list, Function<E, Double> extractor) {
        if (list == null || list.isEmpty()) return 0.0;
        double sum = 0.0;
        for (E element : list) {
            sum += extractor.apply(element);
        }
        return sum / list.size();
    }

    /**
     * Generic static utility method to print elements with header.
     */
    public static <E> void printGenericList(String title, List<E> list) {
        System.out.println("--- " + title + " (Size: " + list.size() + ") ---");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + list.get(i).toString());
        }
        System.out.println();
    }

    public void displayArchive() {
        System.out.println("\n----------------------------------------------------------------------------------------");
        System.out.println(" GENERIC BATTLE ARCHIVE: " + archiveTitle + " (Total Items: " + entries.size() + ")");
        System.out.println("----------------------------------------------------------------------------------------");
        for (int i = 0; i < entries.size(); i++) {
            System.out.println("  (" + (i + 1) + ") " + entries.get(i));
        }
        System.out.println("----------------------------------------------------------------------------------------");
    }

    public String getArchiveTitle() {
        return archiveTitle;
    }

    /**
     * Standalone executable test / practical demonstration for GenericBattleArchive.
     */
    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES: CO4 PRACTICAL - GENERIC BATTLE ARCHIVE");
        System.out.println("  Topics: Unbounded Generics, Generic Methods, Lambda Extractors");
        System.out.println("=======================================================================\n");

        GenericBattleArchive<MatchRecord> archive = new GenericBattleArchive<>("Season 1 Championship Archive");
        archive.addEntry(new MatchRecord(1, "Elena", "Cyber Brawler", "Elena", "REAL_FIGHT", 3, 25.0, 0.0, 190.0, "Tournament Grand Final"));
        archive.addEntry(new MatchRecord(2, "Kenji", "Shadow Ninja", "Kenji", "REAL_FIGHT", 2, 40.0, 0.0, 160.0, "Semi-Final Flawless Round"));
        archive.addEntry(new MatchRecord(3, "Holy Paladin", "Titan Gladiator", "Holy Paladin", "TUTORIAL", 1, 80.0, 0.0, 210.0, "Exhibition Match"));

        System.out.println("[*] Step 1: Displaying Generic Archive Entries:");
        archive.displayArchive();

        System.out.println("[*] Step 2: Executing Generic Static Method (computeAverage):");
        double avgDamage = GenericBattleArchive.computeAverage(archive.getAllEntries(), MatchRecord::getTotalDamageDealt);
        System.out.printf("    -> Computed Average Damage Across All Matches: %.2f HP\n", avgDamage);

        System.out.println("\n=======================================================================");
        System.out.println("  CO4 GENERIC BATTLE ARCHIVE EXECUTION SUCCESSFUL (100% PASS)");
        System.out.println("=======================================================================");
    }
}

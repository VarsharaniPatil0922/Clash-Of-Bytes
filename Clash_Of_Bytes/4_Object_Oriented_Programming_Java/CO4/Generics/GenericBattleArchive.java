package CO4.Generics;

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
}

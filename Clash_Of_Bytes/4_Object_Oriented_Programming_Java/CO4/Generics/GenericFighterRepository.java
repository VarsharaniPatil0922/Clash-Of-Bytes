package CO4.Generics;

import CO2.OOP.BaseFighter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * ============================================================================
 * CLASH OF BYTES: PROBLEM SOLVING USING OOP (JAVA) - COURSE CODE: 2310217L
 * Practical No. 04 / Task 4 | Course Outcome 4 (CO4):
 * JAVA GENERICS - TYPE-SAFE FIGHTER REPOSITORY & GENERIC DATA STRUCTURES
 * ============================================================================
 * 
 * ACADEMIC & THEORETICAL FOUNDATIONS (CO4 - GENERICS):
 * 1. TYPE SAFETY AT COMPILE TIME:
 *    - Parametric polymorphism guarantees that only objects of type T (or its subclasses)
 *      can be stored in the repository.
 *    - Eliminates the need for manual, risky downcasting `(WarriorFighter) obj` and
 *      prevents `ClassCastException` at runtime.
 * 
 * 2. BOUNDED TYPE PARAMETER `<T extends BaseFighter>`:
 *    - Restricts type argument T to BaseFighter and its subclasses (WarriorFighter, NinjaFighter, etc.).
 *    - Enables the repository to directly invoke methods defined on BaseFighter (e.g.,
 *      `getName()`, `getHealth()`, `getBaseAttack()`, `performSpecialMove()`) without casting.
 * 
 * 3. CODE REUSABILITY & ELIMINATING DUPLICATION:
 *    - Instead of writing separate repositories for WarriorRepository, NinjaRepository,
 *      PaladinRepository, etc., a single generic repository manages all fighter categories.
 */
public class GenericFighterRepository<T extends BaseFighter> {

    private final String repositoryName;
    private final List<T> fighters;

    public GenericFighterRepository(String repositoryName) {
        this.repositoryName = repositoryName;
        this.fighters = new ArrayList<>();
    }

    /**
     * Add a fighter of generic type T to the repository.
     * Enforces compile-time type safety.
     */
    public void addFighter(T fighter) {
        if (fighter == null) {
            throw new IllegalArgumentException("Cannot add null fighter to repository.");
        }
        fighters.add(fighter);
    }

    public int size() {
        return fighters.size();
    }

    public List<T> getAllFighters() {
        return Collections.unmodifiableList(fighters);
    }

    /**
     * Generic Search by Fighter Name.
     */
    public T findByName(String name) {
        for (T f : fighters) {
            if (f.getName().equalsIgnoreCase(name)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Generic Filter using functional Predicate interface.
     */
    public List<T> filter(Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T f : fighters) {
            if (predicate.test(f)) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Filter fighters by minimum health threshold.
     */
    public List<T> filterByMinHealth(double minHp) {
        return filter(f -> f.getHealth() >= minHp);
    }

    /**
     * Generic Sorting using Comparator.
     */
    public void sortByAttackPowerDescending() {
        fighters.sort(Comparator.comparingDouble(BaseFighter::getBaseAttack).reversed());
    }

    public void sortByHealthDescending() {
        fighters.sort(Comparator.comparingDouble(BaseFighter::getHealth).reversed());
    }

    /**
     * Display all fighters stored in the generic repository.
     */
    public void displayRepository() {
        System.out.println("\n========================================================================================");
        System.out.println(" GENERIC FIGHTER REPOSITORY: [" + repositoryName + "] (Count: " + fighters.size() + ")");
        System.out.println(" Type Parameter: <T extends BaseFighter>");
        System.out.println("========================================================================================");
        System.out.printf("%-6s %-20s %-16s %-12s %-12s %-12s\n",
                "No.", "Fighter Name", "Concrete Type", "Health", "Max HP", "Attack Power");
        System.out.println("----------------------------------------------------------------------------------------");
        int index = 1;
        for (T f : fighters) {
            System.out.printf("#%-5d %-20s %-16s %-12.1f %-12.1f %-12.1f\n",
                    index++, f.getName(), f.getClass().getSimpleName(), f.getHealth(), f.getMaxHealth(), f.getBaseAttack());
        }
        System.out.println("========================================================================================");
    }

    public String getRepositoryName() {
        return repositoryName;
    }
}

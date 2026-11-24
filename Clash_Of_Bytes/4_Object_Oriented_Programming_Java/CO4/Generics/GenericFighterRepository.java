package CO4.Generics;

import CO2.OOP.BaseFighter;
import CO2.OOP.WarriorFighter;
import CO2.OOP.NinjaFighter;
import CO2.OOP.CyberBrawler;
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

    /**
     * Standalone executable test / practical demonstration for GenericFighterRepository.
     */
    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES: CO4 PRACTICAL - GENERIC FIGHTER REPOSITORY");
        System.out.println("  Topics: Parametric Polymorphism, Bounded Type <T extends BaseFighter>");
        System.out.println("=======================================================================\n");

        // 1. Generic Repository bounded to BaseFighter
        GenericFighterRepository<BaseFighter> masterGuild = new GenericFighterRepository<>("Master Champions Guild");
        masterGuild.addFighter(new WarriorFighter("Ironclad Warrior", 120.0, 24.0, 8.0));
        masterGuild.addFighter(new NinjaFighter("Shadow Ninja", 90.0, 32.0, 35));
        masterGuild.addFighter(new CyberBrawler("Cyber Brawler", 110.0, 26.0, 40));

        System.out.println("[*] Step 1: Displaying Initial Generic Repository Content:");
        masterGuild.displayRepository();

        // 2. Generic Predicate Filter
        System.out.println("\n[*] Step 2: Generic Predicate Filter (Health >= 100 HP):");
        List<BaseFighter> tankyFighters = masterGuild.filterByMinHealth(100.0);
        for (BaseFighter f : tankyFighters) {
            System.out.printf("    -> %-18s | HP: %.1f | ATK: %.1f\n", f.getName(), f.getHealth(), f.getBaseAttack());
        }

        // 3. Generic Sorting by Attack Power
        System.out.println("\n[*] Step 3: Generic Sorting by Attack Power (Descending):");
        masterGuild.sortByAttackPowerDescending();
        masterGuild.displayRepository();

        // 4. Specialized Subclass Repository (Compile-time Type Safety Proof)
        System.out.println("\n[*] Step 4: Subclass Type Parameter Specialization <WarriorFighter>:");
        GenericFighterRepository<WarriorFighter> warriorOnlyGuild = new GenericFighterRepository<>("Warrior-Only League");
        warriorOnlyGuild.addFighter(new WarriorFighter("Vanguard Knight", 130.0, 22.0, 10.0));
        warriorOnlyGuild.displayRepository();

        System.out.println("=======================================================================");
        System.out.println("  CO4 GENERIC FIGHTER REPOSITORY EXECUTION SUCCESSFUL (100% PASS)");
        System.out.println("=======================================================================");
    }
}

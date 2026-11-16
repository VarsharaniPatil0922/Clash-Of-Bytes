package CO1.JavaFundamentals;

import CO2.OOP.BaseFighter;





/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO1 | Practical 01
 * Topic: Java Fundamentals, 1D Arrays, Looping, and Control Flow for Fighter Roster
 */
public class CombatAnalytics {

    // Demonstrates 1D Array Statistics & Iteration for Character Roster
    public static void demonstrateRosterAnalytics(BaseFighter[] roster) {
        System.out.println("=======================================================================");
        System.out.println("  PRACTICAL 01: JAVA FUNDAMENTALS, CONTROL STATEMENTS, LOOPS & ARRAYS  ");
        System.out.println("=======================================================================");

        // Part 1: Looping over 1D Array
        System.out.println("[ROSTER 1D ARRAY ITERATION]:");
        for (int i = 0; i < roster.length; i++) {
            System.out.printf("  [%d] %-18s | Health: %5.1f | Base Attack: %4.1f%n",
                    (i + 1), roster[i].getName(), roster[i].getHealth(), roster[i].getBaseAttack());
        }

        // Part 2: Array Operations (Min, Max, Average Calculation)
        double totalHealth = 0.0;
        double maxHealth = roster[0].getHealth();
        double minHealth = roster[0].getHealth();
        String highestHpFighter = roster[0].getName();
        String lowestHpFighter = roster[0].getName();

        for (BaseFighter fighter : roster) {
            totalHealth += fighter.getHealth();
            if (fighter.getHealth() > maxHealth) {
                maxHealth = fighter.getHealth();
                highestHpFighter = fighter.getName();
            }
            if (fighter.getHealth() < minHealth) {
                minHealth = fighter.getHealth();
                lowestHpFighter = fighter.getName();
            }
        }
        double averageHealth = totalHealth / roster.length;

        System.out.println("\n[ARRAY STATISTICAL COMPUTATION]:");
        System.out.printf("  Average Roster HP : %.1f%n", averageHealth);
        System.out.printf("  Highest HP (Tank) : %s (%.1f HP)%n", highestHpFighter, maxHealth);
        System.out.printf("  Lowest HP (Agile) : %s (%.1f HP)%n", lowestHpFighter, minHealth);

        // Part 3: Control Statements (if-else branching for competitive tier grading)
        System.out.println("\n[CONTROL STATEMENTS - CHARACTER POWER TIER GRADING]:");
        for (BaseFighter fighter : roster) {
            String tier;
            if (fighter.getBaseAttack() >= 22.0) {
                tier = "S-Tier [Heavy Striker]";
            } else if (fighter.getBaseAttack() >= 18.0) {
                tier = "A-Tier [Balanced Vanguard]";
            } else {
                tier = "B-Tier [Speed Infiltrator]";
            }
            System.out.printf("  - %-18s -> Assigned Tier: %s%n", fighter.getName(), tier);
        }
    }
}

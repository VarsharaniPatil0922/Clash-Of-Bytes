package CO1.StringHandling;

/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO2 | Practical 02
 * Topic: String, StringBuffer & StringBuilder Operations for High-Performance Combat Logging
 */
public class CombatLogger {

    private StringBuilder combatHistory;
    private StringBuffer battleHistory;

    public CombatLogger() {
        this.combatHistory = new StringBuilder();
        this.combatHistory.append("=== REAL-TIME COMBAT ACTION LOG (StringBuilder) ===\n");
        this.battleHistory = new StringBuffer();
    }

    // Appends combat moves efficiently using mutable StringBuilder
    public void logAction(String attacker, String target, String moveName, double damage, double remainingHp) {
        String entry = String.format("  [*] %s executes %s on %s for %.1f dmg [Target HP: %.1f]%n",
                attacker, moveName, target, damage, remainingHp);
        combatHistory.append(entry);
        battleHistory.append(entry);
    }

    public StringBuffer getBattleHistory() {
        return battleHistory;
    }

    public void logBanner(String bannerText) {
        combatHistory.append("  >>> ").append(bannerText).append(" <<<\n");
    }

    public String getFormattedLog() {
        return combatHistory.toString();
    }

    public void demonstrateStringBufferOperations(String fighterName) {
        System.out.println("\n=======================================================================");
        System.out.println("  PRACTICAL 02: STRINGBUFFER OPERATIONS DEMONSTRATION                  ");
        System.out.println("=======================================================================");
        
        StringBuffer buffer = new StringBuffer();
        
        // 1. append()
        buffer.append(fighterName).append(" entered the arena.");
        System.out.println("  [append]  : " + buffer);
        
        // 2. insert()
        buffer.insert(0, "[EVENT] ");
        System.out.println("  [insert]  : " + buffer);
        
        // 3. replace()
        int idx = buffer.indexOf(fighterName);
        if (idx != -1) {
            buffer.replace(idx, idx + fighterName.length(), "The Champion");
        }
        System.out.println("  [replace] : " + buffer);
        
        // 4. delete()
        buffer.delete(0, 8); // Removes "[EVENT] "
        System.out.println("  [delete]  : " + buffer);
        
        // 5. reverse()
        buffer.reverse();
        System.out.println("  [reverse] : " + buffer);
    }

    // Benchmark demonstration: Demonstrates why StringBuilder is superior to String (+)
    public static void demonstratePerformanceBenchmark() {
        System.out.println("\n=======================================================================");
        System.out.println("  PRACTICAL 02: STRING VS STRINGBUILDER PERFORMANCE BENCHMARK          ");
        System.out.println("=======================================================================");

        int iterations = 10000;

        // Test immutable String concatenation
        long startStr = System.nanoTime();
        String strTest = "";
        for (int i = 0; i < iterations; i++) {
            strTest += "Hit ";
        }
        long timeStr = System.nanoTime() - startStr;

        // Test mutable StringBuilder appending
        long startSb = System.nanoTime();
        StringBuilder sbTest = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sbTest.append("Hit ");
        }
        long timeSb = System.nanoTime() - startSb;

        double speedup = (double) timeStr / Math.max(1, timeSb);

        System.out.printf("  Concatenating %d entries (Sample: %d chars):%n", iterations, strTest.length());
        System.out.printf("  - String (+) elapsed time        : %7.3f ms%n", timeStr / 1e6);
        System.out.printf("  - StringBuilder elapsed time     : %7.3f ms%n", timeSb / 1e6);
        System.out.printf("  - StringBuilder Performance Gain : %5.1fx faster!%n", speedup);
        System.out.println("  [CONCLUSION] StringBuilder avoids heap churn from immutable String reallocation.");
    }
}

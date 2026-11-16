# CO1 Implementations

| Concept | File | Implementation | Why Used |
|---------|------|----------------|----------|
| Control statements | `CO1/JavaFundamentals/CombatAnalytics.java` | `if-else` statements rank fighters into tiers (S-Tier, A-Tier, etc.) based on stats. | To demonstrate decision-making logic and branching based on character attributes. |
| Loops | `CO1/JavaFundamentals/CombatAnalytics.java` | A `for-each` loop iterates over the roster array to compute averages. | To process collections of characters efficiently. |
| Arrays | `Main/CombatManager.java` | A 1D array `roster` stores multiple fighter objects. | To group related objects and pass them collectively to utility methods. |
| Objects | `Main/CombatManager.java` | Concrete instances of fighters are created using the `new` keyword. | Objects are the foundational instances representing the game entities in OOP. |
| Methods | `CO2/OOP/BaseFighter.java` | Methods like `takeDamage()` and getters encapsulate behavior. | To modularize reusable logic and manipulate object state securely. |
| StringBuilder | `CO1/StringHandling/CombatLogger.java` | `StringBuilder` appends combat events iteratively. | To avoid creating redundant immutable `String` objects, greatly improving memory efficiency. |
| StringBuffer | `CO1/StringHandling/CombatLogger.java` | `StringBuffer` creates the final match report using `append`, `insert`, `replace`, `delete`, and `reverse`. | To demonstrate thread-safe mutable string operations during report generation. |
| Guidelines & "When to Hit" Indicator | `CO1/JavaFundamentals/HitTimingIndicator.java` | Renders the pre-match "How to Play" screen, startup `[ENTER]` prompt, and dynamic beginner "When to Hit" sweet-spot meter with stance vulnerability scoring. | To guide first-time players with visual cues for timing critical strikes vs defending against fortified guards. |

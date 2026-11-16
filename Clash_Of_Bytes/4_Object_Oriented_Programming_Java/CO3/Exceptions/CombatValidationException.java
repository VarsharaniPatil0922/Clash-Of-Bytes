package CO3.Exceptions;

/**
 * Course Outcome: CO3 | Practical 03: Exception Handling
 * 
 * Thrown during combat simulations or match processing when an action violates
 * combat constraints (e.g., negative damage values, actions on fainted fighters,
 * or dequeuing from an empty combat action queue).
 */
public class CombatValidationException extends ClashOfBytesException {

    private static final long serialVersionUID = 1L;
    private final String combatantName;
    private final double invalidValue;

    public CombatValidationException(String message) {
        super("ERR_COMBAT_VALIDATION", message);
        this.combatantName = "UNKNOWN";
        this.invalidValue = Double.NaN;
    }

    public CombatValidationException(String reason, String combatantName, double invalidValue) {
        super("ERR_COMBAT_STATE_VIOLATION",
              String.format("Combat validation failed for fighter '%s': %s (Value: %.2f)",
                            combatantName, reason, invalidValue),
              "Combatant=" + combatantName + ", Value=" + invalidValue);
        this.combatantName = combatantName;
        this.invalidValue = invalidValue;
    }

    public String getCombatantName() {
        return combatantName;
    }

    public double getInvalidValue() {
        return invalidValue;
    }
}

package CO3.Collections;

/**
 * Course Outcome: CO3 | Practical 03: Collections Framework
 * 
 * Represents an individual combat action queued for sequential FIFO processing.
 */
public class CombatAction {

    public enum ActionType {
        FAST_PUNCH("Fast Punch", 1.0),
        HEAVY_KICK("Heavy Kick", 1.5),
        SPECIAL_ART("Class Special Art", 2.0),
        GUARD_DEFENSE("Shield / Guard Defense", 0.0),
        SPARRING_HEAL("Tutorial Sparring Regen", 0.0);

        private final String displayName;
        private final double defaultMultiplier;

        ActionType(String displayName, double defaultMultiplier) {
            this.displayName = displayName;
            this.defaultMultiplier = defaultMultiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getDefaultMultiplier() {
            return defaultMultiplier;
        }
    }

    private final int turnNumber;
    private final String attackerName;
    private final String targetName;
    private final ActionType actionType;
    private final double basePower;
    private final double multiplier;
    private final String note;

    public CombatAction(int turnNumber, String attackerName, String targetName, 
                        ActionType actionType, double basePower, double multiplier, String note) {
        this.turnNumber = turnNumber;
        this.attackerName = attackerName;
        this.targetName = targetName;
        this.actionType = actionType;
        this.basePower = Math.max(0.0, basePower);
        this.multiplier = Math.max(0.0, multiplier);
        this.note = (note != null) ? note : "";
    }

    public int getTurnNumber() { return turnNumber; }
    public String getAttackerName() { return attackerName; }
    public String getTargetName() { return targetName; }
    public ActionType getActionType() { return actionType; }
    public double getBasePower() { return basePower; }
    public double getMultiplier() { return multiplier; }
    public String getNote() { return note; }

    public double calculateRawDamage() {
        return basePower * multiplier;
    }

    @Override
    public String toString() {
        return String.format("[Turn %d] %s -> %s via %s (Power: %.1f x %.2f)%s",
                turnNumber, attackerName, targetName, actionType.getDisplayName(),
                basePower, multiplier, note.isEmpty() ? "" : " [" + note + "]");
    }
}

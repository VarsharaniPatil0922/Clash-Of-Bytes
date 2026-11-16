package CO3.Exceptions;

/**
 * Course Outcome: CO3 | Practical 03: Exception Handling
 * 
 * Thrown when an invalid fighter index, unregistered fighter name, or illegal
 * mirror-match selection is requested by the player, AI, or GUI.
 */
public class InvalidFighterSelectionException extends ClashOfBytesException {

    private static final long serialVersionUID = 1L;
    private final int requestedIndex;
    private final String fighterName;

    public InvalidFighterSelectionException(String message) {
        super("ERR_INVALID_FIGHTER_SELECTION", message);
        this.requestedIndex = -1;
        this.fighterName = "UNKNOWN";
    }

    public InvalidFighterSelectionException(int requestedIndex, int maxIndex) {
        super("ERR_FIGHTER_INDEX_OUT_OF_BOUNDS",
              String.format("Fighter index %d is out of bounds. Valid index range is [0, %d].", 
                            requestedIndex, Math.max(0, maxIndex - 1)),
              "Index=" + requestedIndex + ", MaxAllowed=" + (maxIndex - 1));
        this.requestedIndex = requestedIndex;
        this.fighterName = "INDEX_" + requestedIndex;
    }

    public InvalidFighterSelectionException(String fighterName, String reason) {
        super("ERR_FIGHTER_LOOKUP_FAILED",
              String.format("Invalid fighter selection '%s': %s", 
                            (fighterName != null ? fighterName : "<null>"), reason),
              "FighterName=" + fighterName);
        this.requestedIndex = -1;
        this.fighterName = (fighterName != null ? fighterName : "<null>");
    }

    public int getRequestedIndex() {
        return requestedIndex;
    }

    public String getFighterName() {
        return fighterName;
    }
}

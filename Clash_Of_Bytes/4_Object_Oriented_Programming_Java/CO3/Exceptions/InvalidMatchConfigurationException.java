package CO3.Exceptions;

/**
 * Course Outcome: CO3 | Practical 03: Exception Handling
 * 
 * Thrown when match parameters (e.g. rounds count, round timer duration,
 * handicap multiplier, or combat mode string) violate defined limits.
 */
public class InvalidMatchConfigurationException extends ClashOfBytesException {

    private static final long serialVersionUID = 1L;
    private final String parameterName;
    private final String invalidValue;

    public InvalidMatchConfigurationException(String message) {
        super("ERR_INVALID_MATCH_CONFIG", message);
        this.parameterName = "UNKNOWN";
        this.invalidValue = null;
    }

    public InvalidMatchConfigurationException(String parameterName, Object invalidValue, String validRangeDescription) {
        super("ERR_CONFIG_PARAMETER_OUT_OF_RANGE",
              String.format("Invalid configuration for '%s' with value '%s'. Expected: %s",
                            parameterName, String.valueOf(invalidValue), validRangeDescription),
              "Param=" + parameterName + ", Provided=" + invalidValue);
        this.parameterName = parameterName;
        this.invalidValue = String.valueOf(invalidValue);
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}

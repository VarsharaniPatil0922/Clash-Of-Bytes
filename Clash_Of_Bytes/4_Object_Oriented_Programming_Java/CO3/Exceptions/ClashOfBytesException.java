package CO3.Exceptions;

import java.time.Instant;

/**
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO3 | Practical 03: Exception Handling Architecture
 * 
 * Base checked exception class for all domain-specific errors in "Clash of Bytes".
 * Encapsulates error codes, timestamps, and contextual diagnostic details.
 */
public class ClashOfBytesException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final long timestamp;
    private final String details;

    public ClashOfBytesException(String message) {
        this("ERR_GENERAL", message, null, null);
    }

    public ClashOfBytesException(String errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public ClashOfBytesException(String errorCode, String message, String details) {
        this(errorCode, message, details, null);
    }

    public ClashOfBytesException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public ClashOfBytesException(String errorCode, String message, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = (errorCode != null && !errorCode.trim().isEmpty()) ? errorCode : "ERR_GENERAL";
        this.timestamp = Instant.now().toEpochMilli();
        this.details = (details != null) ? details : "";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode).append("] ").append(getMessage());
        if (!details.isEmpty()) {
            sb.append(" (Details: ").append(details).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}

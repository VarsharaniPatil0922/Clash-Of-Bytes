package service;

/**
 * Thrown when a player attempts to log in but their email address has not yet been confirmed.
 */
public class EmailNotConfirmedException extends Exception {
    private static final long serialVersionUID = 1L;

    public EmailNotConfirmedException(String message) {
        super(message);
    }
}

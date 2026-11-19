package service;

/**
 * Thrown when a player enters an incorrect password or unregistered email address.
 */
public class InvalidCredentialsException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

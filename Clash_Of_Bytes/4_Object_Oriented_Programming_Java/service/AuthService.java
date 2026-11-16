package service;

import dao.UserDAO;
import model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Service handling Secure Authentication, Registration, Input Validation,
 * and Cryptographic Password Hashing.
 */
public class AuthService {

    private final UserDAO userDAO;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user with input validation and cryptographic salted hash.
     */
    public User registerUser(String username, String email, String password) throws Exception {
        // 1. Validation
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException("Username must be 3-20 characters long and contain only letters, numbers, or underscores.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please provide a valid email address (e.g., player@example.com).");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters in length.");
        }

        username = username.trim();
        email = email.trim();

        // 2. Uniqueness checks
        if (userDAO.isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered. Please choose another.");
        }
        if (userDAO.isEmailTaken(email)) {
            throw new IllegalArgumentException("Email address '" + email + "' is already in use.");
        }

        // 3. Cryptographic Salting & Hashing
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);

        // 4. Persistence
        User newUser = new User(username, email, passwordHash, salt);
        return userDAO.createUser(newUser);
    }

    /**
     * Authenticates a user by username or email and validates password.
     */
    public User authenticateUser(String identifier, String password) throws Exception {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your username or email address.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Please enter your password.");
        }

        identifier = identifier.trim();
        User user = null;

        if (identifier.contains("@")) {
            user = userDAO.getUserByEmail(identifier);
        } else {
            user = userDAO.getUserByUsername(identifier);
        }

        if (user == null) {
            throw new IllegalArgumentException("Invalid credentials: No account found for '" + identifier + "'.");
        }

        String computedHash = hashPassword(password, user.getSalt());
        if (!computedHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Authentication failed: Incorrect password.");
        }

        return user;
    }

    /**
     * Cryptographic SHA-256 salted hash generator with 1,000 stretching iterations.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(hexStringToByteArray(salt));
            byte[] hashed = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Stretching iterations to deter brute-force attacks
            for (int i = 0; i < 1000; i++) {
                md.reset();
                hashed = md.digest(hashed);
            }
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable on current JVM", e);
        }
    }

    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return bytesToHex(salt);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((java.lang.Character.digit(s.charAt(i), 16) << 4)
                                 + java.lang.Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

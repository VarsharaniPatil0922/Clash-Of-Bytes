package service;

import model.PlayerSession;
import util.SimpleJson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service managing user authentication, registration, email confirmation handling,
 * and token refresh against the Supabase Auth GoTrue REST API.
 *
 * Implements:
 * 1. Player registration with username metadata.
 * 2. Player login with email and password.
 * 3. Token refresh and session renewal.
 * 4. Independent player logout.
 * 5. Distinct handling for unconfirmed emails vs. invalid credentials.
 */
public class SupabaseAuthService {

    private final HttpClient httpClient;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public static class AuthResult {
        public final String userId;
        public final String username;
        public final String email;
        public final boolean emailConfirmationRequired;
        public final PlayerSession session;
        public final String message;

        public AuthResult(String userId, String username, String email,
                          boolean emailConfirmationRequired, PlayerSession session, String message) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.emailConfirmationRequired = emailConfirmationRequired;
            this.session = session;
            this.message = message;
        }
    }

    public SupabaseAuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public SupabaseAuthService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Registers a new user with Supabase Auth.
     */
    public AuthResult registerUser(String username, String email, String password) throws Exception {
        // 1. Client-Side Input Validation
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

        // 2. Prepare GoTrue Signup Payload
        String jsonPayload = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"data\":{\"username\":\"%s\"}}",
                SimpleJson.escape(email),
                SimpleJson.escape(password),
                SimpleJson.escape(username)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(SupabaseConfig.getAuthUri("/signup"))
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IOException("Unable to reach Supabase Auth server: " + e.getMessage(), e);
        }

        int statusCode = response.statusCode();
        String responseBody = response.body();
        Map<String, Object> respMap = SimpleJson.parseObject(responseBody);

        if (statusCode >= 200 && statusCode < 300) {
            String userId = SimpleJson.getString(respMap, "id");
            if (userId == null) {
                Map<String, Object> userObj = SimpleJson.getObject(respMap, "user");
                userId = SimpleJson.getString(userObj, "id");
            }

            String accessToken = SimpleJson.getString(respMap, "access_token");
            String refreshToken = SimpleJson.getString(respMap, "refresh_token");
            long expiresIn = SimpleJson.getLong(respMap, "expires_in", 3600);

            boolean requiresConfirmation = (accessToken == null || accessToken.isEmpty());
            PlayerSession session = null;

            if (!requiresConfirmation && userId != null) {
                session = new PlayerSession(userId, username, email, accessToken, refreshToken, expiresIn);
            }

            String msg = requiresConfirmation
                    ? "Confirmation email sent to " + email + ". Please verify your email before logging in."
                    : "Registration successful! Account active.";

            return new AuthResult(userId, username, email, requiresConfirmation, session, msg);
        } else {
            String errorCode = SimpleJson.getString(respMap, "error_code");
            String msg = SimpleJson.getString(respMap, "msg");
            if (msg == null) msg = SimpleJson.getString(respMap, "message");

            if ("user_already_exists".equalsIgnoreCase(errorCode) || (msg != null && msg.toLowerCase().contains("already registered"))) {
                throw new IllegalArgumentException("An account with email '" + email + "' already exists. Please log in.");
            }
            if ("over_email_send_rate_limit".equalsIgnoreCase(errorCode)) {
                throw new IllegalStateException("Email send rate limit reached. Please wait a few minutes before registering again.");
            }
            if (msg != null && !msg.isEmpty()) {
                throw new IllegalArgumentException(msg);
            }
            throw new IllegalArgumentException("Registration failed (HTTP " + statusCode + ")");
        }
    }

    /**
     * Authenticates a user with Supabase Auth using email and password.
     */
    public PlayerSession authenticateUser(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your registered email address.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Please enter your password.");
        }

        email = email.trim();

        String jsonPayload = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                SimpleJson.escape(email),
                SimpleJson.escape(password)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(SupabaseConfig.getAuthUri("/token?grant_type=password"))
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IOException("Unable to reach Supabase Auth server: " + e.getMessage(), e);
        }

        int statusCode = response.statusCode();
        String responseBody = response.body();
        Map<String, Object> respMap = SimpleJson.parseObject(responseBody);

        if (statusCode >= 200 && statusCode < 300) {
            String accessToken = SimpleJson.getString(respMap, "access_token");
            String refreshToken = SimpleJson.getString(respMap, "refresh_token");
            long expiresIn = SimpleJson.getLong(respMap, "expires_in", 3600);

            Map<String, Object> userObj = SimpleJson.getObject(respMap, "user");
            String userId = SimpleJson.getString(userObj, "id");
            String userEmail = SimpleJson.getString(userObj, "email", email);

            Map<String, Object> meta = SimpleJson.getObject(userObj, "user_metadata");
            String username = SimpleJson.getString(meta, "username");
            if (username == null || username.trim().isEmpty()) {
                username = userEmail.contains("@") ? userEmail.split("@")[0] : userEmail;
            }

            if (userId == null || accessToken == null) {
                throw new IllegalStateException("Supabase Auth returned malformed token response.");
            }

            return new PlayerSession(userId, username, userEmail, accessToken, refreshToken, expiresIn);
        } else {
            String errorCode = SimpleJson.getString(respMap, "error_code");
            String msg = SimpleJson.getString(respMap, "msg");
            if (msg == null) msg = SimpleJson.getString(respMap, "message");

            if ("email_not_confirmed".equalsIgnoreCase(errorCode) || (msg != null && msg.toLowerCase().contains("email not confirmed"))) {
                throw new EmailNotConfirmedException("Email address '" + email + "' has not been confirmed yet.\n"
                        + "Please check your inbox for the confirmation link from Supabase before logging in.");
            }

            if ("invalid_credentials".equalsIgnoreCase(errorCode) || (msg != null && msg.toLowerCase().contains("invalid login credentials"))) {
                throw new InvalidCredentialsException("Invalid login credentials. Please check your registered email address and password.");
            }

            throw new IllegalArgumentException(msg != null ? msg : "Authentication failed with status " + statusCode);
        }
    }

    /**
     * Refreshes an expired or expiring player session token.
     */
    public void refreshToken(PlayerSession session) throws Exception {
        if (session == null || session.getRefreshToken() == null) {
            throw new IllegalArgumentException("Cannot refresh token: null session or missing refresh token.");
        }

        String jsonPayload = String.format(
                "{\"refresh_token\":\"%s\"}",
                SimpleJson.escape(session.getRefreshToken())
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(SupabaseConfig.getAuthUri("/token?grant_type=refresh_token"))
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            Map<String, Object> respMap = SimpleJson.parseObject(response.body());
            String newAccessToken = SimpleJson.getString(respMap, "access_token");
            String newRefreshToken = SimpleJson.getString(respMap, "refresh_token");
            long expiresIn = SimpleJson.getLong(respMap, "expires_in", 3600);
            session.updateTokens(newAccessToken, newRefreshToken, expiresIn);
        } else {
            throw new IOException("Failed to refresh session token: HTTP " + response.statusCode());
        }
    }

    /**
     * Logs out a player session from Supabase GoTrue.
     */
    public void logoutUser(PlayerSession session) {
        if (session == null || session.getAccessToken() == null) return;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(SupabaseConfig.getAuthUri("/logout"))
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .header("Authorization", "Bearer " + session.getAccessToken())
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // Best effort logout; local in-memory session is cleared regardless
        }
    }
}

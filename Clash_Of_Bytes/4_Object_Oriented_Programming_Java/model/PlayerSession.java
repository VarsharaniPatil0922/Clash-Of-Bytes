package model;

import java.time.Instant;

/**
 * In-memory authentication session for a logged-in player.
 * Contains:
 * - Supabase Auth UUID
 * - Username
 * - Email
 * - JWT Access Token (Bearer)
 * - Refresh Token
 * - Expiration Timestamp
 */
public class PlayerSession {

    private final String userId;      // Supabase Auth UUID (primary key in public.users)
    private final String username;    // Profile username
    private final String email;       // Registered email address
    private String accessToken;       // JWT Bearer access token
    private String refreshToken;      // Refresh token
    private long expiresAtEpochSec;   // Expiration in epoch seconds

    public PlayerSession(String userId, String username, String email,
                         String accessToken, String refreshToken, long expiresInSeconds) {
        this.userId = userId;
        this.username = (username != null && !username.isEmpty()) ? username : email.split("@")[0];
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAtEpochSec = Instant.now().getEpochSecond() + expiresInSeconds;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized String getRefreshToken() {
        return refreshToken;
    }

    public synchronized long getExpiresAtEpochSec() {
        return expiresAtEpochSec;
    }

    public synchronized void updateTokens(String newAccessToken, String newRefreshToken, long expiresInSeconds) {
        if (newAccessToken != null && !newAccessToken.isEmpty()) {
            this.accessToken = newAccessToken;
        }
        if (newRefreshToken != null && !newRefreshToken.isEmpty()) {
            this.refreshToken = newRefreshToken;
        }
        this.expiresAtEpochSec = Instant.now().getEpochSecond() + expiresInSeconds;
    }

    /**
     * Checks if the access token has expired (or is within 30 seconds of expiring).
     */
    public synchronized boolean isExpired() {
        return Instant.now().getEpochSecond() >= (expiresAtEpochSec - 30);
    }

    @Override
    public String toString() {
        return "PlayerSession{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", expiresAt=" + expiresAtEpochSec +
                ", isExpired=" + isExpired() +
                '}';
    }
}

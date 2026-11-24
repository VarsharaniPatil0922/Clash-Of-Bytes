package CO3.Auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for communicating with Supabase Authentication & PostgreSQL REST endpoints.
 * 
 * Uses standard java.net.HttpURLConnection for maximum portability and resilience
 * across all Windows JDK environments (eliminates local loopback pipe socket errors).
 * 
 * Key Capabilities:
 * 1. User Sign Up (POST /auth/v1/signup) with user metadata (username)
 * 2. User Sign In (POST /auth/v1/token?grant_type=password) returning JWT access token
 * 3. Connection Health Check (GET /auth/v1/health)
 * 4. Dual Player Match History Recording (POST /rest/v1/match_history with 2 rows per match)
 * 5. Standings / Profiles Management (GET /rest/v1/profiles, PATCH/POST upsert)
 */
public class SupabaseAuthService {

    public SupabaseAuthService() {
    }

    /**
     * DTO for authentication responses.
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String userId;
        private final String email;
        private final String username;
        private final String accessToken;

        public AuthResult(boolean success, String message, String userId, String email, String username, String accessToken) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.email = email;
            this.username = username != null && !username.isBlank() ? username : (email != null ? email.split("@")[0] : "Player");
            this.accessToken = accessToken;
        }

        public static AuthResult success(String message, String userId, String email, String username, String accessToken) {
            return new AuthResult(true, message, userId, email, username, accessToken);
        }

        public static AuthResult failure(String errorMessage) {
            return new AuthResult(false, errorMessage, null, null, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getAccessToken() { return accessToken; }

        @Override
        public String toString() {
            return "AuthResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", userId='" + userId + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }

    /**
     * DTO for Supabase Player Profile & Match Statistics.
     */
    public static class UserProfile {
        private final String id;
        private final String username;
        private final String email;
        private final int wins;
        private final int losses;
        private final int matches;
        private final double highestDamage;
        private final String favoriteFighter;

        public UserProfile(String id, String username, String email, int wins, int losses, int matches, double highestDamage, String favoriteFighter) {
            this.id = id;
            this.username = username != null ? username : "Unknown";
            this.email = email != null ? email : "";
            this.wins = wins;
            this.losses = losses;
            this.matches = matches;
            this.highestDamage = highestDamage;
            this.favoriteFighter = favoriteFighter != null && !favoriteFighter.isBlank() ? favoriteFighter : "Ironclad Warrior";
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getMatches() { return matches; }
        public double getHighestDamage() { return highestDamage; }
        public String getFavoriteFighter() { return favoriteFighter; }

        public double getWinRate() {
            return matches > 0 ? ((double) wins / matches) * 100.0 : 0.0;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "UserProfile[user=%s, wins=%d, losses=%d, matches=%d, dmg=%.1f, fav=%s]",
                    username, wins, losses, matches, highestDamage, favoriteFighter);
        }
    }

    /**
     * DTO for Supabase Match History Item.
     */
    public static class SupabaseMatchRecord {
        public final String matchId;
        public final String playerId;
        public final String playerName;
        public final String p1Fighter;
        public final String p2Fighter;
        public final String winner;
        public final String result; // WIN, LOSS, DRAW
        public final int roundsP1;
        public final int roundsP2;
        public final String playedAt;
        public final String player1;
        public final String player2;
        public final String score;
        public final String outcome;

        public SupabaseMatchRecord(String matchId, String playerId, String playerName,
                                   String p1Fighter, String p2Fighter, String winner,
                                   String result, int roundsP1, int roundsP2, String playedAt) {
            this(matchId, playerId, playerName, p1Fighter, p2Fighter, winner, result, roundsP1, roundsP2, playedAt, null, null, null, null);
        }

        public SupabaseMatchRecord(String matchId, String playerId, String playerName,
                                   String p1Fighter, String p2Fighter, String winner,
                                   String result, int roundsP1, int roundsP2, String playedAt,
                                   String player1, String player2, String score, String outcome) {
            this.matchId = matchId;
            this.playerId = playerId;
            this.playerName = playerName != null ? playerName : "Player";
            this.p1Fighter = p1Fighter != null ? p1Fighter : "Kenji";
            this.p2Fighter = p2Fighter != null ? p2Fighter : "Elena";
            this.winner = winner != null ? winner : "DRAW";
            this.result = result != null ? result : "WIN";
            this.roundsP1 = roundsP1;
            this.roundsP2 = roundsP2;
            this.playedAt = playedAt != null ? playedAt : "";
            this.player1 = (player1 != null && !player1.isBlank()) ? player1 : this.playerName;
            this.player2 = (player2 != null && !player2.isBlank()) ? player2 : this.p2Fighter;
            this.score = (score != null && !score.isBlank()) ? score : (this.roundsP1 + " - " + this.roundsP2);
            this.outcome = (outcome != null && !outcome.isBlank()) ? outcome : ("WIN".equalsIgnoreCase(this.result) ? (this.playerName + " WON") : (this.winner + " WON"));
        }
    }

    // =========================================================================
    // CORE HTTP REQUEST DISPATCHER (HttpURLConnection)
    // =========================================================================

    public static class HttpResult {
        public final int statusCode;
        public final String body;
        public HttpResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body != null ? body : "";
        }
    }

    private HttpResult sendHttpRequest(String urlStr, String method, String jsonPayload, String authToken, String prefer) throws IOException {
        URL url;
        try {
            url = new URI(urlStr).toURL();
        } catch (Exception e) {
            throw new IOException("Invalid URL: " + urlStr, e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(10000);

        String anonKey = SupabaseConfig.getSupabaseAnonKey();
        conn.setRequestProperty("apikey", anonKey);
        String token = (authToken != null && !authToken.isBlank()) ? authToken : anonKey;
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");

        if (prefer != null && !prefer.isBlank()) {
            conn.setRequestProperty("Prefer", prefer);
        }

        if ("PATCH".equalsIgnoreCase(method)) {
            // Standard PostgREST method override header
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        } else {
            conn.setRequestMethod(method);
        }

        if (jsonPayload != null && !jsonPayload.isBlank()) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] bytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(bytes, 0, bytes.length);
            }
        }

        int statusCode;
        try {
            statusCode = conn.getResponseCode();
        } catch (IOException e) {
            throw e;
        }

        InputStream is = (statusCode >= 200 && statusCode < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
        }

        return new HttpResult(statusCode, sb.toString().trim());
    }

    // =========================================================================
    // AUTHENTICATION METHODS (Sign In, Sign Up, Ping)
    // =========================================================================

    public AuthResult signIn(String email, String password) {
        if (!SupabaseConfig.isConfigured()) {
            return AuthResult.failure("Supabase credentials not configured in 'supabase.properties'.");
        }

        String url = cleanUrl(SupabaseConfig.getSupabaseUrl()) + "/auth/v1/token?grant_type=password";
        String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", escapeJson(email), escapeJson(password));

        try {
            HttpResult res = sendHttpRequest(url, "POST", payload, null, null);
            if (res.statusCode == 200) {
                String token = extractJsonField(res.body, "access_token");
                String userId = extractJsonField(res.body, "id");
                String userEmail = extractJsonField(res.body, "email");
                String username = extractJsonField(res.body, "username");
                if (username == null || username.isBlank()) {
                    username = userEmail != null ? userEmail.split("@")[0] : "Player";
                }
                return AuthResult.success("Login successful! Welcome back, " + username + "!", userId, userEmail, username, token);
            } else {
                String errorDesc = extractJsonField(res.body, "error_description");
                if (errorDesc == null || errorDesc.isBlank()) errorDesc = extractJsonField(res.body, "msg");
                if (errorDesc == null || errorDesc.isBlank()) errorDesc = extractJsonField(res.body, "message");
                if (errorDesc == null || errorDesc.isBlank()) errorDesc = "Authentication failed (HTTP " + res.statusCode + ")";

                if (errorDesc.toLowerCase().contains("invalid login credentials")) {
                    errorDesc = "Invalid email or password. Please verify and try again.";
                } else if (errorDesc.toLowerCase().contains("email not confirmed")) {
                    errorDesc = "Email is not confirmed! Check your inbox or disable email confirmation in Supabase Auth settings.";
                }
                return AuthResult.failure(errorDesc);
            }
        } catch (Exception e) {
            return AuthResult.failure(formatNetworkException(e));
        }
    }

    public AuthResult signUp(String email, String password, String username) {
        if (!SupabaseConfig.isConfigured()) {
            return AuthResult.failure("Supabase credentials not configured in 'supabase.properties'.");
        }

        String url = cleanUrl(SupabaseConfig.getSupabaseUrl()) + "/auth/v1/signup";
        String safeUsername = (username == null || username.isBlank()) ? email.split("@")[0] : username.trim();
        String payload = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"data\":{\"username\":\"%s\"}}",
                escapeJson(email), escapeJson(password), escapeJson(safeUsername)
        );

        try {
            HttpResult res = sendHttpRequest(url, "POST", payload, null, null);
            if (res.statusCode == 200 || res.statusCode == 201) {
                String userId = extractJsonField(res.body, "id");
                String userEmail = extractJsonField(res.body, "email");
                String token = extractJsonField(res.body, "access_token");
                return AuthResult.success("Account registered for " + safeUsername + "!", userId, userEmail, safeUsername, token);
            } else {
                String errorDesc = extractJsonField(res.body, "error_description");
                if (errorDesc == null || errorDesc.isBlank()) errorDesc = extractJsonField(res.body, "msg");
                if (errorDesc == null || errorDesc.isBlank()) errorDesc = extractJsonField(res.body, "message");
                if (errorDesc == null || errorDesc.isBlank()) errorDesc = "Sign-up failed (HTTP " + res.statusCode + ")";

                if (errorDesc.toLowerCase().contains("already registered") || errorDesc.toLowerCase().contains("user already exists")) {
                    errorDesc = "An account with this email already exists! Please log in.";
                }
                return AuthResult.failure(errorDesc);
            }
        } catch (Exception e) {
            return AuthResult.failure(formatNetworkException(e));
        }
    }

    public AuthResult testConnection() {
        if (!SupabaseConfig.isConfigured()) {
            return AuthResult.failure("Supabase credentials not set.");
        }
        String url = cleanUrl(SupabaseConfig.getSupabaseUrl()) + "/auth/v1/health";
        try {
            HttpResult res = sendHttpRequest(url, "GET", null, null, null);
            if (res.statusCode == 200 || res.statusCode == 204) {
                return AuthResult.success("Connection to Supabase cloud is active and healthy!", null, null, null, null);
            } else {
                return AuthResult.failure("Supabase responded with HTTP " + res.statusCode);
            }
        } catch (Exception e) {
            return AuthResult.failure(formatNetworkException(e));
        }
    }

    // =========================================================================
    // PROFILES & STANDINGS METHODS (Single Players Table)
    // =========================================================================

    /**
     * Fetches a single user profile by UUID or username.
     */
    public UserProfile fetchUserProfile(String identifier) {
        if (!SupabaseConfig.isConfigured() || identifier == null || identifier.isBlank()) return null;

        String cleanBase = cleanUrl(SupabaseConfig.getSupabaseUrl());
        String safeId = identifier.trim();
        boolean isUuid = safeId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        String filter = isUuid ? ("id=eq." + safeId) : ("username=ilike." + java.net.URLEncoder.encode(safeId, StandardCharsets.UTF_8));
        String url = cleanBase + "/rest/v1/profiles?" + filter + "&select=*";

        try {
            HttpResult res = sendHttpRequest(url, "GET", null, null, null);
            if (res.statusCode == 200 && res.body.startsWith("[") && res.body.contains("{")) {
                List<String> objs = extractJsonArrayObjects(res.body);
                if (!objs.isEmpty()) {
                    return parseUserProfileJson(objs.get(0));
                }
            }
        } catch (Exception e) {
            System.err.println("[SupabaseAuthService] fetchUserProfile error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches all registered players from Supabase 'public.profiles' table,
     * ordered by wins descending (Player Standings).
     */
    public List<UserProfile> fetchAllProfiles() {
        List<UserProfile> list = new ArrayList<>();
        if (!SupabaseConfig.isConfigured()) return list;

        String url = cleanUrl(SupabaseConfig.getSupabaseUrl()) + "/rest/v1/profiles?select=*&order=wins.desc,matches.desc";
        try {
            HttpResult res = sendHttpRequest(url, "GET", null, null, null);
            if (res.statusCode == 200 && res.body.startsWith("[")) {
                List<String> objs = extractJsonArrayObjects(res.body);
                for (String obj : objs) {
                    UserProfile p = parseUserProfileJson(obj);
                    if (p != null) {
                        list.add(p);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SupabaseAuthService] fetchAllProfiles error: " + e.getMessage());
        }
        return list;
    }

    private UserProfile parseUserProfileJson(String json) {
        if (json == null || json.isBlank()) return null;
        String id = extractJsonField(json, "id");
        String username = extractJsonField(json, "username");
        String email = extractJsonField(json, "email");
        int wins = parseIntSafe(extractJsonField(json, "wins"), 0);
        int losses = parseIntSafe(extractJsonField(json, "losses"), 0);
        int matches = parseIntSafe(extractJsonField(json, "matches"), 0);
        double dmg = parseDoubleSafe(extractJsonField(json, "highest_damage"), 0.0);
        String fav = extractJsonField(json, "favorite_fighter");
        return new UserProfile(id, username, email, wins, losses, matches, dmg, fav);
    }

    /**
     * Upserts or updates a player's profile in Supabase 'public.profiles'.
     */
    public boolean upsertProfile(String userId, String username, int wins, int losses, int matches, double highestDamage, String favoriteFighter, String token) {
        if (!SupabaseConfig.isConfigured() || username == null || username.isBlank()) return false;

        String cleanBase = cleanUrl(SupabaseConfig.getSupabaseUrl());
        String postUrl = cleanBase + "/rest/v1/profiles";
        String email = username.contains("@") ? username : (username.toLowerCase() + "@clashofbytes.local");

        String payload;
        if (userId != null && userId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            payload = String.format(Locale.US,
                    "{\"id\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"wins\":%d,\"losses\":%d,\"matches\":%d,\"highest_damage\":%.2f,\"favorite_fighter\":\"%s\",\"updated_at\":\"%s\"}",
                    escapeJson(userId), escapeJson(username), escapeJson(email),
                    wins, losses, matches, highestDamage, escapeJson(favoriteFighter != null ? favoriteFighter : "Ironclad Warrior"),
                    java.time.Instant.now().toString());
        } else {
            payload = String.format(Locale.US,
                    "{\"username\":\"%s\",\"email\":\"%s\",\"wins\":%d,\"losses\":%d,\"matches\":%d,\"highest_damage\":%.2f,\"favorite_fighter\":\"%s\",\"updated_at\":\"%s\"}",
                    escapeJson(username), escapeJson(email),
                    wins, losses, matches, highestDamage, escapeJson(favoriteFighter != null ? favoriteFighter : "Ironclad Warrior"),
                    java.time.Instant.now().toString());
        }

        try {
            HttpResult res = sendHttpRequest(postUrl, "POST", payload, token, "resolution=merge-duplicates");
            return res.statusCode == 200 || res.statusCode == 201 || res.statusCode == 204;
        } catch (Exception e) {
            System.err.println("[SupabaseAuthService] upsertProfile error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // DUAL PLAYER MATCH HISTORY RECORDING (User Requirement: 2 Rows per match)
    // =========================================================================

    /**
     * Atomically records a local 2-player match outcome:
     * 1. Inserts TWO rows into 'public.match_history':
     *    - Row 1: Player 1 name, P1 character, P2 character, result (WIN/LOSS), rounds
     *    - Row 2: Player 2 name, P2 character, P1 character, result (LOSS/WIN), rounds
     * 2. Updates 'public.profiles' for Player 1 (wins/losses/matches)
     * 3. Updates 'public.profiles' for Player 2 (wins/losses/matches)
     */
    public boolean recordDualMatchHistory(String p1Name, String p1Id, String p1Fighter, int p1Rounds, double dmgP1,
                                          String p2Name, String p2Id, String p2Fighter, int p2Rounds, double dmgP2,
                                          String winnerName, boolean p1Won, boolean isDraw,
                                          String token) {
        if (!SupabaseConfig.isConfigured()) return false;

        String safeP1Name = (p1Name != null && !p1Name.isBlank()) ? p1Name.trim() : "Player 1";
        String safeP2Name = (p2Name != null && !p2Name.isBlank()) ? p2Name.trim() : "Player 2";
        String safeP1Fighter = (p1Fighter != null && !p1Fighter.isBlank()) ? p1Fighter.trim() : "Kenji";
        String safeP2Fighter = (p2Fighter != null && !p2Fighter.isBlank()) ? p2Fighter.trim() : "Elena";

        String p1Result = isDraw ? "DRAW" : (p1Won ? "WIN" : "LOSS");
        String p2Result = isDraw ? "DRAW" : (p1Won ? "LOSS" : "WIN");

        String displayWinner = (winnerName != null && !winnerName.isBlank()) ? winnerName : (p1Won ? safeP1Name : safeP2Name);
        String scoreStr = p1Rounds + " - " + p2Rounds;
        String outcomeStr = isDraw ? "DRAW" : (p1Won ? (safeP1Name + " WON") : (safeP2Name + " WON"));

        String cleanBase = cleanUrl(SupabaseConfig.getSupabaseUrl());
        String historyUrl = cleanBase + "/rest/v1/match_history";

        // Build match slot row matching game table exactly:
        // Slot # | Match Date & Time | Player 1 | P1 Fighter | Player 2 | P2 Fighter | Score / Rounds | Winner | Outcome
        StringBuilder slotPayload = new StringBuilder("[{");
        if (p1Id != null && p1Id.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            slotPayload.append("\"player_id\":\"").append(escapeJson(p1Id)).append("\",");
        }
        slotPayload.append("\"player_1\":\"").append(escapeJson(safeP1Name)).append("\",")
                   .append("\"p1_fighter\":\"").append(escapeJson(safeP1Fighter)).append("\",")
                   .append("\"player_2\":\"").append(escapeJson(safeP2Name)).append("\",")
                   .append("\"p2_fighter\":\"").append(escapeJson(safeP2Fighter)).append("\",")
                   .append("\"score\":\"").append(escapeJson(scoreStr)).append("\",")
                   .append("\"winner\":\"").append(escapeJson(displayWinner)).append("\",")
                   .append("\"outcome\":\"").append(escapeJson(outcomeStr)).append("\",")
                   .append("\"player_name\":\"").append(escapeJson(safeP1Name)).append("\",")
                   .append("\"result\":\"").append(p1Result).append("\",")
                   .append("\"rounds_p1\":").append(p1Rounds).append(",")
                   .append("\"rounds_p2\":").append(p2Rounds)
                   .append("}]");

        boolean historySuccess = false;
        try {
            HttpResult res = sendHttpRequest(historyUrl, "POST", slotPayload.toString(), token, "return=representation");
            historySuccess = (res.statusCode == 200 || res.statusCode == 201 || res.statusCode == 204);
            if (!historySuccess) {
                // If columns player_1 etc don't exist yet in Supabase schema cache, fall back to compatible columns
                StringBuilder fallbackArray = new StringBuilder("[{");
                if (p1Id != null && p1Id.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                    fallbackArray.append("\"player_id\":\"").append(escapeJson(p1Id)).append("\",");
                }
                fallbackArray.append("\"player_name\":\"").append(escapeJson(safeP1Name)).append("\",")
                             .append("\"p1_fighter\":\"").append(escapeJson(safeP1Fighter)).append("\",")
                             .append("\"p2_fighter\":\"").append(escapeJson(safeP2Fighter)).append("\",")
                             .append("\"winner\":\"").append(escapeJson(displayWinner)).append("\",")
                             .append("\"result\":\"").append(p1Result).append("\",")
                             .append("\"rounds_p1\":").append(p1Rounds).append(",")
                             .append("\"rounds_p2\":").append(p2Rounds)
                             .append("}]");
                HttpResult fbRes = sendHttpRequest(historyUrl, "POST", fallbackArray.toString(), token, "return=representation");
                historySuccess = (fbRes.statusCode == 200 || fbRes.statusCode == 201 || fbRes.statusCode == 204);
            }
        } catch (Exception e) {
            System.err.println("[SupabaseAuthService] Failed to insert match slot: " + e.getMessage());
        }

        // Now update profiles for BOTH Player 1 and Player 2
        updateSinglePlayerStats(safeP1Name, p1Id, p1Won, !p1Won && !isDraw, safeP1Fighter, dmgP1, token);
        updateSinglePlayerStats(safeP2Name, p2Id, !p1Won && !isDraw, p1Won, safeP2Fighter, dmgP2, token);

        return historySuccess;
    }

    private void updateSinglePlayerStats(String username, String userId, boolean isWin, boolean isLoss, String fighter, double damage, String token) {
        try {
            UserProfile prof = fetchUserProfile(userId != null ? userId : username);
            int wins = (prof != null ? prof.getWins() : 0) + (isWin ? 1 : 0);
            int losses = (prof != null ? prof.getLosses() : 0) + (isLoss ? 1 : 0);
            int matches = Math.max((prof != null ? prof.getMatches() : 0) + 1, wins + losses);
            double peakDmg = Math.max(prof != null ? prof.getHighestDamage() : 0.0, damage);
            String fav = (fighter != null && !fighter.isBlank()) ? fighter : (prof != null ? prof.getFavoriteFighter() : "Ironclad Warrior");

            String effectiveId = (prof != null && prof.getId() != null) ? prof.getId() : userId;
            if (effectiveId != null && effectiveId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                upsertProfile(effectiveId, prof != null ? prof.getUsername() : username, wins, losses, matches, peakDmg, fav, token);
            }
        } catch (Exception e) {
            System.err.println("[SupabaseAuthService] Error updating stats for " + username + ": " + e.getMessage());
        }
    }

    /**
     * Fetches recent match history from Supabase 'public.match_history' table.
     */
    public List<SupabaseMatchRecord> fetchAllMatchHistory(int limit) {
        List<SupabaseMatchRecord> list = new ArrayList<>();
        if (!SupabaseConfig.isConfigured()) return list;

        int safeLimit = limit <= 0 ? 50 : limit;
        String url = cleanUrl(SupabaseConfig.getSupabaseUrl()) + "/rest/v1/match_history?select=*&order=played_at.desc&limit=" + safeLimit;

        try {
            HttpResult res = sendHttpRequest(url, "GET", null, null, null);
            if (res.statusCode == 200 && res.body.startsWith("[")) {
                List<String> objs = extractJsonArrayObjects(res.body);
                for (String obj : objs) {
                    String mId = extractJsonField(obj, "match_id");
                    String pId = extractJsonField(obj, "player_id");
                    String pName = extractJsonField(obj, "player_name");
                    String p1F = extractJsonField(obj, "p1_fighter");
                    String p2F = extractJsonField(obj, "p2_fighter");
                    String win = extractJsonField(obj, "winner");
                    String resStr = extractJsonField(obj, "result");
                    int r1 = parseIntSafe(extractJsonField(obj, "rounds_p1"), 0);
                    int r2 = parseIntSafe(extractJsonField(obj, "rounds_p2"), 0);
                    String playedAt = extractJsonField(obj, "played_at");

                    String p1Col = extractJsonField(obj, "player_1");
                    String p2Col = extractJsonField(obj, "player_2");
                    String scCol = extractJsonField(obj, "score");
                    String outCol = extractJsonField(obj, "outcome");

                    list.add(new SupabaseMatchRecord(mId, pId, pName, p1F, p2F, win, resStr, r1, r2, playedAt, p1Col, p2Col, scCol, outCol));
                }
            }
        } catch (Exception e) {
            System.err.println("[SupabaseAuthService] fetchAllMatchHistory error: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // BACKWARD COMPATIBILITY METHOD
    // =========================================================================

    public boolean updateUserMatchStats(String identifier, boolean isWin, boolean isLoss,
                                       String p1Fighter, String p2Fighter,
                                       int roundsP1, int roundsP2,
                                       double matchDamage, String token) {
        return recordDualMatchHistory(
                identifier, null, p1Fighter, roundsP1, matchDamage,
                "Opponent", null, p2Fighter, roundsP2, 0.0,
                isWin ? identifier : "Opponent", isWin, !isWin && !isLoss,
                token
        );
    }

    // =========================================================================
    // UTILITY METHODS (JSON String Escaping, URL Cleaning & Regex/Brace Extraction)
    // =========================================================================

    private String cleanUrl(String rawUrl) {
        if (rawUrl == null) return "";
        String trimmed = rawUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.endsWith("/rest/v1")) trimmed = trimmed.substring(0, trimmed.length() - "/rest/v1".length());
        if (trimmed.endsWith("/auth/v1")) trimmed = trimmed.substring(0, trimmed.length() - "/auth/v1".length());
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String formatNetworkException(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank() || "null".equalsIgnoreCase(msg.trim())) {
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                msg = e.getCause().getMessage();
            } else {
                msg = e.getClass().getSimpleName();
            }
        }
        String lower = msg.toLowerCase();
        if (lower.contains("unresolved") || lower.contains("unknownhost") || lower.contains("connect") || lower.contains("timeout") || lower.contains("socketexception")) {
            return "Supabase host unreachable: Cloud project URL cannot be reached. You can play in Offline / Demo Mode or check internet connection.";
        }
        return "Network error connecting to Supabase: " + msg;
    }

    public static List<String> extractJsonArrayObjects(String json) {
        List<String> list = new ArrayList<>();
        if (json == null) return list;
        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    list.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return list;
    }

    private String extractJsonField(String json, String fieldName) {
        if (json == null || fieldName == null) return null;
        Pattern stringPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher stringMatcher = stringPattern.matcher(json);
        if (stringMatcher.find()) {
            return stringMatcher.group(1);
        }
        Pattern primPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*([^,\\}\\]\\s]+)");
        Matcher primMatcher = primPattern.matcher(json);
        if (primMatcher.find()) {
            String val = primMatcher.group(1).trim();
            if (!val.equalsIgnoreCase("null")) {
                return val;
            }
        }
        return null;
    }

    private int parseIntSafe(String val, int def) {
        if (val == null) return def;
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    private double parseDoubleSafe(String val, double def) {
        if (val == null) return def;
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }

    public String checkDatabaseTablesStatus() {
        if (!SupabaseConfig.isConfigured()) return "Supabase credentials not configured.";
        String url = cleanUrl(SupabaseConfig.getSupabaseUrl()) + "/rest/v1/profiles?select=id&limit=1";
        try {
            HttpResult res = sendHttpRequest(url, "GET", null, null, null);
            if (res.statusCode == 200) {
                return "ACTIVE (Table 'public.profiles' exists & accessible)";
            } else if (res.statusCode == 404 || res.body.contains("PGRST205")) {
                return "PENDING (Run 'supabase_schema.sql' in Supabase SQL Editor)";
            } else {
                return "HTTP " + res.statusCode + ": " + res.body;
            }
        } catch (Exception e) {
            return "Error checking tables: " + e.getMessage();
        }
    }
}

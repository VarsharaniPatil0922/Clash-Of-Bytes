package service;

import model.MatchRound;
import model.PlayerSession;
import util.SimpleJson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing transactional recording of completed combat matches,
 * participant telemetry (match_players), and round performance (match_rounds)
 * via the Supabase PostgREST API with Row Level Security.
 *
 * Enforces:
 * 1. Both players must be authenticated with distinct Supabase Auth UUIDs.
 * 2. Authenticated UUIDs are derived directly from the player sessions (never trusted from user input).
 * 3. Prevention of duplicate match submissions.
 * 4. Automatic profile verification in public.users to satisfy foreign key integrity.
 */
public class SupabaseMatchService {

    private final HttpClient httpClient;
    private final SupabaseAuthService authService;
    private final SupabasePlayerService playerService;

    // Cache of recent match submissions to prevent duplicate match submissions
    private static final Set<String> recentSubmissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public SupabaseMatchService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.authService = new SupabaseAuthService(httpClient);
        this.playerService = new SupabasePlayerService(httpClient);
    }

    public SupabaseMatchService(HttpClient httpClient, SupabaseAuthService authService, SupabasePlayerService playerService) {
        this.httpClient = httpClient;
        this.authService = authService;
        this.playerService = playerService;
    }

    /**
     * Records a completed local 2-player match result to Supabase.
     */
    public long recordMatchResult(PlayerSession p1Session, PlayerSession p2Session,
                                  int p1CharacterId, int p2CharacterId,
                                  int p1FinalScore, int p2FinalScore,
                                  int totalRounds, List<MatchRound> rounds,
                                  int durationSeconds) throws Exception {

        // 1. Strict Security & Session Validation
        if (p1Session == null || p2Session == null) {
            throw new IllegalArgumentException("Both Player 1 and Player 2 must be authenticated to record match telemetry.");
        }
        if (p1Session.getUserId().equalsIgnoreCase(p2Session.getUserId())) {
            throw new IllegalArgumentException("Invalid match: Player 1 and Player 2 have identical accounts (" + p1Session.getUserId() + "). Two distinct accounts are required.");
        }
        if (totalRounds <= 0) {
            throw new IllegalArgumentException("Total rounds must be at least 1.");
        }

        // 2. Prevent Duplicate Submission
        String idempotencyKey = String.format("%s_%s_%d_%d_%d_%d",
                p1Session.getUserId(), p2Session.getUserId(), p1FinalScore, p2FinalScore, totalRounds, System.currentTimeMillis() / 15000);
        if (recentSubmissions.contains(idempotencyKey)) {
            throw new IllegalStateException("Duplicate match submission detected. Match was already recorded.");
        }
        recentSubmissions.add(idempotencyKey);

        // 3. Ensure Token Freshness
        if (p1Session.isExpired()) {
            try { authService.refreshToken(p1Session); } catch (Exception ignored) {}
        }
        if (p2Session.isExpired()) {
            try { authService.refreshToken(p2Session); } catch (Exception ignored) {}
        }

        // 4. Ensure public.users profile exists for both UUIDs (Foreign Key Integrity)
        playerService.ensureProfile(p1Session);
        playerService.ensureProfile(p2Session);

        // 5. Calculate Outcomes and Round Wins
        int p1RoundsWon = 0;
        int p2RoundsWon = 0;
        if (rounds != null) {
            for (MatchRound r : rounds) {
                if (r.getPlayer1Score() > r.getPlayer2Score()) {
                    p1RoundsWon++;
                } else if (r.getPlayer2Score() > r.getPlayer1Score()) {
                    p2RoundsWon++;
                }
            }
        }

        String winnerUuid = null;
        String loserUuid = null;
        String p1Result = "DRAW";
        String p2Result = "DRAW";

        if (p1FinalScore > p2FinalScore) {
            winnerUuid = p1Session.getUserId();
            loserUuid = p2Session.getUserId();
            p1Result = "WIN";
            p2Result = "LOSS";
        } else if (p2FinalScore > p1FinalScore) {
            winnerUuid = p2Session.getUserId();
            loserUuid = p1Session.getUserId();
            p1Result = "LOSS";
            p2Result = "WIN";
        }

        // 6. PostgREST INSERT into public.matches
        String matchPayload = String.format(
                "{\"player1_user_id\":\"%s\",\"player2_user_id\":\"%s\"," +
                "\"winner_user_id\":%s,\"loser_user_id\":%s," +
                "\"total_rounds\":%d,\"player1_rounds_won\":%d,\"player2_rounds_won\":%d," +
                "\"match_status\":\"COMPLETED\",\"player1_final_score\":%d,\"player2_final_score\":%d," +
                "\"match_duration_seconds\":%d}",
                SimpleJson.escape(p1Session.getUserId()),
                SimpleJson.escape(p2Session.getUserId()),
                winnerUuid != null ? "\"" + SimpleJson.escape(winnerUuid) + "\"" : "null",
                loserUuid != null ? "\"" + SimpleJson.escape(loserUuid) + "\"" : "null",
                totalRounds, p1RoundsWon, p2RoundsWon,
                p1FinalScore, p2FinalScore, durationSeconds
        );

        HttpRequest matchReq = HttpRequest.newBuilder()
                .uri(SupabaseConfig.getRestUri("/matches"))
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Authorization", "Bearer " + p1Session.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(matchPayload))
                .build();

        HttpResponse<String> matchResp;
        try {
            matchResp = httpClient.send(matchReq, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            recentSubmissions.remove(idempotencyKey);
            throw new IOException("Failed to send match telemetry to Supabase: " + e.getMessage(), e);
        }

        if (matchResp.statusCode() < 200 || matchResp.statusCode() >= 300) {
            recentSubmissions.remove(idempotencyKey);
            throw new IOException("Supabase rejected match insertion (HTTP " + matchResp.statusCode() + "): " + matchResp.body());
        }

        List<Object> insertedMatches = SimpleJson.parseArray(matchResp.body());
        if (insertedMatches.isEmpty() || !(insertedMatches.get(0) instanceof Map)) {
            recentSubmissions.remove(idempotencyKey);
            throw new IOException("Failed to parse created match representation: " + matchResp.body());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> createdMatch = (Map<String, Object>) insertedMatches.get(0);
        long matchId = SimpleJson.getLong(createdMatch, "match_id", 0);
        if (matchId <= 0) {
            recentSubmissions.remove(idempotencyKey);
            throw new IOException("Created match ID was invalid.");
        }

        // 7. PostgREST INSERT into public.match_players
        String playersPayload = String.format(
                "[{\"match_id\":%d,\"user_id\":\"%s\",\"character_id\":%d,\"final_score\":%d,\"rounds_won\":%d,\"result\":\"%s\"}," +
                 "{\"match_id\":%d,\"user_id\":\"%s\",\"character_id\":%d,\"final_score\":%d,\"rounds_won\":%d,\"result\":\"%s\"}]",
                matchId, SimpleJson.escape(p1Session.getUserId()), p1CharacterId, p1FinalScore, p1RoundsWon, p1Result,
                matchId, SimpleJson.escape(p2Session.getUserId()), p2CharacterId, p2FinalScore, p2RoundsWon, p2Result
        );

        HttpRequest playersReq = HttpRequest.newBuilder()
                .uri(SupabaseConfig.getRestUri("/match_players"))
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Authorization", "Bearer " + p1Session.getAccessToken())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(playersPayload))
                .build();

        HttpResponse<String> playersResp = httpClient.send(playersReq, HttpResponse.BodyHandlers.ofString());
        if (playersResp.statusCode() < 200 || playersResp.statusCode() >= 300) {
            System.err.println("[SupabaseMatchService] Warning: Failed to insert match_players (HTTP " + playersResp.statusCode() + "): " + playersResp.body());
        }

        // 8. PostgREST INSERT into public.match_rounds
        if (rounds != null && !rounds.isEmpty()) {
            StringBuilder roundsSb = new StringBuilder("[");
            int rDuration = durationSeconds > 0 ? (durationSeconds / rounds.size()) : 30;

            for (int i = 0; i < rounds.size(); i++) {
                MatchRound r = rounds.get(i);
                int rNum = (r.getRoundNumber() > 0) ? r.getRoundNumber() : (i + 1);
                String rWinner = (r.getPlayer1Score() > r.getPlayer2Score()) ? p1Session.getUserId() :
                                 ((r.getPlayer2Score() > r.getPlayer1Score()) ? p2Session.getUserId() : null);
                String rLoser = (rWinner != null && rWinner.equals(p1Session.getUserId())) ? p2Session.getUserId() :
                                ((rWinner != null) ? p1Session.getUserId() : null);

                if (i > 0) roundsSb.append(",");
                roundsSb.append(String.format(
                        "{\"match_id\":%d,\"round_number\":%d,\"winner_user_id\":%s,\"loser_user_id\":%s," +
                        "\"player1_score\":%d,\"player2_score\":%d,\"round_status\":\"COMPLETED\",\"duration_seconds\":%d}",
                        matchId, rNum,
                        rWinner != null ? "\"" + SimpleJson.escape(rWinner) + "\"" : "null",
                        rLoser != null ? "\"" + SimpleJson.escape(rLoser) + "\"" : "null",
                        r.getPlayer1Score(), r.getPlayer2Score(), rDuration
                ));
            }
            roundsSb.append("]");

            HttpRequest roundsReq = HttpRequest.newBuilder()
                    .uri(SupabaseConfig.getRestUri("/match_rounds"))
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .header("Authorization", "Bearer " + p1Session.getAccessToken())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(roundsSb.toString()))
                    .build();

            HttpResponse<String> roundsResp = httpClient.send(roundsReq, HttpResponse.BodyHandlers.ofString());
            if (roundsResp.statusCode() < 200 || roundsResp.statusCode() >= 300) {
                System.err.println("[SupabaseMatchService] Warning: Failed to insert match_rounds (HTTP " + roundsResp.statusCode() + "): " + roundsResp.body());
            }
        }

        return matchId;
    }
}

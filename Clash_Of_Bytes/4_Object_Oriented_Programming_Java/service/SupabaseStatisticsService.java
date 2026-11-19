package service;

import model.PlayerSession;
import model.PlayerStats;
import util.SimpleJson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Service managing player career statistics and match history retrieval
 * using Supabase PostgREST APIs governed by Row Level Security.
 */
public class SupabaseStatisticsService {

    private final HttpClient httpClient;

    public static class SupabaseMatchHistoryItem {
        public final long matchId;
        public final String matchDate;
        public final String opponentIdentifier;
        public final String result; // WIN, LOSS, DRAW
        public final int playerScore;
        public final int opponentScore;
        public final int totalRounds;
        public final int durationSeconds;

        public SupabaseMatchHistoryItem(long matchId, String matchDate, String opponentIdentifier,
                                       String result, int playerScore, int opponentScore,
                                       int totalRounds, int durationSeconds) {
            this.matchId = matchId;
            this.matchDate = matchDate;
            this.opponentIdentifier = opponentIdentifier;
            this.result = result;
            this.playerScore = playerScore;
            this.opponentScore = opponentScore;
            this.totalRounds = totalRounds;
            this.durationSeconds = durationSeconds;
        }
    }

    public SupabaseStatisticsService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public SupabaseStatisticsService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Retrieves aggregated career statistics for an authenticated player session.
     */
    public PlayerStats getPlayerStats(PlayerSession session) throws Exception {
        if (session == null || session.getUserId() == null) {
            throw new IllegalArgumentException("Cannot fetch statistics: null session.");
        }

        List<SupabaseMatchHistoryItem> history = getMatchHistory(session);

        int totalMatches = history.size();
        int totalWins = 0;
        int totalLosses = 0;
        int totalDraws = 0;
        int totalScore = 0;

        for (SupabaseMatchHistoryItem item : history) {
            totalScore += item.playerScore;
            if ("WIN".equalsIgnoreCase(item.result)) {
                totalWins++;
            } else if ("LOSS".equalsIgnoreCase(item.result)) {
                totalLosses++;
            } else {
                totalDraws++;
            }
        }

        String favChar = getFavoriteCharacter(session);

        return new PlayerStats(
                0, // legacy int id
                session.getUsername(),
                totalMatches,
                totalWins,
                totalLosses,
                totalDraws,
                totalScore,
                favChar
        );
    }

    /**
     * Retrieves recent match history telemetry where the player was a participant.
     */
    public List<SupabaseMatchHistoryItem> getMatchHistory(PlayerSession session) throws Exception {
        List<SupabaseMatchHistoryItem> items = new ArrayList<>();
        if (session == null || session.getUserId() == null) return items;

        String uid = session.getUserId();
        String query = String.format("/matches?or=(player1_user_id.eq.%s,player2_user_id.eq.%s)&order=match_date.desc&limit=50", uid, uid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(SupabaseConfig.getRestUri(query))
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Authorization", "Bearer " + session.getAccessToken())
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IOException("Failed to load match history: " + e.getMessage(), e);
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            List<Object> list = SimpleJson.parseArray(response.body());
            for (Object obj : list) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;

                    long matchId = SimpleJson.getLong(map, "match_id", 0);
                    String date = SimpleJson.getString(map, "match_date", "N/A");
                    String p1Id = SimpleJson.getString(map, "player1_user_id", "");
                    String p2Id = SimpleJson.getString(map, "player2_user_id", "");
                    String winnerId = SimpleJson.getString(map, "winner_user_id", null);

                    int p1Score = SimpleJson.getInt(map, "player1_final_score", 0);
                    int p2Score = SimpleJson.getInt(map, "player2_final_score", 0);
                    int rounds = SimpleJson.getInt(map, "total_rounds", 3);
                    int duration = SimpleJson.getInt(map, "match_duration_seconds", 0);

                    boolean isP1 = uid.equalsIgnoreCase(p1Id);
                    int myScore = isP1 ? p1Score : p2Score;
                    int oppScore = isP1 ? p2Score : p1Score;
                    String oppUuid = isP1 ? p2Id : p1Id;

                    // Resolve opponent display name
                    String oppName = resolveOpponentName(oppUuid);

                    String result = "DRAW";
                    if (winnerId != null && !winnerId.isEmpty()) {
                        result = uid.equalsIgnoreCase(winnerId) ? "WIN" : "LOSS";
                    }

                    items.add(new SupabaseMatchHistoryItem(
                            matchId, date, oppName, result, myScore, oppScore, rounds, duration
                    ));
                }
            }
        }

        return items;
    }

    private String getFavoriteCharacter(PlayerSession session) {
        try {
            String query = String.format("/match_players?user_id=eq.%s&select=character_id", session.getUserId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(SupabaseConfig.getRestUri(query))
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .header("Authorization", "Bearer " + session.getAccessToken())
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                List<Object> list = SimpleJson.parseArray(response.body());
                Map<Integer, Integer> counts = new HashMap<>();
                for (Object item : list) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) item;
                        int cId = SimpleJson.getInt(map, "character_id", 0);
                        counts.put(cId, counts.getOrDefault(cId, 0) + 1);
                    }
                }

                int maxCount = 0;
                int bestCharId = 1;
                for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        bestCharId = entry.getKey();
                    }
                }

                switch (bestCharId) {
                    case 1: return "Ironclad Warrior";
                    case 2: return "Blaze Berserker";
                    case 3: return "Shadow Ninja";
                    case 4: return "Cyber Brawler";
                    case 5: return "Aura Paladin";
                    case 6: return "Titan Gladiator";
                    default: return "Ironclad Warrior";
                }
            }
        } catch (Exception ignored) {}
        return "Ironclad Warrior";
    }

    private String resolveOpponentName(String oppUuid) {
        if (oppUuid == null || oppUuid.isEmpty()) return "Opponent";

        // Check if opponent is the other player in PlayerSessionManager
        PlayerSession p1 = PlayerSessionManager.getInstance().getPlayer1Session();
        PlayerSession p2 = PlayerSessionManager.getInstance().getPlayer2Session();

        if (p1 != null && oppUuid.equalsIgnoreCase(p1.getUserId())) return p1.getUsername();
        if (p2 != null && oppUuid.equalsIgnoreCase(p2.getUserId())) return p2.getUsername();

        return "Player (" + (oppUuid.length() > 8 ? oppUuid.substring(0, 8) : oppUuid) + ")";
    }
}

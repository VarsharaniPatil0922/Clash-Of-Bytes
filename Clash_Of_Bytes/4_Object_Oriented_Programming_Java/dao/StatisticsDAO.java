package dao;

import database.DBConnection;
import model.PlayerStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Player Statistics and Match History Queries.
 */
public class StatisticsDAO {

    /**
     * DTO for representing a full Match History Row in the JTable.
     */
    public static class MatchHistoryRow {
        public int matchId;
        public String matchDate;
        public String opponentUsername;
        public String myCharacterName;
        public String opponentCharacterName;
        public int myScore;
        public int opponentScore;
        public int roundsPlayed;
        public String result;
        public String winnerName;

        public MatchHistoryRow(int matchId, String matchDate, String opponentUsername, String myCharacterName,
                               String opponentCharacterName, int myScore, int opponentScore,
                               int roundsPlayed, String result, String winnerName) {
            this.matchId = matchId;
            this.matchDate = matchDate;
            this.opponentUsername = opponentUsername;
            this.myCharacterName = myCharacterName;
            this.opponentCharacterName = opponentCharacterName;
            this.myScore = myScore;
            this.opponentScore = opponentScore;
            this.roundsPlayed = roundsPlayed;
            this.result = result;
            this.winnerName = winnerName;
        }
    }

    /**
     * Retrieves aggregated statistics for a specific user.
     */
    public PlayerStats getPlayerStats(int userId) throws SQLException {
        Connection conn = DBConnection.getConnection();

        String username = "Unknown";
        String userSql = "SELECT username FROM users WHERE user_id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(userSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        }

        // Aggregate statistics from match_players
        String statsSql = "SELECT "
                + "COUNT(*) AS total_matches, "
                + "SUM(CASE WHEN result = 'WIN' THEN 1 ELSE 0 END) AS wins, "
                + "SUM(CASE WHEN result = 'LOSS' THEN 1 ELSE 0 END) AS losses, "
                + "SUM(CASE WHEN result = 'DRAW' THEN 1 ELSE 0 END) AS draws, "
                + "COALESCE(SUM(final_score), 0) AS total_score "
                + "FROM match_players mp "
                + "JOIN matches m ON mp.match_id = m.match_id "
                + "WHERE mp.user_id = ? AND m.match_status = 'COMPLETED';";

        int totalMatches = 0;
        int totalWins = 0;
        int totalLosses = 0;
        int totalDraws = 0;
        int totalScore = 0;

        try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalMatches = rs.getInt("total_matches");
                    totalWins = rs.getInt("wins");
                    totalLosses = rs.getInt("losses");
                    totalDraws = rs.getInt("draws");
                    totalScore = rs.getInt("total_score");
                }
            }
        }

        // Most frequently chosen fighter character
        String favCharSql = "SELECT c.character_name, COUNT(*) as pick_count "
                + "FROM match_players mp "
                + "JOIN characters c ON mp.character_id = c.character_id "
                + "WHERE mp.user_id = ? "
                + "GROUP BY c.character_name "
                + "ORDER BY pick_count DESC "
                + "LIMIT 1;";

        String favoriteCharacter = "None";
        try (PreparedStatement ps = conn.prepareStatement(favCharSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    favoriteCharacter = rs.getString("character_name") + " (" + rs.getInt("pick_count") + " picks)";
                }
            }
        }

        return new PlayerStats(userId, username, totalMatches, totalWins, totalLosses, totalDraws, totalScore, favoriteCharacter);
    }

    /**
     * Retrieves full match history for JTable display for a given user.
     */
    public List<MatchHistoryRow> getMatchHistoryForUser(int userId) throws SQLException {
        List<MatchHistoryRow> rows = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        String sql = "SELECT "
                + "m.match_id, "
                + "m.match_date, "
                + "m.total_rounds, "
                + "my_mp.final_score AS my_score, "
                + "my_mp.result AS my_result, "
                + "my_char.character_name AS my_char_name, "
                + "opp_user.username AS opp_username, "
                + "opp_mp.final_score AS opp_score, "
                + "opp_char.character_name AS opp_char_name, "
                + "winner_user.username AS winner_name "
                + "FROM matches m "
                + "JOIN match_players my_mp ON m.match_id = my_mp.match_id AND my_mp.user_id = ? "
                + "JOIN characters my_char ON my_mp.character_id = my_char.character_id "
                + "JOIN match_players opp_mp ON m.match_id = opp_mp.match_id AND opp_mp.user_id != ? "
                + "JOIN users opp_user ON opp_mp.user_id = opp_user.user_id "
                + "JOIN characters opp_char ON opp_mp.character_id = opp_char.character_id "
                + "LEFT JOIN users winner_user ON m.winner_user_id = winner_user.user_id "
                + "ORDER BY m.match_date DESC, m.match_id DESC;";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String winner = rs.getString("winner_name");
                    if (winner == null || winner.trim().isEmpty()) {
                        winner = "DRAW";
                    }

                    rows.add(new MatchHistoryRow(
                            rs.getInt("match_id"),
                            rs.getString("match_date"),
                            rs.getString("opp_username"),
                            rs.getString("my_char_name"),
                            rs.getString("opp_char_name"),
                            rs.getInt("my_score"),
                            rs.getInt("opp_score"),
                            rs.getInt("total_rounds"),
                            rs.getString("my_result"),
                            winner
                    ));
                }
            }
        }
        return rows;
    }
}

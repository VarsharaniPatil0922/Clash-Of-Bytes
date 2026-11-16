package dao;

import database.DBConnection;
import model.Match;
import model.MatchPlayer;
import model.MatchRound;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Match Records, Match Players, and Round Telemetry.
 * Enforces strict transactional integrity: commits all tables or rolls back.
 */
public class MatchDAO {

    /**
     * Atomically saves match, both match_players, and round records in one database transaction.
     */
    public boolean saveMatchComplete(Match match, MatchPlayer player1, MatchPlayer player2, List<MatchRound> rounds) throws SQLException {
        if (player1 == null || player2 == null) {
            throw new IllegalArgumentException("A match must contain exactly two player records.");
        }

        Connection conn = DBConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Insert into matches table
            String matchSql = "INSERT INTO matches (player1_user_id, player2_user_id, winner_user_id, "
                            + "total_rounds, match_status, player1_final_score, player2_final_score) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?);";
            int generatedMatchId = -1;
            try (PreparedStatement psMatch = conn.prepareStatement(matchSql, Statement.RETURN_GENERATED_KEYS)) {
                psMatch.setInt(1, match.getPlayer1UserId());
                psMatch.setInt(2, match.getPlayer2UserId());
                if (match.getWinnerUserId() != null) {
                    psMatch.setInt(3, match.getWinnerUserId());
                } else {
                    psMatch.setNull(3, java.sql.Types.INTEGER);
                }
                psMatch.setInt(4, match.getTotalRounds());
                psMatch.setString(5, match.getMatchStatus());
                psMatch.setInt(6, match.getPlayer1FinalScore());
                psMatch.setInt(7, match.getPlayer2FinalScore());

                int affected = psMatch.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Inserting match record failed, no rows affected.");
                }
                try (ResultSet rs = psMatch.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedMatchId = rs.getInt(1);
                        match.setMatchId(generatedMatchId);
                    } else {
                        throw new SQLException("Inserting match record failed, no ID obtained.");
                    }
                }
            }

            // 2. Insert into match_players table (Exactly Two Records)
            String mpSql = "INSERT INTO match_players (match_id, user_id, character_id, final_score, result) "
                         + "VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement psPlayer = conn.prepareStatement(mpSql)) {
                // Player 1
                psPlayer.setInt(1, generatedMatchId);
                psPlayer.setInt(2, player1.getUserId());
                psPlayer.setInt(3, player1.getCharacterId());
                psPlayer.setInt(4, player1.getFinalScore());
                psPlayer.setString(5, player1.getResult());
                psPlayer.executeUpdate();

                // Player 2
                psPlayer.setInt(1, generatedMatchId);
                psPlayer.setInt(2, player2.getUserId());
                psPlayer.setInt(3, player2.getCharacterId());
                psPlayer.setInt(4, player2.getFinalScore());
                psPlayer.setString(5, player2.getResult());
                psPlayer.executeUpdate();
            }

            // 3. Insert individual rounds into match_rounds table
            if (rounds != null && !rounds.isEmpty()) {
                String roundSql = "INSERT INTO match_rounds (match_id, round_number, winner_user_id, player1_score, player2_score, round_status) "
                                + "VALUES (?, ?, ?, ?, ?, ?);";
                try (PreparedStatement psRound = conn.prepareStatement(roundSql)) {
                    for (MatchRound round : rounds) {
                        psRound.setInt(1, generatedMatchId);
                        psRound.setInt(2, round.getRoundNumber());
                        if (round.getWinnerUserId() != null) {
                            psRound.setInt(3, round.getWinnerUserId());
                        } else {
                            psRound.setNull(3, java.sql.Types.INTEGER);
                        }
                        psRound.setInt(4, round.getPlayer1Score());
                        psRound.setInt(5, round.getPlayer2Score());
                        psRound.setString(6, round.getRoundStatus());
                        psRound.executeUpdate();
                    }
                }
            }

            conn.commit(); // Transaction Committed Successfully
            return true;
        } catch (SQLException ex) {
            conn.rollback(); // Rollback on any failure
            System.err.println("[MatchDAO] Transaction rolled back due to error: " + ex.getMessage());
            throw ex;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public Match getMatchById(int matchId) throws SQLException {
        String sql = "SELECT match_id, match_date, player1_user_id, player2_user_id, winner_user_id, "
                   + "total_rounds, match_status, player1_final_score, player2_final_score "
                   + "FROM matches WHERE match_id = ?;";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer winnerId = rs.getObject("winner_user_id") != null ? rs.getInt("winner_user_id") : null;
                    return new Match(
                            rs.getInt("match_id"),
                            rs.getTimestamp("match_date"),
                            rs.getInt("player1_user_id"),
                            rs.getInt("player2_user_id"),
                            winnerId,
                            rs.getInt("total_rounds"),
                            rs.getString("match_status"),
                            rs.getInt("player1_final_score"),
                            rs.getInt("player2_final_score")
                    );
                }
            }
        }
        return null;
    }

    public List<MatchPlayer> getPlayersForMatch(int matchId) throws SQLException {
        List<MatchPlayer> list = new ArrayList<>();
        String sql = "SELECT match_player_id, match_id, user_id, character_id, final_score, result "
                   + "FROM match_players WHERE match_id = ?;";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MatchPlayer(
                            rs.getInt("match_player_id"),
                            rs.getInt("match_id"),
                            rs.getInt("user_id"),
                            rs.getInt("character_id"),
                            rs.getInt("final_score"),
                            rs.getString("result")
                    ));
                }
            }
        }
        return list;
    }

    public List<MatchRound> getRoundsForMatch(int matchId) throws SQLException {
        List<MatchRound> list = new ArrayList<>();
        String sql = "SELECT round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status "
                   + "FROM match_rounds WHERE match_id = ? ORDER BY round_number ASC;";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer winnerId = rs.getObject("winner_user_id") != null ? rs.getInt("winner_user_id") : null;
                    list.add(new MatchRound(
                            rs.getInt("round_id"),
                            rs.getInt("match_id"),
                            rs.getInt("round_number"),
                            winnerId,
                            rs.getInt("player1_score"),
                            rs.getInt("player2_score"),
                            rs.getString("round_status")
                    ));
                }
            }
        }
        return list;
    }
}

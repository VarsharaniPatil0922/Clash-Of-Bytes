package service;

import dao.MatchDAO;
import model.Match;
import model.MatchPlayer;
import model.MatchRound;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing match validation, scoring rules, winner calculation,
 * and transactional persistence.
 */
public class MatchService {

    private final MatchDAO matchDAO;

    public MatchService() {
        this.matchDAO = new MatchDAO();
    }

    public MatchService(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    /**
     * Records a completed match with both players, final scores, and individual rounds.
     */
    public boolean recordMatchResult(int player1UserId, int player2UserId,
                                     int p1CharacterId, int p2CharacterId,
                                     int p1FinalScore, int p2FinalScore,
                                     int totalRounds, List<MatchRound> rounds) throws SQLException {
        if (player1UserId == player2UserId) {
            throw new IllegalArgumentException("A match cannot be recorded between identical player accounts.");
        }
        if (totalRounds <= 0) {
            throw new IllegalArgumentException("A completed match must have played at least one round.");
        }

        // Determine Winner & Individual Player Results
        Integer winnerUserId = null;
        String p1Result = "DRAW";
        String p2Result = "DRAW";

        if (p1FinalScore > p2FinalScore) {
            winnerUserId = player1UserId;
            p1Result = "WIN";
            p2Result = "LOSS";
        } else if (p2FinalScore > p1FinalScore) {
            winnerUserId = player2UserId;
            p1Result = "LOSS";
            p2Result = "WIN";
        }

        Match match = new Match(
                player1UserId,
                player2UserId,
                winnerUserId,
                totalRounds,
                "COMPLETED",
                p1FinalScore,
                p2FinalScore
        );

        MatchPlayer mp1 = new MatchPlayer(0, player1UserId, p1CharacterId, p1FinalScore, p1Result);
        MatchPlayer mp2 = new MatchPlayer(0, player2UserId, p2CharacterId, p2FinalScore, p2Result);

        return matchDAO.saveMatchComplete(match, mp1, mp2, rounds);
    }
}

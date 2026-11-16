package model;

import java.sql.Timestamp;

/**
 * Model class representing a completed combat match.
 */
public class Match {
    private int matchId;
    private Timestamp matchDate;
    private int player1UserId;
    private int player2UserId;
    private Integer winnerUserId; // null for draw
    private int totalRounds;
    private String matchStatus; // COMPLETED, ABANDONED
    private int player1FinalScore;
    private int player2FinalScore;

    public Match() {}

    public Match(int player1UserId, int player2UserId, Integer winnerUserId,
                 int totalRounds, String matchStatus, int player1FinalScore, int player2FinalScore) {
        this.player1UserId = player1UserId;
        this.player2UserId = player2UserId;
        this.winnerUserId = winnerUserId;
        this.totalRounds = totalRounds;
        this.matchStatus = matchStatus;
        this.player1FinalScore = player1FinalScore;
        this.player2FinalScore = player2FinalScore;
    }

    public Match(int matchId, Timestamp matchDate, int player1UserId, int player2UserId, Integer winnerUserId,
                 int totalRounds, String matchStatus, int player1FinalScore, int player2FinalScore) {
        this.matchId = matchId;
        this.matchDate = matchDate;
        this.player1UserId = player1UserId;
        this.player2UserId = player2UserId;
        this.winnerUserId = winnerUserId;
        this.totalRounds = totalRounds;
        this.matchStatus = matchStatus;
        this.player1FinalScore = player1FinalScore;
        this.player2FinalScore = player2FinalScore;
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public Timestamp getMatchDate() { return matchDate; }
    public void setMatchDate(Timestamp matchDate) { this.matchDate = matchDate; }

    public int getPlayer1UserId() { return player1UserId; }
    public void setPlayer1UserId(int player1UserId) { this.player1UserId = player1UserId; }

    public int getPlayer2UserId() { return player2UserId; }
    public void setPlayer2UserId(int player2UserId) { this.player2UserId = player2UserId; }

    public Integer getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Integer winnerUserId) { this.winnerUserId = winnerUserId; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public int getPlayer1FinalScore() { return player1FinalScore; }
    public void setPlayer1FinalScore(int player1FinalScore) { this.player1FinalScore = player1FinalScore; }

    public int getPlayer2FinalScore() { return player2FinalScore; }
    public void setPlayer2FinalScore(int player2FinalScore) { this.player2FinalScore = player2FinalScore; }
}

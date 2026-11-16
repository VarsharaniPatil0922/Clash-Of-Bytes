package model;

/**
 * Model class representing an individual round within a match.
 */
public class MatchRound {
    private int roundId;
    private int matchId;
    private int roundNumber;
    private Integer winnerUserId; // null if round was a draw
    private int player1Score;
    private int player2Score;
    private String roundStatus; // COMPLETED, K_O, TIME_OUT

    public MatchRound() {}

    public MatchRound(int matchId, int roundNumber, Integer winnerUserId, int player1Score, int player2Score, String roundStatus) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.winnerUserId = winnerUserId;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.roundStatus = roundStatus;
    }

    public MatchRound(int roundId, int matchId, int roundNumber, Integer winnerUserId,
                      int player1Score, int player2Score, String roundStatus) {
        this.roundId = roundId;
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.winnerUserId = winnerUserId;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.roundStatus = roundStatus;
    }

    public int getRoundId() { return roundId; }
    public void setRoundId(int roundId) { this.roundId = roundId; }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public Integer getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Integer winnerUserId) { this.winnerUserId = winnerUserId; }

    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }

    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }

    public String getRoundStatus() { return roundStatus; }
    public void setRoundStatus(String roundStatus) { this.roundStatus = roundStatus; }
}

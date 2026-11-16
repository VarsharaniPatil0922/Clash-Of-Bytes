package com.clashofbytes.dto;

import java.util.List;

public class MatchCreateRequest {

    private Long player1UserId;
    private Long player2UserId;
    private String player1Username;
    private String player2Username;
    private Long player1CharacterId;
    private Long player2CharacterId;
    private String player1Character;
    private String player2Character;
    private Integer player1FinalScore;
    private Integer player2FinalScore;
    private Integer winnerPlayerIndex; // 1 or 2
    private Long winnerUserId;
    private String winnerUsername;
    private Integer totalRounds;
    private List<RoundTelemetry> rounds;

    public MatchCreateRequest() {}

    public Long getPlayer1UserId() { return player1UserId; }
    public void setPlayer1UserId(Long player1UserId) { this.player1UserId = player1UserId; }

    public Long getPlayer2UserId() { return player2UserId; }
    public void setPlayer2UserId(Long player2UserId) { this.player2UserId = player2UserId; }

    public String getPlayer1Username() { return player1Username; }
    public void setPlayer1Username(String player1Username) { this.player1Username = player1Username; }

    public String getPlayer2Username() { return player2Username; }
    public void setPlayer2Username(String player2Username) { this.player2Username = player2Username; }

    public Long getPlayer1CharacterId() { return player1CharacterId; }
    public void setPlayer1CharacterId(Long player1CharacterId) { this.player1CharacterId = player1CharacterId; }

    public Long getPlayer2CharacterId() { return player2CharacterId; }
    public void setPlayer2CharacterId(Long player2CharacterId) { this.player2CharacterId = player2CharacterId; }

    public String getPlayer1Character() { return player1Character; }
    public void setPlayer1Character(String player1Character) { this.player1Character = player1Character; }

    public String getPlayer2Character() { return player2Character; }
    public void setPlayer2Character(String player2Character) { this.player2Character = player2Character; }

    public Integer getPlayer1FinalScore() { return player1FinalScore; }
    public void setPlayer1FinalScore(Integer player1FinalScore) { this.player1FinalScore = player1FinalScore; }

    public Integer getPlayer2FinalScore() { return player2FinalScore; }
    public void setPlayer2FinalScore(Integer player2FinalScore) { this.player2FinalScore = player2FinalScore; }

    public Integer getWinnerPlayerIndex() { return winnerPlayerIndex; }
    public void setWinnerPlayerIndex(Integer winnerPlayerIndex) { this.winnerPlayerIndex = winnerPlayerIndex; }

    public Long getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Long winnerUserId) { this.winnerUserId = winnerUserId; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public Integer getTotalRounds() { return totalRounds; }
    public void setTotalRounds(Integer totalRounds) { this.totalRounds = totalRounds; }

    public List<RoundTelemetry> getRounds() { return rounds; }
    public void setRounds(List<RoundTelemetry> rounds) { this.rounds = rounds; }

    public static class RoundTelemetry {
        private Integer roundNumber;
        private Integer winnerPlayerIndex;
        private Integer p1Score;
        private Integer p2Score;

        public RoundTelemetry() {}

        public Integer getRoundNumber() { return roundNumber; }
        public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

        public Integer getWinnerPlayerIndex() { return winnerPlayerIndex; }
        public void setWinnerPlayerIndex(Integer winnerPlayerIndex) { this.winnerPlayerIndex = winnerPlayerIndex; }

        public Integer getP1Score() { return p1Score; }
        public void setP1Score(Integer p1Score) { this.p1Score = p1Score; }

        public Integer getP2Score() { return p2Score; }
        public void setP2Score(Integer p2Score) { this.p2Score = p2Score; }
    }
}

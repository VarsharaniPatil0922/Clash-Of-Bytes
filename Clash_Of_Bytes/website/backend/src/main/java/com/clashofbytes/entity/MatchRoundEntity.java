package com.clashofbytes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "match_rounds")
public class MatchRoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Long roundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "winner_user_id")
    private Long winnerUserId;

    @Column(name = "player1_score", nullable = false)
    private Integer player1Score = 0;

    @Column(name = "player2_score", nullable = false)
    private Integer player2Score = 0;

    @Column(name = "round_status", nullable = false, length = 20)
    private String roundStatus = "COMPLETED";

    public MatchRoundEntity() {}

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public MatchEntity getMatch() { return match; }
    public void setMatch(MatchEntity match) { this.match = match; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public Long getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Long winnerUserId) { this.winnerUserId = winnerUserId; }

    public Integer getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(Integer player1Score) { this.player1Score = player1Score; }

    public Integer getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(Integer player2Score) { this.player2Score = player2Score; }

    public String getRoundStatus() { return roundStatus; }
    public void setRoundStatus(String roundStatus) { this.roundStatus = roundStatus; }
}

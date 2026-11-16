package com.clashofbytes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "match_date")
    private LocalDateTime matchDate = LocalDateTime.now();

    @Column(name = "player1_user_id", nullable = false)
    private Long player1UserId;

    @Column(name = "player2_user_id", nullable = false)
    private Long player2UserId;

    @Column(name = "winner_user_id")
    private Long winnerUserId;

    @Column(name = "total_rounds", nullable = false)
    private Integer totalRounds = 3;

    @Column(name = "match_status", nullable = false, length = 20)
    private String matchStatus = "COMPLETED";

    @Column(name = "player1_final_score", nullable = false)
    private Integer player1FinalScore = 0;

    @Column(name = "player2_final_score", nullable = false)
    private Integer player2FinalScore = 0;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchPlayerEntity> matchPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchRoundEntity> matchRounds = new ArrayList<>();

    public MatchEntity() {}

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public Long getPlayer1UserId() { return player1UserId; }
    public void setPlayer1UserId(Long player1UserId) { this.player1UserId = player1UserId; }

    public Long getPlayer2UserId() { return player2UserId; }
    public void setPlayer2UserId(Long player2UserId) { this.player2UserId = player2UserId; }

    public Long getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Long winnerUserId) { this.winnerUserId = winnerUserId; }

    public Integer getTotalRounds() { return totalRounds; }
    public void setTotalRounds(Integer totalRounds) { this.totalRounds = totalRounds; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public Integer getPlayer1FinalScore() { return player1FinalScore; }
    public void setPlayer1FinalScore(Integer player1FinalScore) { this.player1FinalScore = player1FinalScore; }

    public Integer getPlayer2FinalScore() { return player2FinalScore; }
    public void setPlayer2FinalScore(Integer player2FinalScore) { this.player2FinalScore = player2FinalScore; }

    public List<MatchPlayerEntity> getMatchPlayers() { return matchPlayers; }
    public void setMatchPlayers(List<MatchPlayerEntity> matchPlayers) { this.matchPlayers = matchPlayers; }

    public List<MatchRoundEntity> getMatchRounds() { return matchRounds; }
    public void setMatchRounds(List<MatchRoundEntity> matchRounds) { this.matchRounds = matchRounds; }
}

package com.clashofbytes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "match_players")
public class MatchPlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_player_id")
    private Long matchPlayerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "final_score", nullable = false)
    private Integer finalScore = 0;

    @Column(name = "result", nullable = false, length = 10)
    private String result; // WIN, LOSS, DRAW

    public MatchPlayerEntity() {}

    public Long getMatchPlayerId() { return matchPlayerId; }
    public void setMatchPlayerId(Long matchPlayerId) { this.matchPlayerId = matchPlayerId; }

    public MatchEntity getMatch() { return match; }
    public void setMatch(MatchEntity match) { this.match = match; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public Integer getFinalScore() { return finalScore; }
    public void setFinalScore(Integer finalScore) { this.finalScore = finalScore; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

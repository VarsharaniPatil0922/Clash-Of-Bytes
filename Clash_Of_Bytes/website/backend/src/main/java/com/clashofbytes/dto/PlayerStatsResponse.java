package com.clashofbytes.dto;

public class PlayerStatsResponse {

    private Long userId;
    private String username;
    private int totalMatches;
    private int wins;
    private int losses;
    private int draws;
    private double winRate;
    private int totalScore;
    private String favoriteCharacter;

    public PlayerStatsResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getFavoriteCharacter() { return favoriteCharacter; }
    public void setFavoriteCharacter(String favoriteCharacter) { this.favoriteCharacter = favoriteCharacter; }
}

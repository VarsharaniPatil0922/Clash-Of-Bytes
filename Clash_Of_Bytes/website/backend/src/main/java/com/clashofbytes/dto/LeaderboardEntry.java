package com.clashofbytes.dto;

public class LeaderboardEntry {

    private int rank;
    private Long userId;
    private String username;
    private int matches;
    private int wins;
    private int losses;
    private double winRate;
    private int totalScore;
    private String favoriteCharacter;

    public LeaderboardEntry() {}

    public LeaderboardEntry(int rank, Long userId, String username, int matches, int wins, int losses, double winRate, int totalScore, String favoriteCharacter) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.matches = matches;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
        this.totalScore = totalScore;
        this.favoriteCharacter = favoriteCharacter;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getMatches() { return matches; }
    public void setMatches(int matches) { this.matches = matches; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getFavoriteCharacter() { return favoriteCharacter; }
    public void setFavoriteCharacter(String favoriteCharacter) { this.favoriteCharacter = favoriteCharacter; }
}

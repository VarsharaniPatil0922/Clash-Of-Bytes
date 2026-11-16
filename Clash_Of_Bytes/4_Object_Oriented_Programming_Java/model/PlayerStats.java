package model;

/**
 * Aggregated Statistics for a Player.
 */
public class PlayerStats {
    private int userId;
    private String username;
    private int totalMatches;
    private int totalWins;
    private int totalLosses;
    private int totalDraws;
    private int totalScore;
    private String favoriteCharacter;
    private double winRate;

    public PlayerStats() {}

    public PlayerStats(int userId, String username, int totalMatches, int totalWins, int totalLosses,
                       int totalDraws, int totalScore, String favoriteCharacter) {
        this.userId = userId;
        this.username = username;
        this.totalMatches = totalMatches;
        this.totalWins = totalWins;
        this.totalLosses = totalLosses;
        this.totalDraws = totalDraws;
        this.totalScore = totalScore;
        this.favoriteCharacter = favoriteCharacter != null ? favoriteCharacter : "None";
        this.winRate = totalMatches > 0 ? ((double) totalWins / totalMatches) * 100.0 : 0.0;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }

    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }

    public int getTotalLosses() { return totalLosses; }
    public void setTotalLosses(int totalLosses) { this.totalLosses = totalLosses; }

    public int getTotalDraws() { return totalDraws; }
    public void setTotalDraws(int totalDraws) { this.totalDraws = totalDraws; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getFavoriteCharacter() { return favoriteCharacter; }
    public void setFavoriteCharacter(String favoriteCharacter) { this.favoriteCharacter = favoriteCharacter; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
}

package model;

/**
 * Model class linking a player to a specific match, selected character, score, and outcome.
 */
public class MatchPlayer {
    private int matchPlayerId;
    private int matchId;
    private int userId;
    private int characterId;
    private int finalScore;
    private String result; // WIN, LOSS, DRAW

    public MatchPlayer() {}

    public MatchPlayer(int matchId, int userId, int characterId, int finalScore, String result) {
        this.matchId = matchId;
        this.userId = userId;
        this.characterId = characterId;
        this.finalScore = finalScore;
        this.result = result;
    }

    public MatchPlayer(int matchPlayerId, int matchId, int userId, int characterId, int finalScore, String result) {
        this.matchPlayerId = matchPlayerId;
        this.matchId = matchId;
        this.userId = userId;
        this.characterId = characterId;
        this.finalScore = finalScore;
        this.result = result;
    }

    public int getMatchPlayerId() { return matchPlayerId; }
    public void setMatchPlayerId(int matchPlayerId) { this.matchPlayerId = matchPlayerId; }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCharacterId() { return characterId; }
    public void setCharacterId(int characterId) { this.characterId = characterId; }

    public int getFinalScore() { return finalScore; }
    public void setFinalScore(int finalScore) { this.finalScore = finalScore; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

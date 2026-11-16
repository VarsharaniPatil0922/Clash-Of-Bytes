package CO3.Collections;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Course Outcome: CO3 | Practical 03: Collections Framework
 * 
 * Immutable data transfer object storing comprehensive match telemetry.
 */
public class MatchRecord {

    private final int matchId;
    private final String timestamp;
    private final String player1Name;
    private final String player2Name;
    private final String winnerName;
    private final String gameMode;
    private final int roundsPlayed;
    private final double p1RemainingHp;
    private final double p2RemainingHp;
    private final double totalDamageDealt;
    private final String summaryNote;

    public MatchRecord(int matchId, String player1Name, String player2Name, String winnerName,
                       String gameMode, int roundsPlayed, double p1RemainingHp, double p2RemainingHp,
                       double totalDamageDealt, String summaryNote) {
        this.matchId = matchId;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.winnerName = (winnerName != null && !winnerName.isEmpty()) ? winnerName : "DRAW / UNFINISHED";
        this.gameMode = gameMode;
        this.roundsPlayed = roundsPlayed;
        this.p1RemainingHp = Math.max(0.0, p1RemainingHp);
        this.p2RemainingHp = Math.max(0.0, p2RemainingHp);
        this.totalDamageDealt = Math.max(0.0, totalDamageDealt);
        this.summaryNote = (summaryNote != null) ? summaryNote : "";
    }

    public int getMatchId() { return matchId; }
    public String getTimestamp() { return timestamp; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public String getWinnerName() { return winnerName; }
    public String getGameMode() { return gameMode; }
    public int getRoundsPlayed() { return roundsPlayed; }
    public double getP1RemainingHp() { return p1RemainingHp; }
    public double getP2RemainingHp() { return p2RemainingHp; }
    public double getTotalDamageDealt() { return totalDamageDealt; }
    public String getSummaryNote() { return summaryNote; }

    @Override
    public String toString() {
        return String.format("Match #%d [%s] | %s vs %s | Winner: %s | Mode: %s | Total Dmg: %.1f",
                matchId, timestamp, player1Name, player2Name, winnerName, gameMode, totalDamageDealt);
    }
}

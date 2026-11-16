package CO3.Collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Course Outcome: CO3 | Practical 03: Collections Framework
 * 
 * Manages game history and statistics using List (ArrayList) and Map (HashMap).
 * - List<MatchRecord> preserves exact chronological order of completed matches.
 * - Map<String, Integer> tracks win-loss tallies per fighter with O(1) performance.
 */
public class MatchHistoryManager {

    private static final MatchHistoryManager INSTANCE = new MatchHistoryManager();

    private final List<MatchRecord> historyList;
    private final Map<String, Integer> winCountMap;
    private int matchIdCounter;

    public MatchHistoryManager() {
        this.historyList = new ArrayList<>();
        this.winCountMap = new HashMap<>();
        this.matchIdCounter = 1;
    }

    public static MatchHistoryManager getInstance() {
        return INSTANCE;
    }

    /**
     * Records a completed match into the history list and updates the win map.
     */
    public synchronized MatchRecord recordMatch(String p1Name, String p2Name, String winnerName,
                                                String gameMode, int rounds, double p1Hp, double p2Hp,
                                                double totalDmg, String note) {
        MatchRecord record = new MatchRecord(matchIdCounter++, p1Name, p2Name, winnerName,
                                             gameMode, rounds, p1Hp, p2Hp, totalDmg, note);
        historyList.add(record);

        if (winnerName != null && !winnerName.trim().isEmpty() && !winnerName.contains("DRAW")) {
            winCountMap.put(winnerName, winCountMap.getOrDefault(winnerName, 0) + 1);
        }
        return record;
    }

    /**
     * Returns an unmodifiable copy of all match records to preserve encapsulation.
     */
    public synchronized List<MatchRecord> getAllRecords() {
        return Collections.unmodifiableList(new ArrayList<>(historyList));
    }

    /**
     * Safely retrieves the latest record, or null if history is empty.
     */
    public synchronized MatchRecord getLatestRecord() {
        if (historyList.isEmpty()) {
            return null;
        }
        return historyList.get(historyList.size() - 1);
    }

    public synchronized int getMatchCount() {
        return historyList.size();
    }

    public synchronized int getWinsForFighter(String fighterName) {
        if (fighterName == null) return 0;
        return winCountMap.getOrDefault(fighterName, 0);
    }

    public synchronized Map<String, Integer> getWinLeaderboard() {
        return Collections.unmodifiableMap(new HashMap<>(winCountMap));
    }

    public synchronized void clearHistory() {
        historyList.clear();
        winCountMap.clear();
        matchIdCounter = 1;
    }
}

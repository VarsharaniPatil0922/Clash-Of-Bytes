package service;

import dao.StatisticsDAO;
import dao.StatisticsDAO.MatchHistoryRow;
import model.PlayerStats;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for fetching aggregated player statistics and tabular match history.
 */
public class StatisticsService {

    private final StatisticsDAO statisticsDAO;

    public StatisticsService() {
        this.statisticsDAO = new StatisticsDAO();
    }

    public StatisticsService(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    public PlayerStats getPlayerStats(int userId) throws SQLException {
        return statisticsDAO.getPlayerStats(userId);
    }

    public List<MatchHistoryRow> getMatchHistoryForUser(int userId) throws SQLException {
        return statisticsDAO.getMatchHistoryForUser(userId);
    }

    public List<StatisticsDAO.LeaderboardRow> getLeaderboard(int limit) throws SQLException {
        return statisticsDAO.getLeaderboard(limit);
    }
}

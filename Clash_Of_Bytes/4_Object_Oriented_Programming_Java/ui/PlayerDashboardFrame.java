package ui;

import model.PlayerSession;
import model.PlayerStats;
import service.PlayerSessionManager;
import service.SupabaseStatisticsService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Player Statistics Dashboard displaying overall career records,
 * win/loss ratios, accumulated points, and favorite fighter directly from Supabase.
 */
public class PlayerDashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(11, 19, 41);
    private static final Color CARD_BG = new Color(16, 28, 56);
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private PlayerSession currentSession;
    private final SupabaseStatisticsService statisticsService;

    // Stat Values
    private JLabel usernameLbl;
    private JLabel uuidLbl;
    private JLabel totalMatchesVal;
    private JLabel winsVal;
    private JLabel lossesVal;
    private JLabel drawsVal;
    private JLabel winRateVal;
    private JLabel totalScoreVal;
    private JLabel favCharVal;
    private JComboBox<SessionItem> playerCombo;

    public static class SessionItem {
        public final PlayerSession session;
        public SessionItem(PlayerSession session) { this.session = session; }
        @Override public String toString() {
            return session.getUsername() + " (" + (session.getUserId().length() > 8 ? session.getUserId().substring(0, 8) + "..." : session.getUserId()) + ")";
        }
    }

    public PlayerDashboardFrame(PlayerSession session) {
        super("Clash of Bytes - Supabase Career Dossier");
        this.currentSession = session;
        this.statisticsService = new SupabaseStatisticsService();

        initUI();
        loadActiveSessions();
        if (this.currentSession != null) {
            loadPlayerStatistics(this.currentSession);
        }
    }

    /**
     * Backwards compatibility constructor.
     */
    public PlayerDashboardFrame(int userId) {
        this(resolveCurrentSession(userId));
    }

    private static PlayerSession resolveCurrentSession(int userId) {
        PlayerSession p1 = PlayerSessionManager.getInstance().getPlayer1Session();
        PlayerSession p2 = PlayerSessionManager.getInstance().getPlayer2Session();
        if (p1 != null) return p1;
        if (p2 != null) return p2;
        return new PlayerSession("demo-player-uuid", "Demo Fighter", "demo@clashofbytes.com", "anon", "refresh", 3600);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(860, 640);
        setMinimumSize(new Dimension(780, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- Top Navigation & Header Banner ---
        JPanel topContainer = new JPanel(new BorderLayout(10, 8));
        topContainer.setBackground(BG_DARK);
        topContainer.setBorder(new EmptyBorder(16, 25, 5, 25));

        // Navigation Bar
        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navRow.setBackground(BG_DARK);

        JButton homeBtn = new JButton("[MENU] MAIN MENU");
        homeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        homeBtn.setBackground(new Color(30, 41, 59));
        homeBtn.setForeground(TEXT_LIGHT);
        homeBtn.setFocusPainted(false);
        homeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeBtn.addActionListener(e -> {
            new MainMenuFrame().setVisible(true);
            this.dispose();
        });

        JButton leadBtn = new JButton("[RANKS] LEADERBOARD");
        leadBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        leadBtn.setBackground(new Color(30, 41, 59));
        leadBtn.setForeground(ACCENT_GOLD);
        leadBtn.setFocusPainted(false);
        leadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leadBtn.addActionListener(e -> new LeaderboardFrame().setVisible(true));

        JButton historyBtn = new JButton("[LOGS] MATCH HISTORY");
        historyBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyBtn.setBackground(new Color(30, 41, 59));
        historyBtn.setForeground(ACCENT_CYAN);
        historyBtn.setFocusPainted(false);
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.addActionListener(e -> {
            if (currentSession != null) {
                new MatchHistoryFrame(currentSession).setVisible(true);
            }
        });

        navRow.add(homeBtn);
        navRow.add(leadBtn);
        navRow.add(historyBtn);
        topContainer.add(navRow, BorderLayout.NORTH);

        // Header Title and Player Switcher
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel titleBox = new JPanel(new GridLayout(3, 1, 0, 2));
        titleBox.setBackground(BG_DARK);

        JLabel titleLbl = new JLabel("PLAYER CAREER DOSSIER", SwingConstants.LEFT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(TEXT_LIGHT);

        usernameLbl = new JLabel("PLAYER DOSSIER: " + (currentSession != null ? currentSession.getUsername() : "Loading..."), SwingConstants.LEFT);
        usernameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        usernameLbl.setForeground(ACCENT_CYAN);

        uuidLbl = new JLabel("Auth UUID: " + (currentSession != null ? currentSession.getUserId() : "N/A"), SwingConstants.LEFT);
        uuidLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        uuidLbl.setForeground(TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(usernameLbl);
        titleBox.add(uuidLbl);

        // Player Switcher Combo
        JPanel switchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        switchBox.setBackground(BG_DARK);
        JLabel switchLbl = new JLabel("Switch Session:");
        switchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        switchLbl.setForeground(TEXT_MUTED);

        playerCombo = new JComboBox<>();
        playerCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        playerCombo.setBackground(CARD_BG);
        playerCombo.setForeground(TEXT_LIGHT);
        playerCombo.setPreferredSize(new Dimension(240, 32));
        playerCombo.addActionListener(e -> {
            SessionItem item = (SessionItem) playerCombo.getSelectedItem();
            if (item != null && item.session != currentSession) {
                currentSession = item.session;
                usernameLbl.setText("PLAYER DOSSIER: " + currentSession.getUsername());
                uuidLbl.setText("Auth UUID: " + currentSession.getUserId());
                loadPlayerStatistics(currentSession);
            }
        });

        switchBox.add(switchLbl);
        switchBox.add(playerCombo);

        header.add(titleBox, BorderLayout.WEST);
        header.add(switchBox, BorderLayout.EAST);
        topContainer.add(header, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // --- Stat Cards Grid ---
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setBackground(BG_DARK);
        grid.setBorder(new EmptyBorder(5, 25, 10, 25));

        totalMatchesVal = new JLabel("0", SwingConstants.CENTER);
        winsVal = new JLabel("0", SwingConstants.CENTER);
        lossesVal = new JLabel("0", SwingConstants.CENTER);
        drawsVal = new JLabel("0", SwingConstants.CENTER);
        winRateVal = new JLabel("0.0%", SwingConstants.CENTER);
        totalScoreVal = new JLabel("0", SwingConstants.CENTER);

        grid.add(createStatCard("TOTAL MATCHES", totalMatchesVal, TEXT_LIGHT));
        grid.add(createStatCard("VICTORIES (WINS)", winsVal, ACCENT_GREEN));
        grid.add(createStatCard("DEFEATS (LOSSES)", lossesVal, ACCENT_RED));
        grid.add(createStatCard("DRAWS", drawsVal, TEXT_MUTED));
        grid.add(createStatCard("WIN RATIO", winRateVal, ACCENT_GOLD));
        grid.add(createStatCard("CAREER SCORE", totalScoreVal, ACCENT_CYAN));

        // Center Wrapper to include Favorite Character Box
        JPanel centerWrapper = new JPanel(new BorderLayout(10, 12));
        centerWrapper.setBackground(BG_DARK);
        centerWrapper.add(grid, BorderLayout.CENTER);

        JPanel favPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        favPanel.setBackground(CARD_BG);
        favPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JLabel favTitle = new JLabel("Most Frequently Picked Fighter:");
        favTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        favTitle.setForeground(TEXT_MUTED);

        favCharVal = new JLabel("None", SwingConstants.CENTER);
        favCharVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        favCharVal.setForeground(ACCENT_CYAN);

        favPanel.add(favTitle);
        favPanel.add(favCharVal);
        centerWrapper.add(favPanel, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);

        // --- Bottom Actions ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        bottomBar.setBackground(BG_DARK);
        bottomBar.setBorder(new EmptyBorder(5, 25, 18, 25));

        JButton viewHistoryBtn = new JButton("VIEW COMPLETE MATCH TELEMETRY TABLE >>");
        viewHistoryBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewHistoryBtn.setBackground(new Color(30, 41, 59));
        viewHistoryBtn.setForeground(ACCENT_CYAN);
        viewHistoryBtn.setFocusPainted(false);
        viewHistoryBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewHistoryBtn.setPreferredSize(new Dimension(340, 38));
        viewHistoryBtn.addActionListener(e -> {
            if (currentSession != null) {
                new MatchHistoryFrame(currentSession).setVisible(true);
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(new Color(71, 85, 105));
        closeBtn.setForeground(TEXT_LIGHT);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 38));
        closeBtn.addActionListener(e -> this.dispose());

        bottomBar.add(viewHistoryBtn);
        bottomBar.add(closeBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color valColor) {
        JPanel card = new JPanel(new BorderLayout(5, 4));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(14, 15, 14, 15)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(valColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadActiveSessions() {
        playerCombo.removeAllItems();
        PlayerSession p1 = PlayerSessionManager.getInstance().getPlayer1Session();
        PlayerSession p2 = PlayerSessionManager.getInstance().getPlayer2Session();

        SessionItem selectTarget = null;
        if (p1 != null) {
            SessionItem item1 = new SessionItem(p1);
            playerCombo.addItem(item1);
            if (currentSession == p1) selectTarget = item1;
        }
        if (p2 != null) {
            SessionItem item2 = new SessionItem(p2);
            playerCombo.addItem(item2);
            if (currentSession == p2) selectTarget = item2;
        }

        if (selectTarget != null) {
            playerCombo.setSelectedItem(selectTarget);
        }
    }

    private void loadPlayerStatistics(PlayerSession session) {
        new SwingWorker<PlayerStats, Void>() {
            @Override
            protected PlayerStats doInBackground() throws Exception {
                return statisticsService.getPlayerStats(session);
            }

            @Override
            protected void done() {
                try {
                    PlayerStats stats = get();
                    totalMatchesVal.setText(String.valueOf(stats.getTotalMatches()));
                    winsVal.setText(String.valueOf(stats.getTotalWins()));
                    lossesVal.setText(String.valueOf(stats.getTotalLosses()));
                    drawsVal.setText(String.valueOf(stats.getTotalDraws()));
                    winRateVal.setText(String.format("%.1f%%", stats.getWinRate()));
                    totalScoreVal.setText(String.valueOf(stats.getTotalScore()));
                    favCharVal.setText(stats.getFavoriteCharacter());
                } catch (Exception ex) {
                    System.err.println("[PlayerDashboardFrame] Warning loading player stats: " + ex.getMessage());
                }
            }
        }.execute();
    }
}

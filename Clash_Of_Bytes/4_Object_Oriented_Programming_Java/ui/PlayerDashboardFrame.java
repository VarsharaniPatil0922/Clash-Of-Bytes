package ui;

import model.PlayerStats;
import service.StatisticsService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Player Statistics Dashboard displaying overall career records,
 * win/loss ratios, accumulated points, and favorite fighter.
 */
public class PlayerDashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color TEXT_LIGHT = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final int userId;
    private final StatisticsService statisticsService;

    // Stat Values
    private JLabel usernameLbl;
    private JLabel totalMatchesVal;
    private JLabel winsVal;
    private JLabel lossesVal;
    private JLabel drawsVal;
    private JLabel winRateVal;
    private JLabel totalScoreVal;
    private JLabel favCharVal;

    public PlayerDashboardFrame(int userId) {
        super("Clash of Bytes - Player Career Dashboard");
        this.userId = userId;
        this.statisticsService = new StatisticsService();

        initUI();
        loadPlayerStatistics();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 560);
        setMinimumSize(new Dimension(720, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- Header Banner ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel titleLbl = new JLabel("PLAYER CAREER DASHBOARD", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Impact", Font.PLAIN, 32));
        titleLbl.setForeground(TEXT_LIGHT);

        usernameLbl = new JLabel("Loading Player Dossier...", SwingConstants.CENTER);
        usernameLbl.setFont(new Font("Segoe UI Black", Font.BOLD, 15));
        usernameLbl.setForeground(ACCENT_CYAN);

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(usernameLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // --- Stat Cards Grid ---
        JPanel grid = new JPanel(new GridLayout(2, 3, 15, 15));
        grid.setBackground(BG_DARK);
        grid.setBorder(new EmptyBorder(10, 25, 15, 25));

        totalMatchesVal = new JLabel("0", SwingConstants.CENTER);
        winsVal = new JLabel("0", SwingConstants.CENTER);
        lossesVal = new JLabel("0", SwingConstants.CENTER);
        drawsVal = new JLabel("0", SwingConstants.CENTER);
        winRateVal = new JLabel("0.0%", SwingConstants.CENTER);
        totalScoreVal = new JLabel("0", SwingConstants.CENTER);

        grid.add(createStatCard("TOTAL MATCHES", totalMatchesVal, TEXT_LIGHT));
        grid.add(createStatCard("VICTORIES (WINS)", winsVal, ACCENT_GREEN));
        grid.add(createStatCard("DEFEATS (LOSSES)", lossesVal, ACCENT_RED));
        grid.add(createStatCard("DRAWS", drawsVal, ACCENT_GOLD));
        grid.add(createStatCard("WIN RATIO", winRateVal, ACCENT_CYAN));
        grid.add(createStatCard("TOTAL SCORE", totalScoreVal, ACCENT_GOLD));

        // Center Wrapper to include Favorite Character Box
        JPanel centerWrapper = new JPanel(new BorderLayout(10, 15));
        centerWrapper.setBackground(BG_DARK);
        centerWrapper.add(grid, BorderLayout.CENTER);

        JPanel favPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        favPanel.setBackground(CARD_BG);
        favPanel.setBorder(new CompoundBorder(new LineBorder(new Color(71, 85, 105), 1), new EmptyBorder(10, 20, 10, 20)));

        JLabel favTitle = new JLabel("Most Picked Fighter: ");
        favTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        favTitle.setForeground(TEXT_MUTED);

        favCharVal = new JLabel("None", SwingConstants.CENTER);
        favCharVal.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
        favCharVal.setForeground(ACCENT_CYAN);

        favPanel.add(favTitle);
        favPanel.add(favCharVal);
        centerWrapper.add(favPanel, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);

        // --- Bottom Actions ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomBar.setBackground(BG_DARK);
        bottomBar.setBorder(new EmptyBorder(5, 25, 15, 25));

        JButton viewHistoryBtn = new JButton("VIEW COMPLETE MATCH HISTORY TABLE >>");
        viewHistoryBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewHistoryBtn.setBackground(new Color(51, 65, 85));
        viewHistoryBtn.setForeground(ACCENT_CYAN);
        viewHistoryBtn.setFocusPainted(false);
        viewHistoryBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewHistoryBtn.addActionListener(e -> {
            new MatchHistoryFrame(userId, usernameLbl.getText().replace("PLAYER: ", "")).setVisible(true);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setBackground(new Color(71, 85, 105));
        closeBtn.setForeground(TEXT_LIGHT);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> this.dispose());

        bottomBar.add(viewHistoryBtn);
        bottomBar.add(closeBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color valColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(71, 85, 105), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Impact", Font.PLAIN, 32));
        valueLabel.setForeground(valColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadPlayerStatistics() {
        new SwingWorker<PlayerStats, Void>() {
            @Override
            protected PlayerStats doInBackground() throws Exception {
                return statisticsService.getPlayerStats(userId);
            }

            @Override
            protected void done() {
                try {
                    PlayerStats stats = get();
                    usernameLbl.setText("PLAYER: " + stats.getUsername().toUpperCase());
                    totalMatchesVal.setText(String.valueOf(stats.getTotalMatches()));
                    winsVal.setText(String.valueOf(stats.getTotalWins()));
                    lossesVal.setText(String.valueOf(stats.getTotalLosses()));
                    drawsVal.setText(String.valueOf(stats.getTotalDraws()));
                    winRateVal.setText(String.format("%.1f%%", stats.getWinRate()));
                    totalScoreVal.setText(String.format("%,d", stats.getTotalScore()));
                    favCharVal.setText(stats.getFavoriteCharacter());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PlayerDashboardFrame.this,
                            "Error loading player statistics: " + ex.getMessage(),
                            "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}

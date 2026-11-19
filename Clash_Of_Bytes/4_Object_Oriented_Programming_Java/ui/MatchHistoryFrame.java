package ui;

import model.PlayerSession;
import service.PlayerSessionManager;
import service.SupabaseStatisticsService;
import service.SupabaseStatisticsService.SupabaseMatchHistoryItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Tabular Match Telemetry Archive displaying completed combat records,
 * participants, scoring, duration, and outcomes loaded directly from Supabase.
 */
public class MatchHistoryFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(11, 19, 41);
    private static final Color CARD_BG = new Color(16, 28, 56);
    private static final Color ROW_ALT = new Color(20, 34, 68);
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final PlayerSession session;
    private final String username;
    private final SupabaseStatisticsService statisticsService;

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable matchTable;
    private JLabel statusLabel;
    private JTextField filterField;

    public MatchHistoryFrame(PlayerSession session) {
        super("Clash of Bytes - Supabase Match Archive (" + (session != null ? session.getUsername() : "Player") + ")");
        this.session = session;
        this.username = session != null ? session.getUsername() : "Player";
        this.statisticsService = new SupabaseStatisticsService();

        initUI();
        loadHistoryData();
    }

    /**
     * Backwards compatibility constructor.
     */
    public MatchHistoryFrame(int userId, String username) {
        this(resolveSession(userId, username));
    }

    private static PlayerSession resolveSession(int userId, String username) {
        PlayerSession p1 = PlayerSessionManager.getInstance().getPlayer1Session();
        PlayerSession p2 = PlayerSessionManager.getInstance().getPlayer2Session();
        if (p1 != null && (username == null || p1.getUsername().equalsIgnoreCase(username))) return p1;
        if (p2 != null && (username == null || p2.getUsername().equalsIgnoreCase(username))) return p2;
        if (p1 != null) return p1;
        return new PlayerSession(String.valueOf(userId), username != null ? username : "Player", "player@clashofbytes.com", "anon", "refresh", 3600);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1020, 620);
        setMinimumSize(new Dimension(880, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));

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

        JButton dashBtn = new JButton("[STATS] CAREER DOSSIER");
        dashBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dashBtn.setBackground(new Color(30, 41, 59));
        dashBtn.setForeground(ACCENT_GREEN);
        dashBtn.setFocusPainted(false);
        dashBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dashBtn.addActionListener(e -> new PlayerDashboardFrame(session).setVisible(true));

        navRow.add(homeBtn);
        navRow.add(leadBtn);
        navRow.add(dashBtn);
        topContainer.add(navRow, BorderLayout.NORTH);

        // Header Title & Filter Search Field
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setBackground(BG_DARK);

        JLabel titleLbl = new JLabel("MATCH TELEMETRY ARCHIVE", SwingConstants.LEFT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(TEXT_LIGHT);

        JLabel userSubtitle = new JLabel("SUPABASE COMBAT TELEMETRY FOR PLAYER: " + username.toUpperCase(), SwingConstants.LEFT);
        userSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userSubtitle.setForeground(ACCENT_CYAN);

        titleBox.add(titleLbl);
        titleBox.add(userSubtitle);

        // Filter Bar
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        searchBox.setBackground(BG_DARK);
        JLabel searchLbl = new JLabel("Filter Records:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLbl.setForeground(TEXT_MUTED);

        filterField = new JTextField(15);
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterField.setBackground(new Color(8, 13, 26));
        filterField.setForeground(TEXT_LIGHT);
        filterField.setCaretColor(TEXT_LIGHT);
        filterField.setBorder(new CompoundBorder(new LineBorder(new Color(51, 65, 85), 1), new EmptyBorder(4, 6, 4, 6)));
        filterField.addCaretListener(e -> filterTable(filterField.getText().trim()));

        searchBox.add(searchLbl);
        searchBox.add(filterField);

        header.add(titleBox, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.EAST);
        topContainer.add(header, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // --- Table Section ---
        String[] columns = {
                "Match ID", "Date & Time", "Opponent", "My Score", "Opp Score", "Rounds", "Duration", "Result"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        matchTable = new JTable(tableModel);
        matchTable.setBackground(CARD_BG);
        matchTable.setForeground(TEXT_LIGHT);
        matchTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        matchTable.setRowHeight(30);
        matchTable.setGridColor(new Color(30, 41, 59));
        matchTable.setSelectionBackground(new Color(30, 58, 110));
        matchTable.setSelectionForeground(ACCENT_CYAN);

        rowSorter = new TableRowSorter<>(tableModel);
        matchTable.setRowSorter(rowSorter);

        // Header Styling
        JTableHeader th = matchTable.getTableHeader();
        th.setBackground(new Color(8, 13, 26));
        th.setForeground(ACCENT_CYAN);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(th.getWidth(), 34));

        // Result column styling
        matchTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if ("WIN".equals(value)) {
                    setForeground(ACCENT_GREEN);
                } else if ("LOSS".equals(value)) {
                    setForeground(ACCENT_RED);
                } else {
                    setForeground(ACCENT_GOLD);
                }
                return c;
            }
        });

        // Alternating row background renderer for all cells
        matchTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(matchTable);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(new EmptyBorder(5, 25, 10, 25));
        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Status & Control Bar ---
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG_DARK);
        bottomBar.setBorder(new EmptyBorder(5, 25, 18, 25));

        statusLabel = new JLabel("Loading match records from Supabase...", SwingConstants.LEFT);
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(BG_DARK);

        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(30, 41, 59));
        refreshBtn.setForeground(TEXT_LIGHT);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadHistoryData());

        JButton closeBtn = new JButton("CLOSE");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(new Color(71, 85, 105));
        closeBtn.setForeground(TEXT_LIGHT);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> this.dispose());

        btnGroup.add(refreshBtn);
        btnGroup.add(closeBtn);

        bottomBar.add(statusLabel, BorderLayout.WEST);
        bottomBar.add(btnGroup, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void filterTable(String query) {
        if (query.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void loadHistoryData() {
        if (session == null) {
            statusLabel.setText("No active session; sign in to view Supabase match history.");
            return;
        }

        statusLabel.setText("Querying Supabase matches API for " + session.getUsername() + "...");
        tableModel.setRowCount(0);

        new SwingWorker<List<SupabaseMatchHistoryItem>, Void>() {
            @Override
            protected List<SupabaseMatchHistoryItem> doInBackground() throws Exception {
                return statisticsService.getMatchHistory(session);
            }

            @Override
            protected void done() {
                try {
                    List<SupabaseMatchHistoryItem> rows = get();
                    for (SupabaseMatchHistoryItem r : rows) {
                        tableModel.addRow(new Object[]{
                                "#" + r.matchId,
                                r.matchDate,
                                r.opponentIdentifier,
                                r.playerScore,
                                r.opponentScore,
                                r.totalRounds,
                                r.durationSeconds + "s",
                                r.result
                        });
                    }
                    statusLabel.setText("Telemetry archive loaded: " + rows.size() + " matches retrieved from Supabase.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load match history: " + ex.getMessage());
                }
            }
        }.execute();
    }
}

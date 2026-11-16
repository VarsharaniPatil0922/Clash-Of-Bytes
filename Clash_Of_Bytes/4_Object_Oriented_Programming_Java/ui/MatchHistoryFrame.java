package ui;

import dao.StatisticsDAO.MatchHistoryRow;
import service.StatisticsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Match History Window rendering past combat telemetry inside a stylized JTable.
 */
public class MatchHistoryFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color ROW_ALT = new Color(22, 33, 50);
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color TEXT_LIGHT = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final int userId;
    private final String username;
    private final StatisticsService statisticsService;

    private DefaultTableModel tableModel;
    private JTable matchTable;
    private JLabel statusLabel;

    public MatchHistoryFrame(int userId, String username) {
        super("Clash of Bytes - Match Telemetry Archive (" + username + ")");
        this.userId = userId;
        this.username = username;
        this.statisticsService = new StatisticsService();

        initUI();
        loadHistoryData();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 580);
        setMinimumSize(new Dimension(850, 450));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));

        // --- Header Banner ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(15, 20, 10, 20));

        JLabel titleLbl = new JLabel("MATCH TELEMETRY ARCHIVE", SwingConstants.LEFT);
        titleLbl.setFont(new Font("Impact", Font.PLAIN, 28));
        titleLbl.setForeground(TEXT_LIGHT);

        JLabel userSubtitle = new JLabel("COMBAT LOGS FOR PLAYER: " + username.toUpperCase(), SwingConstants.LEFT);
        userSubtitle.setFont(new Font("Segoe UI Black", Font.BOLD, 13));
        userSubtitle.setForeground(ACCENT_CYAN);

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(userSubtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // --- Table Section ---
        String[] columns = {
            "Match ID", "Date & Time", "Opponent", "My Fighter",
            "Opponent Fighter", "My Score", "Opp Score", "Rounds", "Result", "Winner"
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
        matchTable.setRowHeight(28);
        matchTable.setGridColor(new Color(51, 65, 85));
        matchTable.setSelectionBackground(new Color(51, 65, 85));
        matchTable.setSelectionForeground(ACCENT_CYAN);

        // Header Styling
        JTableHeader th = matchTable.getTableHeader();
        th.setBackground(new Color(15, 23, 42));
        th.setForeground(ACCENT_CYAN);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(th.getWidth(), 32));

        // Custom Cell Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        matchTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Match ID
        matchTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // My Score
        matchTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Opp Score
        matchTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Rounds

        // Result Custom Color Renderer
        matchTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI Black", Font.BOLD, 12));
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
        scrollPane.setBorder(new EmptyBorder(5, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Status & Control Bar ---
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG_DARK);
        bottomBar.setBorder(new EmptyBorder(5, 20, 15, 20));

        statusLabel = new JLabel("Loading match records from database...", SwingConstants.LEFT);
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(BG_DARK);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(51, 65, 85));
        refreshBtn.setForeground(TEXT_LIGHT);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadHistoryData());

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(71, 85, 105));
        closeBtn.setForeground(TEXT_LIGHT);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> this.dispose());

        btnGroup.add(refreshBtn);
        btnGroup.add(closeBtn);

        bottomBar.add(statusLabel, BorderLayout.WEST);
        bottomBar.add(btnGroup, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadHistoryData() {
        statusLabel.setText("Querying database match_players...");
        tableModel.setRowCount(0);

        new SwingWorker<List<MatchHistoryRow>, Void>() {
            @Override
            protected List<MatchHistoryRow> doInBackground() throws Exception {
                return statisticsService.getMatchHistoryForUser(userId);
            }

            @Override
            protected void done() {
                try {
                    List<MatchHistoryRow> rows = get();
                    for (MatchHistoryRow r : rows) {
                        tableModel.addRow(new Object[]{
                                r.matchId,
                                r.matchDate,
                                r.opponentUsername,
                                r.myCharacterName,
                                r.opponentCharacterName,
                                r.myScore,
                                r.opponentScore,
                                r.roundsPlayed,
                                r.result,
                                r.winnerName
                        });
                    }
                    statusLabel.setText("Retrieved " + rows.size() + " completed match records from database.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to query match history: " + ex.getMessage());
                }
            }
        }.execute();
    }
}

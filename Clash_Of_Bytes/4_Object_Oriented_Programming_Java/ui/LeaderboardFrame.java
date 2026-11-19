package ui;

import dao.StatisticsDAO.LeaderboardRow;
import service.StatisticsService;

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
 * Global Tournament Hall of Fame & Leaderboard Frame for Clash of Bytes.
 * Displays ranked combat champions from the database with win rates and career points.
 */
public class LeaderboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(11, 19, 41);
    private static final Color CARD_BG = new Color(16, 28, 56);
    private static final Color ROW_ALT = new Color(20, 34, 66);
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color ACCENT_PINK = new Color(255, 42, 109);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final StatisticsService statisticsService;
    private DefaultTableModel tableModel;
    private JTable leaderboardTable;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel statusLabel;
    private JTextField searchField;

    public LeaderboardFrame() {
        super("Clash of Bytes - Global Tournament Leaderboard");
        this.statisticsService = new StatisticsService();

        initUI();
        loadLeaderboardData();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 620);
        setMinimumSize(new Dimension(860, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(12, 12));

        // --- Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 6));
        headerPanel.setBackground(BG_DARK);
        headerPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel titleLbl = new JLabel("GLOBAL TOURNAMENT LEADERBOARD", SwingConstants.LEFT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(TEXT_LIGHT);

        JLabel subtitleLbl = new JLabel("Ranked player standings based on verified match telemetry and career victories", SwingConstants.LEFT);
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLbl.setForeground(ACCENT_GOLD);

        // Search & Filter Toolbar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBar.setBackground(BG_DARK);

        JLabel searchLbl = new JLabel("Search Fighter:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLbl.setForeground(TEXT_MUTED);

        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBackground(new Color(8, 13, 26));
        searchField.setForeground(TEXT_LIGHT);
        searchField.setCaretColor(TEXT_LIGHT);
        searchField.setBorder(new CompoundBorder(new LineBorder(new Color(51, 65, 85), 1), new EmptyBorder(4, 8, 4, 8)));
        searchField.addCaretListener(e -> filterTable(searchField.getText().trim()));

        searchBar.add(searchLbl);
        searchBar.add(searchField);

        JPanel headerNorth = new JPanel(new BorderLayout());
        headerNorth.setBackground(BG_DARK);
        headerNorth.add(titleLbl, BorderLayout.WEST);
        headerNorth.add(searchBar, BorderLayout.EAST);

        headerPanel.add(headerNorth, BorderLayout.NORTH);
        headerPanel.add(subtitleLbl, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Leaderboard Table ---
        String[] columns = {
            "Rank", "User ID", "Player Username", "Total Matches", "Wins", "Losses", "Draws", "Win Rate", "Career Score"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setBackground(CARD_BG);
        leaderboardTable.setForeground(TEXT_LIGHT);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leaderboardTable.setRowHeight(32);
        leaderboardTable.setGridColor(new Color(30, 41, 59));
        leaderboardTable.setSelectionBackground(new Color(30, 58, 110));
        leaderboardTable.setSelectionForeground(ACCENT_CYAN);

        rowSorter = new TableRowSorter<>(tableModel);
        leaderboardTable.setRowSorter(rowSorter);

        // Header Styling
        JTableHeader th = leaderboardTable.getTableHeader();
        th.setBackground(new Color(8, 13, 26));
        th.setForeground(ACCENT_CYAN);
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setPreferredSize(new Dimension(th.getWidth(), 36));

        // Column Preferred Widths (Prevents Truncation & Ellipses)
        int[] colWidths = {55, 60, 165, 95, 65, 65, 65, 80, 100};
        for (int i = 0; i < colWidths.length && i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        // Cell Alignment and Custom Medals Renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < columns.length; i++) {
            if (i != 2) { // Username left-aligned
                leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Rank Renderer with Medals
        leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("#1".equals(value)) {
                    setForeground(new Color(255, 215, 0)); // Gold
                } else if ("#2".equals(value)) {
                    setForeground(new Color(192, 192, 192)); // Silver
                } else if ("#3".equals(value)) {
                    setForeground(new Color(205, 127, 50)); // Bronze
                } else {
                    setForeground(TEXT_MUTED);
                }
                return c;
            }
        });

        // Alternating row background renderer
        leaderboardTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(new EmptyBorder(5, 25, 10, 25));
        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Control Bar ---
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG_DARK);
        bottomBar.setBorder(new EmptyBorder(5, 25, 20, 25));

        statusLabel = new JLabel("Querying rankings from database...", SwingConstants.LEFT);
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnGroup.setBackground(BG_DARK);

        JButton viewDossierBtn = new JButton("VIEW SELECTED PLAYER DOSSIER");
        viewDossierBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewDossierBtn.setBackground(new Color(30, 41, 59));
        viewDossierBtn.setForeground(ACCENT_CYAN);
        viewDossierBtn.setFocusPainted(false);
        viewDossierBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDossierBtn.addActionListener(e -> onViewSelectedPlayer());

        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(51, 65, 85));
        refreshBtn.setForeground(TEXT_LIGHT);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadLeaderboardData());

        JButton closeBtn = new JButton("CLOSE");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(new Color(71, 85, 105));
        closeBtn.setForeground(TEXT_LIGHT);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> this.dispose());

        btnGroup.add(viewDossierBtn);
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
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 2)); // Filter by username
        }
    }

    private void onViewSelectedPlayer() {
        int selectedRow = leaderboardTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a player row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = leaderboardTable.convertRowIndexToModel(selectedRow);
        int userId = (int) tableModel.getValueAt(modelRow, 1);
        new PlayerDashboardFrame(userId).setVisible(true);
    }

    private void loadLeaderboardData() {
        statusLabel.setText("Querying database rankings...");
        tableModel.setRowCount(0);

        new SwingWorker<List<LeaderboardRow>, Void>() {
            @Override
            protected List<LeaderboardRow> doInBackground() throws Exception {
                return statisticsService.getLeaderboard(50);
            }

            @Override
            protected void done() {
                try {
                    List<LeaderboardRow> rows = get();
                    for (LeaderboardRow r : rows) {
                        String rankStr = "#" + r.rank;
                        tableModel.addRow(new Object[]{
                                rankStr,
                                r.userId,
                                r.username,
                                r.totalMatches,
                                r.wins,
                                r.losses,
                                r.draws,
                                String.format("%.1f%%", r.winRate),
                                String.format("%,d", r.careerScore)
                        });
                    }
                    statusLabel.setText("Ranked " + rows.size() + " active tournament fighters from database.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load leaderboard: " + ex.getMessage());
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LeaderboardFrame().setVisible(true));
    }
}

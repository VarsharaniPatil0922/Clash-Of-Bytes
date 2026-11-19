package ui;

import database.DatabaseInitializer;
import database.DBConnection;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.sql.Connection;

/**
 * Main Menu & Navigation Hub for the Clash of Bytes Desktop Application.
 * Unifies Two-Player Authentication, Character Select, Career Dashboard,
 * Match Telemetry, Global Leaderboard, and Academic Practical Suites.
 */
public class MainMenuFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Cyberpunk Theme Color System
    private static final Color BG_DARK = new Color(11, 19, 41);
    private static final Color CARD_BG = new Color(16, 28, 56);
    private static final Color CARD_HOVER = new Color(24, 40, 78);
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);
    private static final Color ACCENT_PINK = new Color(255, 42, 109);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private JLabel dbStatusLabel;

    public MainMenuFrame() {
        super("Clash of Bytes - Main Menu & Combat Hub");
        initDatabase();
        initUI();
    }

    private void initDatabase() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(4))
                            .build();
                    java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(service.SupabaseConfig.getRestUri("/characters?limit=1"))
                            .header("apikey", service.SupabaseConfig.getAnonKey())
                            .timeout(java.time.Duration.ofSeconds(4))
                            .GET()
                            .build();
                    java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                    return resp.statusCode() >= 200 && resp.statusCode() < 300;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    boolean sbOnline = get();
                    if (dbStatusLabel != null) {
                        if (sbOnline) {
                            dbStatusLabel.setText("● Supabase Cloud: Connected (REST API & Auth Active)");
                            dbStatusLabel.setForeground(ACCENT_GREEN);
                        } else {
                            dbStatusLabel.setText("▲ Supabase Cloud: Unreachable (Offline / Network Issue)");
                            dbStatusLabel.setForeground(ACCENT_GOLD);
                        }
                    }
                } catch (Exception ex) {
                    if (dbStatusLabel != null) {
                        dbStatusLabel.setText("▲ Supabase: " + ex.getMessage());
                        dbStatusLabel.setForeground(ACCENT_GOLD);
                    }
                }
            }
        }.execute();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(880, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_DARK);
        headerPanel.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel titleLabel = new JLabel("CLASH OF BYTES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subtitleLabel = new JLabel("2D LOCAL MULTIPLAYER FIGHTING ARENA & ACADEMIC SUITE", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitleLabel.setForeground(ACCENT_CYAN);

        JLabel academicLabel = new JLabel("MIT Academy of Engineering (MIT AOE), Pune • Semester III Integrated Project", SwingConstants.CENTER);
        academicLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        academicLabel.setForeground(TEXT_MUTED);

        JPanel headerContent = new JPanel(new GridLayout(3, 1, 0, 4));
        headerContent.setBackground(BG_DARK);
        headerContent.add(titleLabel);
        headerContent.add(subtitleLabel);
        headerContent.add(academicLabel);

        headerPanel.add(headerContent, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Menu Grid ---
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 20, 16));
        centerPanel.setBackground(BG_DARK);
        centerPanel.setBorder(new EmptyBorder(10, 40, 15, 40));

        // 1. Two-Player Dual Login & Battle Arena
        centerPanel.add(createMenuCard(
                "[ARENA] TWO-PLAYER COMBAT ARENA",
                "Side-by-side independent authentication, fighter selection, and local keyboard match play.",
                ACCENT_CYAN,
                () -> {
                    new TwoPlayerLoginFrame().setVisible(true);
                    this.dispose();
                }
        ));

        // 2. Global Tournament Leaderboard
        centerPanel.add(createMenuCard(
                "[RANKS] GLOBAL LEADERBOARD",
                "View ranked fighter standings, tournament champions, win/loss ratios, and career points.",
                ACCENT_GOLD,
                () -> new LeaderboardFrame().setVisible(true)
        ));

        // 3. Player Career Dossier & Statistics
        centerPanel.add(createMenuCard(
                "[DOSSIER] CAREER DOSSIER & STATS",
                "Inspect comprehensive player profiles, accumulated scores, win percentages, and favorite fighters.",
                ACCENT_GREEN,
                () -> new PlayerDashboardFrame(1).setVisible(true)
        ));

        // 4. Match Telemetry & History Archive
        centerPanel.add(createMenuCard(
                "[TELEMETRY] MATCH HISTORY ARCHIVE",
                "Browse transactional combat logs, score tallies, round telemetry, and past opponents in JTable.",
                ACCENT_PINK,
                () -> new MatchHistoryFrame(1, "shadow_master").setVisible(true)
        ));

        // 5. CO3 & CO4 Academic Practical Suite
        centerPanel.add(createMenuCard(
                "[ACADEMIC] CO3/CO4 PRACTICAL SUITE",
                "Demonstrate Swing GUI, Collections registries, Exception recovery, and File Handling archives.",
                new Color(168, 85, 247),
                () -> new CO3.SwingGUI.GameLauncherGUI().setVisible(true)
        ));

        // 6. Launch C++ OpenGL Engine / Web Client
        centerPanel.add(createMenuCard(
                "[ENGINES] LAUNCH ARCADES (C++ / WEB)",
                "Run the native C++ OpenGL arcade game (clash_of_bytes.exe) or launch the Web Client in browser.",
                new Color(56, 189, 248),
                this::showLaunchEnginesDialog
        ));

        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Status Bar ---
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(6, 9, 19));
        bottomBar.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 41, 59), 1),
                new EmptyBorder(10, 25, 12, 25)
        ));

        dbStatusLabel = new JLabel("● Connecting to database...", SwingConstants.LEFT);
        dbStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatusLabel.setForeground(ACCENT_GOLD);

        JLabel versionLabel = new JLabel("Clash of Bytes v2.4 (Ready for GitHub Release)", SwingConstants.RIGHT);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(TEXT_MUTED);

        bottomBar.add(dbStatusLabel, BorderLayout.WEST);
        bottomBar.add(versionLabel, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createMenuCard(String title, String description, Color accentColor, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBackground(CARD_BG);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(accentColor);

        JLabel descLbl = new JLabel("<html><body style='width: 300px;'>" + description + "</body></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(TEXT_LIGHT);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);

        // Hover Effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.setBorder(new CompoundBorder(
                        new LineBorder(accentColor, 2, true),
                        new EmptyBorder(15, 19, 15, 19)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                card.setBorder(new CompoundBorder(
                        new LineBorder(accentColor, 1, true),
                        new EmptyBorder(16, 20, 16, 20)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        return card;
    }

    private void showLaunchEnginesDialog() {
        String[] options = {"Launch C++ OpenGL (clash_of_bytes.exe)", "Open Web Multiplayer (Browser)", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select which game engine client to launch:",
                "Launch Game Client",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            try {
                File exe = new File("clash_of_bytes.exe");
                if (!exe.exists()) {
                    exe = new File("Clash_Of_Bytes/clash_of_bytes.exe");
                }
                if (exe.exists()) {
                    new ProcessBuilder(exe.getAbsolutePath()).start();
                    JOptionPane.showMessageDialog(this, "C++ OpenGL Arcade Game launched successfully!", "Engine Launched", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "clash_of_bytes.exe not found. Please run build.bat first.", "File Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to launch C++ engine: " + ex.getMessage(), "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == 1) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                } else {
                    JOptionPane.showMessageDialog(this, "Open your web browser and navigate to: http://localhost:8080", "Web Client URL", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open browser: " + ex.getMessage(), "Browser Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenuFrame().setVisible(true));
    }
}

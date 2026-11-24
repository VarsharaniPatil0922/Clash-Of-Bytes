package CO3.Auth;

import CO3.SwingGUI.GameLauncherGUI;
import database.DatabaseInitializer;
import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Cyberpunk Supabase Authentication Portal (Java Swing GUI).
 * 
 * Features:
 * 1. Dual Local Player Authentication: Both Player 1 and Player 2 can authenticate
 *    or select accounts on the SAME screen before the match starts!
 * 2. Individual Sign-In & Registration Cards with Supabase REST API (GoTrue).
 * 3. Quick Account Switchers & Offline 2-Player Demo mode.
 * 4. Smooth Transition into Clash of Bytes GameLauncherGUI with both authenticated players.
 */
public class SupabaseAuthGUI extends JFrame implements ActionListener, ItemListener {

    private static final long serialVersionUID = 1L;

    // --- Cyberpunk Theme Color Palette ---
    private static final Color BG_DARK = new Color(11, 17, 32);          // Deep Slate
    private static final Color CARD_BG = new Color(24, 33, 50);          // Card Slate
    private static final Color INPUT_BG = new Color(15, 23, 42);         // Input Slate
    private static final Color BORDER_SLATE = new Color(51, 65, 85);     // Slate Border
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);     // Neon Cyan (P1)
    private static final Color ACCENT_MAGENTA = new Color(255, 42, 109); // Neon Magenta (P2)
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);    // Neon Green
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);     // Neon Gold
    private static final Color TEXT_LIGHT = new Color(241, 245, 249);    // Text Bright
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Text Dim
    private static final Color ERROR_RED = new Color(239, 68, 68);       // Neon Red Error

    private final SupabaseAuthService authService;

    // --- Player 1 State & Controls ---
    private String p1Username = "roti";
    private String p1UserId = null;
    private String p1Email = "roti@gmail.com";
    private String p1Token = null;
    private JTextField p1NameField;
    private JTextField p1EmailField;
    private JPasswordField p1PasswordField;
    private JLabel p1StatusBadge;
    private JButton p1LoginBtn;
    private JButton p1RegisterBtn;

    // --- Player 2 State & Controls ---
    private String p2Username = "Atharva";
    private String p2UserId = null;
    private String p2Email = "atharva@clashofbytes.com";
    private String p2Token = null;
    private JTextField p2NameField;
    private JTextField p2EmailField;
    private JPasswordField p2PasswordField;
    private JLabel p2StatusBadge;
    private JButton p2LoginBtn;
    private JButton p2RegisterBtn;

    // --- Bottom Controls ---
    private JButton launchBothPlayersBtn;
    private JButton demoModeBtn;
    private JButton configSettingsBtn;
    private JLabel statusBannerLabel;
    private JLabel connectionDotLabel;

    public SupabaseAuthGUI() {
        super("Clash of Bytes — Supabase Dual Player Authentication Arena");
        this.authService = new SupabaseAuthService();

        initializeWindow();
        buildUI();
        refreshConnectionStatus();
    }

    private void initializeWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 680);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
    }

    private void buildUI() {
        // 1. NORTH: Cyberpunk Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER: Dual Player Authentication Grid (P1 & P2 Side by Side)
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(14, 20, 10, 20));

        JLabel infoBanner = new JLabel(
            "⚔️ 2-PLAYER LOCAL AUTHENTICATION : Both fighters log in on this device with their chosen account before entering the arena.",
            SwingConstants.CENTER
        );
        infoBanner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoBanner.setForeground(ACCENT_GOLD);
        infoBanner.setBorder(new EmptyBorder(0, 0, 8, 0));
        centerPanel.add(infoBanner, BorderLayout.NORTH);

        JPanel dualGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        dualGrid.setOpaque(false);
        dualGrid.add(createPlayerAuthCard(1, ACCENT_CYAN));
        dualGrid.add(createPlayerAuthCard(2, ACCENT_MAGENTA));
        centerPanel.add(dualGrid, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. SOUTH: Launch Action, Feedback & Config Tools
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_CYAN),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("⚡ CLASH OF BYTES : DUAL AUTH PORTAL");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT_CYAN);

        JLabel subtitle = new JLabel("Supabase Cloud Database & Two-Player Local Authentication System (Java Swing)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subtitle);

        connectionDotLabel = new JLabel("● Supabase: Configured");
        connectionDotLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        connectionDotLabel.setForeground(ACCENT_GREEN);

        header.add(titleBox, BorderLayout.WEST);
        header.add(connectionDotLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createPlayerAuthCard(int playerNum, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(accent, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Header Label
        JLabel titleLbl = new JLabel("👤 PLAYER " + playerNum + " (" + (playerNum == 1 ? "P1 - LEFT" : "P2 - RIGHT") + ")");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(accent);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Status Badge
        JLabel statusBadge = new JLabel(playerNum == 1 ? "✔ Active: roti [Registered]" : "✔ Active: Atharva [Registered]");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(ACCENT_GREEN);
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (playerNum == 1) p1StatusBadge = statusBadge;
        else p2StatusBadge = statusBadge;

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(statusBadge);
        card.add(Box.createVerticalStrut(14));

        // 1. Fighter Name / Username Field (New Players Can Add Their Own Name)
        JLabel nameLbl = new JLabel("FIGHTER NAME / USERNAME:");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLbl.setForeground(TEXT_LIGHT);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = createStyledTextField(playerNum == 1 ? "roti" : "Atharva");
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (playerNum == 1) p1NameField = nameField;
        else p2NameField = nameField;

        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(nameField);
        card.add(Box.createVerticalStrut(10));

        // 2. Email Address Field
        JLabel emailLbl = new JLabel("EMAIL ADDRESS:");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        emailLbl.setForeground(TEXT_LIGHT);
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField emailField = createStyledTextField(playerNum == 1 ? "roti@gmail.com" : "atharva@clashofbytes.com");
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (playerNum == 1) p1EmailField = emailField;
        else p2EmailField = emailField;

        card.add(emailLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(emailField);
        card.add(Box.createVerticalStrut(10));

        // 3. Password Field
        JLabel passLbl = new JLabel("SUPABASE PASSWORD (MIN 6 CHARACTERS):");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passLbl.setForeground(TEXT_LIGHT);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField passField = createStyledPasswordField();
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (playerNum == 1) p1PasswordField = passField;
        else p2PasswordField = passField;

        card.add(passLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(passField);
        card.add(Box.createVerticalStrut(14));

        // Action Buttons: Login / Register P1 or P2
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JButton loginBtn = new JButton("🔑 Sign In P" + playerNum);
        styleActionButton(loginBtn, accent, Color.BLACK);
        loginBtn.addActionListener(e -> handlePlayerLogin(playerNum));
        if (playerNum == 1) p1LoginBtn = loginBtn;
        else p2LoginBtn = loginBtn;

        JButton regBtn = new JButton("✚ Register P" + playerNum);
        styleActionButton(regBtn, new Color(45, 55, 72), TEXT_LIGHT);
        regBtn.addActionListener(e -> handlePlayerRegister(playerNum));
        if (playerNum == 1) p1RegisterBtn = regBtn;
        else p2RegisterBtn = regBtn;

        btnRow.add(loginBtn);
        btnRow.add(regBtn);
        card.add(btnRow);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private void setPlayerAccount(int playerNum, String username, String email, String token) {
        if (playerNum == 1) {
            p1Username = username;
            p1Email = email;
            p1Token = token;
            if (p1NameField != null) p1NameField.setText(username);
            if (p1EmailField != null) p1EmailField.setText(email);
            if (p1StatusBadge != null) {
                p1StatusBadge.setText("✔ Active: " + username + " [Active]");
                p1StatusBadge.setForeground(ACCENT_GREEN);
            }
        } else {
            p2Username = username;
            p2Email = email;
            p2Token = token;
            if (p2NameField != null) p2NameField.setText(username);
            if (p2EmailField != null) p2EmailField.setText(email);
            if (p2StatusBadge != null) {
                p2StatusBadge.setText("✔ Active: " + username + " [Active]");
                p2StatusBadge.setForeground(ACCENT_GREEN);
            }
        }
        statusBannerLabel.setText("Player " + playerNum + " set to: [" + username + "]. Ready to enter Arena.");
        statusBannerLabel.setForeground(ACCENT_GREEN);
    }

    private void handlePlayerLogin(int playerNum) {
        String name = (playerNum == 1 ? (p1NameField != null ? p1NameField.getText() : "") : (p2NameField != null ? p2NameField.getText() : "")).trim();
        String email = (playerNum == 1 ? p1EmailField.getText() : p2EmailField.getText()).trim();
        String pass = new String(playerNum == 1 ? p1PasswordField.getPassword() : p2PasswordField.getPassword()).trim();

        if (email.isBlank() && !name.isBlank()) {
            email = name.contains("@") ? name : (name.toLowerCase() + "@clashofbytes.local");
            if (playerNum == 1 && p1EmailField != null) p1EmailField.setText(email);
            if (playerNum == 2 && p2EmailField != null) p2EmailField.setText(email);
        }

        if (email.isBlank() || pass.isBlank()) {
            if (!name.isBlank()) {
                setPlayerAccount(playerNum, name, email.isBlank() ? (name.toLowerCase() + "@clashofbytes.local") : email, null);
                showSuccess("Player " + playerNum + " set to: [" + name + "] (Local Mode)");
                return;
            }
            showError("Please enter email and password for Player " + playerNum + ".");
            return;
        }

        final String finalEmail = email;
        final String finalName = !name.isBlank() ? name : (email.contains("@") ? email.split("@")[0] : email);

        setLoadingState(true, "Authenticating Player " + playerNum + " (" + finalName + ") via Supabase...");

        new SwingWorker<SupabaseAuthService.AuthResult, Void>() {
            @Override
            protected SupabaseAuthService.AuthResult doInBackground() {
                return authService.signIn(finalEmail, pass);
            }

            @Override
            protected void done() {
                setLoadingState(false, null);
                try {
                    SupabaseAuthService.AuthResult res = get();
                    if (res.isSuccess()) {
                        String loadedName = (res.getUsername() != null && !res.getUsername().isBlank()) ? res.getUsername() : finalName;
                        if (playerNum == 1) {
                            p1UserId = res.getUserId();
                            p1Username = loadedName;
                            if (p1NameField != null) p1NameField.setText(loadedName);
                        } else {
                            p2UserId = res.getUserId();
                            p2Username = loadedName;
                            if (p2NameField != null) p2NameField.setText(loadedName);
                        }
                        setPlayerAccount(playerNum, loadedName, res.getEmail(), res.getAccessToken());
                        showSuccess("Player " + playerNum + " logged in: [" + loadedName + "]!");
                        registerUserLocally(loadedName, res.getEmail());
                    } else {
                        showError("Player " + playerNum + " login: " + res.getMessage());
                        setPlayerAccount(playerNum, finalName, finalEmail, null);
                        registerUserLocally(finalName, finalEmail);
                    }
                } catch (Exception ex) {
                    showError("Player " + playerNum + " error: " + ex.getMessage());
                    setPlayerAccount(playerNum, finalName, finalEmail, null);
                    registerUserLocally(finalName, finalEmail);
                }
            }
        }.execute();
    }

    private void handlePlayerRegister(int playerNum) {
        String name = (playerNum == 1 ? (p1NameField != null ? p1NameField.getText() : "") : (p2NameField != null ? p2NameField.getText() : "")).trim();
        String email = (playerNum == 1 ? p1EmailField.getText() : p2EmailField.getText()).trim();
        String pass = new String(playerNum == 1 ? p1PasswordField.getPassword() : p2PasswordField.getPassword()).trim();

        if (name.isBlank()) {
            showError("Please enter a Fighter Name / Username for Player " + playerNum + ".");
            return;
        }

        if (email.isBlank()) {
            email = name.toLowerCase() + "@clashofbytes.local";
            if (playerNum == 1 && p1EmailField != null) p1EmailField.setText(email);
            if (playerNum == 2 && p2EmailField != null) p2EmailField.setText(email);
        }

        if (pass.length() < 6) {
            showError("Password must be at least 6 characters for Player " + playerNum + ".");
            return;
        }

        final String finalName = name;
        final String finalEmail = email;

        setLoadingState(true, "Registering [" + finalName + "] in Supabase Cloud Database...");

        new SwingWorker<SupabaseAuthService.AuthResult, Void>() {
            @Override
            protected SupabaseAuthService.AuthResult doInBackground() {
                return authService.signUp(finalEmail, pass, finalName);
            }

            @Override
            protected void done() {
                setLoadingState(false, null);
                try {
                    SupabaseAuthService.AuthResult res = get();
                    String uid = res.getUserId();
                    if (res.isSuccess()) {
                        if (playerNum == 1) {
                            p1UserId = uid;
                            p1Username = res.getUsername();
                        } else {
                            p2UserId = uid;
                            p2Username = res.getUsername();
                        }
                        setPlayerAccount(playerNum, res.getUsername(), res.getEmail(), res.getAccessToken());

                        // Immediately initialize profile row in Supabase and SQLite
                        final String token = res.getAccessToken();
                        new Thread(() -> {
                            authService.upsertProfile(uid, finalName, 0, 0, 0, 0.0, "Ironclad Warrior", token);
                            registerUserLocally(finalName, finalEmail);
                        }).start();

                        showSuccess("Player " + playerNum + " registered: [" + res.getUsername() + "]! Ready for battle.");
                    } else {
                        showError("Sign-up note: " + res.getMessage());
                        setPlayerAccount(playerNum, finalName, finalEmail, null);
                        registerUserLocally(finalName, finalEmail);
                    }
                } catch (Exception ex) {
                    showError("Player " + playerNum + " registration note: " + ex.getMessage());
                    setPlayerAccount(playerNum, finalName, finalEmail, null);
                    registerUserLocally(finalName, finalEmail);
                }
            }
        }.execute();
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setBackground(new Color(15, 23, 42));
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SLATE),
                new EmptyBorder(10, 20, 14, 20)
        ));

        statusBannerLabel = new JLabel("Ready. Both Player 1 (" + p1Username + ") and Player 2 (" + p2Username + ") are configured.");
        statusBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBannerLabel.setForeground(TEXT_LIGHT);
        statusBannerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Center Action: Enter Arena
        JPanel launchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        launchPanel.setOpaque(false);

        launchBothPlayersBtn = new JButton("🥊 ENTER CLASH OF BYTES ARENA WITH BOTH PLAYERS");
        launchBothPlayersBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        launchBothPlayersBtn.setBackground(new Color(185, 28, 28));
        launchBothPlayersBtn.setForeground(Color.WHITE);
        launchBothPlayersBtn.setFocusPainted(false);
        launchBothPlayersBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        launchBothPlayersBtn.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_GOLD, 2),
                new EmptyBorder(10, 24, 10, 24)
        ));
        launchBothPlayersBtn.addActionListener(this);

        launchPanel.add(launchBothPlayersBtn);

        // Bottom Tools Bar: Demo Mode & Supabase Settings
        JPanel bottomBar = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomBar.setOpaque(false);

        demoModeBtn = new JButton("🎮 Play Demo / Offline Mode (2-Player)");
        demoModeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        demoModeBtn.setBackground(new Color(30, 41, 59));
        demoModeBtn.setForeground(ACCENT_GREEN);
        demoModeBtn.setBorder(new LineBorder(ACCENT_GREEN, 1));
        demoModeBtn.setFocusPainted(false);
        demoModeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        demoModeBtn.addActionListener(this);

        configSettingsBtn = new JButton("⚙ Supabase Setup & Config");
        configSettingsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        configSettingsBtn.setBackground(new Color(30, 41, 59));
        configSettingsBtn.setForeground(ACCENT_GOLD);
        configSettingsBtn.setBorder(new LineBorder(ACCENT_GOLD, 1));
        configSettingsBtn.setFocusPainted(false);
        configSettingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configSettingsBtn.addActionListener(this);

        bottomBar.add(demoModeBtn);
        bottomBar.add(configSettingsBtn);

        footer.add(statusBannerLabel, BorderLayout.NORTH);
        footer.add(launchPanel, BorderLayout.CENTER);
        footer.add(bottomBar, BorderLayout.SOUTH);
        return footer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == launchBothPlayersBtn) {
            launchArenaWithBothPlayers();
        } else if (src == demoModeBtn) {
            p1Username = "Guest_P1";
            p2Username = "Guest_P2";
            launchArenaWithBothPlayers();
        } else if (src == configSettingsBtn) {
            showConfigSettingsDialog();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    }

    private void launchArenaWithBothPlayers() {
        if (p1NameField != null && !p1NameField.getText().trim().isBlank()) {
            p1Username = p1NameField.getText().trim();
        }
        if (p2NameField != null && !p2NameField.getText().trim().isBlank()) {
            p2Username = p2NameField.getText().trim();
        }
        if (p1EmailField != null && !p1EmailField.getText().trim().isBlank()) {
            p1Email = p1EmailField.getText().trim();
        }
        if (p2EmailField != null && !p2EmailField.getText().trim().isBlank()) {
            p2Email = p2EmailField.getText().trim();
        }

        registerUserLocally(p1Username, p1Email);
        registerUserLocally(p2Username, p2Email);

        dispose();
        SwingUtilities.invokeLater(() -> {
            GameLauncherGUI launcher = new GameLauncherGUI(
                    p1Username, p1UserId, p1Email, p1Token,
                    p2Username, p2UserId, p2Email, p2Token
            );
            launcher.setVisible(true);
        });
    }

    private void registerUserLocally(String uname, String email) {
        if (uname == null || uname.isBlank()) return;
        try {
            DatabaseInitializer.initializeDatabase();
            UserDAO dao = new UserDAO();
            if (dao.getUserByUsername(uname) == null) {
                dao.createUser(new User(
                    uname,
                    (email != null && !email.isBlank()) ? email : (uname.toLowerCase() + "@clashofbytes.local"),
                    "hash_" + uname,
                    "salt_" + uname
                ));
            }
        } catch (Throwable ignored) {}
    }

    private void setLoadingState(boolean loading, String message) {
        if (launchBothPlayersBtn != null) launchBothPlayersBtn.setEnabled(!loading);
        if (demoModeBtn != null) demoModeBtn.setEnabled(!loading);
        if (configSettingsBtn != null) configSettingsBtn.setEnabled(!loading);

        if (loading) {
            statusBannerLabel.setText("⏳ " + message);
            statusBannerLabel.setForeground(ACCENT_GOLD);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void showError(String msg) {
        statusBannerLabel.setText("❌ " + msg);
        statusBannerLabel.setForeground(ERROR_RED);
    }

    private void showSuccess(String msg) {
        statusBannerLabel.setText("✔ " + msg);
        statusBannerLabel.setForeground(ACCENT_GREEN);
    }

    private void refreshConnectionStatus() {
        if (SupabaseConfig.isConfigured()) {
            connectionDotLabel.setText("● Supabase: Configured");
            connectionDotLabel.setForeground(ACCENT_GREEN);
        } else {
            connectionDotLabel.setText("● Supabase: Offline / Placeholder");
            connectionDotLabel.setForeground(ACCENT_GOLD);
        }
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Consolas", Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SLATE, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Consolas", Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SLATE, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return field;
    }

    private void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    private void showConfigSettingsDialog() {
        JDialog dialog = new JDialog(this, "Supabase Database & Auth Configuration", true);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_DARK);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("⚙ Supabase Project Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ACCENT_CYAN);

        JLabel urlLbl = new JLabel("PROJECT URL:");
        urlLbl.setForeground(TEXT_LIGHT);
        JTextField urlField = createStyledTextField(SupabaseConfig.getSupabaseUrl());

        JLabel keyLbl = new JLabel("ANON PUBLIC API KEY:");
        keyLbl.setForeground(TEXT_LIGHT);
        JTextField keyField = createStyledTextField(SupabaseConfig.getSupabaseAnonKey());

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(urlLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(urlField);
        content.add(Box.createVerticalStrut(10));
        content.add(keyLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(keyField);

        JButton saveBtn = new JButton("Save & Test Connection");
        styleActionButton(saveBtn, ACCENT_GREEN, Color.BLACK);
        saveBtn.addActionListener(e -> {
            SupabaseConfig.saveConfiguration(urlField.getText().trim(), keyField.getText().trim());
            SupabaseAuthService.AuthResult ping = authService.testConnection();
            JOptionPane.showMessageDialog(dialog, ping.getMessage(), "Supabase Ping Status",
                    ping.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            refreshConnectionStatus();
            dialog.dispose();
        });

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SupabaseAuthGUI().setVisible(true);
        });
    }
}

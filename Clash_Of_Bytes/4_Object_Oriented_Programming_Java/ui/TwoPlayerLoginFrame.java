package ui;

import model.PlayerSession;
import service.EmailNotConfirmedException;
import service.InvalidCredentialsException;
import service.PlayerSessionManager;
import service.SupabaseAuthService;
import service.SupabaseConfig;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Two-Player Authentication Window displaying side-by-side independent
 * login/registration panels for Player 1 and Player 2 on the SAME screen.
 *
 * Integrated exclusively with Supabase Auth REST API (GoTrue):
 * - Independent authentication sessions (player1Session and player2Session)
 * - Distinct handling for email confirmation vs. invalid credentials
 * - Independent player logout (logging out P1 never affects P2)
 * - Strict prevention of using the same Supabase account for both players
 */
public class TwoPlayerLoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Cyberpunk Theme Colors
    private static final Color BG_DARK = new Color(11, 19, 41);
    private static final Color CARD_BG = new Color(16, 28, 56);
    private static final Color ACCENT_P1 = new Color(0, 229, 255);    // Neon Cyan
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);  // Neon Cyan
    private static final Color ACCENT_P2 = new Color(255, 42, 109);   // Neon Magenta
    private static final Color ACCENT_GREEN = new Color(0, 255, 136); // Success Green
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);  // Gold
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color ERROR_RED = new Color(239, 68, 68);

    private final SupabaseAuthService authService;
    private final PlayerSessionManager sessionManager;

    // Player 1 Controls
    private JTextField p1EmailField;
    private JPasswordField p1PasswordField;
    private JButton p1LoginBtn;
    private JButton p1SignUpBtn;
    private JLabel p1StatusLabel;
    private JButton p1LogoutBtn;

    // Player 2 Controls
    private JTextField p2EmailField;
    private JPasswordField p2PasswordField;
    private JButton p2LoginBtn;
    private JButton p2SignUpBtn;
    private JLabel p2StatusLabel;
    private JButton p2LogoutBtn;

    // Bottom Navigation
    private JButton proceedBtn;
    private JLabel globalStatusLabel;

    public TwoPlayerLoginFrame() {
        super("Clash of Bytes - Supabase Two-Player Authentication Arena");
        this.authService = new SupabaseAuthService();
        this.sessionManager = PlayerSessionManager.getInstance();

        initUI();
        syncUIWithSessions();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 720);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- Top Navigation & Header Banner ---
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_DARK);
        topContainer.setBorder(new EmptyBorder(16, 25, 5, 25));

        // Quick Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navBar.setBackground(BG_DARK);

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
        dashBtn.addActionListener(e -> {
            PlayerSession p1 = sessionManager.getPlayer1Session();
            PlayerSession p2 = sessionManager.getPlayer2Session();
            if (p1 != null) {
                new PlayerDashboardFrame(p1).setVisible(true);
            } else if (p2 != null) {
                new PlayerDashboardFrame(p2).setVisible(true);
            } else {
                new PlayerDashboardFrame(1).setVisible(true);
            }
        });

        navBar.add(homeBtn);
        navBar.add(leadBtn);
        navBar.add(dashBtn);

        topContainer.add(navBar, BorderLayout.NORTH);

        // Header Title
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        headerPanel.setBackground(BG_DARK);
        headerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel titleLabel = new JLabel("CLASH OF BYTES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subtitleLabel = new JLabel("SUPABASE DUAL-PLAYER DESKTOP AUTHENTICATION", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitleLabel.setForeground(ACCENT_CYAN);

        JLabel hostLabel = new JLabel("Supabase Cloud Instance: " + SupabaseConfig.getUrl(), SwingConstants.CENTER);
        hostLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hostLabel.setForeground(TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        headerPanel.add(hostLabel);
        topContainer.add(headerPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // --- Center Side-by-Side Panels ---
        JPanel centerContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        centerContainer.setBackground(BG_DARK);
        centerContainer.setBorder(new EmptyBorder(5, 25, 10, 25));

        JPanel p1Panel = createPlayerCard(1, "PLAYER 1 (LEFT STANCE)", ACCENT_P1);
        JPanel p2Panel = createPlayerCard(2, "PLAYER 2 (RIGHT STANCE)", ACCENT_P2);

        centerContainer.add(p1Panel);
        centerContainer.add(p2Panel);
        add(centerContainer, BorderLayout.CENTER);

        // --- Bottom Action Panel ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(BG_DARK);
        bottomPanel.setBorder(new EmptyBorder(5, 25, 22, 25));

        globalStatusLabel = new JLabel("Awaiting independent Supabase authentication for Player 1 and Player 2...", SwingConstants.CENTER);
        globalStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        globalStatusLabel.setForeground(TEXT_MUTED);

        proceedBtn = new JButton("PROCEED TO CHARACTER SELECTION >>");
        proceedBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        proceedBtn.setForeground(Color.BLACK);
        proceedBtn.setBackground(new Color(100, 116, 139)); // Disabled Gray initially
        proceedBtn.setEnabled(false);
        proceedBtn.setFocusPainted(false);
        proceedBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        proceedBtn.setPreferredSize(new Dimension(380, 46));
        proceedBtn.addActionListener(e -> onProceedToCharacterSelection());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setBackground(BG_DARK);
        btnWrapper.add(proceedBtn);

        bottomPanel.add(globalStatusLabel, BorderLayout.NORTH);
        bottomPanel.add(btnWrapper, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlayerCard(int playerNum, String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 12));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Header Title & Controls Subtitle
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setBackground(CARD_BG);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(accentColor);

        JLabel keysLbl = new JLabel(playerNum == 1 ? "Keys: [A][D] Move â€¢ [W] Jump â€¢ [F] Atk â€¢ [G] Def" : "Keys: [â† ][â†’] Move â€¢ [â†‘] Jump â€¢ [K] Atk â€¢ [L] Def", SwingConstants.CENTER);
        keysLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        keysLbl.setForeground(TEXT_MUTED);

        header.add(titleLbl);
        header.add(keysLbl);
        card.add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        JLabel emailLbl = new JLabel("Registered Supabase Email:");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLbl.setForeground(TEXT_LIGHT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(emailLbl, gbc);

        JTextField emailField = new JTextField(15);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setBackground(new Color(20, 32, 60));
        emailField.setForeground(TEXT_LIGHT);
        emailField.setCaretColor(TEXT_LIGHT);
        emailField.setBorder(new CompoundBorder(new LineBorder(new Color(71, 85, 105), 1), new EmptyBorder(6, 8, 6, 8)));
        gbc.gridy = 1;
        form.add(emailField, gbc);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLbl.setForeground(TEXT_LIGHT);
        gbc.gridy = 2;
        form.add(passLbl, gbc);

        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passField.setBackground(new Color(20, 32, 60));
        passField.setForeground(TEXT_LIGHT);
        passField.setCaretColor(TEXT_LIGHT);
        passField.setBorder(new CompoundBorder(new LineBorder(new Color(71, 85, 105), 1), new EmptyBorder(6, 8, 6, 8)));
        gbc.gridy = 3;
        form.add(passField, gbc);

        // Quick Autofill Test Account Button
        JButton autofillBtn = new JButton(playerNum == 1 ? ">> Quick Fill Test P1" : ">> Quick Fill Test P2");
        autofillBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        autofillBtn.setBackground(new Color(30, 41, 59));
        autofillBtn.setForeground(accentColor);
        autofillBtn.setFocusPainted(false);
        autofillBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        autofillBtn.addActionListener(e -> {
            if (playerNum == 1) {
                emailField.setText("test_player_swing_1@gmail.com");
                passField.setText("Password@123");
            } else {
                emailField.setText("test_player_swing_2@gmail.com");
                passField.setText("Password@123");
            }
        });
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 4, 10, 4);
        form.add(autofillBtn, gbc);

        // Buttons: Login and Sign Up
        JButton loginBtn = new JButton("LOG IN");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(accentColor);
        loginBtn.setForeground(playerNum == 1 ? Color.BLACK : Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(120, 36));

        JButton signUpBtn = new JButton("SIGN UP");
        signUpBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signUpBtn.setBackground(new Color(51, 65, 85));
        signUpBtn.setForeground(TEXT_LIGHT);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpBtn.setPreferredSize(new Dimension(120, 36));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(CARD_BG);
        btnRow.add(loginBtn);
        btnRow.add(signUpBtn);
        gbc.gridy = 5;
        gbc.insets = new Insets(8, 4, 8, 4);
        form.add(btnRow, gbc);

        // Status Banner & Logout
        JLabel statusLbl = new JLabel("[NOT LOGGED IN]", SwingConstants.CENTER);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setForeground(ERROR_RED);
        gbc.gridy = 6;
        form.add(statusLbl, gbc);

        JButton logoutBtn = new JButton("Logout Account");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoutBtn.setBackground(new Color(71, 85, 105));
        logoutBtn.setForeground(TEXT_LIGHT);
        logoutBtn.setVisible(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        form.add(logoutBtn, gbc);

        card.add(form, BorderLayout.CENTER);

        // Wire references and listeners
        if (playerNum == 1) {
            p1EmailField = emailField;
            p1PasswordField = passField;
            p1LoginBtn = loginBtn;
            p1SignUpBtn = signUpBtn;
            p1StatusLabel = statusLbl;
            p1LogoutBtn = logoutBtn;

            p1LoginBtn.addActionListener(e -> handleLogin(1));
            p1SignUpBtn.addActionListener(e -> showSignUpDialog(1));
            p1LogoutBtn.addActionListener(e -> handleLogout(1));
        } else {
            p2EmailField = emailField;
            p2PasswordField = passField;
            p2LoginBtn = loginBtn;
            p2SignUpBtn = signUpBtn;
            p2StatusLabel = statusLbl;
            p2LogoutBtn = logoutBtn;

            p2LoginBtn.addActionListener(e -> handleLogin(2));
            p2SignUpBtn.addActionListener(e -> showSignUpDialog(2));
            p2LogoutBtn.addActionListener(e -> handleLogout(2));
        }

        return card;
    }

    private void handleLogin(int playerNum) {
        String email = (playerNum == 1 ? p1EmailField : p2EmailField).getText().trim();
        String password = new String((playerNum == 1 ? p1PasswordField : p2PasswordField).getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Player " + playerNum + ": Please enter both email address and password.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Player " + playerNum + ": Supabase Auth requires a valid email address (e.g. player@example.com).");
            return;
        }

        JButton targetBtn = (playerNum == 1) ? p1LoginBtn : p2LoginBtn;
        targetBtn.setEnabled(false);
        targetBtn.setText("Checking...");

        new SwingWorker<PlayerSession, Void>() {
            @Override
            protected PlayerSession doInBackground() throws Exception {
                return authService.authenticateUser(email, password);
            }

            @Override
            protected void done() {
                targetBtn.setEnabled(true);
                targetBtn.setText("LOG IN");
                try {
                    PlayerSession session = get();

                    // Prevent same account for both players
                    if (playerNum == 1) {
                        sessionManager.setPlayer1Session(session);
                        p1StatusLabel.setText("âœ“ READY: " + session.getUsername());
                        p1StatusLabel.setForeground(ACCENT_GREEN);
                        p1EmailField.setEnabled(false);
                        p1PasswordField.setEnabled(false);
                        p1LoginBtn.setVisible(false);
                        p1SignUpBtn.setVisible(false);
                        p1LogoutBtn.setVisible(true);
                    } else {
                        sessionManager.setPlayer2Session(session);
                        p2StatusLabel.setText("âœ“ READY: " + session.getUsername());
                        p2StatusLabel.setForeground(ACCENT_GREEN);
                        p2EmailField.setEnabled(false);
                        p2PasswordField.setEnabled(false);
                        p2LoginBtn.setVisible(false);
                        p2SignUpBtn.setVisible(false);
                        p2LogoutBtn.setVisible(true);
                    }

                    checkBothAuthenticated();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof EmailNotConfirmedException) {
                        JOptionPane.showMessageDialog(TwoPlayerLoginFrame.this,
                                "<html><div style='width:320px;'><strong>Email Confirmation Required:</strong><br><br>"
                                        + "Your Supabase Auth account has not been confirmed yet.<br><br>"
                                        + "Please check your inbox for the confirmation link sent to <code>" + email + "</code> "
                                        + "and verify your email before logging in.</div></html>",
                                "Player " + playerNum + " - Email Not Confirmed", JOptionPane.WARNING_MESSAGE);
                    } else if (cause instanceof InvalidCredentialsException) {
                        JOptionPane.showMessageDialog(TwoPlayerLoginFrame.this,
                                "<html><div style='width:300px;'><strong>Invalid Login Credentials:</strong><br><br>"
                                        + "The email or password entered is incorrect.<br>"
                                        + "Please verify your registered Supabase credentials.</div></html>",
                                "Player " + playerNum + " - Authentication Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        showError("Player " + playerNum + " Error: " + cause.getMessage());
                    }
                }
            }
        }.execute();
    }

    private void handleLogout(int playerNum) {
        if (playerNum == 1) {
            PlayerSession p1 = sessionManager.getPlayer1Session();
            if (p1 != null) authService.logoutUser(p1);
            sessionManager.clearPlayer1Session();

            p1EmailField.setEnabled(true);
            p1PasswordField.setEnabled(true);
            p1PasswordField.setText("");
            p1LoginBtn.setVisible(true);
            p1SignUpBtn.setVisible(true);
            p1LogoutBtn.setVisible(false);
            p1StatusLabel.setText("[NOT LOGGED IN]");
            p1StatusLabel.setForeground(ERROR_RED);
        } else {
            PlayerSession p2 = sessionManager.getPlayer2Session();
            if (p2 != null) authService.logoutUser(p2);
            sessionManager.clearPlayer2Session();

            p2EmailField.setEnabled(true);
            p2PasswordField.setEnabled(true);
            p2PasswordField.setText("");
            p2LoginBtn.setVisible(true);
            p2SignUpBtn.setVisible(true);
            p2LogoutBtn.setVisible(false);
            p2StatusLabel.setText("[NOT LOGGED IN]");
            p2StatusLabel.setForeground(ERROR_RED);
        }
        checkBothAuthenticated();
    }

    private void showSignUpDialog(int playerNum) {
        JDialog dialog = new JDialog(this, "Player " + playerNum + " - Supabase Registration", true);
        dialog.setSize(460, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        dialog.setLayout(new BorderLayout(10, 15));

        JPanel form = new JPanel(new GridLayout(6, 1, 5, 8));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel uLbl = new JLabel("Choose Username (3-20 characters):");
        uLbl.setForeground(TEXT_LIGHT);
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField uField = new JTextField();
        uField.setBackground(new Color(8, 13, 26));
        uField.setForeground(TEXT_LIGHT);
        uField.setCaretColor(TEXT_LIGHT);
        uField.setBorder(new CompoundBorder(new LineBorder(new Color(51, 65, 85), 1), new EmptyBorder(4, 6, 4, 6)));

        JLabel eLbl = new JLabel("Registered Email Address (e.g. you@gmail.com):");
        eLbl.setForeground(TEXT_LIGHT);
        eLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField eField = new JTextField();
        eField.setBackground(new Color(8, 13, 26));
        eField.setForeground(TEXT_LIGHT);
        eField.setCaretColor(TEXT_LIGHT);
        eField.setBorder(new CompoundBorder(new LineBorder(new Color(51, 65, 85), 1), new EmptyBorder(4, 6, 4, 6)));

        JLabel pLbl = new JLabel("Password (min 6 characters):");
        pLbl.setForeground(TEXT_LIGHT);
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JPasswordField pField = new JPasswordField();
        pField.setBackground(new Color(8, 13, 26));
        pField.setForeground(TEXT_LIGHT);
        pField.setCaretColor(TEXT_LIGHT);
        pField.setBorder(new CompoundBorder(new LineBorder(new Color(51, 65, 85), 1), new EmptyBorder(4, 6, 4, 6)));

        form.add(uLbl); form.add(uField);
        form.add(eLbl); form.add(eField);
        form.add(pLbl); form.add(pField);
        dialog.add(form, BorderLayout.CENTER);

        JButton registerBtn = new JButton("CREATE SUPABASE ACCOUNT");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setBackground(playerNum == 1 ? ACCENT_P1 : ACCENT_P2);
        registerBtn.setForeground(playerNum == 1 ? Color.BLACK : Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(220, 36));

        registerBtn.addActionListener(e -> {
            String u = uField.getText().trim();
            String em = eField.getText().trim();
            String pass = new String(pField.getPassword());

            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            new SwingWorker<SupabaseAuthService.AuthResult, Void>() {
                @Override
                protected SupabaseAuthService.AuthResult doInBackground() throws Exception {
                    return authService.registerUser(u, em, pass);
                }

                @Override
                protected void done() {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("CREATE SUPABASE ACCOUNT");
                    try {
                        SupabaseAuthService.AuthResult res = get();
                        dialog.dispose();

                        if (res.emailConfirmationRequired) {
                            JOptionPane.showMessageDialog(TwoPlayerLoginFrame.this,
                                    "<html><div style='width:340px;'><strong>Registration Initiated!</strong><br><br>"
                                            + "A confirmation link has been sent to:<br><code>" + res.email + "</code><br><br>"
                                            + "Please open your email inbox, click the verification link, and return here to log in.</div></html>",
                                    "Email Verification Dispatched", JOptionPane.INFORMATION_MESSAGE);

                            if (playerNum == 1) {
                                p1EmailField.setText(res.email);
                                p1PasswordField.setText(pass);
                            } else {
                                p2EmailField.setText(res.email);
                                p2PasswordField.setText(pass);
                            }
                        } else if (res.session != null) {
                            JOptionPane.showMessageDialog(TwoPlayerLoginFrame.this,
                                    "Account registered and activated for '" + res.username + "'!\nAuto-logging in...",
                                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);

                            if (playerNum == 1) {
                                sessionManager.setPlayer1Session(res.session);
                            } else {
                                sessionManager.setPlayer2Session(res.session);
                            }
                            syncUIWithSessions();
                        }
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(dialog,
                                cause.getMessage(),
                                "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(CARD_BG);
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 20));
        btnPanel.add(registerBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void syncUIWithSessions() {
        PlayerSession p1 = sessionManager.getPlayer1Session();
        if (p1 != null) {
            p1StatusLabel.setText("âœ“ READY: " + p1.getUsername());
            p1StatusLabel.setForeground(ACCENT_GREEN);
            p1EmailField.setText(p1.getEmail());
            p1EmailField.setEnabled(false);
            p1PasswordField.setEnabled(false);
            p1LoginBtn.setVisible(false);
            p1SignUpBtn.setVisible(false);
            p1LogoutBtn.setVisible(true);
        }

        PlayerSession p2 = sessionManager.getPlayer2Session();
        if (p2 != null) {
            p2StatusLabel.setText("âœ“ READY: " + p2.getUsername());
            p2StatusLabel.setForeground(ACCENT_GREEN);
            p2EmailField.setText(p2.getEmail());
            p2EmailField.setEnabled(false);
            p2PasswordField.setEnabled(false);
            p2LoginBtn.setVisible(false);
            p2SignUpBtn.setVisible(false);
            p2LogoutBtn.setVisible(true);
        }

        checkBothAuthenticated();
    }

    private void checkBothAuthenticated() {
        PlayerSession p1 = sessionManager.getPlayer1Session();
        PlayerSession p2 = sessionManager.getPlayer2Session();

        if (p1 != null && p2 != null) {
            proceedBtn.setEnabled(true);
            proceedBtn.setBackground(ACCENT_GREEN);
            proceedBtn.setForeground(Color.BLACK);
            globalStatusLabel.setText("âœ“ READY! Both players authenticated via Supabase. Proceed to Character Selection.");
            globalStatusLabel.setForeground(ACCENT_GREEN);
        } else {
            proceedBtn.setEnabled(false);
            proceedBtn.setBackground(new Color(100, 116, 139));
            proceedBtn.setForeground(Color.BLACK);
            if (p1 != null) {
                globalStatusLabel.setText("Player 1 is ready (" + p1.getUsername() + "). Waiting for Player 2...");
                globalStatusLabel.setForeground(ACCENT_P1);
            } else if (p2 != null) {
                globalStatusLabel.setText("Player 2 is ready (" + p2.getUsername() + "). Waiting for Player 1...");
                globalStatusLabel.setForeground(ACCENT_P2);
            } else {
                globalStatusLabel.setText("Awaiting independent Supabase authentication for Player 1 and Player 2...");
                globalStatusLabel.setForeground(TEXT_MUTED);
            }
        }
    }

    private void onProceedToCharacterSelection() {
        PlayerSession p1 = sessionManager.getPlayer1Session();
        PlayerSession p2 = sessionManager.getPlayer2Session();

        if (p1 == null || p2 == null) {
            showError("Both players must be authenticated before continuing.");
            return;
        }

        CharacterSelectionFrame charFrame = new CharacterSelectionFrame(p1, p2);
        charFrame.setVisible(true);
        this.dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Authentication Alert", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TwoPlayerLoginFrame frame = new TwoPlayerLoginFrame();
            frame.setVisible(true);
        });
    }
}

package ui;

import database.DatabaseInitializer;
import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Two-Player Authentication Window displaying side-by-side independent
 * login/registration panels for Player 1 and Player 2 on the SAME screen.
 */
public class TwoPlayerLoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Cyberpunk Theme Colors
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color ACCENT_P1 = new Color(0, 229, 255);    // Neon Cyan
    private static final Color ACCENT_P2 = new Color(255, 42, 109);   // Neon Magenta
    private static final Color ACCENT_GREEN = new Color(0, 255, 136); // Success Green
    private static final Color TEXT_LIGHT = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color ERROR_RED = new Color(239, 68, 68);

    private final AuthService authService;

    // Player 1 State & Controls
    private User authenticatedP1 = null;
    private JTextField p1IdentifierField;
    private JPasswordField p1PasswordField;
    private JButton p1LoginBtn;
    private JButton p1SignUpBtn;
    private JLabel p1StatusLabel;
    private JButton p1LogoutBtn;

    // Player 2 State & Controls
    private User authenticatedP2 = null;
    private JTextField p2IdentifierField;
    private JPasswordField p2PasswordField;
    private JButton p2LoginBtn;
    private JButton p2SignUpBtn;
    private JLabel p2StatusLabel;
    private JButton p2LogoutBtn;

    // Bottom Navigation
    private JButton proceedBtn;
    private JLabel globalStatusLabel;

    public TwoPlayerLoginFrame() {
        super("Clash of Bytes - Two-Player Dual Authentication");
        this.authService = new AuthService();

        // Initialize Database Schema in Background
        initDatabase();

        initUI();
    }

    private void initDatabase() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatabaseInitializer.initializeDatabase();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    showError("Database connection warning: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setMinimumSize(new Dimension(880, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- Header Banner ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_DARK);
        headerPanel.setBorder(new EmptyBorder(25, 25, 10, 25));

        JLabel titleLabel = new JLabel("CLASH OF BYTES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.PLAIN, 38));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subtitleLabel = new JLabel("DUAL-PLAYER LOCAL ARENA AUTHENTICATION", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitleLabel.setForeground(new Color(0, 229, 255));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Side-by-Side Panels ---
        JPanel centerContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        centerContainer.setBackground(BG_DARK);
        centerContainer.setBorder(new EmptyBorder(10, 25, 15, 25));

        JPanel p1Panel = createPlayerCard(1, "PLAYER 1 (LEFT STANCE)", ACCENT_P1);
        JPanel p2Panel = createPlayerCard(2, "PLAYER 2 (RIGHT STANCE)", ACCENT_P2);

        centerContainer.add(p1Panel);
        centerContainer.add(p2Panel);
        add(centerContainer, BorderLayout.CENTER);

        // --- Bottom Action Panel ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(BG_DARK);
        bottomPanel.setBorder(new EmptyBorder(10, 25, 25, 25));

        globalStatusLabel = new JLabel("Awaiting authentication for both Player 1 and Player 2...", SwingConstants.CENTER);
        globalStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        globalStatusLabel.setForeground(TEXT_MUTED);

        proceedBtn = new JButton("PROCEED TO CHARACTER SELECTION >>");
        proceedBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        proceedBtn.setForeground(Color.BLACK);
        proceedBtn.setBackground(new Color(100, 116, 139)); // Disabled Gray initially
        proceedBtn.setEnabled(false);
        proceedBtn.setFocusPainted(false);
        proceedBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        proceedBtn.setPreferredSize(new Dimension(360, 48));
        proceedBtn.addActionListener(e -> onProceedToCharacterSelection());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setBackground(BG_DARK);
        btnWrapper.add(proceedBtn);

        bottomPanel.add(globalStatusLabel, BorderLayout.NORTH);
        bottomPanel.add(btnWrapper, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlayerCard(int playerNum, String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Header Title
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI Black", Font.BOLD, 17));
        titleLbl.setForeground(accentColor);
        card.add(titleLbl, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JLabel userLbl = new JLabel("Username / Email:");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLbl.setForeground(TEXT_LIGHT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(userLbl, gbc);

        JTextField idField = new JTextField(15);
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idField.setBackground(new Color(15, 23, 42));
        idField.setForeground(TEXT_LIGHT);
        idField.setCaretColor(TEXT_LIGHT);
        idField.setBorder(new CompoundBorder(new LineBorder(new Color(71, 85, 105), 1), new EmptyBorder(6, 8, 6, 8)));
        gbc.gridy = 1;
        form.add(idField, gbc);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLbl.setForeground(TEXT_LIGHT);
        gbc.gridy = 2;
        form.add(passLbl, gbc);

        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setBackground(new Color(15, 23, 42));
        passField.setForeground(TEXT_LIGHT);
        passField.setCaretColor(TEXT_LIGHT);
        passField.setBorder(new CompoundBorder(new LineBorder(new Color(71, 85, 105), 1), new EmptyBorder(6, 8, 6, 8)));
        gbc.gridy = 3;
        form.add(passField, gbc);

        // Buttons: Login and Sign Up
        JButton loginBtn = new JButton("LOG IN");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(accentColor);
        loginBtn.setForeground(playerNum == 1 ? Color.BLACK : Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton signUpBtn = new JButton("SIGN UP");
        signUpBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUpBtn.setBackground(new Color(51, 65, 85));
        signUpBtn.setForeground(TEXT_LIGHT);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(CARD_BG);
        btnRow.add(loginBtn);
        btnRow.add(signUpBtn);
        gbc.gridy = 4;
        gbc.insets = new Insets(14, 4, 8, 4);
        form.add(btnRow, gbc);

        // Status Banner & Logout
        JLabel statusLbl = new JLabel("[NOT LOGGED IN]", SwingConstants.CENTER);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLbl.setForeground(ERROR_RED);
        gbc.gridy = 5;
        form.add(statusLbl, gbc);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoutBtn.setBackground(new Color(71, 85, 105));
        logoutBtn.setForeground(TEXT_LIGHT);
        logoutBtn.setVisible(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        form.add(logoutBtn, gbc);

        card.add(form, BorderLayout.CENTER);

        // Wire references and listeners
        if (playerNum == 1) {
            p1IdentifierField = idField;
            p1PasswordField = passField;
            p1LoginBtn = loginBtn;
            p1SignUpBtn = signUpBtn;
            p1StatusLabel = statusLbl;
            p1LogoutBtn = logoutBtn;

            p1LoginBtn.addActionListener(e -> handleLogin(1));
            p1SignUpBtn.addActionListener(e -> showSignUpDialog(1));
            p1LogoutBtn.addActionListener(e -> handleLogout(1));
        } else {
            p2IdentifierField = idField;
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
        String identifier = (playerNum == 1 ? p1IdentifierField : p2IdentifierField).getText().trim();
        String password = new String((playerNum == 1 ? p1PasswordField : p2PasswordField).getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Player " + playerNum + ": Please fill in both username/email and password.");
            return;
        }

        JButton targetBtn = (playerNum == 1) ? p1LoginBtn : p2LoginBtn;
        targetBtn.setEnabled(false);
        targetBtn.setText("Checking...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.authenticateUser(identifier, password);
            }

            @Override
            protected void done() {
                targetBtn.setEnabled(true);
                targetBtn.setText("LOG IN");
                try {
                    User user = get();
                    // Prevent same account for both players
                    if (playerNum == 1 && authenticatedP2 != null && authenticatedP2.getUserId() == user.getUserId()) {
                        showError("Player 1: Account '" + user.getUsername() + "' is already authenticated as Player 2! Two distinct accounts are required.");
                        return;
                    }
                    if (playerNum == 2 && authenticatedP1 != null && authenticatedP1.getUserId() == user.getUserId()) {
                        showError("Player 2: Account '" + user.getUsername() + "' is already authenticated as Player 1! Two distinct accounts are required.");
                        return;
                    }

                    if (playerNum == 1) {
                        authenticatedP1 = user;
                        p1StatusLabel.setText("LOGGED IN: " + user.getUsername());
                        p1StatusLabel.setForeground(ACCENT_GREEN);
                        p1IdentifierField.setEnabled(false);
                        p1PasswordField.setEnabled(false);
                        p1LoginBtn.setVisible(false);
                        p1SignUpBtn.setVisible(false);
                        p1LogoutBtn.setVisible(true);
                    } else {
                        authenticatedP2 = user;
                        p2StatusLabel.setText("LOGGED IN: " + user.getUsername());
                        p2StatusLabel.setForeground(ACCENT_GREEN);
                        p2IdentifierField.setEnabled(false);
                        p2PasswordField.setEnabled(false);
                        p2LoginBtn.setVisible(false);
                        p2SignUpBtn.setVisible(false);
                        p2LogoutBtn.setVisible(true);
                    }

                    checkBothAuthenticated();
                } catch (Exception ex) {
                    showError("Player " + playerNum + " Error: " + getCleanExceptionMessage(ex));
                }
            }
        }.execute();
    }

    private void handleLogout(int playerNum) {
        if (playerNum == 1) {
            authenticatedP1 = null;
            p1IdentifierField.setEnabled(true);
            p1PasswordField.setEnabled(true);
            p1PasswordField.setText("");
            p1LoginBtn.setVisible(true);
            p1SignUpBtn.setVisible(true);
            p1LogoutBtn.setVisible(false);
            p1StatusLabel.setText("[NOT LOGGED IN]");
            p1StatusLabel.setForeground(ERROR_RED);
        } else {
            authenticatedP2 = null;
            p2IdentifierField.setEnabled(true);
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
        JDialog dialog = new JDialog(this, "Player " + playerNum + " - Account Registration", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        dialog.setLayout(new BorderLayout(10, 15));

        JPanel form = new JPanel(new GridLayout(6, 1, 5, 8));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel uLbl = new JLabel("Choose Username (3-20 characters):");
        uLbl.setForeground(TEXT_LIGHT);
        JTextField uField = new JTextField();

        JLabel eLbl = new JLabel("Email Address:");
        eLbl.setForeground(TEXT_LIGHT);
        JTextField eField = new JTextField();

        JLabel pLbl = new JLabel("Password (min 6 characters):");
        pLbl.setForeground(TEXT_LIGHT);
        JPasswordField pField = new JPasswordField();

        form.add(uLbl); form.add(uField);
        form.add(eLbl); form.add(eField);
        form.add(pLbl); form.add(pField);
        dialog.add(form, BorderLayout.CENTER);

        JButton registerBtn = new JButton("CREATE ACCOUNT");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setBackground(playerNum == 1 ? ACCENT_P1 : ACCENT_P2);
        registerBtn.setForeground(playerNum == 1 ? Color.BLACK : Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerBtn.addActionListener(e -> {
            String u = uField.getText().trim();
            String em = eField.getText().trim();
            String pass = new String(pField.getPassword());

            new SwingWorker<User, Void>() {
                @Override
                protected User doInBackground() throws Exception {
                    return authService.registerUser(u, em, pass);
                }

                @Override
                protected void done() {
                    try {
                        User user = get();
                        JOptionPane.showMessageDialog(dialog,
                                "Account registered successfully for '" + user.getUsername() + "'!\nYou may now log in.",
                                "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();

                        if (playerNum == 1) {
                            p1IdentifierField.setText(user.getUsername());
                            p1PasswordField.setText(pass);
                            handleLogin(1); // Auto-login on success
                        } else {
                            p2IdentifierField.setText(user.getUsername());
                            p2PasswordField.setText(pass);
                            handleLogin(2); // Auto-login on success
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog,
                                getCleanExceptionMessage(ex),
                                "Registration Failed", JOptionPane.ERROR_MESSAGE);
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

    private void checkBothAuthenticated() {
        if (authenticatedP1 != null && authenticatedP2 != null) {
            proceedBtn.setEnabled(true);
            proceedBtn.setBackground(ACCENT_GREEN);
            proceedBtn.setForeground(Color.BLACK);
            globalStatusLabel.setText("READY! Both players successfully authenticated. Proceed to Character Selection.");
            globalStatusLabel.setForeground(ACCENT_GREEN);
        } else {
            proceedBtn.setEnabled(false);
            proceedBtn.setBackground(new Color(100, 116, 139));
            proceedBtn.setForeground(Color.BLACK);
            if (authenticatedP1 != null) {
                globalStatusLabel.setText("Player 1 is ready (" + authenticatedP1.getUsername() + "). Waiting for Player 2...");
                globalStatusLabel.setForeground(ACCENT_P1);
            } else if (authenticatedP2 != null) {
                globalStatusLabel.setText("Player 2 is ready (" + authenticatedP2.getUsername() + "). Waiting for Player 1...");
                globalStatusLabel.setForeground(ACCENT_P2);
            } else {
                globalStatusLabel.setText("Awaiting authentication for both Player 1 and Player 2...");
                globalStatusLabel.setForeground(TEXT_MUTED);
            }
        }
    }

    private void onProceedToCharacterSelection() {
        if (authenticatedP1 == null || authenticatedP2 == null) {
            showError("Both players must be authenticated before continuing.");
            return;
        }

        // Open Character Selection Window and close login frame
        CharacterSelectionFrame charFrame = new CharacterSelectionFrame(authenticatedP1, authenticatedP2);
        charFrame.setVisible(true);
        this.dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Authentication Alert", JOptionPane.WARNING_MESSAGE);
    }

    private String getCleanExceptionMessage(Throwable t) {
        if (t.getCause() != null) return t.getCause().getMessage();
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TwoPlayerLoginFrame frame = new TwoPlayerLoginFrame();
            frame.setVisible(true);
        });
    }
}

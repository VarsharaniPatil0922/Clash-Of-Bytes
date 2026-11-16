package ui;

import dao.CharacterDAO;
import model.Character;
import model.MatchRound;
import model.User;
import service.MatchService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Two-Player Character Selection Screen with Cyberpunk Visuals,
 * Stat Visualizers, Fighter Archetype inspection, and Match Engine integration.
 */
public class CharacterSelectionFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color ACCENT_P1 = new Color(0, 229, 255);
    private static final Color ACCENT_P2 = new Color(255, 42, 109);
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);
    private static final Color TEXT_LIGHT = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final User player1;
    private final User player2;
    private final CharacterDAO characterDAO;
    private final MatchService matchService;

    private List<Character> availableCharacters = new ArrayList<>();

    // P1 Controls
    private JComboBox<Character> p1Combo;
    private JLabel p1ArchetypeLbl;
    private JProgressBar p1HpBar;
    private JProgressBar p1AtkBar;
    private JProgressBar p1DefBar;
    private JProgressBar p1SpdBar;
    private JTextArea p1DescArea;

    // P2 Controls
    private JComboBox<Character> p2Combo;
    private JLabel p2ArchetypeLbl;
    private JProgressBar p2HpBar;
    private JProgressBar p2AtkBar;
    private JProgressBar p2DefBar;
    private JProgressBar p2SpdBar;
    private JTextArea p2DescArea;

    // Match Config
    private JComboBox<String> roundsCombo;
    private JButton startMatchBtn;

    public CharacterSelectionFrame(User player1, User player2) {
        super("Clash of Bytes - Character Selection Arena");
        this.player1 = player1;
        this.player2 = player2;
        this.characterDAO = new CharacterDAO();
        this.matchService = new MatchService();

        loadCharacters();
        initUI();
    }

    private void loadCharacters() {
        try {
            availableCharacters = characterDAO.getAllCharacters();
        } catch (Exception e) {
            System.err.println("[CharacterSelectionFrame] Failed to load characters from DB: " + e.getMessage());
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 720);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- Header Banner ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel titleLbl = new JLabel("CHARACTER SELECT ARENA", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Impact", Font.PLAIN, 34));
        titleLbl.setForeground(TEXT_LIGHT);

        JLabel matchupLbl = new JLabel(player1.getUsername().toUpperCase() + " (P1)   VS   "
                + player2.getUsername().toUpperCase() + " (P2)", SwingConstants.CENTER);
        matchupLbl.setFont(new Font("Segoe UI Black", Font.BOLD, 15));
        matchupLbl.setForeground(new Color(255, 204, 0)); // Gold

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(matchupLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // --- Center Roster Selection Cards ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        centerPanel.setBackground(BG_DARK);
        centerPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        JPanel p1Card = createCharacterCard(1, player1, ACCENT_P1);
        JPanel p2Card = createCharacterCard(2, player2, ACCENT_P2);

        centerPanel.add(p1Card);
        centerPanel.add(p2Card);
        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Command Bar ---
        JPanel bottomBar = new JPanel(new BorderLayout(15, 10));
        bottomBar.setBackground(BG_DARK);
        bottomBar.setBorder(new EmptyBorder(10, 25, 20, 25));

        // Navigation Toolbar
        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        navRow.setBackground(BG_DARK);

        JButton p1StatsBtn = new JButton("P1 DASHBOARD (" + player1.getUsername() + ")");
        p1StatsBtn.setBackground(new Color(51, 65, 85));
        p1StatsBtn.setForeground(ACCENT_P1);
        p1StatsBtn.setFocusPainted(false);
        p1StatsBtn.addActionListener(e -> new PlayerDashboardFrame(player1.getUserId()).setVisible(true));

        JButton p2StatsBtn = new JButton("P2 DASHBOARD (" + player2.getUsername() + ")");
        p2StatsBtn.setBackground(new Color(51, 65, 85));
        p2StatsBtn.setForeground(ACCENT_P2);
        p2StatsBtn.setFocusPainted(false);
        p2StatsBtn.addActionListener(e -> new PlayerDashboardFrame(player2.getUserId()).setVisible(true));

        JButton switchUsersBtn = new JButton("SWITCH USERS");
        switchUsersBtn.setBackground(new Color(71, 85, 105));
        switchUsersBtn.setForeground(TEXT_LIGHT);
        switchUsersBtn.setFocusPainted(false);
        switchUsersBtn.addActionListener(e -> {
            new TwoPlayerLoginFrame().setVisible(true);
            this.dispose();
        });

        navRow.add(p1StatsBtn);
        navRow.add(p2StatsBtn);
        navRow.add(switchUsersBtn);

        // Start Match Row
        JPanel startRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        startRow.setBackground(BG_DARK);

        JLabel roundsLbl = new JLabel("Match Format:");
        roundsLbl.setForeground(TEXT_LIGHT);
        roundsLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        roundsCombo = new JComboBox<>(new String[]{"Best of 1 Round", "Best of 3 Rounds", "Best of 5 Rounds"});
        roundsCombo.setBackground(CARD_BG);
        roundsCombo.setForeground(TEXT_LIGHT);

        startMatchBtn = new JButton("FIGHT! START REAL MATCH");
        startMatchBtn.setFont(new Font("Impact", Font.PLAIN, 22));
        startMatchBtn.setBackground(ACCENT_GREEN);
        startMatchBtn.setForeground(Color.BLACK);
        startMatchBtn.setPreferredSize(new Dimension(320, 48));
        startMatchBtn.setFocusPainted(false);
        startMatchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startMatchBtn.addActionListener(e -> onStartMatchClicked());

        startRow.add(roundsLbl);
        startRow.add(roundsCombo);
        startRow.add(startMatchBtn);

        bottomBar.add(navRow, BorderLayout.NORTH);
        bottomBar.add(startRow, BorderLayout.SOUTH);
        add(bottomBar, BorderLayout.SOUTH);

        // Update default selection displays
        if (!availableCharacters.isEmpty()) {
            p1Combo.setSelectedIndex(0);
            p2Combo.setSelectedIndex(Math.min(1, availableCharacters.size() - 1));
            updateCardDisplay(1);
            updateCardDisplay(2);
        }
    }

    private JPanel createCharacterCard(int playerNum, User user, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(15, 18, 15, 18)
        ));

        // Player Info Header
        JPanel pHeader = new JPanel(new GridLayout(2, 1));
        pHeader.setBackground(CARD_BG);
        JLabel pNumLbl = new JLabel("PLAYER " + playerNum + ": " + user.getUsername(), SwingConstants.CENTER);
        pNumLbl.setFont(new Font("Segoe UI Black", Font.BOLD, 16));
        pNumLbl.setForeground(accentColor);

        JLabel promptLbl = new JLabel("Choose Your Fighter Archetype:", SwingConstants.CENTER);
        promptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        promptLbl.setForeground(TEXT_MUTED);

        pHeader.add(pNumLbl);
        pHeader.add(promptLbl);
        card.add(pHeader, BorderLayout.NORTH);

        // Form Center
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD_BG);

        JComboBox<Character> combo = new JComboBox<>(availableCharacters.toArray(new Character[0]));
        combo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        combo.setBackground(new Color(15, 23, 42));
        combo.setForeground(TEXT_LIGHT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.addActionListener(e -> updateCardDisplay(playerNum));
        body.add(combo);
        body.add(Box.createVerticalStrut(10));

        JLabel archLbl = new JLabel("Archetype: WarriorFighter");
        archLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        archLbl.setForeground(new Color(0, 229, 255));
        body.add(archLbl);
        body.add(Box.createVerticalStrut(10));

        // Stat Bars
        JProgressBar hpBar = createStatBar("Health: 115 HP", Color.RED, 150);
        JProgressBar atkBar = createStatBar("Attack: 18 ATK", Color.ORANGE, 40);
        JProgressBar defBar = createStatBar("Defense: 6 DEF", Color.CYAN, 15);
        JProgressBar spdBar = createStatBar("Speed: 5.0 SPD", Color.GREEN, 10);

        body.add(hpBar); body.add(Box.createVerticalStrut(5));
        body.add(atkBar); body.add(Box.createVerticalStrut(5));
        body.add(defBar); body.add(Box.createVerticalStrut(5));
        body.add(spdBar); body.add(Box.createVerticalStrut(12));

        JLabel descHeader = new JLabel("Combat Tactics & Description:");
        descHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        descHeader.setForeground(TEXT_LIGHT);
        body.add(descHeader);
        body.add(Box.createVerticalStrut(4));

        JTextArea descArea = new JTextArea(4, 20);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBackground(new Color(15, 23, 42));
        descArea.setForeground(TEXT_LIGHT);
        descArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        body.add(new JScrollPane(descArea));

        card.add(body, BorderLayout.CENTER);

        if (playerNum == 1) {
            p1Combo = combo;
            p1ArchetypeLbl = archLbl;
            p1HpBar = hpBar;
            p1AtkBar = atkBar;
            p1DefBar = defBar;
            p1SpdBar = spdBar;
            p1DescArea = descArea;
        } else {
            p2Combo = combo;
            p2ArchetypeLbl = archLbl;
            p2HpBar = hpBar;
            p2AtkBar = atkBar;
            p2DefBar = defBar;
            p2SpdBar = spdBar;
            p2DescArea = descArea;
        }

        return card;
    }

    private JProgressBar createStatBar(String title, Color barColor, int maxVal) {
        JProgressBar bar = new JProgressBar(0, maxVal);
        bar.setStringPainted(true);
        bar.setString(title);
        bar.setForeground(barColor);
        bar.setBackground(new Color(15, 23, 42));
        bar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bar.setBorder(new LineBorder(new Color(51, 65, 85), 1));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return bar;
    }

    private void updateCardDisplay(int playerNum) {
        JComboBox<Character> combo = (playerNum == 1) ? p1Combo : p2Combo;
        Character selected = (Character) combo.getSelectedItem();
        if (selected == null) return;

        JLabel archLbl = (playerNum == 1) ? p1ArchetypeLbl : p2ArchetypeLbl;
        JProgressBar hpBar = (playerNum == 1) ? p1HpBar : p2HpBar;
        JProgressBar atkBar = (playerNum == 1) ? p1AtkBar : p2AtkBar;
        JProgressBar defBar = (playerNum == 1) ? p1DefBar : p2DefBar;
        JProgressBar spdBar = (playerNum == 1) ? p1SpdBar : p2SpdBar;
        JTextArea descArea = (playerNum == 1) ? p1DescArea : p2DescArea;

        archLbl.setText("Archetype: " + selected.getArchetype());
        hpBar.setValue((int) selected.getBaseHp());
        hpBar.setString("HP: " + selected.getBaseHp());
        atkBar.setValue((int) selected.getBaseAttack());
        atkBar.setString("Attack Power: " + selected.getBaseAttack());
        defBar.setValue((int) selected.getBaseDefense());
        defBar.setString("Shield Defense: " + selected.getBaseDefense());
        spdBar.setValue((int) selected.getSpeed());
        spdBar.setString("Agility Speed: " + selected.getSpeed());
        descArea.setText(selected.getCharacterDescription());
    }

    private void onStartMatchClicked() {
        Character c1 = (Character) p1Combo.getSelectedItem();
        Character c2 = (Character) p2Combo.getSelectedItem();

        if (c1 == null || c2 == null) {
            JOptionPane.showMessageDialog(this, "Both players must select a fighter character.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int targetRounds = 1;
        if (roundsCombo.getSelectedIndex() == 1) targetRounds = 3;
        else if (roundsCombo.getSelectedIndex() == 2) targetRounds = 5;

        startMatchBtn.setEnabled(false);
        startMatchBtn.setText("BATTLE IN PROGRESS...");

        final int numRounds = targetRounds;

        new SwingWorker<Boolean, Void>() {
            private int p1TotalScore = 0;
            private int p2TotalScore = 0;
            private final List<MatchRound> recordedRounds = new ArrayList<>();
            private String matchWinnerName = "DRAW";

            @Override
            protected Boolean doInBackground() throws Exception {
                // Simulate genuine real-time combat calculation using fighter stats and damage formulas
                int p1Wins = 0;
                int p2Wins = 0;

                for (int r = 1; r <= numRounds; r++) {
                    // Base strikes + variability modeled after x86-64 combat formula
                    double p1Dmg = (c1.getBaseAttack() * 1.5 - c2.getBaseDefense() * 0.5) + (Math.random() * 15.0);
                    double p2Dmg = (c2.getBaseAttack() * 1.5 - c1.getBaseDefense() * 0.5) + (Math.random() * 15.0);

                    int rScore1 = Math.max(10, (int)(p1Dmg * 3.5));
                    int rScore2 = Math.max(10, (int)(p2Dmg * 3.5));

                    p1TotalScore += rScore1;
                    p2TotalScore += rScore2;

                    Integer roundWinnerId = null;
                    if (rScore1 > rScore2) {
                        roundWinnerId = player1.getUserId();
                        p1Wins++;
                    } else if (rScore2 > rScore1) {
                        roundWinnerId = player2.getUserId();
                        p2Wins++;
                    }

                    recordedRounds.add(new MatchRound(0, r, roundWinnerId, rScore1, rScore2, "COMPLETED"));
                }

                if (p1TotalScore > p2TotalScore) {
                    matchWinnerName = player1.getUsername();
                } else if (p2TotalScore > p1TotalScore) {
                    matchWinnerName = player2.getUsername();
                }

                // Transactional save to Database
                return matchService.recordMatchResult(
                        player1.getUserId(), player2.getUserId(),
                        c1.getCharacterId(), c2.getCharacterId(),
                        p1TotalScore, p2TotalScore,
                        numRounds, recordedRounds
                );
            }

            @Override
            protected void done() {
                startMatchBtn.setEnabled(true);
                startMatchBtn.setText("FIGHT! START REAL MATCH");
                try {
                    boolean saved = get();
                    if (saved) {
                        String summary = "=========================================\n"
                                + "          MATCH RESULT RECORDED          \n"
                                + "=========================================\n"
                                + "Player 1 (" + player1.getUsername() + " as " + c1.getCharacterName() + "): " + p1TotalScore + " PTS\n"
                                + "Player 2 (" + player2.getUsername() + " as " + c2.getCharacterName() + "): " + p2TotalScore + " PTS\n"
                                + "Total Rounds: " + numRounds + "\n"
                                + "WINNER: " + matchWinnerName.toUpperCase() + "\n"
                                + "=========================================\n"
                                + "Match telemetry successfully saved to database!";

                        int opt = JOptionPane.showOptionDialog(CharacterSelectionFrame.this,
                                summary, "Match Concluded & Saved",
                                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, new String[]{"View Match History", "Continue Here"}, "View Match History");

                        if (opt == 0) {
                            new MatchHistoryFrame(player1.getUserId(), player1.getUsername()).setVisible(true);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CharacterSelectionFrame.this,
                            "Failed to save match to database: " + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}

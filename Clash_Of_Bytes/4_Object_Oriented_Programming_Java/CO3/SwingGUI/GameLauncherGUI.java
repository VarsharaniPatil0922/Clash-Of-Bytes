package CO3.SwingGUI;

import CO2.OOP.BaseFighter;
import CO2.OOP.BerserkerWarrior;
import CO2.OOP.CyberBrawler;
import CO2.OOP.HybridGladiator;
import CO2.OOP.NinjaFighter;
import CO2.OOP.PaladinFighter;
import CO2.OOP.WarriorFighter;
import CO3.Collections.CombatAction;
import CO3.Collections.CombatActionQueue;
import CO3.Collections.FighterRosterRegistry;
import CO3.Collections.MatchHistoryManager;
import CO3.Collections.MatchRecord;
import CO3.Exceptions.ClashOfBytesException;
import CO3.Exceptions.InvalidFighterSelectionException;
import CO3.Exceptions.ResourceNotFoundException;
import CO3.Testing.CO3PracticalTestSuite;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Course Outcome 3 (CO3): GUI Programming using Java Swing, Event Handling,
 * Packages, Collections Framework, and Custom Exception Handling.
 * 
 * Practical: Cyberpunk Game Launcher & Fighter Selection Dashboard (CO3 Enhanced)
 * 
 * Concepts Demonstrated:
 * 1. Top-Level Container : JFrame
 * 2. Layout Managers     : BorderLayout, GridLayout, FlowLayout, BoxLayout
 * 3. Containers & Panels : JPanel, JScrollPane, BorderFactory (TitledBorder, CompoundBorder)
 * 4. Interactive Controls: JComboBox, JRadioButton, ButtonGroup, JProgressBar, JButton, JLabel, JCheckBox, JTable
 * 5. Event Handling      : ActionListener, ItemListener, Lambda Expressions
 * 6. Dialog Modals       : JOptionPane (Information, Warning, Error, and Confirmation dialogs)
 * 7. Collections Binding : LinkedHashMap, ArrayList, ArrayDeque (Queue), LinkedHashSet, HashMap
 * 8. Exception Handling  : ClashOfBytesException hierarchy with full UI error recovery
 * 9. Concurrency & EDT   : All UI mutations on Event Dispatch Thread; async executable launch
 */
public class GameLauncherGUI extends JFrame implements ActionListener, ItemListener {

    private static final long serialVersionUID = 1L;

    // --- Color Palette (Cyberpunk Theme) ---
    private static final Color BG_DARK = new Color(15, 23, 42);          // Dark Slate
    private static final Color CARD_BG = new Color(30, 41, 59);         // Slate Card
    private static final Color ACCENT_CYAN = new Color(0, 229, 255);     // Neon Cyan (P1)
    private static final Color ACCENT_MAGENTA = new Color(255, 42, 109); // Neon Red/Magenta (P2)
    private static final Color ACCENT_GREEN = new Color(0, 255, 136);    // Neon Green
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);     // Gold
    private static final Color TEXT_LIGHT = new Color(241, 245, 249);    // White/Slate
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Muted gray
    private static final Color ERROR_RED = new Color(239, 68, 68);       // Bright Error Red

    // --- Collections & Model Registries ---
    private final transient FighterRosterRegistry rosterRegistry;
    private final transient MatchHistoryManager historyManager;

    // --- GUI Components: Player 1 Card ---
    private JComboBox<String> p1FighterSelector;
    private JLabel p1ArchetypeLabel;
    private JLabel p1InheritanceLabel;
    private JProgressBar p1HealthBar;
    private JProgressBar p1AttackBar;
    private JProgressBar p1DefenseBar;
    private JProgressBar p1SpeedBar;
    private JLabel p1SpecialMoveLabel;

    // --- GUI Components: Player 2 Card ---
    private JComboBox<String> p2FighterSelector;
    private JLabel p2ArchetypeLabel;
    private JLabel p2InheritanceLabel;
    private JProgressBar p2HealthBar;
    private JProgressBar p2AttackBar;
    private JProgressBar p2DefenseBar;
    private JProgressBar p2SpeedBar;
    private JLabel p2SpecialMoveLabel;

    // --- Match Configuration & Validation Controls ---
    private JComboBox<String> matchRoundsCombo;
    private JComboBox<String> roundTimerCombo;
    private JCheckBox disallowMirrorCheckBox;
    private JLabel validationBannerLabel;

    // --- Mode Selection Controls ---
    private JRadioButton tutorialModeRadio;
    private JRadioButton realFightModeRadio;
    private ButtonGroup modeButtonGroup;
    private JLabel modeDescriptionLabel;

    // --- Action Buttons ---
    private JButton launchGameButton;
    private JButton simulateJavaMatchButton;
    private JButton howToPlayButton;
    private JButton syllabusMappingButton;
    private JButton historyButton;
    private JButton rankingsButton;
    private JButton testSuiteButton;
    private JButton exitButton;

    // --- Bottom Status Bar ---
    private JLabel statusBarLabel;

    @SuppressWarnings("this-escape")
    public GameLauncherGUI() {
        super("Clash of Bytes - Cyberpunk Game Launcher & Fighter Select (CO3 Swing GUI)");

        // Initialize collections from central registry
        this.rosterRegistry = FighterRosterRegistry.getDefaultRegistry();
        this.historyManager = MatchHistoryManager.getInstance();

        initializeWindow();
        buildUI();

        // Safe initial stat refresh
        updateFighterStats(1);
        updateFighterStats(2);
        validateMatchSetup();
        updateStatusBar("Ready | Roster: " + rosterRegistry.getFighterCount() + " Archetypes | Matches Recorded: " 
                        + historyManager.getMatchCount() + " | Concurrency: Event Dispatch Thread Active");
    }

    private void initializeWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 780);
        setMinimumSize(new Dimension(960, 720));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));
    }

    private void buildUI() {
        // 1. NORTH: Header Banner Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER: Main Content (Fighter Cards, Match Config, & Mode Selection)
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // Fighter Selector Grid (P1 on Left, P2 on Right)
        JPanel fighterGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        fighterGrid.setOpaque(false);
        fighterGrid.add(createFighterCard(1, ACCENT_CYAN));
        fighterGrid.add(createFighterCard(2, ACCENT_MAGENTA));
        centerPanel.add(fighterGrid, BorderLayout.CENTER);

        // Configuration and Mode Panels stacked in the South of Center
        JPanel configAndModeContainer = new JPanel();
        configAndModeContainer.setLayout(new BoxLayout(configAndModeContainer, BoxLayout.Y_AXIS));
        configAndModeContainer.setOpaque(false);

        configAndModeContainer.add(createMatchConfigPanel());
        configAndModeContainer.add(Box.createVerticalStrut(6));
        configAndModeContainer.add(createModeSelectionPanel());

        centerPanel.add(configAndModeContainer, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // 3. SOUTH: Action Controls & Status Bar
        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setOpaque(false);
        southContainer.add(createFooterPanel(), BorderLayout.NORTH);
        southContainer.add(createStatusBarPanel(), BorderLayout.SOUTH);

        add(southContainer, BorderLayout.SOUTH);

        // Attach fighter selection listeners now that all UI controls are fully constructed
        if (p1FighterSelector != null) {
            p1FighterSelector.addItemListener(this);
        }
        if (p2FighterSelector != null) {
            p2FighterSelector.addItemListener(this);
        }
    }

    // =========================================================================
    // UI BUILDER METHODS (Containers, Layouts, & Custom Components)
    // =========================================================================

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(10, 15, 28));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_CYAN),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel titleLabel = new JLabel("⚡ CLASH OF BYTES : ARENA LAUNCHER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_CYAN);

        JLabel subtitleLabel = new JLabel("Object-Oriented Programming (Java) | CO3: Swing, Collections, Packages & Exceptions");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createFighterCard(int playerNum, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(CARD_BG);

        Border lineBorder = BorderFactory.createLineBorder(accentColor, 1);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            lineBorder,
            " PLAYER " + playerNum + " FIGHTER SELECTION (COLLECTIONS REGISTRY) ",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            accentColor
        );
        card.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Top Section: ComboBox & Archetype
        JPanel topSection = new JPanel(new GridLayout(3, 1, 3, 3));
        topSection.setOpaque(false);

        // Dynamically populated from Collections (FighterRosterRegistry)
        String[] names = rosterRegistry.getFighterNames();
        JComboBox<String> selector = new JComboBox<>(names);
        selector.setBackground(new Color(45, 55, 72));
        selector.setForeground(TEXT_LIGHT);
        selector.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selector.setFocusable(false);

        JLabel archetypeLabel = new JLabel("Role: Vanguard Tank");
        archetypeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        archetypeLabel.setForeground(TEXT_MUTED);

        JLabel inheritanceLabel = new JLabel("Inheritance: Single (BaseFighter -> WarriorFighter)");
        inheritanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        inheritanceLabel.setForeground(ACCENT_GOLD);

        topSection.add(selector);
        topSection.add(archetypeLabel);
        topSection.add(inheritanceLabel);

        // Center Section: Progress Bars for Stats
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 4, 6));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JProgressBar hpBar = createStyledProgressBar(0, 160, ACCENT_GREEN);
        JProgressBar atkBar = createStyledProgressBar(0, 30, ACCENT_MAGENTA);
        JProgressBar defBar = createStyledProgressBar(0, 12, ACCENT_CYAN);
        JProgressBar spdBar = createStyledProgressBar(0, 8, ACCENT_GOLD);

        statsPanel.add(createStatLabel("Health (HP):"));
        statsPanel.add(hpBar);
        statsPanel.add(createStatLabel("Attack (ATK):"));
        statsPanel.add(atkBar);
        statsPanel.add(createStatLabel("Defense (DEF):"));
        statsPanel.add(defBar);
        statsPanel.add(createStatLabel("Agility (SPD):"));
        statsPanel.add(spdBar);

        // Bottom Section: Special Move
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setOpaque(false);
        bottomSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        JLabel specialMoveTitle = new JLabel("SPECIAL MOVE (POLYMORPHISM):");
        specialMoveTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        specialMoveTitle.setForeground(accentColor);

        JLabel specialMoveLabel = new JLabel("Titan Shield Bash");
        specialMoveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        specialMoveLabel.setForeground(TEXT_LIGHT);

        bottomSection.add(specialMoveTitle, BorderLayout.NORTH);
        bottomSection.add(specialMoveLabel, BorderLayout.CENTER);

        card.add(topSection, BorderLayout.NORTH);
        card.add(statsPanel, BorderLayout.CENTER);
        card.add(bottomSection, BorderLayout.SOUTH);

        // Save component references
        if (playerNum == 1) {
            p1FighterSelector = selector;
            p1ArchetypeLabel = archetypeLabel;
            p1InheritanceLabel = inheritanceLabel;
            p1HealthBar = hpBar;
            p1AttackBar = atkBar;
            p1DefenseBar = defBar;
            p1SpeedBar = spdBar;
            p1SpecialMoveLabel = specialMoveLabel;
            selector.setSelectedIndex(0); // Ironclad Warrior
        } else {
            p2FighterSelector = selector;
            p2ArchetypeLabel = archetypeLabel;
            p2InheritanceLabel = inheritanceLabel;
            p2HealthBar = hpBar;
            p2AttackBar = atkBar;
            p2DefenseBar = defBar;
            p2SpeedBar = spdBar;
            p2SpecialMoveLabel = specialMoveLabel;
            selector.setSelectedIndex(1); // Shadow Ninja
        }

        return card;
    }

    private JProgressBar createStyledProgressBar(int min, int max, Color fgColor) {
        JProgressBar bar = new JProgressBar(min, max);
        bar.setValue(min);
        bar.setStringPainted(true);
        bar.setForeground(fgColor);
        bar.setBackground(new Color(20, 26, 40));
        bar.setFont(new Font("Consolas", Font.BOLD, 11));
        bar.setBorder(BorderFactory.createLineBorder(new Color(40, 50, 70), 1));
        return bar;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_LIGHT);
        return label;
    }

    /**
     * Match Configuration Panel: Validates rounds, duration, and mirror-match settings.
     */
    private JPanel createMatchConfigPanel() {
        JPanel configPanel = new JPanel(new BorderLayout(8, 4));
        configPanel.setBackground(CARD_BG);

        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            " MATCH PARAMETERS & REAL-TIME INPUT VALIDATION (CO3 EXCEPTION HANDLING) ",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11),
            ACCENT_CYAN
        );
        configPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        controlsRow.setOpaque(false);

        // Rounds Dropdown
        controlsRow.add(createStatLabel("Rounds:"));
        matchRoundsCombo = new JComboBox<>(new String[]{"1 Round (Sudden Death)", "3 Rounds (Standard Match)", "5 Rounds (Championship)"});
        matchRoundsCombo.setSelectedIndex(1); // Default: 3 Rounds
        matchRoundsCombo.setBackground(new Color(45, 55, 72));
        matchRoundsCombo.setForeground(TEXT_LIGHT);
        matchRoundsCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        matchRoundsCombo.addItemListener(this);
        controlsRow.add(matchRoundsCombo);

        // Timer Dropdown
        controlsRow.add(createStatLabel("Round Timer:"));
        roundTimerCombo = new JComboBox<>(new String[]{"60 Seconds", "90 Seconds", "Infinite (Practice)"});
        roundTimerCombo.setSelectedIndex(0); // Default: 60 Seconds
        roundTimerCombo.setBackground(new Color(45, 55, 72));
        roundTimerCombo.setForeground(TEXT_LIGHT);
        roundTimerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roundTimerCombo.addItemListener(this);
        controlsRow.add(roundTimerCombo);

        // Mirror Match Checkbox
        disallowMirrorCheckBox = new JCheckBox("Enforce Unique Fighters (Disallow Mirror Matches)", true);
        disallowMirrorCheckBox.setBackground(CARD_BG);
        disallowMirrorCheckBox.setForeground(TEXT_LIGHT);
        disallowMirrorCheckBox.setFont(new Font("Segoe UI", Font.BOLD, 11));
        disallowMirrorCheckBox.setFocusable(false);
        disallowMirrorCheckBox.addItemListener(this);
        controlsRow.add(disallowMirrorCheckBox);

        // Real-Time Validation Feedback Banner
        validationBannerLabel = new JLabel("✓ [VALID CONFIGURATION] Ready to Brawl in Cyberpunk Arena!");
        validationBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        validationBannerLabel.setForeground(ACCENT_GREEN);
        validationBannerLabel.setBorder(BorderFactory.createEmptyBorder(3, 4, 0, 0));

        configPanel.add(controlsRow, BorderLayout.CENTER);
        configPanel.add(validationBannerLabel, BorderLayout.SOUTH);
        return configPanel;
    }

    private JPanel createModeSelectionPanel() {
        JPanel modePanel = new JPanel(new BorderLayout(8, 4));
        modePanel.setBackground(CARD_BG);

        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            " GAMEPLAY MODE SELECTION (PROFESSOR'S REQUIREMENT) ",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11),
            ACCENT_GOLD
        );
        modePanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JPanel radioBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        radioBox.setOpaque(false);

        tutorialModeRadio = new JRadioButton("Tutorial Mode (Action Badges, Sparring Regen & 145px Sweet-Spot Cues)", true);
        tutorialModeRadio.setForeground(TEXT_LIGHT);
        tutorialModeRadio.setBackground(CARD_BG);
        tutorialModeRadio.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tutorialModeRadio.setFocusable(false);
        tutorialModeRadio.addActionListener(this);

        realFightModeRadio = new JRadioButton("Real Fight Mode (Authentic Competitive Arcade Match - 0 Indicators)", false);
        realFightModeRadio.setForeground(TEXT_LIGHT);
        realFightModeRadio.setBackground(CARD_BG);
        realFightModeRadio.setFont(new Font("Segoe UI", Font.BOLD, 11));
        realFightModeRadio.setFocusable(false);
        realFightModeRadio.addActionListener(this);

        modeButtonGroup = new ButtonGroup();
        modeButtonGroup.add(tutorialModeRadio);
        modeButtonGroup.add(realFightModeRadio);

        radioBox.add(tutorialModeRadio);
        radioBox.add(realFightModeRadio);

        modeDescriptionLabel = new JLabel("Active: Beginners receive overhead badges for Move, Jump, Punch, Kick, Block & 145px sweet-spot cues.");
        modeDescriptionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        modeDescriptionLabel.setForeground(ACCENT_GREEN);
        modeDescriptionLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));

        modePanel.add(radioBox, BorderLayout.NORTH);
        modePanel.add(modeDescriptionLabel, BorderLayout.SOUTH);
        return modePanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout(10, 0));
        footerPanel.setBackground(new Color(10, 15, 28));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // Left button group: Documentation, Rankings, Match History
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);

        howToPlayButton = createButton("📖 Guidelines", new Color(45, 55, 72), TEXT_LIGHT);
        syllabusMappingButton = createButton("📊 OOP Mapping", new Color(45, 55, 72), TEXT_LIGHT);
        rankingsButton = createButton("⚡ Rankings", new Color(45, 55, 72), ACCENT_GOLD);
        historyButton = createButton("📜 Match History", new Color(45, 55, 72), ACCENT_CYAN);

        leftButtons.add(howToPlayButton);
        leftButtons.add(syllabusMappingButton);
        leftButtons.add(rankingsButton);
        leftButtons.add(historyButton);

        // Right button group: Unit Tests, Java Simulation, C++ Launch, Exit
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);

        testSuiteButton = createButton("🧪 Run CO3 Tests", new Color(88, 28, 135), TEXT_LIGHT);
        simulateJavaMatchButton = createButton("⚔️ Simulate (Queue)", new Color(13, 71, 161), TEXT_LIGHT);
        launchGameButton = createButton("🎮 LAUNCH C++ GAME (60 FPS)", new Color(0, 150, 136), Color.BLACK);
        launchGameButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exitButton = createButton("Exit", new Color(183, 28, 28), TEXT_LIGHT);

        rightButtons.add(testSuiteButton);
        rightButtons.add(simulateJavaMatchButton);
        rightButtons.add(launchGameButton);
        rightButtons.add(exitButton);

        footerPanel.add(leftButtons, BorderLayout.WEST);
        footerPanel.add(rightButtons, BorderLayout.EAST);
        return footerPanel;
    }

    private JPanel createStatusBarPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(5, 8, 18));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)),
            BorderFactory.createEmptyBorder(4, 16, 4, 16)
        ));

        statusBarLabel = new JLabel("STATUS: Ready");
        statusBarLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusBarLabel.setForeground(TEXT_MUTED);

        statusPanel.add(statusBarLabel, BorderLayout.WEST);
        return statusPanel;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg.darker(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);
        return btn;
    }

    private void updateStatusBar(String message) {
        if (statusBarLabel != null) {
            statusBarLabel.setText(message);
        }
    }

    // =========================================================================
    // EVENT HANDLING & DYNAMIC VALIDATION
    // =========================================================================

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (p1FighterSelector == null || p2FighterSelector == null || disallowMirrorCheckBox == null) {
            return;
        }
        if (e.getSource() == disallowMirrorCheckBox || e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == p1FighterSelector) {
                updateFighterStats(1);
            } else if (e.getSource() == p2FighterSelector) {
                updateFighterStats(2);
            }
            validateMatchSetup();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        try {
            if (source == tutorialModeRadio) {
                modeDescriptionLabel.setText("Active: Beginners receive overhead badges for Move, Jump, Punch, Kick, Block & 145px sweet-spot cues.");
                modeDescriptionLabel.setForeground(ACCENT_GREEN);
            } else if (source == realFightModeRadio) {
                modeDescriptionLabel.setText("Active: Authentic competitive fight. All coaching bars and overhead indicators removed (0 indicators).");
                modeDescriptionLabel.setForeground(ACCENT_GOLD);
            } else if (source == launchGameButton) {
                launchCppGameExecutable();
            } else if (source == simulateJavaMatchButton) {
                simulateMatchWithQueue();
            } else if (source == howToPlayButton) {
                showHowToPlayDialog();
            } else if (source == syllabusMappingButton) {
                showSyllabusMappingDialog();
            } else if (source == rankingsButton) {
                showRankingsDialog();
            } else if (source == historyButton) {
                showHistoryDialog();
            } else if (source == testSuiteButton) {
                runCO3TestSuiteFromGUI();
            } else if (source == exitButton) {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit Clash of Bytes Launcher?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        } catch (ClashOfBytesException ex) {
            handleExceptionGracefully(ex);
        } catch (Exception ex) {
            handleExceptionGracefully(new ClashOfBytesException("ERR_UNEXPECTED", ex.getMessage(), ex));
        }
    }

    /**
     * Real-time validation verifying fighter selection pair and match parameters.
     */
    private boolean validateMatchSetup() {
        if (p1FighterSelector == null || p2FighterSelector == null || 
            disallowMirrorCheckBox == null || validationBannerLabel == null ||
            launchGameButton == null || simulateJavaMatchButton == null) {
            return false;
        }

        int p1Index = p1FighterSelector.getSelectedIndex();
        int p2Index = p2FighterSelector.getSelectedIndex();
        if (p1Index < 0 || p2Index < 0) {
            return false;
        }
        boolean disallowMirror = disallowMirrorCheckBox.isSelected();

        try {
            rosterRegistry.validateFighterPair(p1Index, p2Index, disallowMirror);
            validationBannerLabel.setText("✓ [CONFIGURATION VALID] P1 (" + rosterRegistry.getFighter(p1Index).getName() 
                                          + ") vs P2 (" + rosterRegistry.getFighter(p2Index).getName() + ") - Ready to Brawl!");
            validationBannerLabel.setForeground(ACCENT_GREEN);
            launchGameButton.setEnabled(true);
            simulateJavaMatchButton.setEnabled(true);
            return true;
        } catch (InvalidFighterSelectionException ex) {
            validationBannerLabel.setText("⚠️ [VALIDATION WARNING] " + ex.getMessage());
            validationBannerLabel.setForeground(ERROR_RED);
            launchGameButton.setEnabled(false);
            simulateJavaMatchButton.setEnabled(false);
            return false;
        }
    }

    private void updateFighterStats(int playerNum) {
        if (playerNum == 1 && (p1FighterSelector == null || p1ArchetypeLabel == null)) return;
        if (playerNum == 2 && (p2FighterSelector == null || p2ArchetypeLabel == null)) return;
        int index = (playerNum == 1) ? p1FighterSelector.getSelectedIndex() : p2FighterSelector.getSelectedIndex();
        if (index < 0) return;

        try {
            BaseFighter fighter = rosterRegistry.getFighter(index);
            double hp = fighter.getHealth();
            double atk = fighter.getBaseAttack();
            double def = 4.0;
            int spd = 4;
            String archetype = "Balanced Combatant";
            String inheritance = "Single Inheritance (BaseFighter -> " + fighter.getClass().getSimpleName() + ")";
            String specialMove = "Dynamic Strike Art";

            if (fighter instanceof HybridGladiator) {
                HybridGladiator h = (HybridGladiator) fighter;
                def = h.getShieldDefense();
                spd = 5;
                archetype = "Grand Champion (Multilevel + Dual Interface Contracts)";
                inheritance = "Hybrid Inheritance: Extends BerserkerWarrior + Implements ShieldBearer & ElementalAttacker";
                specialMove = "Colosseum Wrath of Bytes (Earthquake Slam)";
            } else if (fighter instanceof BerserkerWarrior) {
                BerserkerWarrior b = (BerserkerWarrior) fighter;
                def = 4.0;
                spd = 4;
                archetype = "Heavy Brawler (Rage Multiplier Boost)";
                inheritance = "Multilevel Inheritance: BaseFighter -> WarriorFighter -> BerserkerWarrior";
                specialMove = "Inferno Greatsword Rend (x" + b.getRageMultiplier() + " Rage Multiplier)";
            } else if (fighter instanceof WarriorFighter) {
                WarriorFighter w = (WarriorFighter) fighter;
                def = w.getShieldDefense();
                spd = 3;
                archetype = "Vanguard Tank (High Defense, Shield Absorption)";
                inheritance = "Single Inheritance: BaseFighter -> WarriorFighter";
                specialMove = "Titan Shield Bash (Bludgeoning Impact)";
            } else if (fighter instanceof NinjaFighter) {
                NinjaFighter n = (NinjaFighter) fighter;
                def = 3.0;
                spd = n.getStealthDuration() * 2;
                archetype = "Infiltrator (High Agility, Swift Combos)";
                inheritance = "Hierarchical Inheritance: BaseFighter -> NinjaFighter";
                specialMove = "Shadow Clone Strike (4 Rapid Shurikens)";
            } else if (fighter instanceof CyberBrawler) {
                CyberBrawler c = (CyberBrawler) fighter;
                def = 5.0;
                spd = Math.min(8, c.getCyberCoreEnergy() * 2);
                archetype = "Cybernetic Striker (Overclocked Combos)";
                inheritance = "Hierarchical Inheritance: BaseFighter -> CyberBrawler";
                specialMove = "Overclocked Plasma Uppercut (Core Energy: " + c.getCyberCoreEnergy() + ")";
            } else if (fighter instanceof PaladinFighter) {
                PaladinFighter p = (PaladinFighter) fighter;
                def = p.getFaithArmor();
                spd = 3;
                archetype = "Holy Defender (Elemental Attacker & Shield Bearer)";
                inheritance = "Multiple Inheritance via Interfaces: implements ShieldBearer, ElementalAttacker";
                specialMove = "Holy Radiance Aegis (Faith Armor: " + String.format("%.1f", p.getFaithArmor()) + ")";
            }

            if (playerNum == 1) {
                p1ArchetypeLabel.setText("Role: " + archetype);
                p1InheritanceLabel.setText("OOP: " + inheritance);
                p1HealthBar.setValue((int) hp);
                p1HealthBar.setString(String.format("%.1f HP", hp));
                p1AttackBar.setValue((int) atk);
                p1AttackBar.setString(String.format("%.1f ATK", atk));
                p1DefenseBar.setValue((int) def);
                p1DefenseBar.setString(String.format("%.1f DEF", def));
                p1SpeedBar.setValue(spd);
                p1SpeedBar.setString(spd + " SPD");
                p1SpecialMoveLabel.setText(specialMove);
            } else {
                p2ArchetypeLabel.setText("Role: " + archetype);
                p2InheritanceLabel.setText("OOP: " + inheritance);
                p2HealthBar.setValue((int) hp);
                p2HealthBar.setString(String.format("%.1f HP", hp));
                p2AttackBar.setValue((int) atk);
                p2AttackBar.setString(String.format("%.1f ATK", atk));
                p2DefenseBar.setValue((int) def);
                p2DefenseBar.setString(String.format("%.1f DEF", def));
                p2SpeedBar.setValue(spd);
                p2SpeedBar.setString(spd + " SPD");
                p2SpecialMoveLabel.setText(specialMove);
            }
        } catch (InvalidFighterSelectionException ex) {
            handleExceptionGracefully(ex);
        }
    }

    /**
     * Robust executable launching: validates executable, enforces validation,
     * and runs asynchronously on a worker thread to prevent freezing the Swing EDT.
     */
    private void launchCppGameExecutable() throws ClashOfBytesException {
        // Enforce fighter selection validation
        if (!validateMatchSetup()) {
            throw new InvalidFighterSelectionException("Cannot launch match with invalid fighter configuration.");
        }

        File exeFile = new File("clash_of_bytes.exe");
        if (!exeFile.exists()) {
            exeFile = new File("..\\clash_of_bytes.exe");
        }
        if (!exeFile.exists()) {
            throw new ResourceNotFoundException("clash_of_bytes.exe", 
                "C++ Arcade Game Binary. Please run build.bat in the project root to compile it.");
        }

        final File targetExe = exeFile;
        final int p1Index = p1FighterSelector.getSelectedIndex();
        final int p2Index = p2FighterSelector.getSelectedIndex();
        final String modeStr = tutorialModeRadio.isSelected() ? "tutorial" : "fight";
        final String p1Name = rosterRegistry.getFighter(p1Index).getName();
        final String p2Name = rosterRegistry.getFighter(p2Index).getName();

        // Concurrency Best Practice: Launch process asynchronously off EDT
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    targetExe.getAbsolutePath(),
                    "--p1", String.valueOf(p1Index),
                    "--p2", String.valueOf(p2Index),
                    "--mode", modeStr
                );
                File parentDir = targetExe.getAbsoluteFile().getParentFile();
                if (parentDir != null) {
                    pb.directory(parentDir);
                }
                pb.start();

                // Safe EDT Callback for UI notification
                SwingUtilities.invokeLater(() -> {
                    updateStatusBar("Launched: " + p1Name + " vs " + p2Name + " (" + modeStr + ") | Process Active");
                    JOptionPane.showMessageDialog(
                        this,
                        "⚡ C++ OpenGL 60 FPS Game Engine launched successfully!\n\n"
                        + "Player 1 : " + p1Name + "\n"
                        + "Player 2 : " + p2Name + "\n"
                        + "Selected Mode: " + (tutorialModeRadio.isSelected() ? "Tutorial Mode" : "Real Fight Mode") + "\n\n"
                        + "Hotkeys:\n"
                        + "  [F4] / [F5] : Cycle P1 / P2 Fighters\n"
                        + "  [F6] / [F7] : Toggle Lighting / Textures (CO6)\n"
                        + "  [M]  / [F3] : Shift Mode on the fly\n"
                        + "  [F9]        : Capture Framebuffer Screenshot",
                        "Arcade Engine Running",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    handleExceptionGracefully(new ClashOfBytesException("ERR_EXECUTION_FAILED", "Failed to launch process: " + ex.getMessage(), ex));
                });
            }
        }, "ArcadeProcessLauncherThread").start();
    }

    /**
     * Enhanced Java Combat Simulation using Queue (FIFO) and Collections.
     */
    private void simulateMatchWithQueue() throws ClashOfBytesException {
        if (!validateMatchSetup()) {
            throw new InvalidFighterSelectionException("Cannot simulate match with an invalid fighter pair.");
        }

        int p1Index = p1FighterSelector.getSelectedIndex();
        int p2Index = p2FighterSelector.getSelectedIndex();

        BaseFighter f1 = rosterRegistry.getFighter(p1Index);
        BaseFighter f2 = rosterRegistry.getFighter(p2Index);

        // Reset health copies for clean simulation
        double f1Hp = f1.getMaxHealth();
        double f2Hp = f2.getMaxHealth();
        double totalDamage = 0.0;

        String modeName = tutorialModeRadio.isSelected() ? "TUTORIAL MODE" : "REAL FIGHT MODE";
        int rounds = (matchRoundsCombo.getSelectedIndex() == 0) ? 1 : (matchRoundsCombo.getSelectedIndex() == 1 ? 3 : 5);

        // 1. Populate CombatActionQueue (FIFO)
        CombatActionQueue actionQueue = new CombatActionQueue();
        actionQueue.enqueueAction(new CombatAction(1, f1.getName(), f2.getName(), CombatAction.ActionType.FAST_PUNCH, f1.getBaseAttack(), 1.0, "Opener"));
        actionQueue.enqueueAction(new CombatAction(2, f2.getName(), f1.getName(), CombatAction.ActionType.HEAVY_KICK, f2.getBaseAttack(), 1.5, "Counter-Attack"));
        actionQueue.enqueueAction(new CombatAction(3, f1.getName(), f2.getName(), CombatAction.ActionType.SPECIAL_ART, f1.getBaseAttack() + 10.0, 1.8, "Polymorphic Ultimate"));

        if (tutorialModeRadio.isSelected()) {
            actionQueue.enqueueAction(new CombatAction(4, f1.getName(), f1.getName(), CombatAction.ActionType.SPARRING_HEAL, 15.0, 1.0, "Tutorial Regen"));
            actionQueue.enqueueAction(new CombatAction(5, f2.getName(), f2.getName(), CombatAction.ActionType.SPARRING_HEAL, 15.0, 1.0, "Tutorial Regen"));
        }

        StringBuilder log = new StringBuilder();
        log.append("=======================================================================\n");
        log.append("  ⚔️ SIMULATED JAVA COMBAT MATCH (COLLECTIONS ACTION QUEUE)\n");
        log.append("  Mode: ").append(modeName).append(" | Rounds: ").append(rounds).append("\n");
        log.append("=======================================================================\n");
        log.append("  Player 1: ").append(f1.getName()).append(" (HP: ").append(f1Hp).append(", ATK: ").append(f1.getBaseAttack()).append(")\n");
        log.append("  Player 2: ").append(f2.getName()).append(" (HP: ").append(f2.getHealth()).append(", ATK: ").append(f2.getBaseAttack()).append(")\n\n");
        log.append("  [FIFO ACTION QUEUE EXECUTION]:\n");

        int turnCount = 0;
        while (!actionQueue.isEmpty()) {
            turnCount++;
            CombatAction action = actionQueue.dequeueNextAction();
            log.append(String.format("  * Turn %d: %s%n", turnCount, action.toString()));

            if (action.getActionType() == CombatAction.ActionType.SPARRING_HEAL) {
                if (action.getAttackerName().equals(f1.getName())) {
                    f1Hp = Math.min(f1.getMaxHealth(), f1Hp + action.getBasePower());
                    log.append(String.format("    -> %s recovers %.1f HP (Current: %.1f)%n", f1.getName(), action.getBasePower(), f1Hp));
                } else {
                    f2Hp = Math.min(f2.getMaxHealth(), f2Hp + action.getBasePower());
                    log.append(String.format("    -> %s recovers %.1f HP (Current: %.1f)%n", f2.getName(), action.getBasePower(), f2Hp));
                }
            } else {
                double dmg = action.calculateRawDamage();
                totalDamage += dmg;
                if (action.getTargetName().equals(f2.getName())) {
                    f2Hp = Math.max(0.0, f2Hp - dmg);
                    log.append(String.format("    -> Dealt %.1f DMG to %s (Remaining HP: %.1f)%n", dmg, f2.getName(), f2Hp));
                } else {
                    f1Hp = Math.max(0.0, f1Hp - dmg);
                    log.append(String.format("    -> Dealt %.1f DMG to %s (Remaining HP: %.1f)%n", dmg, f1.getName(), f1Hp));
                }
            }
        }

        String winner = (f1Hp > f2Hp) ? f1.getName() : ((f2Hp > f1Hp) ? f2.getName() : "DRAW");
        log.append("\n=======================================================================\n");
        log.append("  MATCH RESULT: ").append(winner).append(" WINS!\n");
        log.append("  Final HP -> ").append(f1.getName()).append(": ").append(String.format("%.1f", f1Hp))
           .append(" | ").append(f2.getName()).append(": ").append(String.format("%.1f", f2Hp)).append("\n");
        log.append("  Total Combat Damage Dealt: ").append(String.format("%.1f", totalDamage)).append(" DMG\n");
        log.append("=======================================================================\n");

        // Record in Collections MatchHistoryManager
        MatchRecord record = historyManager.recordMatch(
            f1.getName(), f2.getName(), winner, modeName, rounds, f1Hp, f2Hp, totalDamage, "FIFO Queue Simulation"
        );

        updateStatusBar("Match Recorded (#" + record.getMatchId() + ") | Winner: " + winner 
                        + " | Total Logged: " + historyManager.getMatchCount());

        JTextArea textArea = new JTextArea(log.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(new Color(20, 26, 40));
        textArea.setForeground(ACCENT_GREEN);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(560, 340));

        JOptionPane.showMessageDialog(this, scrollPane, "Java OOP Match Simulation Log (Collections)", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Displays Match History & Win Leaderboard using JTable (List & Map Collections).
     */
    private void showHistoryDialog() {
        List<MatchRecord> records = historyManager.getAllRecords();

        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBackground(BG_DARK);

        // Header statistics
        JLabel countLabel = new JLabel("Total Matches Logged: " + records.size() + " | Stored in: List<MatchRecord> & Map<String, Integer>");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(ACCENT_CYAN);
        countLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        dialogPanel.add(countLabel, BorderLayout.NORTH);

        // JTable for records
        String[] columnNames = {"Match #", "Timestamp", "Player 1", "Player 2", "Winner", "Mode", "Total Damage"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (MatchRecord r : records) {
            model.addRow(new Object[]{
                "#" + r.getMatchId(),
                r.getTimestamp(),
                r.getPlayer1Name(),
                r.getPlayer2Name(),
                r.getWinnerName(),
                r.getGameMode(),
                String.format("%.1f", r.getTotalDamageDealt())
            });
        }

        JTable table = new JTable(model);
        table.setBackground(new Color(25, 35, 55));
        table.setForeground(TEXT_LIGHT);
        table.setGridColor(new Color(50, 65, 90));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(ACCENT_GOLD);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(22);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(720, 260));
        dialogPanel.add(scrollPane, BorderLayout.CENTER);

        // Win Leaderboard from Map<String, Integer>
        Map<String, Integer> leaderboard = historyManager.getWinLeaderboard();
        StringBuilder lbText = new StringBuilder("🏆 WIN LEADERBOARD (Map<String, Integer>):  ");
        if (leaderboard.isEmpty()) {
            lbText.append("No matches completed yet. Simulate or play matches to build leaderboard.");
        } else {
            for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
                lbText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" wins  |  ");
            }
        }
        JLabel lbLabel = new JLabel(lbText.toString());
        lbLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbLabel.setForeground(ACCENT_GREEN);
        dialogPanel.add(lbLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, dialogPanel, "Match History & Analytics (Java Collections)", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Demonstrates Collection sorting using ArrayList and Comparator.
     */
    private void showRankingsDialog() {
        List<BaseFighter> byHp = rosterRegistry.getFightersSortedByHealth();
        List<BaseFighter> byAtk = rosterRegistry.getFightersSortedByAttack();
        Set<String> archetypes = rosterRegistry.getUniqueArchetypes();

        StringBuilder sb = new StringBuilder();
        sb.append("⚡ FIGHTER ROSTER RANKINGS (JAVA COLLECTIONS & COMPARATORS)\n");
        sb.append("=======================================================================\n\n");

        sb.append("1. RANKED BY HEALTH (Highest Tank HP - ArrayList.sort):\n");
        for (int i = 0; i < byHp.size(); i++) {
            sb.append(String.format("   [%d] %-18s : %5.1f HP (Class: %s)%n", 
                      (i + 1), byHp.get(i).getName(), byHp.get(i).getHealth(), byHp.get(i).getClass().getSimpleName()));
        }

        sb.append("\n2. RANKED BY ATTACK POWER (Highest Striker ATK - ArrayList.sort):\n");
        for (int i = 0; i < byAtk.size(); i++) {
            sb.append(String.format("   [%d] %-18s : %5.1f ATK (Class: %s)%n", 
                      (i + 1), byAtk.get(i).getName(), byAtk.get(i).getBaseAttack(), byAtk.get(i).getClass().getSimpleName()));
        }

        sb.append("\n3. UNIQUE ARCHETYPE CLASSIFICATIONS (LinkedHashSet):\n");
        for (String arch : archetypes) {
            sb.append("   * ").append(arch).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(20, 26, 40));
        area.setForeground(ACCENT_GOLD);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(560, 360));
        JOptionPane.showMessageDialog(this, pane, "Fighter Roster Rankings (Java Collections)", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Runs the CO3 test suite directly from the GUI and displays results in a dialog.
     */
    private void runCO3TestSuiteFromGUI() {
        CO3PracticalTestSuite suite = new CO3PracticalTestSuite();
        // Redirect or capture suite run
        suite.runAllTests();

        int passed = suite.getPassedTests();
        int total = suite.getTotalTests();
        double pct = (total > 0) ? (passed * 100.0 / total) : 100.0;

        JOptionPane.showMessageDialog(
            this,
            "🧪 CO3 Practical Test Suite Executed Successfully!\n\n"
            + passed + " of " + total + " Automated Tests Passed (" + String.format("%.1f", pct) + "% Success Rate):\n"
            + "  - Custom Exception Hierarchy (ClashOfBytesException)\n"
            + "  - Fighter Selection Bounds & Name Lookup (InvalidFighterSelectionException)\n"
            + "  - Match Configuration Ranges (InvalidMatchConfigurationException)\n"
            + "  - Combat Action Queue & FIFO Underflow (CombatValidationException)\n"
            + "  - Collections (LinkedHashMap, ArrayList, ArrayDeque, LinkedHashSet, HashMap)\n"
            + "  - Fighter Health Clamping & Boundary Invariants\n"
            + "  - Combat Action Raw Damage Calculation\n"
            + "  - CombatLogger Formatting (StringBuilder & StringBuffer)\n"
            + "  - Post-Exception Recovery & State Continuity\n\n"
            + "Check console output for detailed line-by-line test execution telemetry.",
            "CO3 Practical Test Suite Completed",
            JOptionPane.INFORMATION_MESSAGE
        );
        updateStatusBar("CO3 Test Suite: " + passed + "/" + total + " Tests (" + String.format("%.1f", pct) + "%) Verified!");
    }

    /**
     * Centralized graceful exception handler: prevents uncaught exceptions from
     * terminating the GUI and displays user-friendly recovery alerts.
     */
    private void handleExceptionGracefully(ClashOfBytesException ex) {
        updateStatusBar("ERROR [" + ex.getErrorCode() + "]: " + ex.getMessage());
        JOptionPane.showMessageDialog(
            this,
            ex.getFormattedMessage() + "\n\nPlease correct the setting to proceed.",
            "Validation Notice",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private void showHowToPlayDialog() {
        String msg = "🎮 CLASH OF BYTES : HOW TO PLAY & CONTROLS\n\n"
            + "=== PLAYER 1 (Left Fighter) ===\n"
            + "  Move / Jump  : W / A / S / D\n"
            + "  Fast Punch   : F Key (18 DMG)\n"
            + "  Heavy Kick   : G Key (24 DMG)\n"
            + "  Titan Shield : H Key (Absorbs 70% Damage)\n"
            + "  Special Move : R Key (Titan Shield Bash)\n\n"
            + "=== PLAYER 2 (Right Fighter) ===\n"
            + "  Move / Jump  : Arrow Keys (Up/Left/Down/Right)\n"
            + "  Fast Punch   : J / Num1 (16 DMG)\n"
            + "  Heavy Kick   : K / Num2 (22 DMG)\n"
            + "  Shadow Block : L / Num3 (Absorbs 70% Damage)\n"
            + "  Special Move : U / Num5 (Shadow Clone Strike)\n\n"
            + "=== TWO-MODE SYSTEM ===\n"
            + "  [1] or [T]   : Tutorial Mode (Full movement guides & overhead badges)\n"
            + "  [2] or ENTER : Real Fight Mode (Authentic competitive match, 0 indicators)\n"
            + "  [M] or [F3]  : Shift Mode on the fly during active combat!\n\n"
            + "=== HIT TIMING SWEET SPOT (145 PX) ===\n"
            + "  When distance is within 145px and enemy is vulnerable, strike immediately for critical hits!";

        JTextArea area = new JTextArea(msg);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(20, 26, 40));
        area.setForeground(ACCENT_CYAN);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Game Guidelines & Hit Indicators", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSyllabusMappingDialog() {
        String msg = "📚 JAVA OOP COURSE OUTCOMES (CO) SYLLABUS MAPPING\n\n"
            + "1. CO1 - Java Fundamentals & Strings:\n"
            + "   * CombatAnalytics: Arrays, For-each loops, if-else tier ranking\n"
            + "   * CombatLogger: StringBuilder & StringBuffer high-performance logging\n"
            + "   * HitTimingIndicator: Mathematical range calculations & stance evaluation\n\n"
            + "2. CO2 - Object-Oriented Programming (All 5 Inheritance Models):\n"
            + "   * Single Inheritance: WarriorFighter extends BaseFighter\n"
            + "   * Multilevel Inheritance: BerserkerWarrior extends WarriorFighter extends BaseFighter\n"
            + "   * Hierarchical Inheritance: NinjaFighter & CyberBrawler extend BaseFighter\n"
            + "   * Multiple Inheritance (via Interfaces): PaladinFighter implements ShieldBearer, ElementalAttacker\n"
            + "   * Hybrid Inheritance: HybridGladiator extends BerserkerWarrior + 2 Interfaces\n\n"
            + "3. CO3 - Java Swing GUI, Exceptions, Collections, & Packages:\n"
            + "   * Packages: CO3.Exceptions, CO3.Collections, CO3.SwingGUI, CO3.Testing\n"
            + "   * Custom Exceptions: ClashOfBytesException, InvalidFighterSelectionException,\n"
            + "     InvalidMatchConfigurationException, CombatValidationException, ResourceNotFoundException\n"
            + "   * Collections Framework:\n"
            + "     - LinkedHashMap<String, BaseFighter> : O(1) keyed lookup preserving insertion order\n"
            + "     - List<BaseFighter> (ArrayList)     : Fast indexed access matching dropdown selection\n"
            + "     - Queue<CombatAction> (ArrayDeque)   : FIFO sequential combat action processing\n"
            + "     - Set<String> (LinkedHashSet)       : Tracking unique archetypes\n"
            + "     - Map<String, Integer> (HashMap)     : Win leaderboard analytics\n"
            + "   * Concurrency: Asynchronous background process launch off the Event Dispatch Thread (EDT)";

        JTextArea area = new JTextArea(msg);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(20, 26, 40));
        area.setForeground(ACCENT_GOLD);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Java OOP Practical Syllabus Mapping", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // APPLICATION ENTRY POINT
    // =========================================================================

    public static void main(String[] args) {
        // Run strictly on Event Dispatch Thread (Swing Concurrency Best Practice)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

            GameLauncherGUI launcher = new GameLauncherGUI();
            launcher.setVisible(true);
        });
    }
}

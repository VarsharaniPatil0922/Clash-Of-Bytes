package CO1.JavaFundamentals;

import CO2.OOP.BaseFighter;
import java.util.Scanner;

/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcomes: CO1 & CO2 | Practicals 01, 02 & 03
 * Topic: Interactive User Guidelines, Control Flow Stance Analysis, and Two Gameplay Modes:
 *        [1] Tutorial Mode (Full Movement & Action Guides)
 *        [2] Real Fight Mode (Competitive Tournament Simulation)
 */
public class HitTimingIndicator {

    public enum GameMode {
        TUTORIAL_MODE("TUTORIAL (Practice Mode with Full Movement & Strike Guides)"),
        REAL_FIGHT_MODE("REAL FIGHT (Original Competitive Combat - No Indicators)");

        private final String title;
        GameMode(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    public enum FighterStance {
        RECOVERY_VULNERABLE("RECOVERY (Whiffed Attack - Defense Lowered)", 100),
        GUARD_FORTIFIED("FORTIFIED GUARD (Shield Raised - High Defense)", 25),
        NEUTRAL_SPACING("NEUTRAL STANCE (Ready & Moving)", 65);

        private final String label;
        private final int vulnerabilityPercent;

        FighterStance(String label, int vulnerabilityPercent) {
            this.label = label;
            this.vulnerabilityPercent = vulnerabilityPercent;
        }

        public String getLabel() { return label; }
        public int getVulnerabilityPercent() { return vulnerabilityPercent; }
    }

    public enum HitTimingRating {
        PERFECT_CRITICAL("[PERFECT HIT!] CRITICAL STRIKE (+50% BONUS DAMAGE)", 1.50),
        GOOD_HIT("[GOOD HIT] STANDARD COMBAT DAMAGE", 1.00),
        DEFLECTED_MISTIMED("[MISTIMED] DEFLECTED BY OPPONENT GUARD (-50% DAMAGE)", 0.50);

        private final String description;
        private final double damageMultiplier;

        HitTimingRating(String description, double damageMultiplier) {
            this.description = description;
            this.damageMultiplier = damageMultiplier;
        }

        public String getDescription() { return description; }
        public double getDamageMultiplier() { return damageMultiplier; }
    }

    /**
     * Displays the complete User Guidelines ("How to Play") screen before match start.
     */
    public static void displayGameGuidelines() {
        System.out.println("==========================================================================================");
        System.out.println("                 [=== CLASH OF BYTES: 2D COMBAT ARENA - HOW TO PLAY ===]                 ");
        System.out.println("              Course: Problem Solving Using OOP (Java) [Practicals 1 to 4]                ");
        System.out.println("==========================================================================================");
        System.out.println();
        System.out.println("  [1] GAME OBJECTIVE & OVERVIEW:");
        System.out.println("      - Command your fighter in the arena and reduce the opponent's HP to 0.0.");
        System.out.println("      - Powered by 5 OOP Inheritance Models: Single, Multilevel, Hierarchical,");
        System.out.println("        Multiple (via Interfaces), and Hybrid.");
        System.out.println();
        System.out.println("  [2] COMBAT CONTROLS & MOVEMENTS:");
        System.out.println("      - MOVE   : [A/D] (Player 1) | [LEFT/RIGHT ARROWS] (Player 2) -> Space & close in.");
        System.out.println("      - JUMP   : [W]   (Player 1) | [UP ARROW]          (Player 2) -> Evasive maneuvers.");
        System.out.println("      - CROUCH : [S]   (Player 1) | [DOWN ARROW]        (Player 2) -> Duck under highs.");
        System.out.println("      - PUNCH  : [F]   (Player 1) | [J / NUM1]          (Player 2) -> Fast recovery poke.");
        System.out.println("      - KICK   : [G]   (Player 1) | [K / NUM2]          (Player 2) -> Heavy knockback strike.");
        System.out.println("      - BLOCK  : [H]   (Player 1) | [L / NUM3]          (Player 2) -> Mitigate 70% damage.");
        System.out.println("      - SPECIAL: [R]   (Player 1) | [U / NUM5]          (Player 2) -> Class ultimate ability.");
        System.out.println();
        System.out.println("  [3] TWO GAMEPLAY MODES (NEW):");
        System.out.println("      [*] MODE 1 - TUTORIAL MODE:");
        System.out.println("          Comprehensive move-by-move coaching (Move, Jump, Punch, Kick, Block, Special,");
        System.out.println("          and Sweet-Spot 'When to Hit' indicator). Practice freely with sparring recovery.");
        System.out.println("      [*] MODE 2 - REAL FIGHT MODE:");
        System.out.println("          Original tournament match with ZERO indicators. Pure authentic arcade combat.");
        System.out.println("==========================================================================================");
    }

    /**
     * Lets the user select between Tutorial Mode and Real Fight Mode.
     */
    public static GameMode selectGameMode(Scanner scanner) {
        System.out.println();
        System.out.println("SELECT GAMEPLAY MODE:");
        System.out.println("  [1] TUTORIAL MODE   : Full Movement & Action Coaching (Recommended for Beginners)");
        System.out.println("  [2] REAL FIGHT MODE : Original Tournament Combat (No Indicators)");
        System.out.print("Enter choice [1 or 2] (Default 1): ");
        
        int choice = 1;
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("2")) {
                choice = 2;
            } else {
                choice = 1;
            }
        }

        if (choice == 2) {
            System.out.println();
            System.out.println("[*] Mode Selected: REAL FIGHT MODE (All indicators disabled).");
            return GameMode.REAL_FIGHT_MODE;
        } else {
            System.out.println();
            System.out.println("[*] Mode Selected: TUTORIAL MODE (Full move & action coaching active).");
            return GameMode.TUTORIAL_MODE;
        }
    }

    /**
     * Comprehensive Tutorial Move Guide covering all movements and actions.
     */
    public static void renderTutorialMoveGuide(Scanner scanner) {
        System.out.println();
        System.out.println("==========================================================================================");
        System.out.println("               [=== TUTORIAL MODE: COMPREHENSIVE MOVEMENT & ACTION GUIDE ===]             ");
        System.out.println("==========================================================================================");
        System.out.println("  STEP 1: MOVEMENT & POSITIONING");
        System.out.println("    - Keys: P1 uses [A/D] to move forward/backward | P2 uses [LEFT/RIGHT ARROWS].");
        System.out.println("    - Tip : Control spacing! Stay beyond 145px to bait whiffs, or close in to attack.");
        System.out.println();
        System.out.println("  STEP 2: JUMP & VERTICAL MOBILITY");
        System.out.println("    - Keys: P1 uses [W] to jump | P2 uses [UP ARROW].");
        System.out.println("    - Tip : Leaping over incoming ground attacks allows clean aerial punishes.");
        System.out.println();
        System.out.println("  STEP 3: PUNCH VS. KICK ATTACKS");
        System.out.println("    - Fast Punch : P1 [F] | P2 [J/1] -> Fast frame recovery; ideal for poking.");
        System.out.println("    - Heavy Kick : P1 [G] | P2 [K/2] -> High knockback; deals 1.5x damage during sweet spots.");
        System.out.println();
        System.out.println("  STEP 4: DEFENSE & SHIELD BLOCKING");
        System.out.println("    - Keys: P1 holds [H] | P2 holds [L/3].");
        System.out.println("    - Effect: Absorbs 70% of incoming physical damage.");
        System.out.println();
        System.out.println("  STEP 5: POLYMORPHIC SPECIAL MOVES");
        System.out.println("    - P1: Titan Shield Bash [R] | P2: Shadow Clone Strike [U/5].");
        System.out.println("    - Effect: Bypasses fortified guard defense to deal crushing damage.");
        System.out.println();
        System.out.println("  STEP 6: REAL-TIME 'WHEN TO HIT' TIMING INDICATOR");
        System.out.println("    - Green Light : Sweet spot open! Strike immediately for 1.5x critical bonus.");
        System.out.println("    - Red Light   : Guard active! Do NOT use normal attacks; raise guard or use Special.");
        System.out.println("==========================================================================================");
        System.out.print(">>> Tutorial practice complete! Press [ENTER] to shift to REAL FIGHT MODE: ");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        System.out.println();
        System.out.println("[*] Transitioning: Entering REAL FIGHT ARENA (No Indicators)...");
        System.out.println();
    }

    /**
     * Pauses execution until the user presses Enter.
     */
    public static void waitForEnter(Scanner scanner) {
        System.out.println();
        System.out.print("Press [ENTER] to continue: ");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    /**
     * Renders the dynamic "When to Hit" HUD for first-time players.
     */
    public static void renderHitIndicator(BaseFighter target, FighterStance stance) {
        System.out.println("  ----------------------------------------------------------------------------------------");
        System.out.println("  [*] [TUTORIAL MODE - REAL-TIME 'WHEN TO HIT' TIMING INDICATOR]");
        System.out.println("  ----------------------------------------------------------------------------------------");
        System.out.printf("  Opponent Target : %s | Current HP: %.1f / %.1f%n",
                target.getName(), target.getHealth(), target.getMaxHealth());
        System.out.printf("  Opponent Stance : %s%n", stance.getLabel());
        System.out.printf("  Vulnerability   : %d%%%n", stance.getVulnerabilityPercent());

        if (stance == FighterStance.RECOVERY_VULNERABLE) {
            System.out.println("  Timing Meter    : [====| >>>>> HIT NOW! (SWEET SPOT OPEN) <<<<< |====]");
            System.out.println("  Coach Advice    : [GREEN LIGHT] Opponent is off-balance. Attack NOW for 1.5x Critical!");
        } else if (stance == FighterStance.GUARD_FORTIFIED) {
            System.out.println("  Timing Meter    : [ [GUARD ACTIVE] |====----====| [GUARD ACTIVE] ]");
            System.out.println("  Coach Advice    : [RED LIGHT] Opponent is blocking. Do NOT use normal strikes! Use Guard or Special!");
        } else {
            System.out.println("  Timing Meter    : [ ------[ STEADY HIT OPPORTUNITY ]------ ]");
            System.out.println("  Coach Advice    : [YELLOW LIGHT] Neutral spacing. Fast strikes are safe.");
        }
        System.out.println("  ----------------------------------------------------------------------------------------");
    }

    /**
     * Evaluates strike timing based on the selected move and target stance.
     */
    public static HitTimingRating evaluateStrikeTiming(FighterStance stance, int moveChoice) {
        if (stance == FighterStance.RECOVERY_VULNERABLE) {
            if (moveChoice == 1 || moveChoice == 2 || moveChoice == 4) {
                return HitTimingRating.PERFECT_CRITICAL;
            }
            return HitTimingRating.GOOD_HIT;
        } else if (stance == FighterStance.GUARD_FORTIFIED) {
            if (moveChoice == 1 || moveChoice == 2) {
                return HitTimingRating.DEFLECTED_MISTIMED;
            }
            return HitTimingRating.GOOD_HIT;
        } else {
            return HitTimingRating.GOOD_HIT;
        }
    }
}

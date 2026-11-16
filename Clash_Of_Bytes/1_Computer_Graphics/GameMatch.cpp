#include "GameMatch.hpp"

GameMatch::GameMatch(int p1Type, int p2Type, GameMode initialMode) 
    : p1CharacterIndex(p1Type), p2CharacterIndex(p2Type),
      arenaBuffQueue(5),
      roundTimer(99.0f), currentRound(1), p1RoundsWon(0), p2RoundsWon(0),
      clippingLaserScanAngle(0.0f),
      debugHitboxes(false), showSyllabusMonitor(false),
      showHowToPlay(false), hitIndicatorPulse(0.0f), matchOver(false),
      currentGameMode(initialMode), modeNoticeTimer(3.0f), 
      modeNoticeText(initialMode == GameMode::TUTORIAL ? "LAUNCHED FROM SWING: TUTORIAL MODE (FULL GUIDES ACTIVE)" : "LAUNCHED FROM SWING: REAL FIGHT MODE (NO INDICATORS)")
{
    player1 = createFighter(p1CharacterIndex, 0, 360.0f, 140.0f, true);
    player2 = createFighter(p2CharacterIndex, 1, 920.0f, 140.0f, false);

    // Practical 05: Circular Linked List for Arena Power-Up & Buff Queue
    arenaBuffQueue.enqueueBuff(1, "Dragon's Might", player1->getName(), 0.25f, 12.0f, "ATTACK_BOOST");
    arenaBuffQueue.enqueueBuff(2, "Shadow Haste", player2->getName(), 0.20f, 10.0f, "SPEED_HASTE");
    arenaBuffQueue.enqueueBuff(3, "Titan Aegis", player1->getName(), 0.30f, 8.0f, "SHIELD_FORTIFY");

    // Practical 06: Stack Combat Formula Prefix to Infix & Postfix Damage Evaluator
    std::string prefixFormula = "* + ATK RAGE / COMBO 2";
    std::string postfix;
    currentCombatFormulaResult = CombatFormulaStackEngine::calculateCombatDamage(
        prefixFormula, 24.0, 6.0, 4.0, 2.0, currentCombatFormulaInfix, postfix);

    // Task 03 / Practical 07: Scenario Processing using Action Queue (FIFO) & History Stack (LIFO Undo)
    combatActionQueue.enqueue(CombatAction(1, 1, player1->getName(), CombatActionType::ATTACK, "Slash Strike", 18, 10, 2), false);
    combatActionQueue.enqueue(CombatAction(2, 2, player2->getName(), CombatActionType::DEFEND, "Iron Guard", 0, 5, 1), false);
    combatActionHistoryStack.push(CombatAction(0, 1, player1->getName(), CombatActionType::SPECIAL_ATTACK, "Dragon Upper", 32, 25, 2));
}

GameMatch::~GameMatch() {}

void GameMatch::setPlayer1Character(int typeIndex) {
    p1CharacterIndex = typeIndex;
    player1 = createFighter(p1CharacterIndex, 0, 360.0f, 140.0f, true);
    modeNoticeText = "P1 FIGHTER: " + player1->getName();
    modeNoticeTimer = 2.0f;
}

void GameMatch::setPlayer2Character(int typeIndex) {
    p2CharacterIndex = typeIndex;
    player2 = createFighter(p2CharacterIndex, 1, 920.0f, 140.0f, false);
    modeNoticeText = "P2 FIGHTER: " + player2->getName();
    modeNoticeTimer = 2.0f;
}

void GameMatch::cyclePlayer1Character() {
    setPlayer1Character((p1CharacterIndex + 1) % FIGHTER_COUNT);
}

void GameMatch::cyclePlayer2Character() {
    setPlayer2Character((p2CharacterIndex + 1) % FIGHTER_COUNT);
}

void GameMatch::resetMatch() {
    player1->resetForRound(360.0f, 140.0f, true);
    player2->resetForRound(920.0f, 140.0f, false);
    particleSystem.clear();
    roundTimer = 99.0f;
    matchOver = false;
    winnerText = "";
}

void GameMatch::shiftGameMode(GameMode newMode) {
    currentGameMode = newMode;
    if (newMode == GameMode::REAL_FIGHT) {
        modeNoticeText = "MODE: REAL FIGHT MODE (NO INDICATORS)";
        modeNoticeTimer = 2.5f;
        resetMatch();
    } else {
        modeNoticeText = "MODE: TUTORIAL MODE (FULL GUIDES ACTIVE)";
        modeNoticeTimer = 2.5f;
    }
}

void GameMatch::update(float dt, InputManager& input, Renderer& renderer) {
    hitIndicatorPulse += dt * 5.0f;
    clippingLaserScanAngle += dt * 1.5f;

    if (modeNoticeTimer > 0.0f) {
        modeNoticeTimer -= dt;
    }

    // Toggle In-Game Move & Controls Reference Guide: [F2]
    if (input.isHowToPlayTogglePressed()) {
        showHowToPlay = !showHowToPlay;
    }

    if (showHowToPlay) {
        if ((GetAsyncKeyState(VK_ESCAPE) & 0x8000) || (GetAsyncKeyState(VK_RETURN) & 0x8000) || (GetAsyncKeyState(VK_SPACE) & 0x8000)) {
            showHowToPlay = false;
        }
        return; // Pause match while viewing controls guide
    }

    // In-game character cycling: [F4] for P1, [F5] for P2
    static bool f4WasDown = false;
    static bool f5WasDown = false;
    bool f4Down = (GetAsyncKeyState(VK_F4) & 0x8000) != 0;
    bool f5Down = (GetAsyncKeyState(VK_F5) & 0x8000) != 0;
    if (f4Down && !f4WasDown) {
        cyclePlayer1Character();
    }
    if (f5Down && !f5WasDown) {
        cyclePlayer2Character();
    }
    f4WasDown = f4Down;
    f5WasDown = f5Down;

    // Practical 06 (CO6) Graphics Toggles: [F6] Lighting, [F7] Textures, [F8] Color Effects
    static bool f6WasDown = false, f7WasDown = false, f8WasDown = false;
    bool f6Down = (GetAsyncKeyState(VK_F6) & 0x8000) != 0;
    bool f7Down = (GetAsyncKeyState(VK_F7) & 0x8000) != 0;
    bool f8Down = (GetAsyncKeyState(VK_F8) & 0x8000) != 0;
    if (f6Down && !f6WasDown) renderer.toggleLighting();
    if (f7Down && !f7WasDown) renderer.toggleTextures();
    if (f8Down && !f8WasDown) renderer.toggleColorEffects();
    f6WasDown = f6Down;
    f7WasDown = f7Down;
    f8WasDown = f8Down;

    if (input.isDebugTogglePressed()) {
        debugHitboxes = !debugHitboxes;
    }
    if (input.isSyllabusTogglePressed()) {
        showSyllabusMonitor = !showSyllabusMonitor;
    }
    if (input.isPausePressed()) {
        resetMatch();
        return;
    }

    if (!matchOver) {
        input.applyInputToFighter(0, player1.get());
        input.applyInputToFighter(1, player2.get());

        player1->update(dt);
        player2->update(dt);
        particleSystem.update(dt);

        collisionSystem.checkCombat(player1.get(), player2.get(), particleSystem, renderer);

        // In Tutorial Mode: continuous sparring practice with auto health regeneration
        if (currentGameMode == GameMode::TUTORIAL) {
            if (player1->getHealth() < 40.0f) player1->heal(dt * 25.0f);
            if (player2->getHealth() < 40.0f) player2->heal(dt * 25.0f);
            roundTimer = 99.0f; // Keep timer maxed in tutorial practice
        } else {
            roundTimer -= dt;
        }

        if (roundTimer <= 0.0f || !player1->isAlive() || !player2->isAlive()) {
            matchOver = true;
            if (!player1->isAlive()) {
                winnerText = "PLAYER 2 (SHADOW NINJA) WINS!";
                p2RoundsWon++;
            } else if (!player2->isAlive()) {
                winnerText = "PLAYER 1 (BLAZE WARRIOR) WINS!";
                p1RoundsWon++;
            } else {
                winnerText = "DRAW GAME - TIME OVER!";
            }

            // Practical No. 04: Record in Doubly Linked List
            matchHistory.insertEnd({player1->getName(), 115 - (int)player2->getHealth(), 10, p1RoundsWon});
            matchHistory.insertEnd({player2->getName(), 95 - (int)player1->getHealth(), 8, p2RoundsWon});
            matchHistory.sortDescending();
        }
    } else {
        player1->update(dt);
        player2->update(dt);
        particleSystem.update(dt);

        if (GetAsyncKeyState(VK_RETURN) & 0x8000) {
            resetMatch();
        }
    }
}

void GameMatch::renderArena(Renderer& renderer) {
    // 1. Cyber Arena Backdrop
    renderer.drawGradientRect(0, 0, 1280, 720, 0.03f, 0.04f, 0.08f, 0.08f, 0.05f, 0.16f);

    // Practical 06 (CO6): Enable hardware scene lighting for textured arena elements
    renderer.beginLighting();

    // 2. Arena Floor Platform (Practical No. 01 & Practical No. 06 Textured Surface)
    renderer.setMaterial(0.35f, 0.5f, 0.7f, 32.0f);
    renderer.drawTexturedFloor(40.0f, 0.0f, 1200.0f, 140.0f);

    // 3. Decorative Cyber Pillars (Practical No. 02 & Practical No. 06 Textured Column)
    renderer.setMaterial(0.4f, 0.6f, 0.8f, 48.0f);
    renderer.drawTexturedPillar(120.0f, 140.0f, 60.0f, 320.0f);
    renderer.drawTexturedPillar(1100.0f, 140.0f, 60.0f, 320.0f);

    // 4. Center Stage Floating Battle Platform (Practical No. 06 Textured Grille)
    renderer.setMaterial(0.85f, 0.65f, 0.25f, 64.0f);
    renderer.drawTexturedPlatform(490.0f, 280.0f, 300.0f, 16.0f);

    // 5. Practical No. 04 & 06: Textured Spinning 3D Circuit Transformation Cube!
    renderer.setMaterial(1.0f, 0.9f, 0.4f, 96.0f, 0.2f, 0.15f, 0.05f);
    renderer.drawTexturedCube(640.0f, 340.0f, 48.0f);

    renderer.resetMaterial();
    renderer.endLighting();

    // 6. Practical No. 05: Cohen-Sutherland Viewport Line Clipping Demonstration
    int clipXmin = 280, clipYmin = 160, clipXmax = 1000, clipYmax = 520;
    renderer.drawRectOutline((float)clipXmin, (float)clipYmin, 
                             (float)(clipXmax - clipXmin), (float)(clipYmax - clipYmin),
                             0.15f, 0.45f, 0.75f, 1.5f);
    renderer.drawText((float)(clipXmin + 12), (float)(clipYmax - 18), 
                      "CLIPPING VIEWPORT [PRAC 05]", 0.3f, 0.75f, 0.95f, 1.1f);

    // Dynamic targeting scanner line crossing through the viewport
    float sweepOffset = std::sin(clippingLaserScanAngle) * 140.0f;
    int rawX1 = 160;
    int rawY1 = (int)(490.0f + sweepOffset);
    int rawX2 = 1120;
    int rawY2 = (int)(190.0f - sweepOffset);

    // A) Draw full unclipped path in faint color (shows outside portions that are clipped away)
    renderer.drawBresenhamLine(rawX1, rawY1, rawX2, rawY2, 0.25f, 0.28f, 0.35f);

    // B) Practical 05: Cohen-Sutherland clipped segment drawn in vivid neon green!
    renderer.drawClippedBresenhamLine(rawX1, rawY1, rawX2, rawY2,
                                      clipXmin, clipYmin, clipXmax, clipYmax,
                                      0.2f, 0.95f, 0.4f);
}

void GameMatch::renderHUD(Renderer& renderer) {
    // 1. Practical No. 03: Scan-Line Polygon Filled Health Bars!
    // P1 Health Bar (Left)
    renderer.drawScanLineHealthBar(80.0f, 640.0f, 440.0f, 28.0f, player1->getHealth(), player1->getMaxHealth(), false);
    renderer.drawText(85.0f, 680.0f, player1->getName(), 0.95f, 0.25f, 0.2f, 1.8f);

    // P2 Health Bar (Right)
    renderer.drawScanLineHealthBar(760.0f, 640.0f, 440.0f, 28.0f, player2->getHealth(), player2->getMaxHealth(), true);
    renderer.drawText(765.0f, 680.0f, player2->getName(), 0.2f, 0.7f, 0.95f, 1.8f);

    // 2. Central Timer Box
    renderer.drawRect(580.0f, 625.0f, 120.0f, 60.0f, 0.08f, 0.1f, 0.15f, 0.95f);
    renderer.drawRectOutline(580.0f, 625.0f, 120.0f, 60.0f, 0.95f, 0.85f, 0.2f, 2.0f);

    int displaySeconds = static_cast<int>(roundTimer);
    if (displaySeconds < 0) displaySeconds = 0;
    std::string timerStr = (displaySeconds < 10 ? "0" : "") + std::to_string(displaySeconds);
    renderer.drawText(615.0f, 642.0f, timerStr, 1.0f, 0.9f, 0.1f, 3.2f);

    std::string rText = "ROUND " + std::to_string(currentRound);
    renderer.drawText(600.0f, 595.0f, rText, 0.8f, 0.8f, 0.8f, 1.6f);

    // 3. Practical No. 03 & 04: COA Real x86-64 Assembly Execution HUD Banner & CG CO6 Status
    renderer.drawRect(40.0f, 10.0f, 1200.0f, 55.0f, 0.04f, 0.05f, 0.08f, 0.92f);
    renderer.drawRectOutline(40.0f, 10.0f, 1200.0f, 55.0f, 0.2f, 0.8f, 0.95f, 1.5f);
    renderer.drawText(55.0f, 48.0f, "COA ALU [x86-64 REGISTERS]: " + collisionSystem.getLatestALUTrace().substr(0, 96), 0.25f, 0.95f, 0.45f, 1.35f);
    renderer.drawText(55.0f, 31.0f, "[F2] HELP | [TAB] SYLLABUS | [F1] HITBOXES | [F6] LIGHTING | [F7] TEXTURES | [F8] AURAS", 0.95f, 0.85f, 0.2f, 1.25f);

    // CG CO6 Real-Time Status Subtitle
    std::string cgInfo = "CG CO6: " + renderer.getLightingModeName() + " | " + renderer.getTexturesModeName() + " | " + renderer.getColorEffectsModeName();
    renderer.drawText(55.0f, 15.0f, cgInfo, 0.35f, 0.85f, 1.0f, 1.15f);

    // Match Over Screen
    if (matchOver) {
        renderer.drawRect(340.0f, 240.0f, 600.0f, 220.0f, 0.05f, 0.06f, 0.1f, 0.96f);
        renderer.drawRectOutline(340.0f, 240.0f, 600.0f, 220.0f, 0.95f, 0.85f, 0.2f, 3.0f);
        renderer.drawText(400.0f, 380.0f, winnerText, 0.95f, 0.85f, 0.2f, 2.4f);
        renderer.drawText(430.0f, 320.0f, "PRESS [ENTER] FOR REMATCH", 1.0f, 1.0f, 1.0f, 2.0f);
        renderer.drawText(400.0f, 270.0f, "PRESS [TAB] FOR SYLLABUS MONITOR", 0.2f, 0.9f, 1.0f, 1.6f);
    }
}

void GameMatch::renderSyllabusMonitor(Renderer& renderer) {
    // Backdrop
    renderer.drawRect(50.0f, 50.0f, 1180.0f, 620.0f, 0.03f, 0.04f, 0.08f, 0.96f);
    renderer.drawRectOutline(50.0f, 50.0f, 1180.0f, 620.0f, 0.95f, 0.85f, 0.2f, 3.0f);

    // Banner
    renderer.drawRect(50.0f, 615.0f, 1180.0f, 55.0f, 0.08f, 0.12f, 0.22f, 0.98f);
    renderer.drawText(160.0f, 632.0f, "CLASH OF BYTES - 4-COURSE SYLLABUS MONITOR", 0.98f, 0.85f, 0.15f, 2.4f);
    renderer.drawText(980.0f, 635.0f, "[TAB] CLOSE", 1.0f, 1.0f, 1.0f, 1.6f);

    // Box 1: Computer Graphics Lab (2310218L) [Top-Left]
    renderer.drawRect(70.0f, 345.0f, 560.0f, 255.0f, 0.06f, 0.08f, 0.14f, 0.92f);
    renderer.drawRectOutline(70.0f, 345.0f, 560.0f, 255.0f, 0.2f, 0.7f, 1.0f, 1.5f);
    renderer.drawText(85.0f, 572.0f, "1. COMPUTER GRAPHICS LAB (2310218L)", 0.2f, 0.85f, 1.0f, 1.7f);
    renderer.drawText(85.0f, 550.0f, "- Prac 01 (CO1): Shaded Primitives & Gouraud Interpolation", 0.9f, 0.9f, 0.9f, 1.08f);
    renderer.drawText(85.0f, 532.0f, "- Prac 02 (CO3): Bresenham Line & Circle VFX (Hitspark Rings)", 0.9f, 0.9f, 0.9f, 1.08f);
    renderer.drawText(85.0f, 514.0f, "- Prac 03 (CO3): Scan-Line Polygon Fill (Live Player HP Gauges)", 0.9f, 0.9f, 0.9f, 1.08f);
    renderer.drawText(85.0f, 496.0f, "- Prac 04 (CO2): Transformation Matrices (3D Cube & Shuriken)", 0.9f, 0.9f, 0.9f, 1.08f);
    renderer.drawText(85.0f, 478.0f, "- Prac 05 (CO5): Cohen-Sutherland Clipping [280, 160, 1000, 520]", 0.9f, 0.9f, 0.9f, 1.08f);
    renderer.drawText(85.0f, 458.0f, "- Prac 06 (CO6) / Task 3: TEXTURES, LIGHTING & COLOR EFFECTS", 0.2f, 0.95f, 0.45f, 1.15f);
    renderer.drawText(85.0f, 440.0f, "  * Textures: Procedural Floor Grid, Tech Pillars, 3D Cube", 0.85f, 0.9f, 1.0f, 1.02f);
    renderer.drawText(85.0f, 422.0f, "  * Lighting: Key Spotlight [LIGHT0] + Dynamic Point [LIGHT1]", 0.85f, 0.9f, 1.0f, 1.02f);
    renderer.drawText(85.0f, 404.0f, "  * Materials: Archetype Specular, Hit Flash & Hex Shield", 0.85f, 0.9f, 1.0f, 1.02f);
    renderer.drawText(85.0f, 386.0f, "  * Controls: [F6] Lighting | [F7] Textures | [F8] Color Effects", 0.95f, 0.85f, 0.2f, 1.05f);
    renderer.drawText(85.0f, 360.0f, "[STATUS] All 6 CG Practicals (CO1-CO6) Active in Real-Time Loop", 0.3f, 0.95f, 0.4f, 1.15f);

    // Box 2: Programming Lab / DSA (2310214L) [Top-Right]
    renderer.drawRect(650.0f, 345.0f, 560.0f, 255.0f, 0.06f, 0.08f, 0.14f, 0.92f);
    renderer.drawRectOutline(650.0f, 345.0f, 560.0f, 255.0f, 0.3f, 0.9f, 0.4f, 1.5f);
    renderer.drawText(665.0f, 572.0f, "2. PROGRAMMING LAB / DSA (2310214L)", 0.3f, 0.95f, 0.4f, 1.7f);
    renderer.drawText(665.0f, 545.0f, "- Prac 01: 1D Arrays (MoveData catalog & Fighter Roster)", 0.9f, 0.9f, 0.9f, 1.2f);
    renderer.drawText(665.0f, 523.0f, "- Prac 02: 2D Sparse Matrix Arena Grid (Fast Transpose)", 0.9f, 0.9f, 0.9f, 1.2f);
    std::string pCount = "- Prac 03: Singly Linked List Particles (" + std::to_string(particleSystem.getActiveCount()) + " active nodes)";
    renderer.drawText(665.0f, 501.0f, pCount, 0.9f, 0.9f, 0.9f, 1.2f);
    renderer.drawText(665.0f, 479.0f, "- Prac 04: Doubly Linked List Sort & Merge for match scores", 0.9f, 0.9f, 0.9f, 1.2f);
    renderer.drawText(665.0f, 455.0f, "- Prac 05: Circular Linked List (Arena Buff & Power-Up Queue)", 0.2f, 0.95f, 0.45f, 1.2f);
    renderer.drawText(665.0f, 435.0f, "           Front: " + arenaBuffQueue.getFrontSummary().substr(0, 36), 0.85f, 0.85f, 0.85f, 1.15f);
    renderer.drawText(665.0f, 411.0f, "- Prac 06: Stack Engine (Prefix->Infix->Postfix Battle Damage)", 0.2f, 0.95f, 0.45f, 1.2f);
    renderer.drawText(665.0f, 393.0f, "           Formula: " + currentCombatFormulaInfix + " = " + std::to_string((int)currentCombatFormulaResult) + " DMG", 0.95f, 0.85f, 0.2f, 1.15f);
    renderer.drawText(665.0f, 375.0f, "- Task 03: Action Queue (FIFO) & Action Stack (LIFO Undo)", 0.2f, 0.95f, 0.45f, 1.2f);
    renderer.drawText(665.0f, 355.0f, "[STATUS] All DSA Practicals (CO1 to CO3) Active in Game Loop", 0.3f, 0.95f, 0.4f, 1.2f);

    // Box 3: COA Lab [80386 ALU & Real Assembly] (2310221L) [Bottom-Left]
    renderer.drawRect(70.0f, 75.0f, 560.0f, 255.0f, 0.06f, 0.08f, 0.14f, 0.92f);
    renderer.drawRectOutline(70.0f, 75.0f, 560.0f, 255.0f, 0.95f, 0.55f, 0.2f, 1.5f);
    renderer.drawText(85.0f, 300.0f, "3. COA LAB: ALU & ASSEMBLY (2310221L)", 0.98f, 0.55f, 0.2f, 1.8f);
    renderer.drawText(85.0f, 268.0f, "- Prac 01: 80386 ALU Micro-Operation Simulation (ADD, SUB)", 0.9f, 0.9f, 0.9f, 1.3f);
    renderer.drawText(85.0f, 238.0f, "- Prac 02: Control Unit Input Decoder (RegRead, ALUOp)", 0.9f, 0.9f, 0.9f, 1.3f);
    renderer.drawText(85.0f, 208.0f, "- Prac 03: REAL x86-64 ASSEMBLY (__asm__) on CPU Registers!", 0.2f, 0.95f, 0.45f, 1.3f);
    renderer.drawText(85.0f, 150.0f, "- Prac 04 / Task 3: 64-bit Shift-and-Add, Booth's & repne scasb", 0.2f, 0.95f, 0.45f, 1.25f);
    renderer.drawText(85.0f, 126.0f, "           Log Parser: CRITICAL/HIT counter, Packed BCD & Booth", 0.9f, 0.9f, 0.9f, 1.15f);
    renderer.drawText(85.0f, 100.0f, "- Source: CO3_Prac04_Combat_Algorithms_64bit.asm (NASM/MinGW64)", 0.95f, 0.85f, 0.2f, 1.15f);

    // Box 4: Problem Solving Using OOP Java (2310262L) [Bottom-Right]
    renderer.drawRect(650.0f, 75.0f, 560.0f, 255.0f, 0.06f, 0.08f, 0.14f, 0.92f);
    renderer.drawRectOutline(650.0f, 75.0f, 560.0f, 255.0f, 0.95f, 0.35f, 0.75f, 1.5f);
    renderer.drawText(665.0f, 300.0f, "4. OOP (JAVA) INTEGRATION (2310262L)", 0.95f, 0.35f, 0.75f, 1.8f);
    renderer.drawText(665.0f, 272.0f, "- Prac 01: Arrays & Loops (Roster, Stats: Min/Max/Avg/Tiers)", 0.9f, 0.9f, 0.9f, 1.2f);
    renderer.drawText(665.0f, 248.0f, "- Prac 02: StringBuilder High-Speed Combat Logger", 0.9f, 0.9f, 0.9f, 1.2f);
    renderer.drawText(665.0f, 224.0f, "- Prac 03: Classes, Overloaded Constructors & Lambda Moves", 0.9f, 0.9f, 0.9f, 1.2f);
    renderer.drawText(665.0f, 198.0f, "- Prac 04: ALL 5 TYPES OF JAVA INHERITANCE:", 0.2f, 0.95f, 0.45f, 1.25f);
    renderer.drawText(665.0f, 174.0f, "  1. Single : BaseFighter -> WarriorFighter", 0.9f, 0.9f, 0.9f, 1.15f);
    renderer.drawText(665.0f, 152.0f, "  2. Multi  : -> BerserkerWarrior", 0.9f, 0.9f, 0.9f, 1.15f);
    renderer.drawText(665.0f, 130.0f, "  3. Hier   : -> NinjaFighter, CyberBrawlerFighter", 0.9f, 0.9f, 0.9f, 1.15f);
    renderer.drawText(665.0f, 108.0f, "  4. Multiple: Paladin implements ShieldBearer, Elemental", 0.95f, 0.85f, 0.3f, 1.15f);
    renderer.drawText(665.0f, 86.0f,  "  5. Hybrid  : Gladiator (Multilevel + Multi-Interfaces)", 0.95f, 0.85f, 0.3f, 1.15f);
}

void GameMatch::renderTutorialIndicators(Renderer& renderer) {
    float p1X = player1->getPosition().x;
    float p1Y = player1->getPosition().y;
    float p2X = player2->getPosition().x;
    float p2Y = player2->getPosition().y;

    float distX = std::abs(p1X - p2X);
    float distY = std::abs(p1Y - p2Y);

    // 1. Top Mode & Shift Status Bar
    renderer.drawRect(290.0f, 688.0f, 700.0f, 24.0f, 0.04f, 0.12f, 0.18f, 0.92f);
    renderer.drawRectOutline(290.0f, 688.0f, 700.0f, 24.0f, 0.2f, 0.9f, 0.45f, 1.8f);
    renderer.drawText(305.0f, 694.0f, "TUTORIAL PRACTICE MODE | PRESS [M] OR [F3] TO SHIFT TO REAL FIGHT", 0.25f, 0.98f, 0.45f, 1.15f);

    // 2. Dynamic Center Coaching Banner (Y: 546, Height: 30)
    float coachX = 350.0f;
    float coachY = 546.0f;
    float coachW = 580.0f;
    float coachH = 30.0f;

    FighterState s1 = player1->getState();
    FighterState s2 = player2->getState();

    if (s2 == FighterState::BLOCKING || s1 == FighterState::BLOCKING) {
        // Opponent is guarding
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.32f, 0.05f, 0.05f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 1.0f, 0.3f, 0.3f, 2.0f);
        renderer.drawText(coachX + 22.0f, coachY + 8.0f, "GUARD ACTIVE! BREAK WITH SPECIAL: P1 [R] | P2 [U]", 1.0f, 0.4f, 0.4f, 1.2f);
    } else if (s2 == FighterState::PUNCHING || s2 == FighterState::KICKING || s2 == FighterState::SPECIAL_ATTACK ||
               s1 == FighterState::PUNCHING || s1 == FighterState::KICKING || s1 == FighterState::SPECIAL_ATTACK) {
        // Attack incoming
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.35f, 0.20f, 0.04f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 1.0f, 0.75f, 0.2f, 2.0f);
        renderer.drawText(coachX + 25.0f, coachY + 8.0f, "ATTACK INCOMING! DEFEND: P1 BLOCK [H] | P2 BLOCK [L]", 1.0f, 0.85f, 0.25f, 1.2f);
    } else if (distX <= 145.0f && distY < 65.0f) {
        // In striking range & vulnerable
        float pulse = (std::sin(hitIndicatorPulse) + 1.0f) * 0.5f;
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.04f, 0.16f + 0.18f * pulse, 0.08f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 0.2f, 0.95f, 0.35f, 2.2f);
        renderer.drawText(coachX + 18.0f, coachY + 8.0f, ">>> HIT NOW! P1: PUNCH [F]/KICK [G] | P2: PUNCH [J]/KICK [K] <<<", 0.3f, 1.0f, 0.45f, 1.15f);
    } else if (distY >= 65.0f) {
        // Airborne
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.05f, 0.12f, 0.25f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 0.3f, 0.75f, 1.0f, 2.0f);
        renderer.drawText(coachX + 35.0f, coachY + 8.0f, "AIRBORNE! EXECUTE JUMPING ATTACK [F / G / J / K]", 0.45f, 0.85f, 1.0f, 1.2f);
    } else {
        // Spacing / Approach
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.06f, 0.08f, 0.15f, 0.88f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 0.25f, 0.6f, 0.9f, 1.5f);
        renderer.drawText(coachX + 30.0f, coachY + 8.0f, "P1: MOVE [D] -> | <- MOVE [LEFT] :P2 | [W/UP] JUMP", 0.65f, 0.85f, 1.0f, 1.15f);
    }

    // 3. Player 1 Overhead Action Badge
    std::string p1Action;
    float p1R = 0.2f, p1G = 0.9f, p1B = 0.4f;
    switch (s1) {
        case FighterState::PUNCHING: p1Action = "P1: FAST PUNCH [F]"; p1R = 1.0f; p1G = 0.85f; p1B = 0.2f; break;
        case FighterState::KICKING: p1Action = "P1: HEAVY KICK [G]"; p1R = 1.0f; p1G = 0.5f; p1B = 0.2f; break;
        case FighterState::SPECIAL_ATTACK: p1Action = "P1: " + player1->getSpecialMoveName() + " [R]"; p1R = 1.0f; p1G = 0.95f; p1B = 0.2f; break;
        case FighterState::BLOCKING: p1Action = "P1: GUARD [H]"; p1R = 0.3f; p1G = 0.7f; p1B = 1.0f; break;
        case FighterState::JUMPING: p1Action = "P1: JUMPING [W]"; p1R = 0.4f; p1G = 0.8f; p1B = 1.0f; break;
        case FighterState::CROUCHING: p1Action = "P1: CROUCH [S]"; p1R = 0.8f; p1G = 0.8f; p1B = 0.8f; break;
        case FighterState::WALKING_FORWARD: p1Action = "P1: MOVE FORWARD [D]"; p1R = 0.4f; p1G = 0.9f; p1B = 0.5f; break;
        case FighterState::WALKING_BACKWARD: p1Action = "P1: RETREAT [A]"; p1R = 0.6f; p1G = 0.7f; p1B = 0.9f; break;
        case FighterState::HIT_STUN: p1Action = "P1: STUNNED!"; p1R = 0.9f; p1G = 0.2f; p1B = 0.2f; break;
        default: p1Action = (distX <= 145.0f && distY < 65.0f) ? "P1: STRIKE! [F/G]" : "P1: APPROACH [D]"; break;
    }
    float p1BadgeW = (float)p1Action.length() * 9.0f * 1.05f + 18.0f;
    float p1BadgeX = p1X - p1BadgeW * 0.5f;
    float p1BadgeY = p1Y + 130.0f;
    renderer.drawRect(p1BadgeX, p1BadgeY, p1BadgeW, 20.0f, 0.04f, 0.08f, 0.12f, 0.90f);
    renderer.drawRectOutline(p1BadgeX, p1BadgeY, p1BadgeW, 20.0f, p1R, p1G, p1B, 1.6f);
    renderer.drawText(p1BadgeX + 9.0f, p1BadgeY + 5.0f, p1Action, p1R, p1G, p1B, 1.05f);

    // 4. Player 2 Overhead Action Badge
    std::string p2Action;
    float p2R = 0.2f, p2G = 0.8f, p2B = 1.0f;
    switch (s2) {
        case FighterState::PUNCHING: p2Action = "P2: FAST PUNCH [J/1]"; p2R = 1.0f; p2G = 0.85f; p2B = 0.2f; break;
        case FighterState::KICKING: p2Action = "P2: HEAVY KICK [K/2]"; p2R = 1.0f; p2G = 0.5f; p2B = 0.2f; break;
        case FighterState::SPECIAL_ATTACK: p2Action = "P2: " + player2->getSpecialMoveName() + " [U/5]"; p2R = 1.0f; p2G = 0.95f; p2B = 0.2f; break;
        case FighterState::BLOCKING: p2Action = "P2: GUARD [L/3]"; p2R = 0.3f; p2G = 0.7f; p2B = 1.0f; break;
        case FighterState::JUMPING: p2Action = "P2: JUMPING [UP]"; p2R = 0.4f; p2G = 0.8f; p2B = 1.0f; break;
        case FighterState::CROUCHING: p2Action = "P2: CROUCH [DOWN]"; p2R = 0.8f; p2G = 0.8f; p2B = 0.8f; break;
        case FighterState::WALKING_FORWARD: p2Action = "P2: MOVE FORWARD [LEFT]"; p2R = 0.4f; p2G = 0.9f; p2B = 0.5f; break;
        case FighterState::WALKING_BACKWARD: p2Action = "P2: RETREAT [RIGHT]"; p2R = 0.6f; p2G = 0.7f; p2B = 0.9f; break;
        case FighterState::HIT_STUN: p2Action = "P2: STUNNED!"; p2R = 0.9f; p2G = 0.2f; p2B = 0.2f; break;
        default: p2Action = (distX <= 145.0f && distY < 65.0f) ? "P2: STRIKE! [J/K]" : "P2: APPROACH [LEFT]"; break;
    }
    float p2BadgeW = (float)p2Action.length() * 9.0f * 1.05f + 18.0f;
    float p2BadgeX = p2X - p2BadgeW * 0.5f;
    float p2BadgeY = p2Y + 130.0f;
    renderer.drawRect(p2BadgeX, p2BadgeY, p2BadgeW, 20.0f, 0.04f, 0.08f, 0.12f, 0.90f);
    renderer.drawRectOutline(p2BadgeX, p2BadgeY, p2BadgeW, 20.0f, p2R, p2G, p2B, 1.6f);
    renderer.drawText(p2BadgeX + 9.0f, p2BadgeY + 5.0f, p2Action, p2R, p2G, p2B, 1.05f);

    // 5. Docked Corner Cards
    // P1 Card (Bottom Left: X=15, Y=12, W=395, H=88)
    renderer.drawRect(15.0f, 12.0f, 395.0f, 88.0f, 0.05f, 0.07f, 0.12f, 0.88f);
    renderer.drawRectOutline(15.0f, 12.0f, 395.0f, 88.0f, 0.95f, 0.35f, 0.25f, 1.6f);
    renderer.drawText(25.0f, 78.0f, "P1 " + player1->getName() + " CONTROLS:", 0.95f, 0.4f, 0.3f, 1.15f);
    renderer.drawText(25.0f, 58.0f, "- MOVE: [A/D] | JUMP: [W] | DUCK: [S]", 0.85f, 0.9f, 0.95f, 1.0f);
    renderer.drawText(25.0f, 40.0f, "- PUNCH: [F]  | KICK: [G] | BLOCK: [H]", 0.85f, 0.9f, 0.95f, 1.0f);
    renderer.drawText(25.0f, 22.0f, "- SPECIAL: [R] " + player1->getSpecialMoveName(), 0.95f, 0.85f, 0.3f, 1.0f);

    // P2 Card (Bottom Right: X=870, Y=12, W=395, H=88)
    renderer.drawRect(870.0f, 12.0f, 395.0f, 88.0f, 0.05f, 0.07f, 0.12f, 0.88f);
    renderer.drawRectOutline(870.0f, 12.0f, 395.0f, 88.0f, 0.25f, 0.7f, 1.0f, 1.6f);
    renderer.drawText(880.0f, 78.0f, "P2 " + player2->getName() + " CONTROLS:", 0.3f, 0.75f, 1.0f, 1.15f);
    renderer.drawText(880.0f, 58.0f, "- MOVE: [ARROWS] | JUMP: [UP] | DUCK: [DOWN]", 0.85f, 0.9f, 0.95f, 1.0f);
    renderer.drawText(880.0f, 40.0f, "- PUNCH: [J/1]   | KICK: [K/2] | BLOCK: [L/3]", 0.85f, 0.9f, 0.95f, 1.0f);
    renderer.drawText(880.0f, 22.0f, "- SPECIAL: [U/5] " + player2->getSpecialMoveName(), 0.95f, 0.85f, 0.3f, 1.0f);
}

void GameMatch::renderModeNotice(Renderer& renderer) {
    renderer.drawRect(270.0f, 335.0f, 740.0f, 50.0f, 0.05f, 0.10f, 0.20f, 0.96f);
    renderer.drawRectOutline(270.0f, 335.0f, 740.0f, 50.0f, 0.95f, 0.85f, 0.2f, 2.5f);
    renderer.drawText(290.0f, 352.0f, modeNoticeText, 0.98f, 0.9f, 0.2f, 1.35f);
}

void GameMatch::renderHowToPlay(Renderer& renderer) {
    // 1. Semi-transparent dark overlay over entire viewport
    renderer.drawRect(0.0f, 0.0f, 1280.0f, 720.0f, 0.02f, 0.03f, 0.06f, 0.88f);

    // 2. Main Dialog Frame
    renderer.drawRect(50.0f, 35.0f, 1180.0f, 650.0f, 0.04f, 0.05f, 0.09f, 0.96f);
    renderer.drawRectOutline(50.0f, 35.0f, 1180.0f, 650.0f, 0.95f, 0.85f, 0.2f, 3.0f);

    // 3. Title Banner
    renderer.drawRect(50.0f, 625.0f, 1180.0f, 60.0f, 0.08f, 0.12f, 0.22f, 0.98f);
    renderer.drawText(75.0f, 645.0f, "CLASH OF BYTES - FIGHTER CONTROLS & MOVE LIST", 0.98f, 0.85f, 0.15f, 1.85f);
    renderer.drawText(960.0f, 645.0f, "[F2] CLOSE / RESUME", 0.2f, 0.95f, 0.4f, 1.3f);

    // 4. Box Left: Player 1 (Dynamic Character Controls)
    renderer.drawRect(70.0f, 360.0f, 560.0f, 250.0f, 0.06f, 0.08f, 0.14f, 0.92f);
    renderer.drawRectOutline(70.0f, 360.0f, 560.0f, 250.0f, 0.95f, 0.35f, 0.25f, 2.0f);
    renderer.drawText(85.0f, 582.0f, "PLAYER 1: " + player1->getName() + " (CONTROLS)", 0.95f, 0.35f, 0.25f, 1.45f);
    renderer.drawText(85.0f, 554.0f, "- W / A / S / D  : JUMP / MOVE LEFT / CROUCH / MOVE RIGHT", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(85.0f, 528.0f, "- F KEY          : FAST PUNCH (18 DMG, FAST RECOVERY)", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(85.0f, 502.0f, "- G KEY          : HEAVY KICK (24 DMG, HEAVY KNOCKBACK)", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(85.0f, 476.0f, "- H KEY          : GUARD SHIELD (ABSORBS 70% DAMAGE)", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(85.0f, 450.0f, "- R KEY          : SPECIAL MOVE (" + player1->getSpecialMoveName() + ")", 0.95f, 0.85f, 0.3f, 1.05f);
    renderer.drawText(85.0f, 418.0f, "[ROLE] " + player1->getRoleName(), 0.7f, 0.85f, 0.95f, 1.05f);

    // 5. Box Right: Player 2 (Dynamic Character Controls)
    renderer.drawRect(650.0f, 360.0f, 560.0f, 250.0f, 0.06f, 0.08f, 0.14f, 0.92f);
    renderer.drawRectOutline(650.0f, 360.0f, 560.0f, 250.0f, 0.2f, 0.7f, 1.0f, 2.0f);
    renderer.drawText(665.0f, 582.0f, "PLAYER 2: " + player2->getName() + " (CONTROLS)", 0.2f, 0.75f, 1.0f, 1.45f);
    renderer.drawText(665.0f, 554.0f, "- ARROWS         : UP=JUMP / DOWN=CROUCH / LEFT / RIGHT", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(665.0f, 528.0f, "- J / NUM1 KEY   : FAST PUNCH (16 DMG, QUICK STRIKE)", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(665.0f, 502.0f, "- K / NUM2 KEY   : HEAVY KICK (22 DMG, HIGH REACH)", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(665.0f, 476.0f, "- L / NUM3 KEY   : GUARD BLOCK (ABSORBS 70% DAMAGE)", 0.9f, 0.9f, 0.9f, 1.05f);
    renderer.drawText(665.0f, 450.0f, "- U / NUM5 KEY   : SPECIAL MOVE (" + player2->getSpecialMoveName() + ")", 0.95f, 0.85f, 0.3f, 1.05f);
    renderer.drawText(665.0f, 418.0f, "[ROLE] " + player2->getRoleName(), 0.7f, 0.85f, 0.95f, 1.05f);

    // 6. Box Middle Lower: Active Mode Status from Swing Launcher
    renderer.drawRect(70.0f, 135.0f, 1140.0f, 210.0f, 0.05f, 0.08f, 0.12f, 0.94f);
    renderer.drawRectOutline(70.0f, 135.0f, 1140.0f, 210.0f, 0.2f, 0.95f, 0.45f, 2.0f);
    
    if (currentGameMode == GameMode::TUTORIAL) {
        renderer.drawText(85.0f, 318.0f, "ACTIVE MODE: TUTORIAL MODE (FULL ACTION & MOVEMENT GUIDES ACTIVE)", 0.25f, 0.98f, 0.45f, 1.45f);
        renderer.drawText(85.0f, 290.0f, "- Live overhead badges indicate Move, Jump, Punch, Kick, Block, and Special moves.", 0.85f, 0.95f, 1.0f, 1.08f);
        renderer.drawText(85.0f, 268.0f, "- Top coaching bar displays 145px strike sweet spot and opponent defensive stances.", 0.85f, 0.95f, 1.0f, 1.08f);
        renderer.drawText(85.0f, 246.0f, "- Continuous practice enabled: sparring fighters auto-regenerate health.", 0.3f, 1.0f, 0.45f, 1.08f);
        renderer.drawText(85.0f, 218.0f, "MODE IS CONFIGURED AND MANAGED IN THE JAVA SWING ARENA LAUNCHER.", 0.95f, 0.85f, 0.25f, 1.08f);
    } else {
        renderer.drawText(85.0f, 318.0f, "ACTIVE MODE: REAL FIGHT MODE (AUTHENTIC TOURNAMENT MATCH)", 0.98f, 0.45f, 0.45f, 1.45f);
        renderer.drawText(85.0f, 290.0f, "- 0 INDICATORS ON SCREEN: Pure competitive arcade combat HUD.", 0.95f, 0.95f, 0.95f, 1.08f);
        renderer.drawText(85.0f, 268.0f, "- Official 99-second match timer, real assembly ALU registers trace, and K.O. rules.", 0.9f, 0.9f, 0.9f, 1.08f);
        renderer.drawText(85.0f, 246.0f, "- No auto-regeneration. Test your true reaction time and fighting mastery!", 0.95f, 0.7f, 0.2f, 1.08f);
        renderer.drawText(85.0f, 218.0f, "MODE IS CONFIGURED AND MANAGED IN THE JAVA SWING ARENA LAUNCHER.", 0.95f, 0.85f, 0.25f, 1.08f);
    }
    renderer.drawText(85.0f, 160.0f, "IN-GAME HOTKEYS: [F2] RESUME COMBAT | [F4] CYCLE P1 | [F5] CYCLE P2 | [F1] HITBOXES | [TAB] SYLLABUS", 0.95f, 0.85f, 0.2f, 1.05f);

    // 7. Resume Button at Bottom
    renderer.drawRect(70.0f, 45.0f, 1140.0f, 75.0f, 0.04f, 0.16f, 0.22f, 0.98f);
    renderer.drawRectOutline(70.0f, 45.0f, 1140.0f, 75.0f, 0.25f, 0.95f, 0.45f, 2.5f);
    renderer.drawText(360.0f, 75.0f, "PRESS [F2], [ESC], OR [ENTER] TO RESUME COMBAT", 0.25f, 0.98f, 0.45f, 1.55f);
}

void GameMatch::render(Renderer& renderer) {
    // 1. Render Arena Scene
    renderArena(renderer);

    // 2. Render Fighters under Lighting with Dynamic Material Reflection (Practical 06)
    renderer.beginLighting();

    // Player 1
    renderer.applyFighterMaterial(p1CharacterIndex, player1->getHealth() / player1->getMaxHealth(),
                                  player1->getState() == FighterState::PUNCHING ||
                                  player1->getState() == FighterState::KICKING ||
                                  player1->getState() == FighterState::SPECIAL_ATTACK,
                                  player1->getState() == FighterState::HIT_STUN);
    player1->render(renderer, debugHitboxes);

    // Player 1 Guard Barrier (Textured Hexagonal Forcefield when blocking)
    if (player1->getState() == FighterState::BLOCKING) {
        float pulse = 0.75f + 0.25f * std::sin(roundTimer * 12.0f);
        Vector2D p = player1->getPosition();
        float dir = player1->isFacingRightDir() ? 1.0f : -1.0f;
        renderer.drawHexagonalShield(p.x + 20.0f * dir, p.y + 70.0f, 55.0f, pulse, 0.2f, 0.85f, 1.0f);
    }

    // Player 2
    renderer.applyFighterMaterial(p2CharacterIndex, player2->getHealth() / player2->getMaxHealth(),
                                  player2->getState() == FighterState::PUNCHING ||
                                  player2->getState() == FighterState::KICKING ||
                                  player2->getState() == FighterState::SPECIAL_ATTACK,
                                  player2->getState() == FighterState::HIT_STUN);
    player2->render(renderer, debugHitboxes);

    // Player 2 Guard Barrier (Textured Hexagonal Forcefield when blocking)
    if (player2->getState() == FighterState::BLOCKING) {
        float pulse = 0.75f + 0.25f * std::sin(roundTimer * 12.0f);
        Vector2D p = player2->getPosition();
        float dir = player2->isFacingRightDir() ? 1.0f : -1.0f;
        renderer.drawHexagonalShield(p.x + 20.0f * dir, p.y + 70.0f, 55.0f, pulse, 1.0f, 0.65f, 0.2f);
    }

    renderer.resetMaterial();
    renderer.endLighting();

    // 3. Render Particles (Singly Linked List + Bresenham)
    particleSystem.render(renderer);

    // 4. Render HUD (Scan-line health bars & COA Assembly live banner)
    renderHUD(renderer);

    // 5. In-Game Indicators based on Game Mode
    if (!matchOver && !showHowToPlay) {
        if (currentGameMode == GameMode::TUTORIAL) {
            renderTutorialIndicators(renderer);
        }
        // In Real Fight Mode: zero indicators rendered!
    }

    // 6. Mode Shift Announcement Notification Banner
    if (modeNoticeTimer > 0.0f && !showHowToPlay) {
        renderModeNotice(renderer);
    }

    // 7. 4-Course Syllabus Monitor Overlay
    if (showSyllabusMonitor) {
        renderSyllabusMonitor(renderer);
    }

    // 8. How To Play & Mode Selection Screen (on startup or when F2 pressed)
    if (showHowToPlay) {
        renderHowToPlay(renderer);
    }
}

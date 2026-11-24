#include "GameMatch.hpp"
#include <fstream>
#include <ctime>

GameMatch::GameMatch(int p1Type, int p2Type, GameMode initialMode, int totalRoundsVal, float roundTimeVal, ArenaTheme initialTheme) 
    : currentArenaTheme(initialTheme),
      p1CharacterIndex(p1Type), p2CharacterIndex(p2Type),
      arenaBuffQueue(5),
      roundTimer(roundTimeVal > 0.0f ? roundTimeVal : 60.0f),
      maxRoundTimer(roundTimeVal > 0.0f ? roundTimeVal : 60.0f),
      currentRound(1),
      totalRounds(totalRoundsVal > 0 ? totalRoundsVal : 3),
      roundsToWin((totalRoundsVal > 0 ? totalRoundsVal : 3) == 1 ? 1 : ((totalRoundsVal > 0 ? totalRoundsVal : 3) / 2 + 1)),
      p1RoundsWon(0), p2RoundsWon(0),
      inRoundIntermission(false), roundIntermissionTimer(0.0f),
      roundEndBanner(""),
      clippingLaserScanAngle(0.0f),
      debugHitboxes(false), showSyllabusMonitor(false),
      showHowToPlay(false), hitIndicatorPulse(0.0f), matchOver(false),
      matchResultWritten(false),
      player1Name("Player 1"), player2Name("Player 2"),
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
    roundTimer = maxRoundTimer;
    currentRound = 1;
    p1RoundsWon = 0;
    p2RoundsWon = 0;
    inRoundIntermission = false;
    roundIntermissionTimer = 0.0f;
    roundEndBanner = "";
    matchOver = false;
    matchResultWritten = false;
    winnerText = "";
    modeNoticeText = "ROUND 1 - FIGHT!";
    modeNoticeTimer = 2.0f;
}

void GameMatch::shiftGameMode(GameMode newMode) {
    currentGameMode = newMode;
    if (newMode == GameMode::REAL_FIGHT) {
        modeNoticeText = "MODE: REAL FIGHT MODE (NO INDICATORS)";
        modeNoticeTimer = 2.0f;
    } else {
        modeNoticeText = "MODE: TUTORIAL MODE (FULL GUIDES ACTIVE)";
        modeNoticeTimer = 2.0f;
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

    // Mode Shift Toggle: [M] or [F3] to seamlessly switch between Tutorial and Real Fight
    static bool modeKeyWasDown = false;
    bool modeKeyDown = ((GetAsyncKeyState('M') & 0x8000) != 0) || ((GetAsyncKeyState(VK_F3) & 0x8000) != 0);
    if (modeKeyDown && !modeKeyWasDown) {
        shiftGameMode(currentGameMode == GameMode::TUTORIAL ? GameMode::REAL_FIGHT : GameMode::TUTORIAL);
    }
    modeKeyWasDown = modeKeyDown;

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
        // Handle intermission between rounds in a multi-round match
        if (inRoundIntermission) {
            roundIntermissionTimer -= dt;
            player1->update(dt);
            player2->update(dt);
            particleSystem.update(dt);

            if (roundIntermissionTimer <= 0.0f) {
                inRoundIntermission = false;
                currentRound++;
                roundTimer = maxRoundTimer;
                player1->resetForRound(360.0f, 140.0f, true);
                player2->resetForRound(920.0f, 140.0f, false);
                particleSystem.clear();
                modeNoticeText = "ROUND " + std::to_string(currentRound) + " - FIGHT!";
                modeNoticeTimer = 2.0f;
            }
            return;
        }

        input.applyInputToFighter(0, player1.get());
        input.applyInputToFighter(1, player2.get());

        player1->update(dt);
        player2->update(dt);
        particleSystem.update(dt);

        collisionSystem.checkCombat(player1.get(), player2.get(), particleSystem, renderer);

        // Continuous health regeneration in Tutorial Mode for sparring practice
        if (currentGameMode == GameMode::TUTORIAL) {
            if (player1->getHealth() < 40.0f) player1->heal(dt * 25.0f);
            if (player2->getHealth() < 40.0f) player2->heal(dt * 25.0f);
        }

        // Active round timer countdown (ticks down continuously in both modes)
        if (maxRoundTimer < 999.0f) {
            roundTimer -= dt;
            if (roundTimer <= 0.0f) {
                roundTimer = 0.0f;
            }
        }

        // Check if the current round has concluded
        if ((maxRoundTimer < 999.0f && roundTimer <= 0.0f) || !player1->isAlive() || !player2->isAlive()) {
            if (currentGameMode == GameMode::TUTORIAL && roundTimer <= 0.0f && player1->isAlive() && player2->isAlive()) {
                // In Tutorial sparring mode: refresh timer so training never stalls abruptly
                roundTimer = maxRoundTimer;
                modeNoticeText = "SPARRING ROUND REFRESHED - KEEP PRACTICING!";
                modeNoticeTimer = 2.0f;
            } else {
            std::string roundWinnerName = "";

            if (!player1->isAlive() && !player2->isAlive()) {
                roundEndBanner = "DOUBLE KO - ROUND " + std::to_string(currentRound) + " DRAW!";
            } else if (!player1->isAlive()) {
                p2RoundsWon++;
                roundWinnerName = player2->getName();
                roundEndBanner = player2->getName() + " WINS ROUND " + std::to_string(currentRound) + "!";
            } else if (!player2->isAlive()) {
                p1RoundsWon++;
                roundWinnerName = player1->getName();
                roundEndBanner = player1->getName() + " WINS ROUND " + std::to_string(currentRound) + "!";
            } else {
                // Time Over
                if (player1->getHealth() > player2->getHealth()) {
                    p1RoundsWon++;
                    roundWinnerName = player1->getName();
                    roundEndBanner = "TIME OVER - " + player1->getName() + " WINS ROUND " + std::to_string(currentRound) + "!";
                } else if (player2->getHealth() > player1->getHealth()) {
                    p2RoundsWon++;
                    roundWinnerName = player2->getName();
                    roundEndBanner = "TIME OVER - " + player2->getName() + " WINS ROUND " + std::to_string(currentRound) + "!";
                } else {
                    roundEndBanner = "TIME OVER - ROUND " + std::to_string(currentRound) + " DRAW!";
                }
            }

            // Check if entire match is decided (best of 1, 3, or 5)
            if (p1RoundsWon >= roundsToWin || p2RoundsWon >= roundsToWin || currentRound >= totalRounds) {
                matchOver = true;
                if (p1RoundsWon > p2RoundsWon) {
                    winnerText = player1->getName() + " WINS MATCH (" + std::to_string(p1RoundsWon) + "-" + std::to_string(p2RoundsWon) + ")!";
                } else if (p2RoundsWon > p1RoundsWon) {
                    winnerText = player2->getName() + " WINS MATCH (" + std::to_string(p2RoundsWon) + "-" + std::to_string(p1RoundsWon) + ")!";
                } else {
                    winnerText = "MATCH TIED - DRAW GAME (" + std::to_string(p1RoundsWon) + "-" + std::to_string(p2RoundsWon) + ")!";
                }

                // Practical No. 04: Record in Doubly Linked List
                matchHistory.insertEnd({player1->getName(), 115 - (int)player2->getHealth(), 10, p1RoundsWon});
                matchHistory.insertEnd({player2->getName(), 95 - (int)player1->getHealth(), 8, p2RoundsWon});
                matchHistory.sortDescending();

                // Record match outcome to match_result.json for Java Swing / Supabase Cloud Sync
                if (!matchResultWritten) {
                    matchResultWritten = true;
                    std::ofstream resFile("match_result.json");
                    if (resFile.is_open()) {
                        std::string resStr = (p1RoundsWon > p2RoundsWon) ? "WIN" : ((p2RoundsWon > p1RoundsWon) ? "LOSS" : "DRAW");
                        int winnerCode = (p1RoundsWon > p2RoundsWon) ? 1 : ((p2RoundsWon > p1RoundsWon) ? 2 : 0);
                        float p1Dmg = 115.0f - player2->getHealth();
                        if (p1Dmg < 0.0f) p1Dmg = 0.0f;
                        float p2Dmg = 95.0f - player1->getHealth();
                        if (p2Dmg < 0.0f) p2Dmg = 0.0f;
                        std::string winName = (winnerCode == 1) ? (player1Name + " (" + player1->getName() + ")") :
                                              ((winnerCode == 2) ? (player2Name + " (" + player2->getName() + ")") : "DRAW");
                        std::string outcomeStr = (winnerCode == 1) ? (player1Name + " WON") :
                                                 ((winnerCode == 2) ? (player2Name + " WON") : "DRAW");
                        resFile << "{\n";
                        resFile << "  \"player_1\": \"" << player1Name << "\",\n";
                        resFile << "  \"p1_fighter\": \"" << player1->getName() << "\",\n";
                        resFile << "  \"player_2\": \"" << player2Name << "\",\n";
                        resFile << "  \"p2_fighter\": \"" << player2->getName() << "\",\n";
                        resFile << "  \"score\": \"" << p1RoundsWon << " - " << p2RoundsWon << "\",\n";
                        resFile << "  \"winner\": \"" << winName << "\",\n";
                        resFile << "  \"outcome\": \"" << outcomeStr << "\",\n";
                        resFile << "  \"p1_rounds\": " << p1RoundsWon << ",\n";
                        resFile << "  \"p2_rounds\": " << p2RoundsWon << ",\n";
                        resFile << "  \"winner_code\": " << winnerCode << ",\n";
                        resFile << "  \"result\": \"" << resStr << "\",\n";
                        resFile << "  \"damage_p1\": " << p1Dmg << ",\n";
                        resFile << "  \"damage_p2\": " << p2Dmg << ",\n";
                        resFile << "  \"timestamp\": " << (long long)time(nullptr) << "\n";
                        resFile << "}\n";
                        resFile.close();
                    }
                }
            } else {
                // Start intermission transition to next round!
                inRoundIntermission = true;
                roundIntermissionTimer = 2.2f;
                modeNoticeText = roundEndBanner;
                modeNoticeTimer = 2.2f;
            }
            }
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
    switch (currentArenaTheme) {
        case ArenaTheme::MANSION:
            renderMansionArena(renderer);
            break;
        case ArenaTheme::PRACTICE_DOJO:
            renderPracticeDojo(renderer);
            break;
        case ArenaTheme::CYBER_ARENA:
        default:
            renderCyberArena(renderer);
            break;
    }
}

// ============================================================================
// 1. BATTLE ARENA: MANSION THEMED ARENA (Victorian / Gothic Grand Hall)
// ============================================================================
void GameMatch::renderMansionArena(Renderer& renderer) {
    // 1. Dark Gothic Hall Wall Backdrop (Deep Indigo & Midnight Violet Gradient)
    renderer.drawGradientRect(0.0f, 0.0f, 1280.0f, 720.0f,
                              0.03f, 0.02f, 0.05f, 0.06f, 0.05f, 0.12f);

    // 2. High Gothic Arched Stained Glass Windows (Silvery Moonlight Beams)
    float winX[3] = { 240.0f, 640.0f, 1040.0f };
    for (int i = 0; i < 3; ++i) {
        float wx = winX[i];
        // Stone window arch frame
        renderer.drawRect(wx - 75.0f, 320.0f, 150.0f, 320.0f, 0.18f, 0.16f, 0.22f);
        renderer.drawRectOutline(wx - 75.0f, 320.0f, 150.0f, 320.0f, 0.35f, 0.32f, 0.42f, 2.0f);
        // Stained glass dark night sky with moon glow
        renderer.drawGradientRect(wx - 65.0f, 330.0f, 130.0f, 300.0f,
                                  0.05f, 0.08f, 0.20f, 0.15f, 0.22f, 0.45f);
        // Arched top curve simulation
        renderer.drawTriangle(wx - 65.0f, 630.0f, wx + 65.0f, 630.0f, wx, 660.0f,
                              0.15f, 0.22f, 0.45f);

        // Diamond leaded lattice mullions
        for (int row = 0; row < 6; ++row) {
            float my = 350.0f + (float)row * 50.0f;
            renderer.drawBresenhamLine((int)(wx - 65.0f), (int)my, (int)(wx + 65.0f), (int)my, 0.25f, 0.28f, 0.35f);
        }
        renderer.drawBresenhamLine((int)wx, 330, (int)wx, 645, 0.30f, 0.32f, 0.40f);

        // Volumetric soft moonlight beams slanting down to the floor
        glBegin(GL_QUADS);
            glColor4f(0.45f, 0.65f, 0.95f, 0.10f);
            glVertex2f(wx - 45.0f, 620.0f);
            glVertex2f(wx + 45.0f, 620.0f);
            glColor4f(0.20f, 0.40f, 0.85f, 0.02f);
            glVertex2f(wx + 110.0f, 140.0f);
            glVertex2f(wx - 110.0f, 140.0f);
        glEnd();
    }

    // 3. Ornate Classical Oil Paintings in Gilded Gold Baroque Frames
    float paintingX[2] = { 130.0f, 850.0f };
    for (int p = 0; p < 2; ++p) {
        float px = paintingX[p];
        // Outer gilded gold frame
        renderer.drawRect(px, 340.0f, 90.0f, 120.0f, 0.88f, 0.74f, 0.25f);
        renderer.drawRectOutline(px, 340.0f, 90.0f, 120.0f, 0.55f, 0.42f, 0.12f, 3.0f);
        // Canvas interior
        renderer.drawGradientRect(px + 8.0f, 348.0f, 74.0f, 104.0f,
                                  0.12f, 0.08f, 0.06f, 0.28f, 0.18f, 0.12f);
        // Antique portrait silhouette
        renderer.drawRect(px + 32.0f, 385.0f, 26.0f, 30.0f, 0.45f, 0.32f, 0.22f);
        renderer.drawTriangle(px + 20.0f, 350.0f, px + 70.0f, 350.0f, px + 45.0f, 390.0f, 0.35f, 0.22f, 0.15f);
    }

    // 4. Carved Stone Fireplace with Animated Roaring Flames & Rising Sparks
    float fX = 460.0f;
    // Stone chimney & mantelpiece
    renderer.drawRect(fX, 140.0f, 180.0f, 160.0f, 0.25f, 0.24f, 0.28f);
    renderer.drawRectOutline(fX, 140.0f, 180.0f, 160.0f, 0.45f, 0.42f, 0.48f, 2.5f);
    // Mantel shelf (Gold accented stone trim)
    renderer.drawRect(fX - 10.0f, 290.0f, 200.0f, 16.0f, 0.35f, 0.32f, 0.38f);
    renderer.drawRect(fX - 8.0f, 292.0f, 196.0f, 4.0f, 0.85f, 0.72f, 0.25f);

    // Dark iron hearth opening
    renderer.drawRect(fX + 25.0f, 140.0f, 130.0f, 110.0f, 0.08f, 0.06f, 0.08f);
    renderer.drawTriangle(fX + 25.0f, 250.0f, fX + 155.0f, 250.0f, fX + 90.0f, 270.0f, 0.08f, 0.06f, 0.08f);

    // Glowing hot embers & burning wood logs
    renderer.drawRect(fX + 40.0f, 142.0f, 100.0f, 16.0f, 0.95f, 0.30f, 0.05f);
    renderer.drawRect(fX + 48.0f, 148.0f, 84.0f, 8.0f, 1.0f, 0.65f, 0.15f);

    // Animated flickering fire flames
    float fireCycle1 = std::sin(roundTimer * 16.0f) * 12.0f;
    float fireCycle2 = std::sin(roundTimer * 22.0f + 1.5f) * 15.0f;
    float fireCycle3 = std::cos(roundTimer * 18.0f + 0.8f) * 10.0f;

    renderer.drawTriangle(fX + 55.0f, 150.0f, fX + 85.0f, 150.0f, fX + 70.0f + fireCycle1 * 0.3f, 215.0f + fireCycle1, 1.0f, 0.25f, 0.05f);
    renderer.drawTriangle(fX + 80.0f, 150.0f, fX + 115.0f, 150.0f, fX + 95.0f + fireCycle2 * 0.3f, 230.0f + fireCycle2, 1.0f, 0.55f, 0.10f);
    renderer.drawTriangle(fX + 105.0f, 150.0f, fX + 135.0f, 150.0f, fX + 120.0f + fireCycle3 * 0.3f, 210.0f + fireCycle3, 1.0f, 0.35f, 0.05f);
    // Inner yellow-hot core
    renderer.drawTriangle(fX + 75.0f, 150.0f, fX + 105.0f, 150.0f, fX + 90.0f, 195.0f + fireCycle2 * 0.5f, 1.0f, 0.90f, 0.25f);

    // Rising fireplace ember sparks
    for (int sp = 0; sp < 4; ++sp) {
        float sparkY = 220.0f + std::fmod((roundTimer * 90.0f + sp * 35.0f), 90.0f);
        float sparkX = fX + 80.0f + std::sin(sparkY * 0.08f + sp) * 20.0f;
        renderer.drawRect(sparkX, sparkY, 3.0f, 3.0f, 1.0f, 0.75f, 0.2f, 0.85f);
    }

    // 5. Grand Golden Chandeliers with Glowing Candle Flames
    float chandX[2] = { 360.0f, 920.0f };
    for (int c = 0; c < 2; ++c) {
        float cx = chandX[c];
        // Hanging brass support chain
        renderer.drawBresenhamLine((int)cx, 720, (int)cx, 580, 0.75f, 0.65f, 0.25f);
        // Chandelier horizontal curving arms
        renderer.drawRect(cx - 70.0f, 576.0f, 140.0f, 8.0f, 0.85f, 0.72f, 0.22f);
        renderer.drawTriangle(cx - 80.0f, 580.0f, cx + 80.0f, 580.0f, cx, 555.0f, 0.70f, 0.58f, 0.18f);

        // 6 Candle fixtures with flickering flames & radial light halo
        for (int k = -2; k <= 2; ++k) {
            float kx = cx + (float)k * 30.0f;
            renderer.drawRect(kx - 3.0f, 582.0f, 6.0f, 18.0f, 0.95f, 0.95f, 0.92f);
            float flameFlicker = std::sin(roundTimer * 20.0f + (float)k) * 2.0f;
            renderer.drawTriangle(kx - 4.0f, 600.0f, kx + 4.0f, 600.0f, kx, 614.0f + flameFlicker, 1.0f, 0.85f, 0.25f);
        }
        // Warm chandelier light bloom halo
        glBegin(GL_QUADS);
            glColor4f(1.0f, 0.85f, 0.35f, 0.14f);
            glVertex2f(cx - 130.0f, 630.0f);
            glVertex2f(cx + 130.0f, 630.0f);
            glColor4f(1.0f, 0.65f, 0.15f, 0.0f);
            glVertex2f(cx + 170.0f, 480.0f);
            glVertex2f(cx - 170.0f, 480.0f);
        glEnd();
    }

    // 6. Victorian Dark Mahogany Wood Wainscoting along back wall
    renderer.drawGradientRect(40.0f, 140.0f, 1200.0f, 90.0f,
                              0.18f, 0.10f, 0.06f, 0.28f, 0.16f, 0.10f);
    for (int p = 60; p < 1220; p += 40) {
        renderer.drawRect((float)p, 142.0f, 2.0f, 86.0f, 0.12f, 0.07f, 0.04f);
    }
    renderer.drawRect(40.0f, 226.0f, 1200.0f, 6.0f, 0.85f, 0.72f, 0.25f); // Gilded rail trim

    // 7. Polished Marble & Parquet Floor Platform (Glossy Reflections)
    renderer.beginLighting();
    renderer.setMaterial(0.55f, 0.45f, 0.25f, 48.0f);
    renderer.drawTexturedFloor(40.0f, 0.0f, 1200.0f, 140.0f);
    renderer.resetMaterial();
    renderer.endLighting();

    // Floor Parquet Wood Slabs & Specular Reflection Lines
    for (int fx = 70; fx < 1220; fx += 80) {
        renderer.drawRect((float)fx, 138.0f, 18.0f, 2.0f, 1.0f, 0.85f, 0.45f, 0.45f);
    }

    // 8. Heavy Crimson Velvet Drapes with Golden Tiebacks & Tassels (Left & Right)
    // Left Drapes
    renderer.drawGradientRect(0.0f, 140.0f, 110.0f, 580.0f,
                              0.58f, 0.05f, 0.08f, 0.38f, 0.03f, 0.05f);
    renderer.drawRect(20.0f, 140.0f, 12.0f, 580.0f, 0.28f, 0.02f, 0.04f);
    renderer.drawRect(55.0f, 140.0f, 14.0f, 580.0f, 0.28f, 0.02f, 0.04f);
    renderer.drawRect(85.0f, 140.0f, 12.0f, 580.0f, 0.28f, 0.02f, 0.04f);
    // Left Gold Tieback & Tassel
    renderer.drawRect(10.0f, 380.0f, 100.0f, 8.0f, 0.92f, 0.78f, 0.22f);
    renderer.drawTriangle(75.0f, 380.0f, 95.0f, 380.0f, 85.0f, 345.0f, 0.95f, 0.82f, 0.25f);

    // Right Drapes
    renderer.drawGradientRect(1170.0f, 140.0f, 110.0f, 580.0f,
                              0.58f, 0.05f, 0.08f, 0.38f, 0.03f, 0.05f);
    renderer.drawRect(1185.0f, 140.0f, 12.0f, 580.0f, 0.28f, 0.02f, 0.04f);
    renderer.drawRect(1215.0f, 140.0f, 14.0f, 580.0f, 0.28f, 0.02f, 0.04f);
    renderer.drawRect(1248.0f, 140.0f, 12.0f, 580.0f, 0.28f, 0.02f, 0.04f);
    // Right Gold Tieback & Tassel
    renderer.drawRect(1170.0f, 380.0f, 100.0f, 8.0f, 0.92f, 0.78f, 0.22f);
    renderer.drawTriangle(1185.0f, 380.0f, 1205.0f, 380.0f, 1195.0f, 345.0f, 0.95f, 0.82f, 0.25f);

    // 9. Practical No. 05: Cohen-Sutherland Viewport Line Clipping (Toggled with [F1] Hitboxes/Debug)
    if (debugHitboxes) {
        int clipXmin = 280, clipYmin = 160, clipXmax = 1000, clipYmax = 520;
        renderer.drawRectOutline((float)clipXmin, (float)clipYmin, 
                                 (float)(clipXmax - clipXmin), (float)(clipYmax - clipYmin),
                                 0.85f, 0.72f, 0.25f, 1.5f);
        renderer.drawText((float)(clipXmin + 12), (float)(clipYmax - 18), 
                          "MANSION COMBAT ZONE [PRAC 05 CLIPPING]", 0.85f, 0.75f, 0.35f, 1.1f);

        float sweepOffset = std::sin(clippingLaserScanAngle) * 140.0f;
        int rawX1 = 160;
        int rawY1 = (int)(490.0f + sweepOffset);
        int rawX2 = 1120;
        int rawY2 = (int)(190.0f - sweepOffset);

        renderer.drawBresenhamLine(rawX1, rawY1, rawX2, rawY2, 0.28f, 0.22f, 0.25f);
        renderer.drawClippedBresenhamLine(rawX1, rawY1, rawX2, rawY2,
                                          clipXmin, clipYmin, clipXmax, clipYmax,
                                          0.95f, 0.45f, 0.15f);
    }
}

// ============================================================================
// 2. PRACTICE DOJO BACKGROUND: Virtual Training Grid with Distance Marks
// ============================================================================
void GameMatch::renderPracticeDojo(Renderer& renderer) {
    // 1. Cyber Dojo Virtual Backdrop
    renderer.drawGradientRect(0.0f, 0.0f, 1280.0f, 720.0f,
                              0.02f, 0.05f, 0.08f, 0.05f, 0.12f, 0.18f);

    // 2. Dojo Perspective Grid Floor (0m to 10m Distance Markings)
    renderer.drawGradientRect(40.0f, 0.0f, 1200.0f, 140.0f,
                              0.08f, 0.14f, 0.20f, 0.04f, 0.08f, 0.12f);

    // Perspective receding grid lines
    float vpX = 640.0f, vpY = 240.0f;
    for (int gx = 80; gx <= 1200; gx += 70) {
        renderer.drawBresenhamLine(gx, 0, (int)(vpX + (gx - vpX) * 0.45f), (int)vpY, 0.15f, 0.45f, 0.65f);
    }
    for (int gy = 20; gy <= 140; gy += 25) {
        renderer.drawBresenhamLine(40, gy, 1240, gy, 0.15f, 0.45f, 0.65f);
    }

    // Physical Meter Lines and Labels on Floor
    // Center is 0m (x=640), 1 meter = 60 pixels
    for (int m = -5; m <= 5; ++m) {
        float markX = 640.0f + (float)m * 60.0f;
        // Floor tick
        renderer.drawRect(markX - 1.0f, 120.0f, 2.0f, 20.0f, 0.2f, 0.95f, 0.45f);
        // Distance text
        std::string label = (m == 0) ? "0m" : (std::to_string(std::abs(m)) + "m");
        renderer.drawText(markX - 10.0f, 125.0f, label, 0.25f, 0.95f, 0.45f, 1.0f);
    }

    // 145px Combat Sweet-Spot Indicator Ring between fighters
    float dist = std::abs(player1->getPosition().x - player2->getPosition().x);
    float midX = (player1->getPosition().x + player2->getPosition().x) * 0.5f;
    renderer.drawBresenhamCircle((int)midX, 140, 72, dist <= 145.0f ? 0.2f : 0.85f, dist <= 145.0f ? 0.95f : 0.2f, 0.2f);
    renderer.drawText(midX - 55.0f, 148.0f, "145px SWEET SPOT", dist <= 145.0f ? 0.25f : 0.85f, dist <= 145.0f ? 0.98f : 0.35f, 0.2f, 1.0f);

    // Wall Telemetry Banners
    renderer.drawRect(340.0f, 540.0f, 600.0f, 40.0f, 0.04f, 0.10f, 0.16f, 0.92f);
    renderer.drawRectOutline(340.0f, 540.0f, 600.0f, 40.0f, 0.2f, 0.85f, 0.95f, 2.0f);
    std::string telemetry = "DOJO TELEMETRY | DIST: " + std::to_string((int)dist) + " px (" +
                            std::to_string(dist / 60.0f).substr(0, 4) + "m) | HITSTUN: 0.16s";
    renderer.drawText(360.0f, 553.0f, telemetry, 0.2f, 0.95f, 1.0f, 1.3f);
}

// ============================================================================
// 3. CYBER ARENA BACKGROUND: Original Sci-Fi Stage
// ============================================================================
void GameMatch::renderCyberArena(Renderer& renderer) {
    renderer.drawGradientRect(0, 0, 1280, 720, 0.03f, 0.04f, 0.08f, 0.08f, 0.05f, 0.16f);

    renderer.beginLighting();
    renderer.setMaterial(0.35f, 0.5f, 0.7f, 32.0f);
    renderer.drawTexturedFloor(40.0f, 0.0f, 1200.0f, 140.0f);

    renderer.setMaterial(0.4f, 0.6f, 0.8f, 48.0f);
    renderer.drawTexturedPillar(120.0f, 140.0f, 60.0f, 320.0f);
    renderer.drawTexturedPillar(1100.0f, 140.0f, 60.0f, 320.0f);

    renderer.setMaterial(0.85f, 0.65f, 0.25f, 64.0f);
    renderer.drawTexturedPlatform(490.0f, 280.0f, 300.0f, 16.0f);

    renderer.setMaterial(1.0f, 0.9f, 0.4f, 96.0f, 0.2f, 0.15f, 0.05f);
    renderer.drawTexturedCube(640.0f, 340.0f, 48.0f);

    renderer.resetMaterial();
    renderer.endLighting();

    int clipXmin = 280, clipYmin = 160, clipXmax = 1000, clipYmax = 520;
    renderer.drawRectOutline((float)clipXmin, (float)clipYmin, 
                             (float)(clipXmax - clipXmin), (float)(clipYmax - clipYmin),
                             0.15f, 0.45f, 0.75f, 1.5f);
    renderer.drawText((float)(clipXmin + 12), (float)(clipYmax - 18), 
                      "CLIPPING VIEWPORT [PRAC 05]", 0.3f, 0.75f, 0.95f, 1.1f);

    float sweepOffset = std::sin(clippingLaserScanAngle) * 140.0f;
    int rawX1 = 160;
    int rawY1 = (int)(490.0f + sweepOffset);
    int rawX2 = 1120;
    int rawY2 = (int)(190.0f - sweepOffset);

    renderer.drawBresenhamLine(rawX1, rawY1, rawX2, rawY2, 0.25f, 0.28f, 0.35f);
    renderer.drawClippedBresenhamLine(rawX1, rawY1, rawX2, rawY2,
                                      clipXmin, clipYmin, clipXmax, clipYmax,
                                      0.2f, 0.95f, 0.4f);
}

// ============================================================================
// IN-GAME HUD OVERHAUL MATCHING USER REFERENCE IMAGE
// ============================================================================
void GameMatch::renderHUD(Renderer& renderer) {
    // ========================================================================
    // 1. TOP ARCADE SCORES (Standard Street Fighter / Capcom Arcade Layout)
    // ========================================================================
    renderer.drawText(132.0f, 696.0f, "P1 SCORE: 00214800", 0.95f, 0.95f, 0.95f, 1.25f);
    renderer.drawText(970.0f, 696.0f, "P2 SCORE: 00109500", 0.95f, 0.95f, 0.95f, 1.25f);

    // ========================================================================
    // 2. FIGHTER NAMES & WIN COUNTERS (Symmetrically Positioned, Zero Overlap)
    // ========================================================================
    // P1 Name (Left) and Wins (Anchored at right of P1 bar)
    renderer.drawText(132.0f, 675.0f, player1->getName(), 0.98f, 0.85f, 0.20f, 1.55f);
    renderer.drawText(462.0f, 675.0f, "[ WINS: " + std::to_string(p1RoundsWon) + " ]", 0.25f, 0.95f, 0.45f, 1.15f);

    // P2 Wins (Anchored at left of P2 bar) and Name (Right-aligned at 1148)
    renderer.drawText(708.0f, 675.0f, "[ WINS: " + std::to_string(p2RoundsWon) + " ]", 0.25f, 0.95f, 0.45f, 1.15f);
    float p2CharW = 9.0f * 1.55f; // ~13.95f advance per character
    float p2NameW = (float)player2->getName().length() * p2CharW;
    float p2NameX = 1148.0f - p2NameW;
    renderer.drawText(p2NameX, 675.0f, player2->getName(), 0.98f, 0.85f, 0.20f, 1.55f);

    // ========================================================================
    // 3. TOP PORTRAIT CARDS & CONTROLLER TAGS
    // ========================================================================
    // P1 Portrait Box (x=45, y=636, 54x54)
    renderer.drawRect(45.0f, 636.0f, 54.0f, 54.0f, 0.08f, 0.10f, 0.18f);
    renderer.drawRectOutline(45.0f, 636.0f, 54.0f, 54.0f, 0.95f, 0.78f, 0.20f, 2.0f);
    renderer.drawRect(54.0f, 644.0f, 36.0f, 24.0f, 0.92f, 0.16f, 0.16f); // Gi collar
    renderer.drawRect(60.0f, 656.0f, 24.0f, 18.0f, 0.96f, 0.78f, 0.65f); // Skin
    renderer.drawRect(58.0f, 668.0f, 28.0f, 14.0f, 0.45f, 0.25f, 0.12f); // Spiky hair

    // P2 Portrait Box (x=1181, y=636, 54x54)
    renderer.drawRect(1181.0f, 636.0f, 54.0f, 54.0f, 0.08f, 0.10f, 0.18f);
    renderer.drawRectOutline(1181.0f, 636.0f, 54.0f, 54.0f, 0.95f, 0.78f, 0.20f, 2.0f);
    renderer.drawRect(1190.0f, 644.0f, 36.0f, 22.0f, 0.22f, 0.85f, 0.32f); // Green top
    renderer.drawRect(1196.0f, 656.0f, 24.0f, 18.0f, 0.86f, 0.62f, 0.45f); // Bronze skin
    renderer.drawRect(1194.0f, 668.0f, 28.0f, 14.0f, 0.28f, 0.16f, 0.08f); // High ponytail

    // P1 Tag [ L ] (x=105, y=642, 22x26)
    renderer.drawRect(105.0f, 642.0f, 22.0f, 26.0f, 0.15f, 0.45f, 0.95f);
    renderer.drawRectOutline(105.0f, 642.0f, 22.0f, 26.0f, 1.0f, 1.0f, 1.0f, 1.5f);
    renderer.drawText(111.0f, 648.0f, "L", 1.0f, 1.0f, 1.0f, 1.35f);

    // P2 Tag [ R ] (x=1153, y=642, 22x26)
    renderer.drawRect(1153.0f, 642.0f, 22.0f, 26.0f, 0.95f, 0.25f, 0.25f);
    renderer.drawRectOutline(1153.0f, 642.0f, 22.0f, 26.0f, 1.0f, 1.0f, 1.0f, 1.5f);
    renderer.drawText(1159.0f, 648.0f, "R", 1.0f, 1.0f, 1.0f, 1.35f);

    // ========================================================================
    // 4. DUAL SCAN-LINE HEALTH BARS (y = 642.0f, h = 26.0f)
    // ========================================================================
    float p1HpPct = std::max(0.0f, std::min(1.0f, player1->getHealth() / player1->getMaxHealth()));
    float p2HpPct = std::max(0.0f, std::min(1.0f, player2->getHealth() / player2->getMaxHealth()));
    float barWidth = 425.0f;

    // P1 Health Bar (x = 132..557) - drains right to left meeting at timer
    float p1BarX = 132.0f;
    float p1BarY = 642.0f;
    renderer.drawRect(p1BarX, p1BarY, barWidth, 26.0f, 0.65f, 0.08f, 0.08f);
    float p1CurrentW = barWidth * p1HpPct;
    renderer.drawGradientRect(p1BarX + (barWidth - p1CurrentW), p1BarY, p1CurrentW, 26.0f,
                              1.0f, 0.55f, 0.05f, 1.0f, 0.88f, 0.20f);
    renderer.drawRectOutline(p1BarX, p1BarY, barWidth, 26.0f, 0.95f, 0.85f, 0.25f, 2.0f);
    std::string p1PctStr = std::to_string((int)(p1HpPct * 100.0f)) + "%";
    renderer.drawText(p1BarX + barWidth * 0.5f - 18.0f, p1BarY + 6.0f, p1PctStr, 1.0f, 1.0f, 1.0f, 1.3f);

    // P2 Health Bar (x = 723..1148) - drains left to right meeting at timer
    float p2BarX = 723.0f;
    float p2BarY = 642.0f;
    renderer.drawRect(p2BarX, p2BarY, barWidth, 26.0f, 0.65f, 0.08f, 0.08f);
    float p2CurrentW = barWidth * p2HpPct;
    renderer.drawGradientRect(p2BarX, p2BarY, p2CurrentW, 26.0f,
                              1.0f, 0.88f, 0.20f, 1.0f, 0.55f, 0.05f);
    renderer.drawRectOutline(p2BarX, p2BarY, barWidth, 26.0f, 0.95f, 0.85f, 0.25f, 2.0f);
    std::string p2PctStr = std::to_string((int)(p2HpPct * 100.0f)) + "%";
    renderer.drawText(p2BarX + barWidth * 0.5f - 18.0f, p2BarY + 6.0f, p2PctStr, 1.0f, 1.0f, 1.0f, 1.3f);

    // ========================================================================
    // 5. CENTRAL DIGITAL TIMER BOX (x = 575..705, y = 626..694)
    // ========================================================================
    renderer.drawRect(575.0f, 626.0f, 130.0f, 68.0f, 0.05f, 0.07f, 0.14f, 0.98f);
    renderer.drawRectOutline(575.0f, 626.0f, 130.0f, 68.0f, 0.98f, 0.85f, 0.20f, 2.5f);

    std::string timerStr;
    if (maxRoundTimer >= 999.0f) {
        timerStr = "INF";
    } else {
        int displaySeconds = static_cast<int>(std::ceil(roundTimer));
        if (displaySeconds < 0) displaySeconds = 0;
        if (displaySeconds < 100) {
            timerStr = (displaySeconds < 10 ? "0" : "") + std::to_string(displaySeconds);
        } else {
            int mins = displaySeconds / 60;
            int secs = displaySeconds % 60;
            timerStr = std::to_string(mins) + ":" + (secs < 10 ? "0" : "") + std::to_string(secs);
        }
    }
    float timerTextX = (maxRoundTimer >= 999.0f) ? 618.0f : (timerStr.length() > 2 ? 595.0f : 612.0f);
    renderer.drawText(timerTextX, 652.0f, timerStr, 1.0f, 0.92f, 0.12f, 2.8f);
    std::string roundStr = "ROUND " + std::to_string(currentRound);
    renderer.drawText(605.0f, 633.0f, roundStr, 0.98f, 0.85f, 0.20f, 1.25f);

    // ========================================================================
    // 6. BOTTOM SUPER / ULTRA MOVE COMMAND CARDS (Clean Layout, Zero Clutter)
    // ========================================================================
    // P1 Bottom Card (Bottom Left: x=40, y=14, w=220, h=95)
    renderer.drawRect(40.0f, 14.0f, 220.0f, 95.0f, 0.05f, 0.07f, 0.14f, 0.94f);
    renderer.drawRectOutline(40.0f, 14.0f, 220.0f, 95.0f, 0.95f, 0.75f, 0.15f, 2.0f);
    renderer.drawText(50.0f, 92.0f, "P1", 0.98f, 0.85f, 0.20f, 1.35f);
    renderer.drawText(85.0f, 92.0f, "SUPER: 78%", 0.2f, 0.95f, 1.0f, 1.15f);
    renderer.drawRect(50.0f, 82.0f, 200.0f, 5.0f, 0.2f, 0.85f, 0.95f);

    // Hadoken Command Card with Blue Icon
    renderer.drawRect(48.0f, 52.0f, 16.0f, 16.0f, 0.2f, 0.65f, 1.0f);
    renderer.drawText(70.0f, 55.0f, "HADOKEN", 1.0f, 1.0f, 1.0f, 0.95f);
    renderer.drawText(148.0f, 55.0f, "D,DF,F+P", 0.95f, 0.85f, 0.25f, 0.92f);

    // Shoryuken Command Card with Fiery Icon
    renderer.drawRect(48.0f, 24.0f, 16.0f, 16.0f, 1.0f, 0.35f, 0.1f);
    renderer.drawText(70.0f, 27.0f, "SHORYUKEN", 1.0f, 1.0f, 1.0f, 0.95f);
    renderer.drawText(158.0f, 27.0f, "F,D,DF+P", 0.95f, 0.85f, 0.25f, 0.92f);

    // P2 Bottom Card (Bottom Right: x=1020, y=14, w=220, h=95)
    renderer.drawRect(1020.0f, 14.0f, 220.0f, 95.0f, 0.05f, 0.07f, 0.14f, 0.94f);
    renderer.drawRectOutline(1020.0f, 14.0f, 220.0f, 95.0f, 0.20f, 0.85f, 1.0f, 2.0f);
    renderer.drawText(1030.0f, 92.0f, "P2", 0.2f, 0.95f, 1.0f, 1.35f);
    renderer.drawText(1065.0f, 92.0f, "ULTRA: 65%", 0.95f, 0.45f, 0.85f, 1.15f);
    renderer.drawRect(1030.0f, 82.0f, 200.0f, 5.0f, 0.95f, 0.45f, 0.85f);

    // Spin Kick Command Card
    renderer.drawRect(1028.0f, 52.0f, 16.0f, 16.0f, 0.25f, 0.88f, 0.32f);
    renderer.drawText(1048.0f, 55.0f, "SPIN KICK", 1.0f, 1.0f, 1.0f, 0.95f);
    renderer.drawText(1136.0f, 55.0f, "B,D,DB+K", 0.25f, 0.95f, 0.55f, 0.92f);

    // Axe Kick Command Card
    renderer.drawRect(1028.0f, 24.0f, 16.0f, 16.0f, 0.25f, 0.65f, 0.95f);
    renderer.drawText(1048.0f, 27.0f, "AXE KICK", 1.0f, 1.0f, 1.0f, 0.95f);
    renderer.drawText(1136.0f, 27.0f, "D,DB,B+K", 0.25f, 0.95f, 0.55f, 0.92f);

    // ========================================================================
    // 7. SLEEK CENTER TELEMETRY & HOTKEY STRIP (x = 275..995, y = 14..62)
    // ========================================================================
    renderer.drawRect(275.0f, 14.0f, 730.0f, 48.0f, 0.04f, 0.05f, 0.08f, 0.90f);
    renderer.drawRectOutline(275.0f, 14.0f, 730.0f, 48.0f, 0.2f, 0.75f, 0.95f, 1.5f);
    renderer.drawText(285.0f, 46.0f, "COA ALU [x86-64 REGISTERS]: " + collisionSystem.getLatestALUTrace().substr(0, 52), 0.25f, 0.95f, 0.45f, 0.92f);
    renderer.drawText(285.0f, 30.0f, "[F2] CONTROLS | [TAB] SYLLABUS | [M/F3] MODE | [F1] HITBOXES | [F6] LIGHT | [F7] TEX", 0.95f, 0.85f, 0.2f, 0.85f);

    std::string arenaName = (currentArenaTheme == ArenaTheme::MANSION) ? "MANSION THEMED ARENA" :
                            ((currentArenaTheme == ArenaTheme::PRACTICE_DOJO) ? "PRACTICE DOJO" : "CYBER ARENA");
    std::string cgInfo = "ARENA: " + arenaName + " | " + renderer.getLightingModeName() + " | " + renderer.getTexturesModeName();
    renderer.drawText(285.0f, 16.0f, cgInfo, 0.35f, 0.85f, 1.0f, 0.85f);

    // Intermission Banner between rounds
    if (inRoundIntermission) {
        renderer.drawRect(300.0f, 320.0f, 680.0f, 110.0f, 0.05f, 0.07f, 0.12f, 0.95f);
        renderer.drawRectOutline(300.0f, 320.0f, 680.0f, 110.0f, 0.95f, 0.85f, 0.2f, 2.5f);
        renderer.drawText(330.0f, 385.0f, roundEndBanner, 0.2f, 0.95f, 1.0f, 1.9f);
        renderer.drawText(440.0f, 345.0f, "NEXT ROUND INCOMING...", 1.0f, 0.85f, 0.2f, 1.7f);
    }

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

    // 1. Slim Top Mode Bar at very top of screen (y = 702.0f, h = 16.0f)
    renderer.drawRect(340.0f, 702.0f, 600.0f, 16.0f, 0.04f, 0.12f, 0.18f, 0.92f);
    renderer.drawRectOutline(340.0f, 702.0f, 600.0f, 16.0f, 0.2f, 0.9f, 0.45f, 1.5f);
    renderer.drawText(360.0f, 705.0f, "TUTORIAL PRACTICE MODE | PRESS [M] OR [F3] TO SHIFT TO REAL FIGHT", 0.25f, 0.98f, 0.45f, 1.0f);

    // 2. Dynamic Coaching Banner placed neatly BELOW Central Timer (y = 596.0f, h = 24.0f)
    float coachX = 390.0f;
    float coachY = 596.0f;
    float coachW = 500.0f;
    float coachH = 24.0f;

    FighterState s1 = player1->getState();
    FighterState s2 = player2->getState();

    if (s2 == FighterState::BLOCKING || s1 == FighterState::BLOCKING) {
        // Opponent is guarding
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.32f, 0.05f, 0.05f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 1.0f, 0.3f, 0.3f, 1.8f);
        renderer.drawText(coachX + 20.0f, coachY + 6.0f, "GUARD ACTIVE! BREAK WITH SPECIAL: P1 [R] | P2 [U]", 1.0f, 0.4f, 0.4f, 1.1f);
    } else if (s2 == FighterState::PUNCHING || s2 == FighterState::KICKING || s2 == FighterState::SPECIAL_ATTACK ||
               s1 == FighterState::PUNCHING || s1 == FighterState::KICKING || s1 == FighterState::SPECIAL_ATTACK) {
        // Attack incoming
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.35f, 0.20f, 0.04f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 1.0f, 0.75f, 0.2f, 1.8f);
        renderer.drawText(coachX + 20.0f, coachY + 6.0f, "ATTACK INCOMING! DEFEND: P1 BLOCK [H] | P2 BLOCK [L]", 1.0f, 0.85f, 0.25f, 1.1f);
    } else if (distX <= 145.0f && distY < 65.0f) {
        // In striking range & vulnerable
        float pulse = (std::sin(hitIndicatorPulse) + 1.0f) * 0.5f;
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.04f, 0.16f + 0.18f * pulse, 0.08f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 0.2f, 0.95f, 0.35f, 2.0f);
        renderer.drawText(coachX + 16.0f, coachY + 6.0f, ">>> HIT NOW! P1: PUNCH [F]/KICK [G] | P2: PUNCH [J]/KICK [K] <<<", 0.3f, 1.0f, 0.45f, 1.1f);
    } else if (distY >= 65.0f) {
        // Airborne
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.05f, 0.12f, 0.25f, 0.92f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 0.3f, 0.75f, 1.0f, 1.8f);
        renderer.drawText(coachX + 25.0f, coachY + 6.0f, "AIRBORNE! EXECUTE JUMPING ATTACK [F / G / J / K]", 0.45f, 0.85f, 1.0f, 1.1f);
    } else {
        // Spacing / Approach
        renderer.drawRect(coachX, coachY, coachW, coachH, 0.06f, 0.08f, 0.15f, 0.88f);
        renderer.drawRectOutline(coachX, coachY, coachW, coachH, 0.25f, 0.6f, 0.9f, 1.5f);
        renderer.drawText(coachX + 25.0f, coachY + 6.0f, "P1: MOVE [D] -> | <- MOVE [LEFT] : P2 | [W/UP] JUMP", 0.65f, 0.85f, 1.0f, 1.1f);
    }

    // 3. Compact Overhead Action Badges
    std::string p1Action;
    float p1R = 0.2f, p1G = 0.9f, p1B = 0.4f;
    switch (s1) {
        case FighterState::PUNCHING: p1Action = "PUNCH [F]"; p1R = 1.0f; p1G = 0.85f; p1B = 0.2f; break;
        case FighterState::KICKING: p1Action = "KICK [G]"; p1R = 1.0f; p1G = 0.5f; p1B = 0.2f; break;
        case FighterState::SPECIAL_ATTACK: p1Action = player1->getSpecialMoveName() + " [R]"; p1R = 1.0f; p1G = 0.95f; p1B = 0.2f; break;
        case FighterState::BLOCKING: p1Action = "GUARD [H]"; p1R = 0.3f; p1G = 0.7f; p1B = 1.0f; break;
        case FighterState::JUMPING: p1Action = "JUMP [W]"; p1R = 0.4f; p1G = 0.8f; p1B = 1.0f; break;
        case FighterState::CROUCHING: p1Action = "CROUCH [S]"; p1R = 0.8f; p1G = 0.8f; p1B = 0.8f; break;
        case FighterState::WALKING_FORWARD: p1Action = "MOVE [D]"; p1R = 0.4f; p1G = 0.9f; p1B = 0.5f; break;
        case FighterState::WALKING_BACKWARD: p1Action = "RETREAT [A]"; p1R = 0.6f; p1G = 0.7f; p1B = 0.9f; break;
        case FighterState::HIT_STUN: p1Action = "STUNNED!"; p1R = 0.9f; p1G = 0.2f; p1B = 0.2f; break;
        default: p1Action = (distX <= 145.0f && distY < 65.0f) ? "STRIKE! [F/G]" : "APPROACH [D]"; break;
    }
    float p1BadgeW = (float)p1Action.length() * 8.5f + 14.0f;
    float p1BadgeX = p1X - p1BadgeW * 0.5f;
    float p1BadgeY = p1Y + 115.0f;
    renderer.drawRect(p1BadgeX, p1BadgeY, p1BadgeW, 18.0f, 0.04f, 0.08f, 0.12f, 0.90f);
    renderer.drawRectOutline(p1BadgeX, p1BadgeY, p1BadgeW, 18.0f, p1R, p1G, p1B, 1.4f);
    renderer.drawText(p1BadgeX + 7.0f, p1BadgeY + 4.0f, p1Action, p1R, p1G, p1B, 0.95f);

    // Player 2 Overhead Action Badge
    std::string p2Action;
    float p2R = 0.2f, p2G = 0.8f, p2B = 1.0f;
    switch (s2) {
        case FighterState::PUNCHING: p2Action = "PUNCH [J/1]"; p2R = 1.0f; p2G = 0.85f; p2B = 0.2f; break;
        case FighterState::KICKING: p2Action = "KICK [K/2]"; p2R = 1.0f; p2G = 0.5f; p2B = 0.2f; break;
        case FighterState::SPECIAL_ATTACK: p2Action = player2->getSpecialMoveName() + " [U/5]"; p2R = 1.0f; p2G = 0.95f; p2B = 0.2f; break;
        case FighterState::BLOCKING: p2Action = "GUARD [L/3]"; p2R = 0.3f; p2G = 0.7f; p2B = 1.0f; break;
        case FighterState::JUMPING: p2Action = "JUMP [UP]"; p2R = 0.4f; p2G = 0.8f; p2B = 1.0f; break;
        case FighterState::CROUCHING: p2Action = "CROUCH [DOWN]"; p2R = 0.8f; p2G = 0.8f; p2B = 0.8f; break;
        case FighterState::WALKING_FORWARD: p2Action = "MOVE [LEFT]"; p2R = 0.4f; p2G = 0.9f; p2B = 0.5f; break;
        case FighterState::WALKING_BACKWARD: p2Action = "RETREAT [RIGHT]"; p2R = 0.6f; p2G = 0.7f; p2B = 0.9f; break;
        case FighterState::HIT_STUN: p2Action = "STUNNED!"; p2R = 0.9f; p2G = 0.2f; p2B = 0.2f; break;
        default: p2Action = (distX <= 145.0f && distY < 65.0f) ? "STRIKE! [J/K]" : "APPROACH [LEFT]"; break;
    }
    float p2BadgeW = (float)p2Action.length() * 8.5f + 14.0f;
    float p2BadgeX = p2X - p2BadgeW * 0.5f;
    float p2BadgeY = p2Y + 115.0f;
    renderer.drawRect(p2BadgeX, p2BadgeY, p2BadgeW, 18.0f, 0.04f, 0.08f, 0.12f, 0.90f);
    renderer.drawRectOutline(p2BadgeX, p2BadgeY, p2BadgeW, 18.0f, p2R, p2G, p2B, 1.4f);
    renderer.drawText(p2BadgeX + 7.0f, p2BadgeY + 4.0f, p2Action, p2R, p2G, p2B, 0.95f);
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

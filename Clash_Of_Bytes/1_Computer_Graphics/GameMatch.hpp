#ifndef GAMEMATCH_HPP
#define GAMEMATCH_HPP

#include <memory>
#include "FighterFactory.hpp"
#include "CollisionSystem.hpp"
#include "CO1_Prac01_OpenGL_Gouraud_Renderer.hpp"
#include "ParticleSystem.hpp"
#include "InputManager.hpp"
#include "../2_Programming_Laboratory_DSA/CO1_Prac02_2D_Sparse_Matrix_Fast_Transpose.hpp"
#include "../2_Programming_Laboratory_DSA/CO2_Prac04_Doubly_Linked_List_Match_Scores.hpp"
#include "../2_Programming_Laboratory_DSA/CO2_Prac05_Circular_Linked_List_Arena_Buffs.hpp"
#include "../2_Programming_Laboratory_DSA/CO3_Prac06_Stack_Combat_Formula_Evaluator.hpp"
#include "../2_Programming_Laboratory_DSA/CO3_Combat_Action_Stack_Queue_Scenario.hpp"

enum class GameMode {
    TUTORIAL,
    REAL_FIGHT
};

enum class ArenaTheme {
    MANSION,        // Victorian / Gothic Grand Hall with stained glass, drapes & fireplace
    PRACTICE_DOJO,  // Virtual Training Dojo with perspective floor grid & distance markers
    CYBER_ARENA     // Sci-Fi Cyberpunk Stage with floating platform & 3D transformation cube
};

class GameMatch {
private:
    ArenaTheme currentArenaTheme;
    int p1CharacterIndex;
    int p2CharacterIndex;
    std::unique_ptr<Fighter> player1;
    std::unique_ptr<Fighter> player2;
    CollisionSystem collisionSystem;
    ParticleSystem particleSystem;
    SparseArenaGrid arenaGrid;
    DoublyLinkedList matchHistory;
    CircularBuffQueue arenaBuffQueue;
    CombatActionQueue combatActionQueue;
    CombatActionStack combatActionHistoryStack;
    std::string currentCombatFormulaInfix;
    double currentCombatFormulaResult;

    float roundTimer;
    float maxRoundTimer;
    int currentRound;
    int totalRounds;
    int roundsToWin;
    int p1RoundsWon;
    int p2RoundsWon;
    bool inRoundIntermission;
    float roundIntermissionTimer;
    std::string roundEndBanner;
    float clippingLaserScanAngle;
    bool debugHitboxes;
    bool showSyllabusMonitor;
    bool showHowToPlay;
    float hitIndicatorPulse;
    bool matchOver;
    bool matchResultWritten;
    std::string winnerText;
    std::string player1Name;
    std::string player2Name;

    GameMode currentGameMode;
    float modeNoticeTimer;
    std::string modeNoticeText;

public:
    GameMatch(int p1Type = 0, int p2Type = 1, GameMode initialMode = GameMode::TUTORIAL, int totalRounds = 3, float roundTime = 60.0f, ArenaTheme initialTheme = ArenaTheme::MANSION);
    ~GameMatch();

    void setPlayerNames(const std::string& p1Name, const std::string& p2Name) {
        if (!p1Name.empty()) player1Name = p1Name;
        if (!p2Name.empty()) player2Name = p2Name;
    }
    std::string getPlayer1Name() const { return player1Name; }
    std::string getPlayer2Name() const { return player2Name; }

    void setPlayer1Character(int typeIndex);
    void setPlayer2Character(int typeIndex);
    void cyclePlayer1Character();
    void cyclePlayer2Character();
    void setShowSyllabusMonitor(bool show) { showSyllabusMonitor = show; }
    void setArenaTheme(ArenaTheme theme) { currentArenaTheme = theme; }
    ArenaTheme getArenaTheme() const { return currentArenaTheme; }

    void update(float dt, InputManager& input, Renderer& renderer);
    void render(Renderer& renderer);

private:
    void renderArena(Renderer& renderer);
    void renderMansionArena(Renderer& renderer);
    void renderPracticeDojo(Renderer& renderer);
    void renderCyberArena(Renderer& renderer);
    void renderHUD(Renderer& renderer);
    void renderSyllabusMonitor(Renderer& renderer);
    void renderHowToPlay(Renderer& renderer);
    void renderTutorialIndicators(Renderer& renderer);
    void renderModeNotice(Renderer& renderer);
    void shiftGameMode(GameMode newMode);
    void resetMatch();
};

#endif // GAMEMATCH_HPP

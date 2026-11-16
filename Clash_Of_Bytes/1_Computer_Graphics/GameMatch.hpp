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

class GameMatch {
private:
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
    int currentRound;
    int p1RoundsWon;
    int p2RoundsWon;
    float clippingLaserScanAngle;
    bool debugHitboxes;
    bool showSyllabusMonitor;
    bool showHowToPlay;
    float hitIndicatorPulse;
    bool matchOver;
    std::string winnerText;

    GameMode currentGameMode;
    float modeNoticeTimer;
    std::string modeNoticeText;

public:
    GameMatch(int p1Type = 0, int p2Type = 1, GameMode initialMode = GameMode::TUTORIAL);
    ~GameMatch();

    void setPlayer1Character(int typeIndex);
    void setPlayer2Character(int typeIndex);
    void cyclePlayer1Character();
    void cyclePlayer2Character();
    void setShowSyllabusMonitor(bool show) { showSyllabusMonitor = show; }

    void update(float dt, InputManager& input, Renderer& renderer);
    void render(Renderer& renderer);

private:
    void renderArena(Renderer& renderer);
    void renderHUD(Renderer& renderer);
    void renderSyllabusMonitor(Renderer& renderer);
    void renderHowToPlay(Renderer& renderer);
    void renderTutorialIndicators(Renderer& renderer);
    void renderModeNotice(Renderer& renderer);
    void shiftGameMode(GameMode newMode);
    void resetMatch();
};

#endif // GAMEMATCH_HPP

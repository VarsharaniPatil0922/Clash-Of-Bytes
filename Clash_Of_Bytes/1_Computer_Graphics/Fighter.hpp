#ifndef FIGHTER_HPP
#define FIGHTER_HPP

/*
 * Subject: Computer Graphics & Game Physics
 * Topic: Fighter State Machine, AABB Bounding Boxes & Collision Hurtboxes
 */
#include <string>
#include "../2_Programming_Laboratory_DSA/Vector2D.hpp"
#include "CO1_Prac01_OpenGL_Gouraud_Renderer.hpp"

enum class FighterState {
    IDLE,
    WALKING_FORWARD,
    WALKING_BACKWARD,
    JUMPING,
    CROUCHING,
    PUNCHING,
    KICKING,
    SPECIAL_ATTACK,
    BLOCKING,
    HIT_STUN,
    KNOCKED_OUT
};

struct Hitbox {
    float offsetX, offsetY;
    float width, height;
    bool active;
};

class Fighter {
protected:
    int playerIndex;
    std::string characterName;
    Vector2D position;
    Vector2D velocity;
    bool isFacingRight;
    bool isGrounded;

    float health;
    float maxHealth;
    float attackPower;
    float defense;

    FighterState currentState;
    float stateTimer;
    float animTimer;
    float attackCooldown;
    float flinchAngle;

    Hitbox hurtbox;
    Hitbox activeHitbox;
    int currentDamage;
    int comboCount;

    // Movement constants
    const float MOVE_SPEED = 240.0f;
    const float JUMP_FORCE = 680.0f;
    const float GRAVITY = 1500.0f;
    const float GROUND_Y = 140.0f;
    const float GROUND_FRICTION = 950.0f;

public:
    Fighter(int pIndex, const std::string& name, float startX, float startY, bool faceRight, float hp, float atk, float def);
    virtual ~Fighter() {}

    virtual void update(float dt);
    virtual void render(Renderer& renderer, bool debugHitboxes) = 0;
    virtual void executeSpecial() = 0;

    void handleInput(bool left, bool right, bool jump, bool crouch, bool punch, bool kick, bool block, bool special);
    void takeHit(int damage, float knockbackDir, bool isSpecial);
    void resetForRound(float startX, float startY, bool faceRight);

    // Getters & Recovery
    Vector2D getPosition() const { return position; }
    float getHealth() const { return health; }
    float getMaxHealth() const { return maxHealth; }
    bool isAlive() const { return health > 0.0f; }
    void heal(float amount) { health = std::min(maxHealth, health + amount); }
    void restoreHealth() { health = maxHealth; }
    bool isFacingRightDir() const { return isFacingRight; }
    void setFacingRight(bool faceRight) { isFacingRight = faceRight; }
    std::string getName() const { return characterName; }
    FighterState getState() const { return currentState; }
    int getCombo() const { return comboCount; }

    Hitbox getHurtboxWorld() const;
    Hitbox getActiveHitboxWorld() const;
    int getAttackDamage() const { return currentDamage; }
    void clearActiveHitbox() { activeHitbox.active = false; }
    virtual std::string getSpecialMoveName() const { return "SPECIAL ATTACK"; }
    virtual std::string getRoleName() const { return "Fighter"; }
};

#endif // FIGHTER_HPP

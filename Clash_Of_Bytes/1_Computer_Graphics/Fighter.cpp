#include "Fighter.hpp"
#include <algorithm>

Fighter::Fighter(int pIndex, const std::string& name, float startX, float startY, bool faceRight, float hp, float atk, float def)
    : playerIndex(pIndex), characterName(name), position(startX, startY), velocity(0.0f, 0.0f),
      isFacingRight(faceRight), isGrounded(true), health(hp), maxHealth(hp), attackPower(atk), defense(def),
      currentState(FighterState::IDLE), stateTimer(0.0f), animTimer(0.0f), attackCooldown(0.0f),
      flinchAngle(0.0f), currentDamage(0), comboCount(0), hitStopTimer(0.0f)
{
    hurtbox = {-35.0f, 0.0f, 70.0f, 150.0f, true};
    activeHitbox = {0.0f, 0.0f, 0.0f, 0.0f, false};
}

void Fighter::resetForRound(float startX, float startY, bool faceRight) {
    position = Vector2D(startX, startY);
    velocity = Vector2D(0.0f, 0.0f);
    isFacingRight = faceRight;
    isGrounded = true;
    health = maxHealth;
    currentState = FighterState::IDLE;
    stateTimer = 0.0f;
    flinchAngle = 0.0f;
    hitStopTimer = 0.0f;
    activeHitbox.active = false;
}

void Fighter::handleInput(bool left, bool right, bool jump, bool crouch, bool punch, bool kick, bool block, bool special) {
    if (currentState == FighterState::HIT_STUN || currentState == FighterState::KNOCKED_OUT) return;

    // Block state
    if (block && isGrounded) {
        currentState = FighterState::BLOCKING;
        velocity.x = 0.0f;
        return;
    }

    // Attacks (only if not already attacking)
    if (currentState != FighterState::PUNCHING && 
        currentState != FighterState::KICKING && 
        currentState != FighterState::SPECIAL_ATTACK) 
    {
        if (special && attackCooldown <= 0.0f) {
            currentState = FighterState::SPECIAL_ATTACK;
            stateTimer = 0.0f;
            executeSpecial();
            return;
        }
        if (punch && attackCooldown <= 0.0f) {
            currentState = FighterState::PUNCHING;
            stateTimer = 0.0f;
            currentDamage = (int)(attackPower + 4);
            activeHitbox = {isFacingRight ? 25.0f : -75.0f, 85.0f, 55.0f, 30.0f, true};
            return;
        }
        if (kick && attackCooldown <= 0.0f) {
            currentState = FighterState::KICKING;
            stateTimer = 0.0f;
            currentDamage = (int)(attackPower + 8);
            activeHitbox = {isFacingRight ? 25.0f : -85.0f, 40.0f, 65.0f, 35.0f, true};
            return;
        }
    }

    // Movement while attacking is locked
    if (currentState == FighterState::PUNCHING || 
        currentState == FighterState::KICKING || 
        currentState == FighterState::SPECIAL_ATTACK) return;

    // Jump with air velocity preservation
    if (jump && isGrounded) {
        velocity.y = JUMP_FORCE;
        isGrounded = false;
        currentState = FighterState::JUMPING;
        if (left) {
            velocity.x = -MOVE_FORWARD_SPEED;
        } else if (right) {
            velocity.x = MOVE_FORWARD_SPEED;
        }
    }

    // Horizontal Movement
    if (isGrounded) {
        if (left) {
            float spd = isFacingRight ? MOVE_BACKWARD_SPEED : MOVE_FORWARD_SPEED;
            velocity.x = -spd;
            currentState = isFacingRight ? FighterState::WALKING_BACKWARD : FighterState::WALKING_FORWARD;
        } else if (right) {
            float spd = isFacingRight ? MOVE_FORWARD_SPEED : MOVE_BACKWARD_SPEED;
            velocity.x = spd;
            currentState = isFacingRight ? FighterState::WALKING_FORWARD : FighterState::WALKING_BACKWARD;
        } else {
            velocity.x = 0.0f;
            if (currentState != FighterState::CROUCHING) {
                currentState = FighterState::IDLE;
            }
        }
    } else {
        // Air steering momentum
        if (left) {
            velocity.x = std::max(velocity.x - 18.0f, -MOVE_FORWARD_SPEED);
        } else if (right) {
            velocity.x = std::min(velocity.x + 18.0f, MOVE_FORWARD_SPEED);
        }
    }

    if (crouch && isGrounded) {
        currentState = FighterState::CROUCHING;
        velocity.x = 0.0f;
    }
}

void Fighter::takeHit(int damage, float knockbackDir, bool isSpecial) {
    if (currentState == FighterState::KNOCKED_OUT) return;

    if (currentState == FighterState::BLOCKING) {
        damage = std::max(1, damage / 4); // 75% damage reduction when blocking
        knockbackDir *= 0.4f;
    }

    health -= damage;
    comboCount++;

    if (health <= 0.0f) {
        health = 0.0f;
        currentState = FighterState::KNOCKED_OUT;
        velocity.y = 350.0f; // KO Launch Arc
        velocity.x = knockbackDir * 280.0f;
        isGrounded = false;
        flinchAngle = isFacingRight ? -45.0f : 45.0f;
        return;
    }

    // Grounded hit response
    currentState = FighterState::HIT_STUN;
    stateTimer = 0.0f;
    activeHitbox.active = false;
    flinchAngle = isFacingRight ? -12.0f : 12.0f;
    triggerHitStop(0.04f); // 40ms impact hit-stop

    if (isGrounded) {
        velocity.y = 0.0f;
        velocity.x = knockbackDir * (isSpecial ? 230.0f : 150.0f);
    } else {
        velocity.y = 100.0f;
        velocity.x = knockbackDir * 170.0f;
    }
}

void Fighter::update(float dt) {
    if (hitStopTimer > 0.0f) {
        hitStopTimer -= dt;
        return; // Freeze momentarily on hit impact
    }

    stateTimer += dt;
    animTimer += dt;
    if (attackCooldown > 0.0f) attackCooldown -= dt;

    // Apply gravity
    if (!isGrounded || position.y > GROUND_Y) {
        velocity.y -= GRAVITY * dt;
    }

    // Apply ground friction to slide to a stop smoothly
    if (isGrounded) {
        if (velocity.x > 0.0f) {
            velocity.x -= GROUND_FRICTION * dt;
            if (velocity.x < 0.0f) velocity.x = 0.0f;
        } else if (velocity.x < 0.0f) {
            velocity.x += GROUND_FRICTION * dt;
            if (velocity.x > 0.0f) velocity.x = 0.0f;
        }
    }

    // Integrate position
    position += velocity * dt;

    // Floor collision
    if (position.y <= GROUND_Y) {
        position.y = GROUND_Y;
        velocity.y = 0.0f;
        isGrounded = true;
        if (currentState == FighterState::JUMPING) {
            currentState = FighterState::IDLE;
        }
    }

    // Screen boundaries
    if (position.x < 60.0f) position.x = 60.0f;
    if (position.x > 1220.0f) position.x = 1220.0f;

    // Attack timer expiration
    if (currentState == FighterState::PUNCHING) {
        if (stateTimer >= 0.22f) {
            activeHitbox.active = false;
            currentState = FighterState::IDLE;
            attackCooldown = 0.10f;
        }
    } else if (currentState == FighterState::KICKING) {
        if (stateTimer >= 0.30f) {
            activeHitbox.active = false;
            currentState = FighterState::IDLE;
            attackCooldown = 0.14f;
        }
    } else if (currentState == FighterState::SPECIAL_ATTACK) {
        if (stateTimer >= 0.50f) {
            activeHitbox.active = false;
            currentState = FighterState::IDLE;
            attackCooldown = 0.35f;
        }
    } else if (currentState == FighterState::HIT_STUN) {
        // Fast, crisp flinch recovery (reduced from 0.28s to 0.16s)
        flinchAngle *= 0.78f;
        if (stateTimer >= 0.16f) {
            currentState = FighterState::IDLE;
            flinchAngle = 0.0f;
            comboCount = 0;
        }
    }
}

Hitbox Fighter::getHurtboxWorld() const {
    Hitbox h = hurtbox;
    h.offsetX += position.x;
    h.offsetY += position.y;
    return h;
}

Hitbox Fighter::getActiveHitboxWorld() const {
    Hitbox h = activeHitbox;
    h.offsetX += position.x;
    h.offsetY += position.y;
    return h;
}

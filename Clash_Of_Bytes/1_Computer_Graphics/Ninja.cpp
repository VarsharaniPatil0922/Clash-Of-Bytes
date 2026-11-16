#include "Ninja.hpp"

Ninja::Ninja(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "SHADOW NINJA", startX, startY, faceRight, 95.0f, 15.0f, 4.0f),
      shurikenAngle(0.0f), shurikenDist(0.0f) {}

void Ninja::update(float dt) {
    Fighter::update(dt);
    if (currentState == FighterState::SPECIAL_ATTACK) {
        shurikenAngle += 720.0f * dt;
        shurikenDist += 500.0f * dt;
    } else {
        shurikenAngle = 0.0f;
        shurikenDist = 0.0f;
    }
}

void Ninja::executeSpecial() {
    currentDamage = (int)(attackPower * 1.6f);
    activeHitbox = {isFacingRight ? 30.0f : -180.0f, 65.0f, 150.0f, 40.0f, true};
    shurikenDist = 30.0f;
}

void Ninja::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // Practical No. 01: Shaded Body (Midnight Blue / Black)
        renderer.drawRect(-20.0f * dir, 35.0f, 40.0f * dir, 70.0f, 0.12f, 0.14f, 0.25f);
        // Shinobi Sash (Cyan Gradient)
        renderer.drawGradientRect(-16.0f * dir, 55.0f, 32.0f * dir, 25.0f, 0.1f, 0.8f, 0.95f, 0.05f, 0.4f, 0.7f);
        // Masked Head
        renderer.drawRect(-15.0f * dir, 105.0f, 30.0f * dir, 32.0f, 0.1f, 0.1f, 0.18f);
        renderer.drawRect(-12.0f * dir, 118.0f, 24.0f * dir, 8.0f, 0.95f, 0.95f, 0.95f); // Glowing ninja eyes visor

        // Legs
        renderer.drawRect(-18.0f * dir, 0.0f, 15.0f * dir, 35.0f, 0.08f, 0.08f, 0.14f);
        renderer.drawRect(3.0f * dir, 0.0f, 15.0f * dir, 35.0f, 0.08f, 0.08f, 0.14f);

        // Attacks
        if (currentState == FighterState::PUNCHING) {
            // Rapid Kunai Jab
            renderer.drawRect(18.0f * dir, 80.0f, 45.0f * dir, 14.0f, 0.7f, 0.8f, 0.95f);
        } else if (currentState == FighterState::KICKING) {
            // Crescent Kick
            renderer.drawRect(15.0f * dir, 45.0f, 48.0f * dir, 16.0f, 0.2f, 0.6f, 0.9f);
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Practical No. 04: Rotating Shuriken with Matrix Transformations!
            renderer.drawRotatingShuriken(shurikenDist * dir, 80.0f, 22.0f, shurikenAngle);
        } else {
            // Idle Ninja stance
            renderer.drawRect(10.0f * dir, 60.0f, 14.0f * dir, 35.0f, 0.12f, 0.14f, 0.25f);
        }

    glPopMatrix();

    if (debugHitboxes) {
        Hitbox hurt = getHurtboxWorld();
        renderer.drawRectOutline(hurt.offsetX, hurt.offsetY, hurt.width, hurt.height, 0.2f, 0.9f, 0.2f, 1.5f);

        if (activeHitbox.active) {
            Hitbox hit = getActiveHitboxWorld();
            renderer.drawRectOutline(hit.offsetX, hit.offsetY, hit.width, hit.height, 0.95f, 0.2f, 0.2f, 2.0f);
        }
    }
}

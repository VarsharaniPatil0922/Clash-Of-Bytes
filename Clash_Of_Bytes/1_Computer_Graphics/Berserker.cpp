#include "Berserker.hpp"
#include <cmath>

Berserker::Berserker(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "BLAZE BERSERKER", startX, startY, faceRight, 130.0f, 22.0f, 4.0f),
      flameTimer(0.0f) {}

void Berserker::update(float dt) {
    Fighter::update(dt);
    flameTimer += 10.0f * dt;
}

void Berserker::executeSpecial() {
    currentDamage = (int)(attackPower * 2.0f); // 2.0x Rage Multiplier
    activeHitbox = {isFacingRight ? 20.0f : -180.0f, 20.0f, 175.0f, 110.0f, true};
}

void Berserker::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // 1. Volcanic Obsidian Torso with Molten Veins
        renderer.drawRect(-26.0f * dir, 35.0f, 52.0f * dir, 75.0f, 0.12f, 0.10f, 0.12f);
        // Burning Lava Veins (Gradient)
        renderer.drawGradientRect(-20.0f * dir, 45.0f, 40.0f * dir, 55.0f, 1.0f, 0.35f, 0.0f, 0.9f, 0.15f, 0.0f);
        renderer.drawRect(-12.0f * dir, 55.0f, 24.0f * dir, 12.0f, 1.0f, 0.85f, 0.1f);

        // 2. Demon Horned War Helm
        renderer.drawRect(-18.0f * dir, 110.0f, 36.0f * dir, 35.0f, 0.15f, 0.12f, 0.14f);
        // Burning Red Eyes
        renderer.drawRect(-12.0f * dir, 122.0f, 8.0f * dir, 6.0f, 1.0f, 0.1f, 0.1f);
        renderer.drawRect(4.0f * dir, 122.0f, 8.0f * dir, 6.0f, 1.0f, 0.1f, 0.1f);
        // Horn Left
        renderer.drawTriangle(-18.0f * dir, 135.0f, -32.0f * dir, 165.0f, -10.0f * dir, 142.0f, 0.95f, 0.4f, 0.1f);
        // Horn Right
        renderer.drawTriangle(18.0f * dir, 135.0f, 32.0f * dir, 165.0f, 10.0f * dir, 142.0f, 0.95f, 0.4f, 0.1f);

        // 3. Spiked Volcanic Greaves
        renderer.drawRect(-23.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.15f, 0.10f, 0.12f);
        renderer.drawRect(5.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.15f, 0.10f, 0.12f);
        renderer.drawTriangle(-23.0f * dir, 15.0f, -30.0f * dir, 25.0f, -23.0f * dir, 35.0f, 1.0f, 0.3f, 0.0f);
        renderer.drawTriangle(23.0f * dir, 15.0f, 30.0f * dir, 25.0f, 23.0f * dir, 35.0f, 1.0f, 0.3f, 0.0f);

        // 4. Attacks & Massive Inferno Greatsword
        if (currentState == FighterState::PUNCHING) {
            // Molten Claw Strike
            glPushMatrix();
                glTranslatef(20.0f * dir, 80.0f, 0.0f);
                renderer.drawRect(0.0f, -10.0f, 48.0f * dir, 22.0f, 0.95f, 0.35f, 0.05f);
                renderer.drawTriangle(45.0f * dir, -12.0f, 62.0f * dir, 0.0f, 45.0f * dir, 12.0f, 1.0f, 0.85f, 0.2f);
            glPopMatrix();
        } else if (currentState == FighterState::KICKING) {
            // Volcanic Dropkick
            glPushMatrix();
                glTranslatef(16.0f * dir, 32.0f, 0.0f);
                glRotatef(dir * 35.0f, 0.0f, 0.0f, 1.0f);
                renderer.drawRect(0.0f, -8.0f, 52.0f * dir, 24.0f, 0.85f, 0.25f, 0.05f);
                renderer.drawTriangle(50.0f * dir, -10.0f, 66.0f * dir, 4.0f, 50.0f * dir, 16.0f, 1.0f, 0.6f, 0.1f);
            glPopMatrix();
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Inferno Greatsword Rend (Massive fiery wave eruption)
            renderer.triggerScreenShake(7.0f, 0.18f);
            renderer.drawTriangle(20.0f * dir, 10.0f, 90.0f * dir, 130.0f, 130.0f * dir, 10.0f, 1.0f, 0.25f, 0.05f);
            renderer.drawTriangle(35.0f * dir, 10.0f, 90.0f * dir, 100.0f, 115.0f * dir, 10.0f, 1.0f, 0.85f, 0.15f);
        } else {
            // Idle Stance: Massive Greatsword on back
            renderer.drawRect(-12.0f * dir, 45.0f, 12.0f * dir, 90.0f, 0.3f, 0.3f, 0.35f);
            renderer.drawRect(-16.0f * dir, 70.0f, 20.0f * dir, 10.0f, 1.0f, 0.4f, 0.1f);
            renderer.drawRect(14.0f * dir, 55.0f, 16.0f * dir, 40.0f, 0.2f, 0.15f, 0.18f);
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

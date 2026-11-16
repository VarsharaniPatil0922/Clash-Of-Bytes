#include "Gladiator.hpp"
#include <cmath>

Gladiator::Gladiator(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "TITAN GLADIATOR", startX, startY, faceRight, 140.0f, 24.0f, 8.0f),
      quakeTimer(0.0f) {}

void Gladiator::update(float dt) {
    Fighter::update(dt);
    quakeTimer += 8.0f * dt;
}

void Gladiator::executeSpecial() {
    currentDamage = (int)(attackPower * 1.9f);
    activeHitbox = {isFacingRight ? 20.0f : -190.0f, 0.0f, 180.0f, 95.0f, true};
}

void Gladiator::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // 1. Bronze Cuirass & Royal Imperial Purple Tunic
        renderer.drawRect(-26.0f * dir, 35.0f, 52.0f * dir, 75.0f, 0.75f, 0.48f, 0.20f);
        // Purple Sash across chest
        renderer.drawGradientRect(-22.0f * dir, 45.0f, 44.0f * dir, 28.0f, 0.50f, 0.12f, 0.65f, 0.35f, 0.08f, 0.45f);
        // Bronze Pteruges (Armor Skirt)
        renderer.drawRect(-24.0f * dir, 28.0f, 48.0f * dir, 12.0f, 0.85f, 0.60f, 0.25f);

        // 2. Centurion Crested Bronze Helm
        renderer.drawRect(-18.0f * dir, 110.0f, 36.0f * dir, 35.0f, 0.78f, 0.52f, 0.22f);
        renderer.drawRect(-14.0f * dir, 120.0f, 28.0f * dir, 8.0f, 0.15f, 0.12f, 0.10f);
        // Tall Crimson Transverse Crest (Horsehair plumage)
        renderer.drawRect(-4.0f * dir, 142.0f, 8.0f * dir, 24.0f, 0.88f, 0.15f, 0.15f);
        renderer.drawTriangle(-18.0f * dir, 142.0f, 0.0f, 168.0f, 18.0f * dir, 142.0f, 0.95f, 0.20f, 0.20f);

        // 3. Bronze Greaves & Muscle Shin Guards
        renderer.drawRect(-23.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.70f, 0.45f, 0.18f);
        renderer.drawRect(5.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.70f, 0.45f, 0.18f);

        // 4. Attacks & Spiked Cestus
        if (currentState == FighterState::PUNCHING) {
            // Spiked Cestus Titan Hook
            glPushMatrix();
                glTranslatef(20.0f * dir, 80.0f, 0.0f);
                renderer.drawRect(0.0f, -12.0f, 48.0f * dir, 24.0f, 0.45f, 0.45f, 0.50f);
                renderer.drawTriangle(46.0f * dir, -14.0f, 64.0f * dir, 0.0f, 46.0f * dir, 14.0f, 0.95f, 0.85f, 0.2f);
            glPopMatrix();
        } else if (currentState == FighterState::KICKING) {
            // Spartan Front Kick
            glPushMatrix();
                glTranslatef(16.0f * dir, 35.0f, 0.0f);
                glRotatef(dir * 38.0f, 0.0f, 0.0f, 1.0f);
                renderer.drawRect(0.0f, -8.0f, 55.0f * dir, 24.0f, 0.75f, 0.50f, 0.22f);
            glPopMatrix();
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Colosseum Wrath of Bytes: Ground Earthquake Spikes
            renderer.triggerScreenShake(8.0f, 0.22f);
            // Jagged stone spikes erupting from ground
            renderer.drawTriangle(20.0f * dir, 0.0f, 45.0f * dir, 80.0f, 70.0f * dir, 0.0f, 0.65f, 0.45f, 0.25f);
            renderer.drawTriangle(65.0f * dir, 0.0f, 95.0f * dir, 110.0f, 125.0f * dir, 0.0f, 0.75f, 0.55f, 0.30f);
            renderer.drawTriangle(120.0f * dir, 0.0f, 145.0f * dir, 70.0f, 170.0f * dir, 0.0f, 0.55f, 0.35f, 0.20f);
        } else {
            // Idle Stance with Spiked Cestus Bracers
            renderer.drawRect(15.0f * dir, 55.0f, 18.0f * dir, 40.0f, 0.45f, 0.45f, 0.50f);
            renderer.drawTriangle(15.0f * dir, 65.0f, 32.0f * dir, 70.0f, 15.0f * dir, 75.0f, 0.95f, 0.85f, 0.2f);
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

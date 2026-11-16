#include "Paladin.hpp"
#include <cmath>

Paladin::Paladin(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "AURA PALADIN", startX, startY, faceRight, 120.0f, 17.0f, 7.5f),
      haloAnim(0.0f), holyAuraHeight(0.0f) {}

void Paladin::update(float dt) {
    Fighter::update(dt);
    haloAnim += 4.0f * dt;
    if (currentState == FighterState::SPECIAL_ATTACK) {
        holyAuraHeight = std::min(400.0f, holyAuraHeight + 900.0f * dt);
    } else {
        holyAuraHeight = 0.0f;
    }
}

void Paladin::executeSpecial() {
    currentDamage = (int)(attackPower * 1.70f);
    activeHitbox = {isFacingRight ? 15.0f : -160.0f, 0.0f, 145.0f, 320.0f, true};
    holyAuraHeight = 50.0f;
}

void Paladin::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // 1. Holy Silver & Gold Armor
        // Silver Cuirass
        renderer.drawRect(-25.0f * dir, 35.0f, 50.0f * dir, 75.0f, 0.88f, 0.90f, 0.95f);
        // Golden Breastplate Inlay
        renderer.drawGradientRect(-18.0f * dir, 48.0f, 36.0f * dir, 52.0f, 1.0f, 0.85f, 0.2f, 0.85f, 0.65f, 0.1f);
        // Golden Cross Emblem on Chest
        renderer.drawRect(-3.0f * dir, 60.0f, 6.0f * dir, 28.0f, 1.0f, 1.0f, 0.8f);
        renderer.drawRect(-12.0f * dir, 74.0f, 24.0f * dir, 6.0f, 1.0f, 1.0f, 0.8f);

        // 2. Helmet with Golden Visor & Floating Halo
        renderer.drawRect(-18.0f * dir, 110.0f, 36.0f * dir, 35.0f, 0.82f, 0.85f, 0.90f);
        renderer.drawRect(-14.0f * dir, 122.0f, 28.0f * dir, 8.0f, 1.0f, 0.85f, 0.2f);
        // Winged crest
        renderer.drawTriangle(-18.0f * dir, 135.0f, -28.0f * dir, 155.0f, -12.0f * dir, 140.0f, 1.0f, 0.85f, 0.2f);
        renderer.drawTriangle(18.0f * dir, 135.0f, 28.0f * dir, 155.0f, 12.0f * dir, 140.0f, 1.0f, 0.85f, 0.2f);

        // 3. Floating Halo (Practical 02: Bresenham Circle)
        int haloY = (int)(162.0f + std::sin(haloAnim) * 4.0f);
        renderer.drawBresenhamCircle((int)position.x, (int)(position.y + haloY), 16, 1.0f, 0.92f, 0.3f);

        // 4. Armored Greaves & Legs
        renderer.drawRect(-22.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.75f, 0.78f, 0.82f);
        renderer.drawRect(4.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.75f, 0.78f, 0.82f);
        renderer.drawRect(-19.0f * dir, 6.0f, 12.0f * dir, 20.0f, 0.95f, 0.8f, 0.2f);
        renderer.drawRect(7.0f * dir, 6.0f, 12.0f * dir, 20.0f, 0.95f, 0.8f, 0.2f);

        // 5. Attacks & Tower Shield
        if (currentState == FighterState::PUNCHING) {
            // Blessed War Hammer Strike
            glPushMatrix();
                glTranslatef(20.0f * dir, 80.0f, 0.0f);
                renderer.drawRect(0.0f, -8.0f, 48.0f * dir, 16.0f, 0.8f, 0.7f, 0.4f);
                renderer.drawRect(40.0f * dir, -22.0f, 20.0f * dir, 44.0f, 0.95f, 0.85f, 0.2f);
            glPopMatrix();
        } else if (currentState == FighterState::KICKING) {
            // Holy Smite Front Kick
            glPushMatrix();
                glTranslatef(15.0f * dir, 32.0f, 0.0f);
                glRotatef(dir * 32.0f, 0.0f, 0.0f, 1.0f);
                renderer.drawRect(0.0f, -8.0f, 50.0f * dir, 22.0f, 0.85f, 0.88f, 0.92f);
                renderer.drawRect(44.0f * dir, -12.0f, 16.0f * dir, 28.0f, 1.0f, 0.9f, 0.3f);
            glPopMatrix();
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Holy Radiance Aegis Pillar (Vertical Golden Pillar)
            renderer.drawGradientRect(15.0f * dir, 0.0f, 120.0f * dir, holyAuraHeight, 1.0f, 0.95f, 0.5f, 1.0f, 0.7f, 0.1f);
            renderer.drawRectOutline(15.0f * dir, 0.0f, 120.0f * dir, holyAuraHeight, 1.0f, 1.0f, 0.9f, 2.5f);
        } else if (currentState == FighterState::BLOCKING) {
            // Massive Aegis Tower Shield
            renderer.drawRect(18.0f * dir, 15.0f, 22.0f * dir, 105.0f, 0.95f, 0.85f, 0.25f);
            renderer.drawRect(22.0f * dir, 25.0f, 14.0f * dir, 85.0f, 0.2f, 0.5f, 0.95f);
        } else {
            // Idle Stance with Tower Shield
            renderer.drawRect(16.0f * dir, 35.0f, 16.0f * dir, 80.0f, 0.95f, 0.85f, 0.25f);
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

#include "Warrior.hpp"

Warrior::Warrior(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "BLAZE WARRIOR", startX, startY, faceRight, 115.0f, 18.0f, 6.0f) {}

void Warrior::executeSpecial() {
    currentDamage = (int)(attackPower * 1.8f);
    activeHitbox = {isFacingRight ? 20.0f : -95.0f, 40.0f, 85.0f, 85.0f, true};
}

void Warrior::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // Practical No. 01: Shaded OpenGL Primitives (Torso & Armor)
        // Body (Crimson Quad)
        renderer.drawRect(-25.0f * dir, 35.0f, 50.0f * dir, 75.0f, 0.85f, 0.15f, 0.15f);
        // Armor Plate (Gold Gradient)
        renderer.drawGradientRect(-20.0f * dir, 45.0f, 40.0f * dir, 60.0f, 0.95f, 0.75f, 0.2f, 0.7f, 0.5f, 0.1f);
        // Head (Helm)
        renderer.drawRect(-18.0f * dir, 110.0f, 36.0f * dir, 35.0f, 0.3f, 0.3f, 0.35f);
        renderer.drawTriangle(-12.0f * dir, 145.0f, 0.0f, 165.0f, 12.0f * dir, 145.0f, 0.95f, 0.75f, 0.2f);

        // Legs (Shaded Quads)
        renderer.drawRect(-22.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.2f, 0.2f, 0.25f);
        renderer.drawRect(4.0f * dir, 0.0f, 18.0f * dir, 35.0f, 0.2f, 0.2f, 0.25f);

        // Attack Arm Animation using Matrix Transformations (Practical No. 04)
        if (currentState == FighterState::PUNCHING) {
            glPushMatrix();
                glTranslatef(20.0f * dir, 80.0f, 0.0f);
                renderer.drawRect(0.0f, -10.0f, 45.0f * dir, 20.0f, 0.95f, 0.4f, 0.1f);
                renderer.drawRect(40.0f * dir, -14.0f, 18.0f * dir, 28.0f, 0.95f, 0.8f, 0.2f);
            glPopMatrix();
        } else if (currentState == FighterState::KICKING) {
            glPushMatrix();
                glTranslatef(15.0f * dir, 30.0f, 0.0f);
                glRotatef(dir * 35.0f, 0.0f, 0.0f, 1.0f);
                renderer.drawRect(0.0f, -8.0f, 50.0f * dir, 22.0f, 0.85f, 0.2f, 0.15f);
            glPopMatrix();
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Magma Slam Ground Eruption
            renderer.drawTriangle(
                10.0f * dir, 0.0f, 50.0f * dir, 90.0f, 80.0f * dir, 0.0f,
                1.0f, 0.45f, 0.1f
            );
        } else {
            // Idle Arm
            renderer.drawRect(12.0f * dir, 60.0f, 16.0f * dir, 40.0f, 0.7f, 0.15f, 0.15f);
        }

    glPopMatrix();

    // Hitbox Debug Visuals (F1 toggle)
    if (debugHitboxes) {
        Hitbox hurt = getHurtboxWorld();
        renderer.drawRectOutline(hurt.offsetX, hurt.offsetY, hurt.width, hurt.height, 0.2f, 0.9f, 0.2f, 1.5f);

        if (activeHitbox.active) {
            Hitbox hit = getActiveHitboxWorld();
            renderer.drawRectOutline(hit.offsetX, hit.offsetY, hit.width, hit.height, 0.95f, 0.2f, 0.2f, 2.0f);
        }
    }
}

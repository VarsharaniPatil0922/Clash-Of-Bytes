#include "Warrior.hpp"
#include <cmath>

Warrior::Warrior(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "KENJI", startX, startY, faceRight, 115.0f, 18.0f, 6.0f) {}

void Warrior::executeSpecial() {
    currentDamage = (int)(attackPower * 1.85f);
    // Hadoken Ki Energy Blast Hitbox
    activeHitbox = {isFacingRight ? 25.0f : -125.0f, 50.0f, 105.0f, 70.0f, true};
}

void Warrior::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // Dynamic motion kinematics & breathing
        float bob = (currentState == FighterState::IDLE) ? std::sin(animTimer * 4.0f) * 2.5f : 0.0f;
        float beltSway = std::sin(animTimer * 6.5f) * 5.0f;

        if (currentState == FighterState::WALKING_FORWARD) {
            float walkCycle = std::sin(animTimer * 12.0f);
            bob = std::abs(walkCycle) * 3.5f;
            beltSway = walkCycle * 14.0f;
        } else if (currentState == FighterState::WALKING_BACKWARD) {
            float walkCycle = std::sin(animTimer * 10.0f);
            bob = std::abs(walkCycle) * 2.0f;
            beltSway = -walkCycle * 10.0f;
        } else if (currentState == FighterState::CROUCHING) {
            bob = -22.0f;
        }

        // ====================================================================
        // 1. FEET & WHITE ANKLE WRAPS (Barefoot Martial Artist)
        // ====================================================================
        if (currentState != FighterState::KICKING) {
            // Back Foot (Left)
            // Bare toes / sole
            renderer.drawRect(-26.0f * dir, 0.0f, 18.0f * dir, 7.0f, 0.94f, 0.74f, 0.60f);
            // White martial arts ankle wrap
            renderer.drawRect(-24.0f * dir, 6.0f, 15.0f * dir, 13.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawRectOutline(-24.0f * dir, 6.0f, 15.0f * dir, 13.0f, 0.75f, 0.75f, 0.80f, 1.0f);

            // Front Foot (Right)
            // Bare toes / sole
            renderer.drawRect(8.0f * dir, 0.0f, 20.0f * dir, 7.0f, 0.96f, 0.78f, 0.65f);
            // White martial arts ankle wrap
            renderer.drawRect(8.0f * dir, 6.0f, 17.0f * dir, 13.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawRectOutline(8.0f * dir, 6.0f, 17.0f * dir, 13.0f, 0.75f, 0.75f, 0.80f, 1.0f);

            // ================================================================
            // 2. BAGGY RED KARATE GI PANTS
            // ================================================================
            // Left leg (thigh to calf)
            renderer.drawGradientRect(-26.0f * dir, 18.0f, 20.0f * dir, 36.0f + bob * 0.5f,
                                      0.75f, 0.08f, 0.08f, 0.92f, 0.16f, 0.16f);
            renderer.drawRect(-25.0f * dir, 30.0f + bob * 0.3f, 18.0f * dir, 3.0f, 0.58f, 0.06f, 0.06f);

            // Right leg (thigh to calf)
            renderer.drawGradientRect(6.0f * dir, 18.0f, 22.0f * dir, 36.0f + bob * 0.5f,
                                      0.82f, 0.12f, 0.12f, 0.98f, 0.22f, 0.22f);
            renderer.drawRect(7.0f * dir, 32.0f + bob * 0.3f, 20.0f * dir, 3.0f, 0.65f, 0.08f, 0.08f);

            // Pelvis connection
            renderer.drawRect(-20.0f * dir, 52.0f + bob * 0.6f, 40.0f * dir, 14.0f, 0.86f, 0.14f, 0.14f);
        } else {
            // Standing plant leg (Back Foot)
            renderer.drawRect(-24.0f * dir, 0.0f, 18.0f * dir, 7.0f, 0.94f, 0.74f, 0.60f);
            renderer.drawRect(-22.0f * dir, 6.0f, 15.0f * dir, 14.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawGradientRect(-24.0f * dir, 18.0f, 18.0f * dir, 36.0f, 0.75f, 0.08f, 0.08f, 0.92f, 0.16f, 0.16f);
            renderer.drawRect(-20.0f * dir, 52.0f, 38.0f * dir, 14.0f, 0.86f, 0.14f, 0.14f);
        }

        // ====================================================================
        // 3. BLACK MARTIAL ARTS BELT & FLUTTERING BELT ENDS
        // ====================================================================
        // Waist belt band
        renderer.drawRect(-22.0f * dir, 64.0f + bob, 44.0f * dir, 10.0f, 0.12f, 0.12f, 0.14f);
        // Belt center knot
        renderer.drawRect(-4.0f * dir, 62.0f + bob, 10.0f * dir, 14.0f, 0.18f, 0.18f, 0.20f);
        // Fluttering dangling belt ends
        renderer.drawRect((-2.0f + beltSway * 0.35f) * dir, 36.0f + bob, 6.0f * dir, 28.0f, 0.12f, 0.12f, 0.14f);
        renderer.drawRect((4.0f + beltSway * 0.65f) * dir, 32.0f + bob, 6.0f * dir, 32.0f, 0.10f, 0.10f, 0.12f);

        // ====================================================================
        // 4. UPPER BODY: RED SLEEVELESS GI WITH OPEN MUSCULAR CHEST
        // ====================================================================
        // Red gi jacket base
        renderer.drawGradientRect(-24.0f * dir, 74.0f + bob, 48.0f * dir, 44.0f,
                                  0.82f, 0.12f, 0.12f, 0.98f, 0.20f, 0.20f);

        // Open V-Neck Chest (Exposed muscular skin)
        renderer.drawTriangle(-12.0f * dir, 118.0f + bob, 12.0f * dir, 118.0f + bob, 0.0f, 76.0f + bob,
                              0.96f, 0.78f, 0.65f);
        // Defined pectoral & abs contours
        renderer.drawRect(-1.0f * dir, 94.0f + bob, 2.0f * dir, 18.0f, 0.82f, 0.62f, 0.50f);
        renderer.drawRect(-8.0f * dir, 94.0f + bob, 16.0f * dir, 2.0f, 0.82f, 0.62f, 0.50f);
        renderer.drawRect(-5.0f * dir, 84.0f + bob, 10.0f * dir, 2.0f, 0.82f, 0.62f, 0.50f);

        // Red Gi Lapels (V-Collar folded over the chest)
        renderer.drawTriangle(-22.0f * dir, 118.0f + bob, -8.0f * dir, 118.0f + bob, -2.0f * dir, 74.0f + bob,
                              0.72f, 0.08f, 0.08f);
        renderer.drawTriangle(22.0f * dir, 118.0f + bob, 8.0f * dir, 118.0f + bob, 2.0f * dir, 74.0f + bob,
                              0.72f, 0.08f, 0.08f);

        // Sleeveless armhole borders
        renderer.drawRect(-25.0f * dir, 98.0f + bob, 4.0f * dir, 18.0f, 0.52f, 0.06f, 0.06f);
        renderer.drawRect(21.0f * dir, 98.0f + bob, 4.0f * dir, 18.0f, 0.52f, 0.06f, 0.06f);

        // ====================================================================
        // 5. HEAD, DETERMINED FACE & SPIKY BROWN ANIME HAIR
        // ====================================================================
        // Neck
        renderer.drawRect(-6.0f * dir, 116.0f + bob, 12.0f * dir, 10.0f, 0.90f, 0.72f, 0.58f);

        // Head / Face
        renderer.drawRect(-14.0f * dir, 124.0f + bob, 28.0f * dir, 22.0f, 0.96f, 0.78f, 0.65f);
        renderer.drawTriangle(-12.0f * dir, 125.0f + bob, 12.0f * dir, 125.0f + bob, 0.0f, 119.0f + bob,
                              0.94f, 0.74f, 0.60f);

        // Facial Features
        // Focused martial arts eyes & eyebrows
        renderer.drawRect(-10.0f * dir, 134.0f + bob, 7.0f * dir, 3.0f, 0.15f, 0.12f, 0.10f);
        renderer.drawRect(3.0f * dir, 134.0f + bob, 8.0f * dir, 3.0f, 0.15f, 0.12f, 0.10f);
        // Eyebrow furrow
        renderer.drawRect(-11.0f * dir, 137.0f + bob, 8.0f * dir, 2.0f, 0.35f, 0.20f, 0.10f);
        renderer.drawRect(3.0f * dir, 137.0f + bob, 9.0f * dir, 2.0f, 0.35f, 0.20f, 0.10f);
        // Mouth
        renderer.drawRect(-4.0f * dir, 126.0f + bob, 8.0f * dir, 2.0f, 0.65f, 0.40f, 0.35f);

        // Spiky Brown Hair Mass
        renderer.drawRect(-16.0f * dir, 140.0f + bob, 32.0f * dir, 14.0f, 0.42f, 0.22f, 0.10f);

        // Forehead Bangs
        renderer.drawTriangle(-14.0f * dir, 142.0f + bob, -6.0f * dir, 142.0f + bob, -10.0f * dir, 132.0f + bob,
                              0.48f, 0.26f, 0.12f);
        renderer.drawTriangle(-6.0f * dir, 142.0f + bob, 4.0f * dir, 142.0f + bob, 0.0f, 130.0f + bob,
                              0.52f, 0.28f, 0.14f);
        renderer.drawTriangle(3.0f * dir, 142.0f + bob, 14.0f * dir, 142.0f + bob, 8.0f * dir, 132.0f + bob,
                              0.48f, 0.26f, 0.12f);

        // Spiky Top Strands (Distinct anime jagged spikes)
        renderer.drawTriangle(-16.0f * dir, 150.0f + bob, -8.0f * dir, 150.0f + bob, -18.0f * dir, 163.0f + bob,
                              0.38f, 0.20f, 0.08f);
        renderer.drawTriangle(-10.0f * dir, 152.0f + bob, 0.0f, 152.0f + bob, -7.0f * dir, 168.0f + bob,
                              0.52f, 0.28f, 0.14f);
        renderer.drawTriangle(-2.0f * dir, 152.0f + bob, 8.0f * dir, 152.0f + bob, 4.0f * dir, 170.0f + bob,
                              0.58f, 0.32f, 0.16f);
        renderer.drawTriangle(6.0f * dir, 150.0f + bob, 16.0f * dir, 150.0f + bob, 17.0f * dir, 164.0f + bob,
                              0.44f, 0.24f, 0.10f);

        // ====================================================================
        // 6. ARMS, WHITE FOREARM WRAPS, BLACK GLOVES & ATTACK STATES
        // ====================================================================
        if (currentState == FighterState::PUNCHING) {
            // Step forward straight right punch
            // Bare shoulder & upper arm
            renderer.drawRect(8.0f * dir, 88.0f, 22.0f * dir, 16.0f, 0.96f, 0.78f, 0.65f);
            // White athletic forearm wrap
            renderer.drawRect(28.0f * dir, 90.0f, 24.0f * dir, 14.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawRectOutline(28.0f * dir, 90.0f, 24.0f * dir, 14.0f, 0.75f, 0.75f, 0.80f, 1.0f);
            // Clenched black combat glove
            renderer.drawRect(50.0f * dir, 87.0f, 18.0f * dir, 18.0f, 0.14f, 0.14f, 0.16f);
            // Speed streak flare
            renderer.drawRect(68.0f * dir, 93.0f, 18.0f * dir, 6.0f, 1.0f, 0.85f, 0.3f, 0.75f);
        } else if (currentState == FighterState::KICKING) {
            // High snap roundhouse kick
            glPushMatrix();
                glTranslatef(12.0f * dir, 46.0f, 0.0f);
                // Sweeping red gi pant leg
                renderer.drawRect(0.0f, -8.0f, 52.0f * dir, 22.0f, 0.94f, 0.16f, 0.16f);
                // White ankle wrap
                renderer.drawRect(50.0f * dir, -7.0f, 15.0f * dir, 20.0f, 0.92f, 0.92f, 0.95f);
                // Bare kicking foot
                renderer.drawRect(63.0f * dir, -9.0f, 18.0f * dir, 24.0f, 0.96f, 0.78f, 0.65f);
                // Kick impact wind crescent
                renderer.drawTriangle(78.0f * dir, 16.0f, 86.0f * dir, 2.0f, 78.0f * dir, -12.0f, 1.0f, 0.9f, 0.4f);
            glPopMatrix();

            // Guarding upper arm
            renderer.drawRect(6.0f * dir, 86.0f, 14.0f * dir, 22.0f, 0.96f, 0.78f, 0.65f);
            renderer.drawRect(12.0f * dir, 96.0f, 14.0f * dir, 14.0f, 0.14f, 0.14f, 0.16f);
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Classic Hadoken Ki Blast Burst Pose (Dual cupped hands thrust forward)
            // Both bare arms thrusting forward
            renderer.drawRect(8.0f * dir, 76.0f, 32.0f * dir, 15.0f, 0.96f, 0.78f, 0.65f);
            // White forearm wraps
            renderer.drawRect(38.0f * dir, 74.0f, 18.0f * dir, 17.0f, 0.92f, 0.92f, 0.95f);
            // Black fingerless gloves cupped together
            renderer.drawRect(54.0f * dir, 72.0f, 16.0f * dir, 22.0f, 0.14f, 0.14f, 0.16f);

            // HADOKEN KI PROJECTILE (Glowing blue-white energy orb & dragon flame flares)
            float orbX = 85.0f * dir;
            float orbY = 82.0f;
            // Outer bright cyan energy shell
            renderer.drawRect(orbX - 18.0f * dir, orbY - 18.0f, 36.0f * dir, 36.0f, 0.2f, 0.75f, 1.0f, 0.85f);
            // Inner fiery plasma core
            renderer.drawRect(orbX - 12.0f * dir, orbY - 12.0f, 24.0f * dir, 24.0f, 0.6f, 0.95f, 1.0f, 0.95f);
            // White hot center
            renderer.drawRect(orbX - 6.0f * dir, orbY - 6.0f, 12.0f * dir, 12.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            // Ki burst arrow flares
            renderer.drawTriangle(orbX + 16.0f * dir, orbY, orbX + 42.0f * dir, orbY + 14.0f, orbX + 16.0f * dir, orbY + 8.0f,
                                  0.3f, 0.85f, 1.0f);
            renderer.drawTriangle(orbX + 16.0f * dir, orbY, orbX + 42.0f * dir, orbY - 14.0f, orbX + 16.0f * dir, orbY - 8.0f,
                                  0.3f, 0.85f, 1.0f);
        } else {
            // Idle / Walking / Guarding stance
            // Back arm (defensive guard)
            renderer.drawRect(-18.0f * dir, 85.0f + bob, 10.0f * dir, 20.0f, 0.90f, 0.72f, 0.58f);
            renderer.drawRect(-17.0f * dir, 72.0f + bob, 9.0f * dir, 15.0f, 0.90f, 0.90f, 0.94f);
            renderer.drawRect(-18.0f * dir, 62.0f + bob, 11.0f * dir, 12.0f, 0.14f, 0.14f, 0.16f);

            // Front arm (raised fighting fist)
            renderer.drawRect(12.0f * dir, 82.0f + bob, 12.0f * dir, 22.0f, 0.96f, 0.78f, 0.65f);
            renderer.drawRect(18.0f * dir, 88.0f + bob, 12.0f * dir, 18.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawRect(24.0f * dir, 98.0f + bob, 14.0f * dir, 14.0f, 0.12f, 0.12f, 0.14f);
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

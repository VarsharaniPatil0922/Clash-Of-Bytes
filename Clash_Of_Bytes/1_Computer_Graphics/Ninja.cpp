#include "Ninja.hpp"
#include <cmath>

Ninja::Ninja(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "ELENA", startX, startY, faceRight, 95.0f, 16.0f, 4.0f),
      shurikenAngle(0.0f), shurikenDist(0.0f) {}

void Ninja::update(float dt) {
    Fighter::update(dt);
    if (currentState == FighterState::SPECIAL_ATTACK) {
        shurikenAngle += 840.0f * dt;
        shurikenDist += 540.0f * dt;
    } else {
        shurikenAngle = 0.0f;
        shurikenDist = 0.0f;
    }
}

void Ninja::executeSpecial() {
    currentDamage = (int)(attackPower * 1.75f);
    // Cyclone Spin Kick & Gale Vortex Hitbox
    activeHitbox = {isFacingRight ? 25.0f : -130.0f, 50.0f, 110.0f, 75.0f, true};
    shurikenDist = 30.0f;
}

void Ninja::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // Dynamic Capoeira / Striker rhythm & movement
        float bob = (currentState == FighterState::IDLE) ? std::sin(animTimer * 5.0f) * 3.0f : 0.0f;
        float ponySway = std::sin(animTimer * 6.0f) * 8.0f;

        if (currentState == FighterState::WALKING_FORWARD) {
            float walkCycle = std::sin(animTimer * 12.0f);
            bob = std::abs(walkCycle) * 3.5f;
            ponySway = walkCycle * 14.0f;
        } else if (currentState == FighterState::WALKING_BACKWARD) {
            float walkCycle = std::sin(animTimer * 10.0f);
            bob = std::abs(walkCycle) * 2.0f;
            ponySway = -walkCycle * 12.0f;
        } else if (currentState == FighterState::CROUCHING) {
            bob = -20.0f;
        }

        // ====================================================================
        // 1. FEET & GREEN HIGH-TOP COMBAT BOOTS
        // ====================================================================
        if (currentState != FighterState::KICKING) {
            // Back Foot / Boot (Left)
            // Grip sole
            renderer.drawRect(-24.0f * dir, 0.0f, 17.0f * dir, 5.0f, 0.38f, 0.25f, 0.14f);
            // Green boot upper
            renderer.drawRect(-23.0f * dir, 4.0f, 15.0f * dir, 15.0f, 0.20f, 0.65f, 0.30f);
            renderer.drawRectOutline(-23.0f * dir, 4.0f, 15.0f * dir, 15.0f, 0.12f, 0.45f, 0.20f, 1.0f);

            // Front Foot / Boot (Right)
            // Grip sole
            renderer.drawRect(8.0f * dir, 0.0f, 19.0f * dir, 5.0f, 0.38f, 0.25f, 0.14f);
            // Green boot upper
            renderer.drawRect(8.0f * dir, 4.0f, 17.0f * dir, 15.0f, 0.22f, 0.70f, 0.32f);
            renderer.drawRectOutline(8.0f * dir, 4.0f, 17.0f * dir, 15.0f, 0.14f, 0.48f, 0.22f, 1.0f);

            // ================================================================
            // 2. LOOSE DARK OLIVE / CAMO CARGO TROUSERS WITH SIDE POCKETS
            // ================================================================
            // Left leg
            renderer.drawGradientRect(-25.0f * dir, 18.0f, 18.0f * dir, 36.0f + bob * 0.5f,
                                      0.24f, 0.30f, 0.20f, 0.34f, 0.40f, 0.28f);
            // Left cargo pocket
            renderer.drawRect(-26.0f * dir, 32.0f + bob * 0.4f, 6.0f * dir, 12.0f, 0.20f, 0.25f, 0.16f);

            // Right leg
            renderer.drawGradientRect(6.0f * dir, 18.0f, 20.0f * dir, 36.0f + bob * 0.5f,
                                      0.28f, 0.34f, 0.22f, 0.38f, 0.45f, 0.30f);
            // Right cargo pocket
            renderer.drawRect(22.0f * dir, 32.0f + bob * 0.4f, 6.0f * dir, 12.0f, 0.22f, 0.28f, 0.18f);

            // Pelvis connection
            renderer.drawRect(-18.0f * dir, 52.0f + bob * 0.6f, 36.0f * dir, 12.0f, 0.32f, 0.38f, 0.26f);
        } else {
            // Standing plant foot (Back)
            renderer.drawRect(-22.0f * dir, 0.0f, 16.0f * dir, 5.0f, 0.38f, 0.25f, 0.14f);
            renderer.drawRect(-21.0f * dir, 4.0f, 14.0f * dir, 15.0f, 0.20f, 0.65f, 0.30f);
            renderer.drawGradientRect(-22.0f * dir, 18.0f, 16.0f * dir, 36.0f, 0.24f, 0.30f, 0.20f, 0.34f, 0.40f, 0.28f);
            renderer.drawRect(-16.0f * dir, 52.0f, 34.0f * dir, 12.0f, 0.32f, 0.38f, 0.26f);
        }

        // ====================================================================
        // 3. BROWN TACTICAL BELT & GOLD BUCKLE
        // ====================================================================
        // Brown leather belt band
        renderer.drawRect(-19.0f * dir, 62.0f + bob, 38.0f * dir, 8.0f, 0.44f, 0.28f, 0.16f);
        // Gold/silver buckle
        renderer.drawRect(-3.0f * dir, 61.0f + bob, 8.0f * dir, 10.0f, 0.88f, 0.78f, 0.30f);
        // Side utility pouch
        renderer.drawRect(15.0f * dir, 60.0f + bob, 6.0f * dir, 9.0f, 0.38f, 0.24f, 0.12f);

        // ====================================================================
        // 4. TONED BRONZE MIDRIFF & BRIGHT GREEN ATHLETIC CROP TOP
        // ====================================================================
        // Exposed toned bronze abs & waist
        renderer.drawRect(-14.0f * dir, 70.0f + bob, 28.0f * dir, 16.0f, 0.86f, 0.62f, 0.45f);
        // Abdominal muscle contour lines
        renderer.drawRect(-1.0f * dir, 72.0f + bob, 2.0f * dir, 12.0f, 0.74f, 0.50f, 0.35f);
        renderer.drawRect(-6.0f * dir, 76.0f + bob, 12.0f * dir, 2.0f, 0.74f, 0.50f, 0.35f);

        // Bright Green Athletic Halter Crop Top / Sports Bra
        // Base green sports bra
        renderer.drawGradientRect(-17.0f * dir, 86.0f + bob, 34.0f * dir, 28.0f,
                                  0.20f, 0.80f, 0.28f, 0.28f, 0.95f, 0.38f);
        // Dark olive halter trim & neck straps
        renderer.drawRect(-17.0f * dir, 84.0f + bob, 34.0f * dir, 3.0f, 0.12f, 0.45f, 0.18f);
        renderer.drawTriangle(-12.0f * dir, 114.0f + bob, -4.0f * dir, 114.0f + bob, -8.0f * dir, 118.0f + bob,
                              0.14f, 0.48f, 0.20f);
        renderer.drawTriangle(4.0f * dir, 114.0f + bob, 12.0f * dir, 114.0f + bob, 8.0f * dir, 118.0f + bob,
                              0.14f, 0.48f, 0.20f);

        // ====================================================================
        // 5. BRONZE FACE, BANGS & HIGH VOLUMINOUS PONYTAIL
        // ====================================================================
        // Slender bronze neck
        renderer.drawRect(-5.0f * dir, 114.0f + bob, 10.0f * dir, 9.0f, 0.82f, 0.58f, 0.40f);

        // Face & Jawline
        renderer.drawRect(-12.0f * dir, 122.0f + bob, 24.0f * dir, 20.0f, 0.86f, 0.62f, 0.45f);
        renderer.drawTriangle(-10.0f * dir, 123.0f + bob, 10.0f * dir, 123.0f + bob, 0.0f, 117.0f + bob,
                              0.82f, 0.58f, 0.40f);

        // Facial Features (Confident eyes, arched eyebrows, determined smile)
        renderer.drawRect(-8.0f * dir, 131.0f + bob, 6.0f * dir, 3.0f, 0.18f, 0.12f, 0.08f);
        renderer.drawRect(3.0f * dir, 131.0f + bob, 7.0f * dir, 3.0f, 0.18f, 0.12f, 0.08f);
        renderer.drawRect(-9.0f * dir, 135.0f + bob, 7.0f * dir, 2.0f, 0.28f, 0.16f, 0.08f);
        renderer.drawRect(3.0f * dir, 135.0f + bob, 8.0f * dir, 2.0f, 0.28f, 0.16f, 0.08f);
        renderer.drawRect(-3.0f * dir, 124.0f + bob, 7.0f * dir, 2.0f, 0.68f, 0.36f, 0.30f);

        // Dark Brown Hair Base
        renderer.drawRect(-14.0f * dir, 138.0f + bob, 28.0f * dir, 12.0f, 0.28f, 0.16f, 0.08f);

        // Bangs framing face
        renderer.drawTriangle(-12.0f * dir, 139.0f + bob, -4.0f * dir, 139.0f + bob, -8.0f * dir, 131.0f + bob,
                              0.32f, 0.18f, 0.10f);
        renderer.drawTriangle(3.0f * dir, 139.0f + bob, 11.0f * dir, 139.0f + bob, 7.0f * dir, 131.0f + bob,
                              0.32f, 0.18f, 0.10f);

        // Green Hair Scrunchie / Tie at base of ponytail
        renderer.drawRect((-12.0f - dir * 4.0f) * dir, 146.0f + bob, 10.0f * dir, 8.0f, 0.20f, 0.85f, 0.32f);

        // High Voluminous Ponytail flowing backward with dynamic bounce!
        float ponyBaseX = -14.0f * dir;
        float ponyBaseY = 148.0f + bob;
        // Ponytail arc 1
        renderer.drawTriangle(ponyBaseX, ponyBaseY,
                              ponyBaseX - (18.0f + ponySway * 0.4f) * dir, ponyBaseY + 14.0f,
                              ponyBaseX - (28.0f + ponySway * 0.7f) * dir, ponyBaseY + 6.0f,
                              0.26f, 0.14f, 0.07f);
        // Ponytail arc 2 (fluffy tip cascading down)
        renderer.drawTriangle(ponyBaseX - (18.0f + ponySway * 0.4f) * dir, ponyBaseY + 14.0f,
                              ponyBaseX - (34.0f + ponySway * 0.9f) * dir, ponyBaseY - 6.0f,
                              ponyBaseX - (22.0f + ponySway * 0.5f) * dir, ponyBaseY - 2.0f,
                              0.32f, 0.18f, 0.09f);

        // ====================================================================
        // 6. ARMS, WHITE WRAPS, GREEN GLOVES & ATTACK STATES
        // ====================================================================
        if (currentState == FighterState::PUNCHING) {
            // Rapid Snap Jab
            // Bronze arm
            renderer.drawRect(8.0f * dir, 86.0f, 20.0f * dir, 14.0f, 0.86f, 0.62f, 0.45f);
            // White athletic forearm wrap
            renderer.drawRect(26.0f * dir, 88.0f, 22.0f * dir, 12.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawRectOutline(26.0f * dir, 88.0f, 22.0f * dir, 12.0f, 0.75f, 0.75f, 0.80f, 1.0f);
            // Bright green glove strike
            renderer.drawRect(46.0f * dir, 85.0f, 16.0f * dir, 16.0f, 0.22f, 0.85f, 0.32f);
            // Speed trail
            renderer.drawRect(60.0f * dir, 90.0f, 16.0f * dir, 5.0f, 0.4f, 1.0f, 0.6f, 0.75f);
        } else if (currentState == FighterState::KICKING) {
            // Spinning Axe Kick / High Arc
            glPushMatrix();
                glTranslatef(10.0f * dir, 44.0f, 0.0f);
                // Sweeping camo cargo pant leg
                renderer.drawRect(0.0f, -6.0f, 50.0f * dir, 19.0f, 0.30f, 0.38f, 0.24f);
                // Green combat boot strike
                renderer.drawRect(48.0f * dir, -8.0f, 18.0f * dir, 22.0f, 0.22f, 0.75f, 0.32f);
                // Wind crescent
                renderer.drawTriangle(68.0f * dir, 18.0f, 76.0f * dir, 4.0f, 68.0f * dir, -10.0f,
                                      0.3f, 1.0f, 0.6f);
            glPopMatrix();

            // Guard arm
            renderer.drawRect(4.0f * dir, 82.0f, 12.0f * dir, 20.0f, 0.86f, 0.62f, 0.45f);
            renderer.drawRect(10.0f * dir, 92.0f, 12.0f * dir, 14.0f, 0.22f, 0.85f, 0.32f);
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Cyclone Spin Kick / Gale Vortex Whirlwind!
            // Dynamic spinning posture
            renderer.drawRect(-8.0f * dir, 78.0f, 24.0f * dir, 14.0f, 0.86f, 0.62f, 0.45f);
            renderer.drawRect(14.0f * dir, 80.0f, 20.0f * dir, 14.0f, 0.22f, 0.85f, 0.32f);

            // Rotating Gale Shuriken / Cyclone Blade (Practical 04 Transformation)
            renderer.drawRotatingShuriken(shurikenDist * dir, 78.0f, 26.0f, shurikenAngle);

            // Swirling green wind vortex arcs
            float vortexX = (shurikenDist - 20.0f) * dir;
            renderer.drawTriangle(vortexX, 78.0f, vortexX + 25.0f * dir, 96.0f, vortexX + 10.0f * dir, 86.0f, 0.3f, 0.95f, 0.5f);
            renderer.drawTriangle(vortexX, 78.0f, vortexX + 25.0f * dir, 60.0f, vortexX + 10.0f * dir, 70.0f, 0.3f, 0.95f, 0.5f);
        } else {
            // Idle / Walking / Guarding stance
            // Back arm (poised guard)
            renderer.drawRect(-16.0f * dir, 82.0f + bob, 9.0f * dir, 18.0f, 0.82f, 0.58f, 0.40f);
            renderer.drawRect(-15.0f * dir, 70.0f + bob, 8.0f * dir, 14.0f, 0.90f, 0.90f, 0.94f);
            renderer.drawRect(-16.0f * dir, 60.0f + bob, 10.0f * dir, 12.0f, 0.20f, 0.75f, 0.28f);

            // Front arm (raised athletic guard)
            renderer.drawRect(10.0f * dir, 80.0f + bob, 11.0f * dir, 20.0f, 0.86f, 0.62f, 0.45f);
            renderer.drawRect(16.0f * dir, 85.0f + bob, 11.0f * dir, 16.0f, 0.92f, 0.92f, 0.95f);
            renderer.drawRect(22.0f * dir, 94.0f + bob, 13.0f * dir, 13.0f, 0.24f, 0.88f, 0.34f);
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

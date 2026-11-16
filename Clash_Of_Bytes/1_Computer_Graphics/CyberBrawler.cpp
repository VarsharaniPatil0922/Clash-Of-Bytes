#include "CyberBrawler.hpp"
#include <cmath>

CyberBrawler::CyberBrawler(int pIndex, float startX, float startY, bool faceRight)
    : Fighter(pIndex, "CYBER BRAWLER", startX, startY, faceRight, 110.0f, 19.0f, 5.0f),
      plasmaDist(0.0f), plasmaPulse(0.0f) {}

void CyberBrawler::update(float dt) {
    Fighter::update(dt);
    plasmaPulse += 12.0f * dt;
    if (currentState == FighterState::SPECIAL_ATTACK) {
        plasmaDist += 700.0f * dt;
    } else {
        plasmaDist = 0.0f;
    }
}

void CyberBrawler::executeSpecial() {
    currentDamage = (int)(attackPower * 1.75f);
    activeHitbox = {isFacingRight ? 30.0f : -220.0f, 55.0f, 210.0f, 50.0f, true};
    plasmaDist = 40.0f;
}

void CyberBrawler::render(Renderer& renderer, bool debugHitboxes) {
    glPushMatrix();
        glTranslatef(position.x, position.y, 0.0f);
        if (flinchAngle != 0.0f) {
            glRotatef(flinchAngle, 0.0f, 0.0f, 1.0f);
        }

        float dir = isFacingRight ? 1.0f : -1.0f;

        // 1. Cyber Chassis Torso (Gunmetal Slate + Neon Green Core)
        renderer.drawRect(-24.0f * dir, 35.0f, 48.0f * dir, 75.0f, 0.16f, 0.20f, 0.26f);
        // Neon Power Core (Gradient)
        renderer.drawGradientRect(-16.0f * dir, 52.0f, 32.0f * dir, 36.0f, 0.0f, 1.0f, 0.55f, 0.0f, 0.5f, 0.95f);
        // Cybernetic Circuit Lines
        renderer.drawRect(-20.0f * dir, 40.0f, 6.0f * dir, 65.0f, 0.0f, 0.9f, 0.8f);
        renderer.drawRect(14.0f * dir, 40.0f, 6.0f * dir, 65.0f, 0.0f, 0.9f, 0.8f);

        // 2. Cyber Head with Visor
        renderer.drawRect(-17.0f * dir, 110.0f, 34.0f * dir, 34.0f, 0.12f, 0.16f, 0.22f);
        // Horizontal Scanning Cyan Visor
        renderer.drawRect(-14.0f * dir, 122.0f, 28.0f * dir, 8.0f, 0.0f, 0.95f, 1.0f);
        // Antenna
        renderer.drawRect(10.0f * dir, 144.0f, 4.0f * dir, 16.0f, 0.0f, 1.0f, 0.5f);

        // 3. Hydraulic Piston Legs
        renderer.drawRect(-20.0f * dir, 0.0f, 16.0f * dir, 35.0f, 0.14f, 0.17f, 0.22f);
        renderer.drawRect(4.0f * dir, 0.0f, 16.0f * dir, 35.0f, 0.14f, 0.17f, 0.22f);
        renderer.drawRect(-17.0f * dir, 8.0f, 10.0f * dir, 18.0f, 0.0f, 0.9f, 0.8f);
        renderer.drawRect(7.0f * dir, 8.0f, 10.0f * dir, 18.0f, 0.0f, 0.9f, 0.8f);

        // 4. Arms & Attacks
        if (currentState == FighterState::PUNCHING) {
            // Overclocked Hydraulic Punch
            glPushMatrix();
                glTranslatef(20.0f * dir, 80.0f, 0.0f);
                renderer.drawRect(0.0f, -10.0f, 50.0f * dir, 22.0f, 0.18f, 0.24f, 0.32f);
                renderer.drawRect(44.0f * dir, -14.0f, 20.0f * dir, 30.0f, 0.0f, 1.0f, 0.55f);
            glPopMatrix();
        } else if (currentState == FighterState::KICKING) {
            // Rocket Thruster Kick
            glPushMatrix();
                glTranslatef(16.0f * dir, 34.0f, 0.0f);
                glRotatef(dir * 38.0f, 0.0f, 0.0f, 1.0f);
                renderer.drawRect(0.0f, -8.0f, 52.0f * dir, 22.0f, 0.18f, 0.24f, 0.32f);
                renderer.drawRect(45.0f * dir, -12.0f, 18.0f * dir, 28.0f, 0.0f, 0.95f, 1.0f);
            glPopMatrix();
        } else if (currentState == FighterState::SPECIAL_ATTACK) {
            // Overclocked Plasma Overdrive Beam (Laser beam + concentric energy rings)
            float beamW = std::min(220.0f, plasmaDist);
            renderer.drawGradientRect(22.0f * dir, 65.0f, beamW * dir, 30.0f, 0.0f, 1.0f, 0.6f, 0.0f, 0.8f, 1.0f);
            int ringRadius = 16 + (int)(std::sin(plasmaPulse) * 4.0f);
            renderer.drawBresenhamCircle((int)(position.x + (beamW * 0.8f * dir)), (int)(position.y + 80.0f), ringRadius, 0.0f, 1.0f, 0.8f);
        } else {
            // Idle Cyber Arm
            renderer.drawRect(14.0f * dir, 58.0f, 16.0f * dir, 42.0f, 0.16f, 0.22f, 0.30f);
            renderer.drawRect(16.0f * dir, 62.0f, 12.0f * dir, 8.0f, 0.0f, 1.0f, 0.55f);
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

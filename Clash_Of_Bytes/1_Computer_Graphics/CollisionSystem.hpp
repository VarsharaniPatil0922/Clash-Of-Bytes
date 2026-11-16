#ifndef COLLISIONSYSTEM_HPP
#define COLLISIONSYSTEM_HPP

/*
 * Subject: Programming Laboratory & Computer Organization
 * Topic: AABB Real-Time Collision Resolution & Hardware Assembly Damage Hook
 */
#include "Fighter.hpp"
#include "ParticleSystem.hpp"
#include "CO1_Prac01_OpenGL_Gouraud_Renderer.hpp"
#include "../3_Computer_Organization_Architecture/CO2_Prac03_x86_64_Assembly_Damage_Engine.hpp"
#include <string>

class CollisionSystem {
private:
    std::string latestALUTrace;

    static bool checkAABB(const Hitbox& a, const Hitbox& b) {
        return (a.offsetX < b.offsetX + b.width &&
                a.offsetX + a.width > b.offsetX &&
                a.offsetY < b.offsetY + b.height &&
                a.offsetY + a.height > b.offsetY);
    }

public:
    CollisionSystem() : latestALUTrace("WAITING FOR COMBAT HIT...") {}

    void checkCombat(Fighter* p1, Fighter* p2, ParticleSystem& particles, Renderer& renderer) {
        if (!p1 || !p2) return;
        if (!p1->isAlive() || !p2->isAlive()) return;

        // 1. Player Body Pushback
        float dist = p2->getPosition().x - p1->getPosition().x;
        float minDist = 70.0f;
        if (std::abs(dist) < minDist) {
            float pushDir = (dist >= 0.0f) ? 1.0f : -1.0f;
            // Push apart
            p1->takeHit(0, -pushDir * 0.2f, false);
            p2->takeHit(0, pushDir * 0.2f, false);
        }

        // Keep fighters facing each other
        if (p1->getPosition().x < p2->getPosition().x) {
            p1->setFacingRight(true);
            p2->setFacingRight(false);
        } else {
            p1->setFacingRight(false);
            p2->setFacingRight(true);
        }

        // 2. Check P1 attacking P2
        Hitbox p1Hit = p1->getActiveHitboxWorld();
        Hitbox p2Hurt = p2->getHurtboxWorld();
        if (p1Hit.active && checkAABB(p1Hit, p2Hurt)) {
            // Practical No. 03: REAL 64-BIT ASSEMBLY DAMAGE CALCULATION!
            CombatALUResult alu = AssemblyDamageEngine::calculateHitDamage(p1->getAttackDamage(), 5, 1);
            latestALUTrace = "P1 HIT! " + alu.assemblyTrace;

            float knockDir = p1->isFacingRightDir() ? 1.0f : -1.0f;
            p2->takeHit((int)alu.finalDamage, knockDir, p1->getState() == FighterState::SPECIAL_ATTACK);
            p1->clearActiveHitbox();

            // Practical No. 02: Bresenham spark emission
            Vector2D sparkPos(p2->getPosition().x, p1Hit.offsetY + p1Hit.height * 0.5f);
            particles.emitHitSparks(sparkPos, alu.finalDamage > 20);
            renderer.triggerScreenShake(alu.finalDamage > 20 ? 7.0f : 4.0f, 0.2f);

            // Practical 06 (CO6): Dynamic Combat Point Light flare
            float lR = 1.0f, lG = 0.85f, lB = 0.2f;
            if (p1->getName().find("WARRIOR") != std::string::npos) { lR = 1.0f; lG = 0.45f; lB = 0.1f; }
            else if (p1->getName().find("NINJA") != std::string::npos) { lR = 0.2f; lG = 0.85f; lB = 1.0f; }
            else if (p1->getName().find("BRAWLER") != std::string::npos) { lR = 0.1f; lG = 0.95f; lB = 1.0f; }
            else if (p1->getName().find("PALADIN") != std::string::npos) { lR = 1.0f; lG = 0.95f; lB = 0.35f; }
            else if (p1->getName().find("BERSERKER") != std::string::npos) { lR = 1.0f; lG = 0.15f; lB = 0.1f; }
            else if (p1->getName().find("GLADIATOR") != std::string::npos) { lR = 1.0f; lG = 0.7f; lB = 0.25f; }
            renderer.setCombatLight(sparkPos.x, sparkPos.y, lR, lG, lB, alu.finalDamage > 20 ? 1.5f : 1.0f);
        }

        // 3. Check P2 attacking P1
        Hitbox p2Hit = p2->getActiveHitboxWorld();
        Hitbox p1Hurt = p1->getHurtboxWorld();
        if (p2Hit.active && checkAABB(p2Hit, p1Hurt)) {
            // Practical No. 03: REAL 64-BIT ASSEMBLY DAMAGE CALCULATION!
            CombatALUResult alu = AssemblyDamageEngine::calculateHitDamage(p2->getAttackDamage(), 5, 1);
            latestALUTrace = "P2 HIT! " + alu.assemblyTrace;

            float knockDir = p2->isFacingRightDir() ? 1.0f : -1.0f;
            p1->takeHit((int)alu.finalDamage, knockDir, p2->getState() == FighterState::SPECIAL_ATTACK);
            p2->clearActiveHitbox();

            // Practical No. 02: Bresenham spark emission
            Vector2D sparkPos(p1->getPosition().x, p2Hit.offsetY + p2Hit.height * 0.5f);
            particles.emitHitSparks(sparkPos, alu.finalDamage > 20);
            renderer.triggerScreenShake(alu.finalDamage > 20 ? 7.0f : 4.0f, 0.2f);

            // Practical 06 (CO6): Dynamic Combat Point Light flare
            float lR = 1.0f, lG = 0.85f, lB = 0.2f;
            if (p2->getName().find("WARRIOR") != std::string::npos) { lR = 1.0f; lG = 0.45f; lB = 0.1f; }
            else if (p2->getName().find("NINJA") != std::string::npos) { lR = 0.2f; lG = 0.85f; lB = 1.0f; }
            else if (p2->getName().find("BRAWLER") != std::string::npos) { lR = 0.1f; lG = 0.95f; lB = 1.0f; }
            else if (p2->getName().find("PALADIN") != std::string::npos) { lR = 1.0f; lG = 0.95f; lB = 0.35f; }
            else if (p2->getName().find("BERSERKER") != std::string::npos) { lR = 1.0f; lG = 0.15f; lB = 0.1f; }
            else if (p2->getName().find("GLADIATOR") != std::string::npos) { lR = 1.0f; lG = 0.7f; lB = 0.25f; }
            renderer.setCombatLight(sparkPos.x, sparkPos.y, lR, lG, lB, alu.finalDamage > 20 ? 1.5f : 1.0f);
        }
    }

    std::string getLatestALUTrace() const { return latestALUTrace; }
};

#endif // COLLISIONSYSTEM_HPP

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
#include "../3_Computer_Organization_Architecture/CO3_CombatLogAnalyzer.hpp"
#include <string>
#include <algorithm>

class CollisionSystem {
private:
    std::string latestALUTrace;
    CombatLogAnalyzer combatLogAnalyzer;

    static bool checkAABB(const Hitbox& a, const Hitbox& b) {
        return (a.offsetX < b.offsetX + b.width &&
                a.offsetX + a.width > b.offsetX &&
                a.offsetY < b.offsetY + b.height &&
                a.offsetY + a.height > b.offsetY);
    }

public:
    CollisionSystem() : latestALUTrace("WAITING FOR COMBAT HIT...") {}

    CombatLogAnalyzer& getCombatLogAnalyzer() { return combatLogAnalyzer; }
    const CombatLogAnalyzer& getCombatLogAnalyzer() const { return combatLogAnalyzer; }

    void checkCombat(Fighter* p1, Fighter* p2, ParticleSystem& particles, Renderer& renderer) {
        if (!p1 || !p2) return;
        if (!p1->isAlive() || !p2->isAlive()) return;

        // 1. Player Body Pushback (Only on ground so fighters can jump cross in the air!)
        if (p1->isGroundedState() && p2->isGroundedState()) {
            float dist = p2->getPosition().x - p1->getPosition().x;
            float minDist = 65.0f;
            if (std::abs(dist) < minDist) {
                float pushDir = (dist >= 0.0f) ? 1.0f : -1.0f;
                if (std::abs(dist) < 0.001f) pushDir = p1->isFacingRightDir() ? -1.0f : 1.0f;
                float overlap = minDist - std::abs(dist);
                float shift = overlap * 0.5f;
                float newP1X = std::max(40.0f, std::min(1240.0f, p1->getPosition().x - pushDir * shift));
                float newP2X = std::max(40.0f, std::min(1240.0f, p2->getPosition().x + pushDir * shift));
                p1->setPositionX(newP1X);
                p2->setPositionX(newP2X);
            }
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
            // Check if P2 is guarding/blocking
            bool p2Blocking = (p2->getState() == FighterState::BLOCKING);
            uint64_t targetDefense = p2Blocking ? 20 : 6;
            uint64_t comboFactor = std::max(1, p1->getCombo());
            bool isSpecial = (p1->getState() == FighterState::SPECIAL_ATTACK);
            if (isSpecial) comboFactor = std::max<uint64_t>(comboFactor, 2);

            // Record telemetry event in CombatLogAnalyzer
            if (p2Blocking) {
                combatLogAnalyzer.recordCombatEvent("P2 BLOCK GUARD DEFENSE ABSORB");
            } else if (isSpecial) {
                combatLogAnalyzer.recordCombatEvent("P1 SPECIAL UNLEASHED CRITICAL IMPACT");
            } else {
                combatLogAnalyzer.recordCombatEvent("P1 ATTACK HIT CONNECTED");
            }

            // Practical No. 03: REAL 64-BIT ASSEMBLY DAMAGE CALCULATION!
            CombatALUResult alu = AssemblyDamageEngine::calculateHitDamage(p1->getAttackDamage(), targetDefense, comboFactor);
            latestALUTrace = "P1 HIT! " + alu.assemblyTrace;

            float knockDir = p1->isFacingRightDir() ? 1.0f : -1.0f;
            p2->takeHit((int)alu.finalDamage, knockDir, isSpecial);
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
            bool p1Blocking = (p1->getState() == FighterState::BLOCKING);
            uint64_t targetDefense = p1Blocking ? 20 : 6;
            uint64_t comboFactor = std::max(1, p2->getCombo());
            bool isSpecial = (p2->getState() == FighterState::SPECIAL_ATTACK);
            if (isSpecial) comboFactor = std::max<uint64_t>(comboFactor, 2);

            // Record telemetry event in CombatLogAnalyzer
            if (p1Blocking) {
                combatLogAnalyzer.recordCombatEvent("P1 BLOCK GUARD DEFENSE ABSORB");
            } else if (isSpecial) {
                combatLogAnalyzer.recordCombatEvent("P2 SPECIAL UNLEASHED CRITICAL IMPACT");
            } else {
                combatLogAnalyzer.recordCombatEvent("P2 ATTACK HIT CONNECTED");
            }

            // Practical No. 03: REAL 64-BIT ASSEMBLY DAMAGE CALCULATION!
            CombatALUResult alu = AssemblyDamageEngine::calculateHitDamage(p2->getAttackDamage(), targetDefense, comboFactor);
            latestALUTrace = "P2 HIT! " + alu.assemblyTrace;

            float knockDir = p2->isFacingRightDir() ? 1.0f : -1.0f;
            p1->takeHit((int)alu.finalDamage, knockDir, isSpecial);
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

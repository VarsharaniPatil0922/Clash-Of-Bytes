#ifndef FIGHTER_FACTORY_HPP
#define FIGHTER_FACTORY_HPP

#include <memory>
#include <string>
#include "Fighter.hpp"
#include "Warrior.hpp"
#include "Ninja.hpp"
#include "CyberBrawler.hpp"
#include "Paladin.hpp"
#include "Berserker.hpp"
#include "Gladiator.hpp"

enum FighterType {
    FIGHTER_WARRIOR = 0,       // Ironclad / Blaze Warrior
    FIGHTER_NINJA = 1,         // Shadow Ninja
    FIGHTER_CYBER_BRAWLER = 2, // Cyber Brawler
    FIGHTER_PALADIN = 3,       // Aura Paladin
    FIGHTER_BERSERKER = 4,     // Blaze Berserker
    FIGHTER_GLADIATOR = 5,     // Titan Gladiator
    FIGHTER_COUNT = 6
};

inline std::unique_ptr<Fighter> createFighter(int typeIndex, int playerIndex, float startX, float startY, bool faceRight) {
    int index = (typeIndex % FIGHTER_COUNT + FIGHTER_COUNT) % FIGHTER_COUNT;
    switch (index) {
        case FIGHTER_WARRIOR:
            return std::make_unique<Warrior>(playerIndex, startX, startY, faceRight);
        case FIGHTER_NINJA:
            return std::make_unique<Ninja>(playerIndex, startX, startY, faceRight);
        case FIGHTER_CYBER_BRAWLER:
            return std::make_unique<CyberBrawler>(playerIndex, startX, startY, faceRight);
        case FIGHTER_PALADIN:
            return std::make_unique<Paladin>(playerIndex, startX, startY, faceRight);
        case FIGHTER_BERSERKER:
            return std::make_unique<Berserker>(playerIndex, startX, startY, faceRight);
        case FIGHTER_GLADIATOR:
            return std::make_unique<Gladiator>(playerIndex, startX, startY, faceRight);
        default:
            return std::make_unique<Warrior>(playerIndex, startX, startY, faceRight);
    }
}

#endif // FIGHTER_FACTORY_HPP

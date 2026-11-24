#ifndef WARRIOR_HPP
#define WARRIOR_HPP

#include "Fighter.hpp"

class Warrior : public Fighter {
public:
    Warrior(int pIndex, float startX, float startY, bool faceRight);
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "HADOKEN (DRAGON FLAME)"; }
    std::string getRoleName() const override { return "Kenji - Martial Arts Master (Hadoken / Shoryuken)"; }
};

#endif // WARRIOR_HPP

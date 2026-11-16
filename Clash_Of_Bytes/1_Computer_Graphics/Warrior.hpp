#ifndef WARRIOR_HPP
#define WARRIOR_HPP

#include "Fighter.hpp"

class Warrior : public Fighter {
public:
    Warrior(int pIndex, float startX, float startY, bool faceRight);
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "TITAN SHIELD BASH"; }
    std::string getRoleName() const override { return "Vanguard (High HP, Heavy Shield)"; }
};

#endif // WARRIOR_HPP

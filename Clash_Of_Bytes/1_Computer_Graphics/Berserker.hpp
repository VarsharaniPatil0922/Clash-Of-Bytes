#ifndef BERSERKER_HPP
#define BERSERKER_HPP

#include "Fighter.hpp"

class Berserker : public Fighter {
private:
    float flameTimer;

public:
    Berserker(int pIndex, float startX, float startY, bool faceRight);
    void update(float dt) override;
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "INFERNO GREATSWORD REND"; }
    std::string getRoleName() const override { return "Heavy Brawler (Rage Multiplier)"; }
};

#endif // BERSERKER_HPP

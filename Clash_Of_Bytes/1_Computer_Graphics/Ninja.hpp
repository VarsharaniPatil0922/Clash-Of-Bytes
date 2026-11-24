#ifndef NINJA_HPP
#define NINJA_HPP

#include "Fighter.hpp"

class Ninja : public Fighter {
private:
    float shurikenAngle;
    float shurikenDist;

public:
    Ninja(int pIndex, float startX, float startY, bool faceRight);
    void update(float dt) override;
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "CYCLONE SPIN KICK"; }
    std::string getRoleName() const override { return "Elena - Capoeira Striker (Spin Kick / Axe Kick)"; }
};

#endif // NINJA_HPP

#ifndef GLADIATOR_HPP
#define GLADIATOR_HPP

#include "Fighter.hpp"

class Gladiator : public Fighter {
private:
    float quakeTimer;

public:
    Gladiator(int pIndex, float startX, float startY, bool faceRight);
    void update(float dt) override;
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "COLOSSEUM WRATH OF BYTES"; }
    std::string getRoleName() const override { return "Grand Champion (Titan Hybrid)"; }
};

#endif // GLADIATOR_HPP

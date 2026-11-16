#ifndef PALADIN_HPP
#define PALADIN_HPP

#include "Fighter.hpp"

class Paladin : public Fighter {
private:
    float haloAnim;
    float holyAuraHeight;

public:
    Paladin(int pIndex, float startX, float startY, bool faceRight);
    void update(float dt) override;
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "HOLY RADIANCE AEGIS"; }
    std::string getRoleName() const override { return "Holy Defender (Elemental & Shield)"; }
};

#endif // PALADIN_HPP

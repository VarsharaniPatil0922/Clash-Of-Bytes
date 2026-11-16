#ifndef CYBERBRAWLER_HPP
#define CYBERBRAWLER_HPP

#include "Fighter.hpp"

class CyberBrawler : public Fighter {
private:
    float plasmaDist;
    float plasmaPulse;

public:
    CyberBrawler(int pIndex, float startX, float startY, bool faceRight);
    void update(float dt) override;
    void render(Renderer& renderer, bool debugHitboxes) override;
    void executeSpecial() override;
    std::string getSpecialMoveName() const override { return "PLASMA OVERDRIVE BEAM"; }
    std::string getRoleName() const override { return "Cyber Striker (Overclocked Combos)"; }
};

#endif // CYBERBRAWLER_HPP

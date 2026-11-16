#ifndef PARTICLESYSTEM_HPP
#define PARTICLESYSTEM_HPP

/*
 * Subject: Computer Graphics & Data Structures
 * Topics: Dynamic Particle Management via Singly Linked List + Bresenham Line & Circle VFX
 */
#include "../2_Programming_Laboratory_DSA/Vector2D.hpp"
#include "../2_Programming_Laboratory_DSA/CO2_Prac03_Singly_Linked_List_Particles.hpp"
#include "CO1_Prac01_OpenGL_Gouraud_Renderer.hpp"
#include <cstdlib>

struct Particle {
    Vector2D position;
    Vector2D velocity;
    float r, g, b;
    float size;
    float life;
    float maxLife;
    bool isBresenhamRing; // True = Drawn using Bresenham's Circle Algorithm
    bool isLaserRay;       // True = Drawn using Bresenham's Line Algorithm
};

class ParticleSystem {
private:
    SinglyLinkedList<Particle> particles;

public:
    ParticleSystem() {}
    ~ParticleSystem() { clear(); }

    void emitHitSparks(const Vector2D& pos, bool isCritical = false) {
        int count = isCritical ? 20 : 12;

        // 1. Emit directional linear spark particles
        for (int i = 0; i < count; i++) {
            float angle = (rand() % 360) * 3.14159f / 180.0f;
            float speed = 150.0f + (rand() % 250);
            Vector2D vel(cosf(angle) * speed, sinf(angle) * speed);

            float red = 1.0f;
            float grn = isCritical ? 0.2f : (0.6f + (rand() % 40) / 100.0f);
            float blu = 0.1f;
            float duration = 0.25f + (rand() % 20) / 100.0f;

            Particle p;
            p.position = pos;
            p.velocity = vel;
            p.r = red; p.g = grn; p.b = blu;
            p.size = 5.0f;
            p.life = duration;
            p.maxLife = duration;
            p.isBresenhamRing = false;
            p.isLaserRay = (i % 3 == 0); // 1 in 3 particles is a Bresenham laser ray

            particles.insertFront(p);
        }

        // 2. Emit Bresenham expanding circle shockwave ring
        Particle ring;
        ring.position = pos;
        ring.velocity = Vector2D(0, 0);
        ring.r = 1.0f; ring.g = isCritical ? 0.3f : 0.9f; ring.b = 0.2f;
        ring.size = 12.0f;
        ring.life = 0.20f;
        ring.maxLife = 0.20f;
        ring.isBresenhamRing = true;
        ring.isLaserRay = false;

        particles.insertFront(ring);
    }

    void update(float dt) {
        // Traversal and node physics update
        particles.forEach([dt](Particle& p) {
            p.position += p.velocity * dt;
            p.life -= dt;
            if (p.isBresenhamRing) {
                p.size += 180.0f * dt; // Expanding circle radius
            }
        });

        // Delete expired particle nodes using Singly Linked List removeIf
        particles.removeIf([](const Particle& p) {
            return p.life <= 0.0f;
        });
    }

    void render(Renderer& renderer) {
        particles.forEach([&renderer](Particle& p) {
            float alpha = p.life / p.maxLife;
            if (alpha < 0.0f) alpha = 0.0f;

            if (p.isBresenhamRing) {
                // Practical No. 02: Bresenham Circle Algorithm
                renderer.drawBresenhamCircle(
                    (int)p.position.x, (int)p.position.y,
                    (int)p.size, p.r * alpha, p.g * alpha, p.b * alpha
                );
            } else if (p.isLaserRay) {
                // Practical No. 02: Bresenham Line Algorithm
                int x2 = (int)(p.position.x - p.velocity.x * 0.04f);
                int y2 = (int)(p.position.y - p.velocity.y * 0.04f);
                renderer.drawBresenhamLine(
                    (int)p.position.x, (int)p.position.y,
                    x2, y2, p.r * alpha, p.g * alpha, p.b * alpha
                );
            } else {
                // Practical No. 01: Shaded quad pixel
                renderer.drawRect(
                    p.position.x - p.size * 0.5f, p.position.y - p.size * 0.5f,
                    p.size, p.size, p.r, p.g, p.b, alpha
                );
            }
        });
    }

    void clear() { particles.clear(); }
    size_t getActiveCount() const { return particles.size(); }
};

#endif // PARTICLESYSTEM_HPP

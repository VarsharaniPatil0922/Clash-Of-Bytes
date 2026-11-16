#ifndef CO6_PRAC06_TEXTURE_LIGHTING_COLOR_EFFECTS_HPP
#define CO6_PRAC06_TEXTURE_LIGHTING_COLOR_EFFECTS_HPP

#include <windows.h>
#include <GL/gl.h>
#include <vector>
#include <cmath>
#include <string>
#include <iostream>
#include <algorithm>

/*
 * ============================================================================
 * CLASH OF BYTES: COMPUTER GRAPHICS LABORATORY (Course Code: 2310218L)
 * Practical No. 06 | Course Outcome 6 (CO6):
 * TEXTURE MAPPING, ILLUMINATION MODELS (LIGHTING) AND DYNAMIC COLOR EFFECTS
 * ============================================================================
 *
 * ACADEMIC & THEORETICAL FOUNDATIONS (CO6):
 * 1. TEXTURE MAPPING:
 *    - Parametric texture coordinate space (S, T) mapped to normalized [0, 1] range.
 *    - Texture Wrapping Modes: GL_REPEAT (periodic tiling for floor and pillars)
 *      and GL_CLAMP / GL_CLAMP_TO_EDGE (boundary clamping for icons and shields).
 *    - Texture Filtering: GL_LINEAR for magnification (bilinear interpolation),
 *      and GL_LINEAR for minification to eliminate aliasing and high-frequency noise.
 *    - Texture Environment: GL_MODULATE mode, where fragment color is calculated as:
 *      C_final = C_texture * C_lighting_and_vertex
 *    - Procedural Texture Synthesis: Pixel data generated algorithmically in memory
 *      (RGBA 32-bit format) to eliminate missing external asset dependencies.
 *
 * 2. ILLUMINATION & LIGHTING PIPELINE (PHONG / GOURAUD ILLUMINATION):
 *    - Fixed-Function OpenGL hardware lighting equation:
 *      I = I_ambient * K_ambient + 
 *          I_diffuse * K_diffuse * max(0, N . L) + 
 *          I_specular * K_specular * max(0, N . H)^s
 *      where N = Surface Normal, L = Light Direction Vector, H = Halfway Vector,
 *      and s = Material Shininess Exponent.
 *    - Light 0: Arena Overhead Directional / Key Spotlight (cool cyber-arena fill).
 *    - Light 1: Dynamic Combat Point Light (tracks strikes, hit sparks, and special moves
 *      with distance attenuation: 1 / (c + l*d + q*d^2)).
 *    - Two-Sided Lighting (GL_LIGHT_MODEL_TWO_SIDE) and Local Viewer calculation.
 *    - Surface Normal generation (glNormal3f) for 2D floor surfaces and 3D cube facets.
 *
 * 3. COLOR EFFECTS & MATERIAL PROPERTIES:
 *    - Material Reflection: Specular coefficient, shininess, and self-luminous emission.
 *    - Fighter Archetype Materials:
 *      * Blaze Warrior: Metallic gold/crimson specular sheen (shininess = 64) + ember emission.
 *      * Shadow Ninja: Matte stealth obsidian (shininess = 24) + moonlit cyan rim.
 *      * Cyber Brawler: Chrome cybernetic polish (shininess = 96) + electric blue circuit emission.
 *      * Aura Paladin: Brilliant holy gold mirror finish (shininess = 128) + radiant halo emission.
 *      * Blood Berserker: Blood-rage dynamic emission scaling inversely with remaining HP!
 *      * Steel Gladiator: Burnished battle-hardened steel (shininess = 48).
 *    - Dynamic Combat VFX:
 *      * Hit Flash: Specular surge and white-hot flash on struck fighter.
 *      * Guard Barrier: Translucent textured hexagonal energy shield during blocking.
 *      * Rage / Overdrive: Pulsing emissive aura during low health or special moves.
 * ============================================================================
 */

namespace CG_CO6 {

    // Procedural Texture Identifiers
    enum TextureType {
        TEX_FLOOR_GRID = 0,
        TEX_CYBER_PILLAR = 1,
        TEX_ENERGY_PLATFORM = 2,
        TEX_CIRCUIT_CUBE = 3,
        TEX_HEX_SHIELD = 4,
        TEX_RADIAL_SPARK = 5,
        TEX_COUNT = 6
    };

    // Lighting Modes for interactive user toggling (HotKey: F6)
    enum class LightingMode {
        FULL_DYNAMIC = 0,   // Key Overhead Light + Dynamic Combat Point Light
        KEY_LIGHT_ONLY = 1, // Key Overhead Floodlight only
        FLAT_UNLIT = 2      // Fixed-function lighting disabled (raw Gouraud flat)
    };

    // Material Configuration Structure
    struct MaterialProperties {
        float ambient[4];
        float diffuse[4];
        float specular[4];
        float emission[4];
        float shininess; // Exponent range: 1.0 (dull) to 128.0 (mirror polish)
    };

    // ------------------------------------------------------------------------
    // PROCEDURAL TEXTURE GENERATOR
    // Generates 32-bit RGBA texture maps algorithmically with zero external file dependencies
    // ------------------------------------------------------------------------
    class ProceduralTextureManager {
    private:
        GLuint textureHandles[TEX_COUNT];
        bool initialized;

    public:
        ProceduralTextureManager() : initialized(false) {
            for (int i = 0; i < TEX_COUNT; i++) {
                textureHandles[i] = 0;
            }
        }

        ~ProceduralTextureManager() {
            cleanup();
        }

        void cleanup() {
            if (initialized) {
                glDeleteTextures(TEX_COUNT, textureHandles);
                for (int i = 0; i < TEX_COUNT; i++) {
                    textureHandles[i] = 0;
                }
                initialized = false;
            }
        }

        bool init() {
            if (initialized) return true;

            glGenTextures(TEX_COUNT, textureHandles);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            generateFloorTexture();
            generatePillarTexture();
            generatePlatformTexture();
            generateCircuitCubeTexture();
            generateHexShieldTexture();
            generateRadialSparkTexture();

            initialized = true;
            return true;
        }

        GLuint getTexture(TextureType type) const {
            if (type >= 0 && type < TEX_COUNT) {
                return textureHandles[type];
            }
            return 0;
        }

        void bind(TextureType type) const {
            if (initialized && type >= 0 && type < TEX_COUNT) {
                glBindTexture(GL_TEXTURE_2D, textureHandles[type]);
            }
        }

        void unbind() const {
            glBindTexture(GL_TEXTURE_2D, 0);
        }

    private:
        // Texture 1: Arena Floor Metallic Hazard Grid (128x128)
        void generateFloorTexture() {
            const int W = 128, H = 128;
            std::vector<unsigned char> pixels(W * H * 4);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int idx = (y * W + x) * 4;

                    // Base dark metallic blue-gray plate
                    unsigned char r = 28 + ((x ^ y) & 7);
                    unsigned char g = 34 + ((x ^ y) & 7);
                    unsigned char b = 48 + ((x ^ y) & 11);
                    unsigned char a = 255;

                    // Beveled border lines around each 32x32 tile
                    int tx = x % 32;
                    int ty = y % 32;
                    if (tx == 0 || ty == 0 || tx == 31 || ty == 31) {
                        r = 16; g = 20; b = 30; // Dark recessed grout
                    } else if (tx == 1 || ty == 1) {
                        r = 60; g = 80; b = 110; // Top-left specular bevel highlight
                    } else if (tx == 30 || ty == 30) {
                        r = 20; g = 24; b = 36; // Bottom-right shadow
                    }

                    // High-tech glowing circuitry conduits along tile centers
                    if ((tx == 16 && (ty >= 8 && ty <= 24)) || (ty == 16 && (tx >= 8 && tx <= 24))) {
                        r = 30; g = 200; b = 255; // Neon cyan circuit trace
                    }
                    if (tx == 16 && ty == 16) {
                        r = 255; g = 230; b = 80; // Gold terminal node
                    }

                    // Diamond tread crosshatch micro-texture
                    if ((tx + ty) % 8 == 0 || (tx - ty + 32) % 8 == 0) {
                        r = (unsigned char)std::min(255, (int)r + 14);
                        g = (unsigned char)std::min(255, (int)g + 16);
                        b = (unsigned char)std::min(255, (int)b + 20);
                    }

                    pixels[idx + 0] = r;
                    pixels[idx + 1] = g;
                    pixels[idx + 2] = b;
                    pixels[idx + 3] = a;
                }
            }

            uploadTexture(TEX_FLOOR_GRID, W, H, pixels.data(), GL_REPEAT, GL_REPEAT);
        }

        // Texture 2: Cyber Pillar Carbon Fiber with Hazard Chevrons (64x128)
        void generatePillarTexture() {
            const int W = 64, H = 128;
            std::vector<unsigned char> pixels(W * H * 4);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int idx = (y * W + x) * 4;

                    // Carbon fiber diagonal weave
                    bool weave = ((x / 4) + (y / 4)) % 2 == 0;
                    unsigned char base = weave ? 36 : 22;
                    unsigned char r = base;
                    unsigned char g = base + 4;
                    unsigned char b = base + 12;
                    unsigned char a = 255;

                    // Diagonal 45-degree cyber warning hazard stripes in lower & upper quarters
                    if ((y >= 16 && y <= 40) || (y >= 88 && y <= 112)) {
                        bool stripe = ((x + y) / 8) % 2 == 0;
                        if (stripe) {
                            r = 240; g = 190; b = 20; // High-visibility hazard gold
                        } else {
                            r = 18;  g = 18;  b = 22; // Matte black
                        }
                    }

                    // Vertical neon blue power spine down the center
                    if (x >= 30 && x <= 33) {
                        r = 40; g = 180; b = 255;
                    } else if (x == 29 || x == 34) {
                        r = 20; g = 80;  b = 140; // Soft glow falloff
                    }

                    // Outer border edges
                    if (x <= 2 || x >= W - 3) {
                        r = 14; g = 18; b = 28;
                    }

                    pixels[idx + 0] = r;
                    pixels[idx + 1] = g;
                    pixels[idx + 2] = b;
                    pixels[idx + 3] = a;
                }
            }

            uploadTexture(TEX_CYBER_PILLAR, W, H, pixels.data(), GL_CLAMP, GL_REPEAT);
        }

        // Texture 3: Floating Battle Platform Reinforced Grille (128x32)
        void generatePlatformTexture() {
            const int W = 128, H = 32;
            std::vector<unsigned char> pixels(W * H * 4);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int idx = (y * W + x) * 4;

                    unsigned char r = 32;
                    unsigned char g = 38;
                    unsigned char b = 52;
                    unsigned char a = 255;

                    // Neon hazard edge border
                    if (y <= 2 || y >= H - 3) {
                        r = 255; g = 180; b = 40; // Top & bottom warning gold rim
                    } else if (x <= 3 || x >= W - 4) {
                        r = 255; g = 180; b = 40;
                    }

                    // Honeycomb grid interior
                    if ((x % 16 == 0 || y % 8 == 0) && y > 2 && y < H - 3) {
                        r = 18; g = 22; b = 32; // Recessed metal grating
                    }

                    // Power bus conduits
                    if (y >= 14 && y <= 17 && x > 6 && x < W - 7) {
                        r = 40; g = 220; b = 255; // Cyan plasma conduit
                    }

                    pixels[idx + 0] = r;
                    pixels[idx + 1] = g;
                    pixels[idx + 2] = b;
                    pixels[idx + 3] = a;
                }
            }

            uploadTexture(TEX_ENERGY_PLATFORM, W, H, pixels.data(), GL_REPEAT, GL_CLAMP);
        }

        // Texture 4: Futuristic Circuit Transformation Cube (64x64)
        void generateCircuitCubeTexture() {
            const int W = 64, H = 64;
            std::vector<unsigned char> pixels(W * H * 4);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int idx = (y * W + x) * 4;

                    // Dark obsidian silicon core
                    unsigned char r = 24;
                    unsigned char g = 28;
                    unsigned char b = 42;
                    unsigned char a = 255;

                    // Beveled chip perimeter
                    if (x <= 3 || y <= 3 || x >= W - 4 || y >= H - 4) {
                        r = 60; g = 75; b = 100;
                    }

                    // Concentric circuit traces
                    float dx = x - 31.5f;
                    float dy = y - 31.5f;
                    float dist = std::sqrt(dx * dx + dy * dy);

                    // Central energy reactor glyph
                    if (dist <= 8.0f) {
                        r = 255; g = 240; b = 120; // Glowing gold reactor core
                    } else if (dist >= 14.0f && dist <= 16.0f) {
                        r = 40;  g = 220; b = 255; // Inner cyan containment ring
                    } else if (dist >= 22.0f && dist <= 24.0f) {
                        r = 255; g = 80;  b = 40;  // Outer amber energy field
                    }

                    // 4 Cardinal circuit buses running outward
                    if ((std::abs(dx) <= 1.5f || std::abs(dy) <= 1.5f) && dist >= 8.0f && dist <= 28.0f) {
                        r = 50; g = 200; b = 255;
                    }

                    // 4 Corner solder alignment marks
                    if ((x <= 8 && y <= 8) || (x >= W - 9 && y <= 8) ||
                        (x <= 8 && y >= H - 9) || (x >= W - 9 && y >= H - 9)) {
                        r = 220; g = 180; b = 40;
                    }

                    pixels[idx + 0] = r;
                    pixels[idx + 1] = g;
                    pixels[idx + 2] = b;
                    pixels[idx + 3] = a;
                }
            }

            uploadTexture(TEX_CIRCUIT_CUBE, W, H, pixels.data(), GL_CLAMP, GL_CLAMP);
        }

        // Texture 5: Hexagonal Guard Shield Forcefield (64x64 RGBA with Alpha Transparency)
        void generateHexShieldTexture() {
            const int W = 64, H = 64;
            std::vector<unsigned char> pixels(W * H * 4);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int idx = (y * W + x) * 4;

                    float dx = x - 31.5f;
                    float dy = y - 31.5f;
                    float dist = std::sqrt(dx * dx + dy * dy);

                    unsigned char r = 60;
                    unsigned char g = 200;
                    unsigned char b = 255;
                    unsigned char a = 0;

                    if (dist <= 30.0f) {
                        // Hexagonal honeycomb wireframe
                        float u = (x * 0.18f);
                        float v = (y * 0.18f);
                        float hex = std::sin(u) * std::cos(v) + std::sin(v * 1.732f);

                        if (std::abs(hex) < 0.25f || dist >= 26.0f) {
                            // Bright barrier perimeter grid
                            r = 180; g = 240; b = 255;
                            a = 210;
                        } else {
                            // Translucent barrier energy film
                            r = 30;  g = 140; b = 230;
                            a = (unsigned char)(60 + (30.0f - dist) * 2.5f);
                        }
                    }

                    pixels[idx + 0] = r;
                    pixels[idx + 1] = g;
                    pixels[idx + 2] = b;
                    pixels[idx + 3] = a;
                }
            }

            uploadTexture(TEX_HEX_SHIELD, W, H, pixels.data(), GL_CLAMP, GL_CLAMP);
        }

        // Texture 6: Radial Bloom Hit Spark (32x32 Smooth Alpha Falloff)
        void generateRadialSparkTexture() {
            const int W = 32, H = 32;
            std::vector<unsigned char> pixels(W * H * 4);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int idx = (y * W + x) * 4;

                    float dx = (x - 15.5f) / 15.5f;
                    float dy = (y - 15.5f) / 15.5f;
                    float r2 = dx * dx + dy * dy;

                    if (r2 <= 1.0f) {
                        float falloff = std::max(0.0f, 1.0f - std::sqrt(r2));
                        float alpha = falloff * falloff; // Quadratic smooth falloff

                        pixels[idx + 0] = (unsigned char)(255);
                        pixels[idx + 1] = (unsigned char)(220 + 35 * falloff);
                        pixels[idx + 2] = (unsigned char)(140 * falloff);
                        pixels[idx + 3] = (unsigned char)(alpha * 255);
                    } else {
                        pixels[idx + 0] = 0;
                        pixels[idx + 1] = 0;
                        pixels[idx + 2] = 0;
                        pixels[idx + 3] = 0;
                    }
                }
            }

            uploadTexture(TEX_RADIAL_SPARK, W, H, pixels.data(), GL_CLAMP, GL_CLAMP);
        }

        void uploadTexture(TextureType type, int width, int height, const unsigned char* data, GLint wrapS, GLint wrapT) {
            glBindTexture(GL_TEXTURE_2D, textureHandles[type]);

            // Bilinear filtering for smooth scaling
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // Tiling vs Clamping configuration
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);

            // Upload 32-bit RGBA texture pixels
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

            // Modulate texture color with underlying vertex colors and illumination
            glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        }
    };

    // ------------------------------------------------------------------------
    // HARDWARE LIGHTING & ILLUMINATION ENGINE
    // Fixed-Function OpenGL Lighting, Spotlights, Dynamic Combat Point Light & Materials
    // ------------------------------------------------------------------------
    class LightingEngine {
    private:
        LightingMode currentMode;
        float combatLightX;
        float combatLightY;
        float combatLightR;
        float combatLightG;
        float combatLightB;
        float combatLightIntensity;
        float pulseTime;

    public:
        LightingEngine()
            : currentMode(LightingMode::FULL_DYNAMIC),
              combatLightX(640.0f), combatLightY(300.0f),
              combatLightR(1.0f), combatLightG(0.85f), combatLightB(0.2f),
              combatLightIntensity(0.0f), pulseTime(0.0f) {}

        void update(float dt) {
            pulseTime += dt;
            if (combatLightIntensity > 0.0f) {
                combatLightIntensity -= dt * 3.2f; // Smooth exponential hit flash decay
                if (combatLightIntensity < 0.0f) combatLightIntensity = 0.0f;
            }
        }

        void triggerCombatLight(float x, float y, float r, float g, float b, float intensity = 1.0f) {
            combatLightX = x;
            combatLightY = y;
            combatLightR = r;
            combatLightG = g;
            combatLightB = b;
            combatLightIntensity = intensity;
        }

        void cycleMode() {
            if (currentMode == LightingMode::FULL_DYNAMIC) {
                currentMode = LightingMode::KEY_LIGHT_ONLY;
            } else if (currentMode == LightingMode::KEY_LIGHT_ONLY) {
                currentMode = LightingMode::FLAT_UNLIT;
            } else {
                currentMode = LightingMode::FULL_DYNAMIC;
            }
        }

        LightingMode getMode() const { return currentMode; }

        std::string getModeName() const {
            switch (currentMode) {
                case LightingMode::FULL_DYNAMIC:   return "FULL DYNAMIC (Spotlight + Point)";
                case LightingMode::KEY_LIGHT_ONLY: return "KEY LIGHT ONLY (Overhead Ambient)";
                case LightingMode::FLAT_UNLIT:     return "FLAT UNLIT (Lighting Disabled)";
            }
            return "UNKNOWN";
        }

        void beginSceneLighting() {
            if (currentMode == LightingMode::FLAT_UNLIT) {
                glDisable(GL_LIGHTING);
                return;
            }

            glEnable(GL_LIGHTING);
            glEnable(GL_NORMALIZE);

            // Configure two-sided and local viewer lighting model
            glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_TRUE);
            glLightModeli(GL_LIGHT_MODEL_LOCAL_VIEWER, GL_TRUE);

            // Enable Color Material tracking so fighter colors & vertex gradients modulate with light
            glEnable(GL_COLOR_MATERIAL);
            glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

            // -------------------------------------------------------------
            // LIGHT 0: Arena Overhead Key Spotlight (Illuminates from Top)
            // -------------------------------------------------------------
            glEnable(GL_LIGHT0);
            GLfloat light0_pos[] = { 640.0f, 750.0f, 320.0f, 1.0f }; // Overhead
            GLfloat light0_amb[] = { 0.35f, 0.38f, 0.48f, 1.0f };    // Cool ambient cyber fill
            GLfloat light0_dif[] = { 0.85f, 0.88f, 0.98f, 1.0f };    // Clean daylight white
            GLfloat light0_spe[] = { 1.00f, 1.00f, 1.00f, 1.0f };    // Crisp specular highlight

            glLightfv(GL_LIGHT0, GL_POSITION, light0_pos);
            glLightfv(GL_LIGHT0, GL_AMBIENT, light0_amb);
            glLightfv(GL_LIGHT0, GL_DIFFUSE, light0_dif);
            glLightfv(GL_LIGHT0, GL_SPECULAR, light0_spe);

            // -------------------------------------------------------------
            // LIGHT 1: Dynamic Combat Action Point Light (Tracks Hits & Moves)
            // -------------------------------------------------------------
            if (currentMode == LightingMode::FULL_DYNAMIC && combatLightIntensity > 0.01f) {
                glEnable(GL_LIGHT1);
                GLfloat light1_pos[] = { combatLightX, combatLightY, 90.0f, 1.0f };
                GLfloat light1_amb[] = { 0.0f, 0.0f, 0.0f, 1.0f };
                GLfloat light1_dif[] = {
                    combatLightR * combatLightIntensity,
                    combatLightG * combatLightIntensity,
                    combatLightB * combatLightIntensity,
                    1.0f
                };
                GLfloat light1_spe[] = {
                    combatLightR * combatLightIntensity,
                    combatLightG * combatLightIntensity,
                    combatLightB * combatLightIntensity,
                    1.0f
                };

                glLightfv(GL_LIGHT1, GL_POSITION, light1_pos);
                glLightfv(GL_LIGHT1, GL_AMBIENT, light1_amb);
                glLightfv(GL_LIGHT1, GL_DIFFUSE, light1_dif);
                glLightfv(GL_LIGHT1, GL_SPECULAR, light1_spe);

                // Inverse-square distance attenuation
                glLightf(GL_LIGHT1, GL_CONSTANT_ATTENUATION, 1.0f);
                glLightf(GL_LIGHT1, GL_LINEAR_ATTENUATION, 0.0022f);
                glLightf(GL_LIGHT1, GL_QUADRATIC_ATTENUATION, 0.000015f);
            } else {
                glDisable(GL_LIGHT1);
            }
        }

        void endSceneLighting() {
            // Disable lighting so 2D HUD text, health bars, and overlays render with raw vibrancy
            glDisable(GL_LIGHTING);
            glDisable(GL_COLOR_MATERIAL);
        }

        // Apply Material Reflection Settings
        void setMaterial(float specR, float specG, float specB, float shininess,
                         float emitR = 0.0f, float emitG = 0.0f, float emitB = 0.0f) 
        {
            if (currentMode == LightingMode::FLAT_UNLIT) return;

            GLfloat spec[] = { specR, specG, specB, 1.0f };
            GLfloat emit[] = { emitR, emitG, emitB, 1.0f };

            float clampedShininess = (shininess < 1.0f) ? 1.0f : ((shininess > 128.0f) ? 128.0f : shininess);
            glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, spec);
            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, clampedShininess);
            glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, emit);
        }

        void resetMaterial() {
            if (currentMode == LightingMode::FLAT_UNLIT) return;

            GLfloat defaultSpec[] = { 0.2f, 0.2f, 0.2f, 1.0f };
            GLfloat defaultEmit[] = { 0.0f, 0.0f, 0.0f, 1.0f };

            glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, defaultSpec);
            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 16.0f);
            glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, defaultEmit);
        }
    };

    // ------------------------------------------------------------------------
    // FIGHTER COLOR EFFECTS & MATERIAL ARCHETYPE SYSTEM
    // Communicates visual identity, elemental attributes, and combat states
    // ------------------------------------------------------------------------
    class FighterColorEffects {
    public:
        // Set material reflection & specular characteristics based on fighter archetype
        static void applyFighterMaterial(LightingEngine& lighting, int characterType, float healthPercent, bool isAttacking, bool isFlinching) {
            if (isFlinching) {
                // High-intensity white-hot specular flash during hit stun
                lighting.setMaterial(1.0f, 1.0f, 1.0f, 128.0f, 0.85f, 0.15f, 0.15f);
                return;
            }

            switch (characterType) {
                case 0: // Blaze Warrior: Golden-Crimson Armor & Ember Glow
                    if (isAttacking) {
                        lighting.setMaterial(1.0f, 0.7f, 0.2f, 96.0f, 0.5f, 0.15f, 0.0f);
                    } else {
                        lighting.setMaterial(0.9f, 0.6f, 0.2f, 64.0f, 0.15f, 0.05f, 0.0f);
                    }
                    break;

                case 1: // Shadow Ninja: Matte Stealth Obsidian & Cyan Rim
                    if (isAttacking) {
                        lighting.setMaterial(0.4f, 0.8f, 1.0f, 64.0f, 0.1f, 0.3f, 0.5f);
                    } else {
                        lighting.setMaterial(0.3f, 0.4f, 0.6f, 24.0f, 0.02f, 0.05f, 0.1f);
                    }
                    break;

                case 2: // Cyber Brawler: Chrome Cybernetic Polish & Neon Plasma
                    if (isAttacking) {
                        lighting.setMaterial(0.3f, 1.0f, 1.0f, 120.0f, 0.2f, 0.6f, 0.9f);
                    } else {
                        lighting.setMaterial(0.4f, 0.8f, 1.0f, 96.0f, 0.05f, 0.2f, 0.35f);
                    }
                    break;

                case 3: // Aura Paladin: Brilliant Holy Gold Mirror Finish
                    if (isAttacking) {
                        lighting.setMaterial(1.0f, 0.95f, 0.5f, 128.0f, 0.6f, 0.5f, 0.1f);
                    } else {
                        lighting.setMaterial(0.95f, 0.85f, 0.4f, 110.0f, 0.25f, 0.2f, 0.05f);
                    }
                    break;

                case 4: // Blood Berserker: Blood-Rage Emission Scales Inversely with Health!
                    if (healthPercent < 0.40f) {
                        // Blood Rage Overdrive: intense crimson pulsation!
                        float pulse = 0.5f + 0.5f * std::sin(GetTickCount() * 0.015f);
                        lighting.setMaterial(1.0f, 0.1f, 0.1f, 80.0f, 0.7f + 0.3f * pulse, 0.05f, 0.0f);
                    } else {
                        lighting.setMaterial(0.7f, 0.2f, 0.2f, 48.0f, 0.15f, 0.02f, 0.0f);
                    }
                    break;

                case 5: // Steel Gladiator: Burnished Iron & Sunburst Specular
                    if (isAttacking) {
                        lighting.setMaterial(0.9f, 0.85f, 0.7f, 72.0f, 0.3f, 0.25f, 0.1f);
                    } else {
                        lighting.setMaterial(0.7f, 0.65f, 0.6f, 48.0f, 0.05f, 0.05f, 0.05f);
                    }
                    break;

                default:
                    lighting.resetMaterial();
                    break;
            }
        }
    };
}

#endif // CO6_PRAC06_TEXTURE_LIGHTING_COLOR_EFFECTS_HPP

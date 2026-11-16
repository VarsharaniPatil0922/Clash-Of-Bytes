#ifndef CO1_PRAC01_OPENGL_GOURAUD_RENDERER_HPP
#define CO1_PRAC01_OPENGL_GOURAUD_RENDERER_HPP

#include <windows.h>
#include <GL/gl.h>
#include <string>
#include <vector>
#include "../2_Programming_Laboratory_DSA/Vector2D.hpp"
#include "CO3_Prac02_Bresenham_Line_Circle_VFX.hpp"
#include "CO3_Prac03_ScanLine_Polygon_HealthBar.hpp"
#include "CO2_Prac04_Transformation_Matrices_3DCube.hpp"
#include "CO5_Prac05_Cohen_Sutherland_Clipping.hpp"
#include "CO6_Prac06_Texture_Lighting_Color_Effects.hpp"

/*
 * Subject: Computer Graphics
 * Course Outcomes: CO1, CO2, CO3, CO5, CO6 | Practicals 01 to 06
 * Topic: Master OpenGL Renderer with Gouraud Shading, Bresenham Algorithms,
 * Scan-Line Polygon Fill, 3D Matrix Transformations, Cohen-Sutherland Clipping,
 * and Practical 06 (CO6): Texture Mapping, Fixed-Function Lighting & Color Effects
 */
class Renderer {
private:
    int screenWidth;
    int screenHeight;
    float screenShakeTimer;
    float screenShakeIntensity;
    float cubeSpinAngle;

    // Practical 06 (CO6) Subsystems
    CG_CO6::ProceduralTextureManager textureManager;
    CG_CO6::LightingEngine lightingEngine;
    bool texturesEnabled;
    bool colorEffectsEnabled;

public:
    Renderer();
    ~Renderer();

    void init(int width, int height);
    void clear(float r = 0.05f, float g = 0.05f, float b = 0.08f);
    void beginFrame();
    void endFrame(HDC hdc);

    // Practical 01: Shaded Geometric Primitives (Gouraud vertex color interpolation)
    void drawRect(float x, float y, float w, float h, float r, float g, float b, float a = 1.0f);
    void drawGradientRect(float x, float y, float w, float h, float r1, float g1, float b1, float r2, float g2, float b2);
    void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, float r, float g, float b);
    void drawRectOutline(float x, float y, float w, float h, float r, float g, float b, float lineWidth = 2.0f);

    // Practical 02: Bresenham's Line & Circle Algorithms
    void drawBresenhamLine(int x1, int y1, int x2, int y2, float r, float g, float b);
    void drawBresenhamCircle(int xc, int yc, int radius, float r, float g, float b);

    // Practical 03: Scan-Line Polygon Filling Algorithm
    void drawScanLineFilledPolygon(const std::vector<std::pair<int, int>>& vertices, float r, float g, float b);
    void drawScanLineHealthBar(float x, float y, float w, float h, float currentHP, float maxHP, bool isFacingLeft);

    // Practical 04: Transformation Matrices & Spinning 3D Color Cube
    void drawSpinning3DCube(float centerX, float centerY, float size);
    void drawRotatingShuriken(float centerX, float centerY, float size, float angle);

    // Practical 05: Cohen-Sutherland Line Clipping Algorithm
    void drawClippedBresenhamLine(int x1, int y1, int x2, int y2,
                                  int xmin, int ymin, int xmax, int ymax,
                                  float r, float g, float b);

    // Practical 06 (CO6): Texture Mapping, Lighting & Dynamic Color Effects
    void beginLighting();
    void endLighting();
    void setCombatLight(float x, float y, float r, float g, float b, float intensity = 1.0f);
    void setMaterial(float specR, float specG, float specB, float shininess, float emitR = 0.0f, float emitG = 0.0f, float emitB = 0.0f);
    void resetMaterial();
    void applyFighterMaterial(int characterType, float healthPercent, bool isAttacking, bool isFlinching);

    void drawTexturedRect(float x, float y, float w, float h, CG_CO6::TextureType texType,
                          float repeatS = 1.0f, float repeatT = 1.0f,
                          float r = 1.0f, float g = 1.0f, float b = 1.0f, float a = 1.0f);
    void drawTexturedFloor(float x, float y, float w, float h);
    void drawTexturedPillar(float x, float y, float w, float h);
    void drawTexturedPlatform(float x, float y, float w, float h);
    void drawTexturedCube(float centerX, float centerY, float size);
    void drawHexagonalShield(float centerX, float centerY, float radius, float pulseAlpha, float r = 0.2f, float g = 0.8f, float b = 1.0f);

    // Interactive CO6 Toggles (Hotkeys: F6, F7, F8)
    void toggleLighting();
    void toggleTextures();
    void toggleColorEffects();
    bool areTexturesEnabled() const { return texturesEnabled; }
    bool areColorEffectsEnabled() const { return colorEffectsEnabled; }
    CG_CO6::LightingMode getLightingMode() const { return lightingEngine.getMode(); }
    std::string getLightingModeName() const { return lightingEngine.getModeName(); }
    std::string getTexturesModeName() const { return texturesEnabled ? "TEXTURED (Procedural RGBA)" : "FLAT SHADED"; }
    std::string getColorEffectsModeName() const { return colorEffectsEnabled ? "AURAS & FLASHES ACTIVE" : "STANDARD"; }

    // In-game 8-bit text rendering & screen shake
    void drawText(float x, float y, const std::string& text, float r = 1.0f, float g = 1.0f, float b = 1.0f, float scale = 2.0f);
    void triggerScreenShake(float intensity = 8.0f, float duration = 0.25f);
    void update(float dt);
};

#endif // CO1_PRAC01_OPENGL_GOURAUD_RENDERER_HPP

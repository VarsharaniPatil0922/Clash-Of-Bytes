/*
 * Subject: Computer Graphics
 * Course Outcomes: CO1, CO2, CO3 | Practicals 01 to 04
 * Topic: OpenGL Primitives, Gouraud Shading, Bresenham VFX, Scan-Line HP Bars & 3D Transformations
 */
#include "CO1_Prac01_OpenGL_Gouraud_Renderer.hpp"
#include <cmath>
#include <algorithm>
#include <cctype>
#include <cstdlib>

Renderer::Renderer() 
    : screenWidth(1280), screenHeight(720), 
      screenShakeTimer(0.0f), screenShakeIntensity(0.0f), cubeSpinAngle(0.0f),
      texturesEnabled(true), colorEffectsEnabled(true) {}

Renderer::~Renderer() {}

void Renderer::init(int width, int height) {
    screenWidth = width;
    screenHeight = height;

    glViewport(0, 0, screenWidth, screenHeight);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0.0, screenWidth, 0.0, screenHeight, -500.0, 500.0);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Practical 06 (CO6): Initialize Procedural 32-bit RGBA Textures
    textureManager.init();
}

void Renderer::clear(float r, float g, float b) {
    glClearColor(r, g, b, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
}

void Renderer::beginFrame() {
    glLoadIdentity();
    if (screenShakeTimer > 0.0f) {
        float shakeX = ((rand() % 100) / 50.0f - 1.0f) * screenShakeIntensity;
        float shakeY = ((rand() % 100) / 50.0f - 1.0f) * screenShakeIntensity;
        glTranslatef(shakeX, shakeY, 0.0f);
    }
}

void Renderer::endFrame(HDC hdc) {
    SwapBuffers(hdc);
}

void Renderer::update(float dt) {
    if (screenShakeTimer > 0.0f) {
        screenShakeTimer -= dt;
        if (screenShakeTimer <= 0.0f) {
            screenShakeTimer = 0.0f;
            screenShakeIntensity = 0.0f;
        }
    }
    cubeSpinAngle += 60.0f * dt;
    if (cubeSpinAngle >= 360.0f) cubeSpinAngle -= 360.0f;

    // Practical 06 (CO6): Update dynamic combat point light decay
    lightingEngine.update(dt);
}

void Renderer::triggerScreenShake(float intensity, float duration) {
    screenShakeIntensity = intensity;
    screenShakeTimer = duration;
}

// ----------------------------------------------------------------------------
// Practical No. 01: Shaded Geometric Primitives (OpenGL Primitives)
// ----------------------------------------------------------------------------
void Renderer::drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
    glNormal3f(0.0f, 0.0f, 1.0f);
    glColor4f(r, g, b, a);
    glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + h);
        glVertex2f(x, y + h);
    glEnd();
}

void Renderer::drawGradientRect(float x, float y, float w, float h, float r1, float g1, float b1, float r2, float g2, float b2) {
    glNormal3f(0.0f, 0.0f, 1.0f);
    glBegin(GL_QUADS);
        glColor3f(r1, g1, b1); glVertex2f(x, y);
        glColor3f(r1, g1, b1); glVertex2f(x + w, y);
        glColor3f(r2, g2, b2); glVertex2f(x + w, y + h);
        glColor3f(r2, g2, b2); glVertex2f(x, y + h);
    glEnd();
}

void Renderer::drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, float r, float g, float b) {
    glNormal3f(0.0f, 0.0f, 1.0f);
    glColor3f(r, g, b);
    glBegin(GL_TRIANGLES);
        glVertex2f(x1, y1);
        glVertex2f(x2, y2);
        glVertex2f(x3, y3);
    glEnd();
}

void Renderer::drawRectOutline(float x, float y, float w, float h, float r, float g, float b, float lineWidth) {
    glColor3f(r, g, b);
    glLineWidth(lineWidth);
    glBegin(GL_LINE_LOOP);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + h);
        glVertex2f(x, y + h);
    glEnd();
}

// ----------------------------------------------------------------------------
// Practical No. 02: Bresenham's Line and Circle Drawing Algorithms
// ----------------------------------------------------------------------------
void Renderer::drawBresenhamLine(int x1, int y1, int x2, int y2, float r, float g, float b) {
    glColor3f(r, g, b);
    glPointSize(2.0f);
    glBegin(GL_POINTS);

    int dx = std::abs(x2 - x1);
    int dy = std::abs(y2 - y1);
    int sx = (x1 < x2) ? 1 : -1;
    int sy = (y1 < y2) ? 1 : -1;
    int err = dx - dy;
    int x = x1, y = y1;

    while (true) {
        glVertex2i(x, y);
        if (x == x2 && y == y2) break;
        int e2 = 2 * err;
        if (e2 > -dy) { err -= dy; x += sx; }
        if (e2 < dx)  { err += dx; y += sy; }
    }
    glEnd();
}

void Renderer::drawBresenhamCircle(int xc, int yc, int radius, float r, float g, float b) {
    glColor3f(r, g, b);
    glPointSize(2.0f);
    glBegin(GL_POINTS);

    int x = 0;
    int y = radius;
    int d = 3 - 2 * radius;

    auto plot8 = [&](int px, int py) {
        glVertex2i(xc + px, yc + py); glVertex2i(xc - px, yc + py);
        glVertex2i(xc + px, yc - py); glVertex2i(xc - px, yc - py);
        glVertex2i(xc + py, yc + px); glVertex2i(xc - py, yc + px);
        glVertex2i(xc + py, yc - px); glVertex2i(xc - py, yc - px);
    };

    plot8(x, y);
    while (y >= x) {
        x++;
        if (d > 0) {
            y--;
            d = d + 4 * (x - y) + 10;
        } else {
            d = d + 4 * x + 6;
        }
        plot8(x, y);
    }
    glEnd();
}

// ----------------------------------------------------------------------------
// Practical No. 03: Scan-Line Polygon Filling Algorithm
// ----------------------------------------------------------------------------
void Renderer::drawScanLineFilledPolygon(const std::vector<std::pair<int, int>>& vertices, float r, float g, float b) {
    if (vertices.size() < 3) return;

    int yMin = vertices[0].second;
    int yMax = vertices[0].second;
    for (const auto& pt : vertices) {
        if (pt.second < yMin) yMin = pt.second;
        if (pt.second > yMax) yMax = pt.second;
    }

    glColor3f(r, g, b);
    glBegin(GL_LINES);
    for (int y = yMin; y <= yMax; y++) {
        std::vector<float> intersections;
        for (size_t i = 0; i < vertices.size(); i++) {
            auto p1 = vertices[i];
            auto p2 = vertices[(i + 1) % vertices.size()];
            if (p1.second > p2.second) std::swap(p1, p2);
            if (y >= p1.second && y < p2.second) {
                float x = p1.first + (float)(y - p1.second) * (p2.first - p1.first) / (float)(p2.second - p1.second);
                intersections.push_back(x);
            }
        }
        std::sort(intersections.begin(), intersections.end());
        for (size_t i = 0; i + 1 < intersections.size(); i += 2) {
            glVertex2f(intersections[i], (float)y);
            glVertex2f(intersections[i + 1], (float)y);
        }
    }
    glEnd();
}

void Renderer::drawScanLineHealthBar(float x, float y, float w, float h, float currentHP, float maxHP, bool isFacingLeft) {
    if (maxHP <= 0.0f) maxHP = 100.0f;
    float healthRatio = currentHP / maxHP;
    if (healthRatio < 0.0f) healthRatio = 0.0f;
    if (healthRatio > 1.0f) healthRatio = 1.0f;

    // Background container
    drawRect(x - 2, y - 2, w + 4, h + 4, 0.1f, 0.1f, 0.15f, 0.9f);
    drawRectOutline(x - 2, y - 2, w + 4, h + 4, 0.8f, 0.7f, 0.2f, 2.0f);

    float activeW = w * healthRatio;
    if (activeW > 2.0f) {
        // Build trapezoidal polygon vertices for modern fighting game look
        std::vector<std::pair<int, int>> poly;
        if (!isFacingLeft) {
            poly.push_back({(int)x, (int)y});
            poly.push_back({(int)(x + activeW), (int)y});
            poly.push_back({(int)(x + activeW - 8), (int)(y + h)});
            poly.push_back({(int)x, (int)(y + h)});
        } else {
            poly.push_back({(int)(x + w - activeW), (int)y});
            poly.push_back({(int)(x + w), (int)y});
            poly.push_back({(int)(x + w), (int)(y + h)});
            poly.push_back({(int)(x + w - activeW + 8), (int)(y + h)});
        }

        float r = (healthRatio > 0.5f) ? (1.0f - (healthRatio - 0.5f) * 2.0f) : 1.0f;
        float g = (healthRatio > 0.5f) ? 1.0f : (healthRatio * 2.0f);
        drawScanLineFilledPolygon(poly, r, g, 0.2f);
    }
}

// ----------------------------------------------------------------------------
// Practical No. 04: Transformation Matrices (Spinning 3D Color Cube & Shuriken)
// ----------------------------------------------------------------------------
void Renderer::drawSpinning3DCube(float centerX, float centerY, float size) {
    glPushMatrix();
        glTranslatef(centerX, centerY, 0.0f);
        glRotatef(cubeSpinAngle, 1.0f, 1.0f, 0.5f);
        glScalef(size, size, size);

        glBegin(GL_QUADS);
            // Front (Red)
            glColor3f(0.95f, 0.2f, 0.2f);
            glVertex3f(-0.5f, -0.5f, 0.5f); glVertex3f(0.5f, -0.5f, 0.5f);
            glVertex3f(0.5f, 0.5f, 0.5f);   glVertex3f(-0.5f, 0.5f, 0.5f);
            // Back (Green)
            glColor3f(0.2f, 0.9f, 0.3f);
            glVertex3f(-0.5f, -0.5f, -0.5f); glVertex3f(-0.5f, 0.5f, -0.5f);
            glVertex3f(0.5f, 0.5f, -0.5f);   glVertex3f(0.5f, -0.5f, -0.5f);
            // Top (Blue)
            glColor3f(0.2f, 0.5f, 1.0f);
            glVertex3f(-0.5f, 0.5f, -0.5f); glVertex3f(-0.5f, 0.5f, 0.5f);
            glVertex3f(0.5f, 0.5f, 0.5f);   glVertex3f(0.5f, 0.5f, -0.5f);
            // Bottom (Yellow)
            glColor3f(1.0f, 0.9f, 0.2f);
            glVertex3f(-0.5f, -0.5f, -0.5f); glVertex3f(0.5f, -0.5f, -0.5f);
            glVertex3f(0.5f, -0.5f, 0.5f);   glVertex3f(-0.5f, -0.5f, 0.5f);
            // Right (Magenta)
            glColor3f(0.9f, 0.2f, 0.9f);
            glVertex3f(0.5f, -0.5f, -0.5f); glVertex3f(0.5f, 0.5f, -0.5f);
            glVertex3f(0.5f, 0.5f, 0.5f);   glVertex3f(0.5f, -0.5f, 0.5f);
            // Left (Cyan)
            glColor3f(0.1f, 0.9f, 0.9f);
            glVertex3f(-0.5f, -0.5f, -0.5f); glVertex3f(-0.5f, -0.5f, 0.5f);
            glVertex3f(-0.5f, 0.5f, 0.5f);   glVertex3f(-0.5f, 0.5f, -0.5f);
        glEnd();
    glPopMatrix();
}

void Renderer::drawRotatingShuriken(float centerX, float centerY, float size, float angle) {
    glPushMatrix();
        glTranslatef(centerX, centerY, 0.0f);
        glRotatef(angle, 0.0f, 0.0f, 1.0f);

        // 4 Star Blades
        glColor3f(0.85f, 0.9f, 1.0f);
        glBegin(GL_TRIANGLES);
            // Blade 1 (North)
            glVertex2f(-size * 0.25f, 0.0f); glVertex2f(size * 0.25f, 0.0f); glVertex2f(0.0f, size);
            // Blade 2 (South)
            glVertex2f(-size * 0.25f, 0.0f); glVertex2f(size * 0.25f, 0.0f); glVertex2f(0.0f, -size);
            // Blade 3 (East)
            glVertex2f(0.0f, -size * 0.25f); glVertex2f(0.0f, size * 0.25f); glVertex2f(size, 0.0f);
            // Blade 4 (West)
            glVertex2f(0.0f, -size * 0.25f); glVertex2f(0.0f, size * 0.25f); glVertex2f(-size, 0.0f);
        glEnd();

        // Center hub
        glColor3f(0.2f, 0.2f, 0.3f);
        drawRect(-size * 0.2f, -size * 0.2f, size * 0.4f, size * 0.4f, 0.2f, 0.2f, 0.3f);
    glPopMatrix();
}

// ----------------------------------------------------------------------------
// Practical No. 05: Cohen-Sutherland Line Clipping Algorithm
// ----------------------------------------------------------------------------
void Renderer::drawClippedBresenhamLine(int x1, int y1, int x2, int y2,
                                       int xmin, int ymin, int xmax, int ymax,
                                       float r, float g, float b) {
    double cx0 = x1, cy0 = y1, cx1 = x2, cy1 = y2;
    if (CohenSutherlandClipping::clipLine(cx0, cy0, cx1, cy1, xmin, ymin, xmax, ymax)) {
        drawBresenhamLine((int)std::round(cx0), (int)std::round(cy0),
                          (int)std::round(cx1), (int)std::round(cy1), r, g, b);
    }
}

// ----------------------------------------------------------------------------
// Custom Vector Font for In-game HUD
// ----------------------------------------------------------------------------
void Renderer::drawText(float x, float y, const std::string& text, float r, float g, float b, float scale) {
    glColor3f(r, g, b);
    glLineWidth(scale);

    float curX = x;
    float charW = 6.0f * scale;
    float charH = 10.0f * scale;

    glBegin(GL_LINES);
    for (char rawC : text) {
        char c = (char)std::toupper((unsigned char)rawC);
        switch (c) {
            case 'A':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                break;
            case 'B':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW * 0.8f, y + charH);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX, y); glVertex2f(curX + charW * 0.8f, y);
                glVertex2f(curX + charW * 0.8f, y + charH); glVertex2f(curX + charW, y + charH * 0.75f);
                glVertex2f(curX + charW, y + charH * 0.75f); glVertex2f(curX + charW * 0.8f, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.8f, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.25f);
                glVertex2f(curX + charW, y + charH * 0.25f); glVertex2f(curX + charW * 0.8f, y);
                break;
            case 'C':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case 'D':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW * 0.7f, y + charH);
                glVertex2f(curX + charW * 0.7f, y + charH); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX + charW, y + charH * 0.5f); glVertex2f(curX + charW * 0.7f, y);
                glVertex2f(curX + charW * 0.7f, y); glVertex2f(curX, y);
                break;
            case 'E':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW * 0.7f, y + charH * 0.5f);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case 'F':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW * 0.7f, y + charH * 0.5f);
                break;
            case 'G':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                break;
            case 'H':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                break;
            case 'I':
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.5f, y + charH);
                glVertex2f(curX + charW * 0.2f, y + charH); glVertex2f(curX + charW * 0.8f, y + charH);
                glVertex2f(curX + charW * 0.2f, y); glVertex2f(curX + charW * 0.8f, y);
                break;
            case 'J':
                glVertex2f(curX + charW * 0.8f, y + charH); glVertex2f(curX + charW * 0.8f, y + charH * 0.2f);
                glVertex2f(curX + charW * 0.8f, y + charH * 0.2f); glVertex2f(curX + charW * 0.5f, y);
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.2f, y);
                glVertex2f(curX + charW * 0.2f, y); glVertex2f(curX, y + charH * 0.35f);
                glVertex2f(curX + charW * 0.4f, y + charH); glVertex2f(curX + charW, y + charH);
                break;
            case 'K':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH * 0.45f); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW * 0.35f, y + charH * 0.6f); glVertex2f(curX + charW, y);
                break;
            case 'L':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case 'M':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW * 0.5f, y + charH * 0.4f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.4f); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y);
                break;
            case 'N':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH);
                break;
            case 'O':
            case '0':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX, y);
                break;
            case 'P':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX + charW, y + charH * 0.5f); glVertex2f(curX, y + charH * 0.5f);
                break;
            case 'Q':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX, y);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.3f); glVertex2f(curX + charW * 1.1f, y - charH * 0.1f);
                break;
            case 'R':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX + charW, y + charH * 0.5f); glVertex2f(curX, y + charH * 0.5f);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y);
                break;
            case 'S':
                glVertex2f(curX + charW, y + charH); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX, y + charH * 0.5f);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX + charW, y + charH * 0.5f); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX, y);
                break;
            case 'T':
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.5f, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                break;
            case 'U':
                glVertex2f(curX, y + charH); glVertex2f(curX, y);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH);
                break;
            case 'V':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW * 0.5f, y);
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW, y + charH);
                break;
            case 'W':
                glVertex2f(curX, y + charH); glVertex2f(curX, y);
                glVertex2f(curX, y); glVertex2f(curX + charW * 0.5f, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.5f); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH);
                break;
            case 'X':
                glVertex2f(curX, y); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y); glVertex2f(curX, y + charH);
                break;
            case 'Y':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW * 0.5f, y + charH * 0.5f);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW * 0.5f, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.5f); glVertex2f(curX + charW * 0.5f, y);
                break;
            case 'Z':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX, y);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case '1':
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.5f, y + charH);
                glVertex2f(curX + charW * 0.2f, y + charH * 0.7f); glVertex2f(curX + charW * 0.5f, y + charH);
                break;
            case '2':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y + charH * 0.55f);
                glVertex2f(curX + charW, y + charH * 0.55f); glVertex2f(curX, y + charH * 0.55f);
                glVertex2f(curX, y + charH * 0.55f); glVertex2f(curX, y);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case '3':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW * 0.5f, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.5f); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX, y);
                break;
            case '4':
                glVertex2f(curX + charW * 0.75f, y); glVertex2f(curX + charW * 0.75f, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX, y + charH * 0.4f);
                glVertex2f(curX, y + charH * 0.4f); glVertex2f(curX + charW, y + charH * 0.4f);
                break;
            case '5':
                glVertex2f(curX + charW, y + charH); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX, y + charH * 0.55f);
                glVertex2f(curX, y + charH * 0.55f); glVertex2f(curX + charW, y + charH * 0.55f);
                glVertex2f(curX + charW, y + charH * 0.55f); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX, y);
                break;
            case '6':
                glVertex2f(curX + charW, y + charH); glVertex2f(curX, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX, y);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH * 0.55f);
                glVertex2f(curX + charW, y + charH * 0.55f); glVertex2f(curX, y + charH * 0.55f);
                break;
            case '7':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW * 0.35f, y);
                break;
            case '8':
                glVertex2f(curX, y); glVertex2f(curX, y + charH);
                glVertex2f(curX + charW, y); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                break;
            case '9':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX, y + charH); glVertex2f(curX, y + charH * 0.5f);
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y);
                glVertex2f(curX + charW, y); glVertex2f(curX, y);
                break;
            case '-':
                glVertex2f(curX, y + charH * 0.5f); glVertex2f(curX + charW, y + charH * 0.5f);
                break;
            case ':':
                glVertex2f(curX + charW * 0.5f, y + charH * 0.75f); glVertex2f(curX + charW * 0.5f, y + charH * 0.75f + 1.0f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.25f); glVertex2f(curX + charW * 0.5f, y + charH * 0.25f + 1.0f);
                break;
            case '[':
                glVertex2f(curX + charW * 0.7f, y + charH); glVertex2f(curX + charW * 0.3f, y + charH);
                glVertex2f(curX + charW * 0.3f, y + charH); glVertex2f(curX + charW * 0.3f, y);
                glVertex2f(curX + charW * 0.3f, y); glVertex2f(curX + charW * 0.7f, y);
                break;
            case ']':
                glVertex2f(curX + charW * 0.3f, y + charH); glVertex2f(curX + charW * 0.7f, y + charH);
                glVertex2f(curX + charW * 0.7f, y + charH); glVertex2f(curX + charW * 0.7f, y);
                glVertex2f(curX + charW * 0.7f, y); glVertex2f(curX + charW * 0.3f, y);
                break;
            case '(':
                glVertex2f(curX + charW * 0.7f, y + charH); glVertex2f(curX + charW * 0.3f, y + charH * 0.75f);
                glVertex2f(curX + charW * 0.3f, y + charH * 0.75f); glVertex2f(curX + charW * 0.3f, y + charH * 0.25f);
                glVertex2f(curX + charW * 0.3f, y + charH * 0.25f); glVertex2f(curX + charW * 0.7f, y);
                break;
            case ')':
                glVertex2f(curX + charW * 0.3f, y + charH); glVertex2f(curX + charW * 0.7f, y + charH * 0.75f);
                glVertex2f(curX + charW * 0.7f, y + charH * 0.75f); glVertex2f(curX + charW * 0.7f, y + charH * 0.25f);
                glVertex2f(curX + charW * 0.7f, y + charH * 0.25f); glVertex2f(curX + charW * 0.3f, y);
                break;
            case '/':
                glVertex2f(curX, y); glVertex2f(curX + charW, y + charH);
                break;
            case '\\':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y);
                break;
            case '>':
                glVertex2f(curX + charW * 0.15f, y + charH * 0.85f); glVertex2f(curX + charW * 0.85f, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.85f, y + charH * 0.5f); glVertex2f(curX + charW * 0.15f, y + charH * 0.15f);
                break;
            case '<':
                glVertex2f(curX + charW * 0.85f, y + charH * 0.85f); glVertex2f(curX + charW * 0.15f, y + charH * 0.5f);
                glVertex2f(curX + charW * 0.15f, y + charH * 0.5f); glVertex2f(curX + charW * 0.85f, y + charH * 0.15f);
                break;
            case '=':
                glVertex2f(curX, y + charH * 0.6f); glVertex2f(curX + charW, y + charH * 0.6f);
                glVertex2f(curX, y + charH * 0.4f); glVertex2f(curX + charW, y + charH * 0.4f);
                break;
            case '!':
                glVertex2f(curX + charW * 0.5f, y + charH); glVertex2f(curX + charW * 0.5f, y + charH * 0.3f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.1f); glVertex2f(curX + charW * 0.5f, y);
                break;
            case '*':
                glVertex2f(curX + charW * 0.2f, y + charH * 0.2f); glVertex2f(curX + charW * 0.8f, y + charH * 0.8f);
                glVertex2f(curX + charW * 0.2f, y + charH * 0.8f); glVertex2f(curX + charW * 0.8f, y + charH * 0.2f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.1f); glVertex2f(curX + charW * 0.5f, y + charH * 0.9f);
                break;
            case '+':
                glVertex2f(curX + charW * 0.5f, y + charH * 0.2f); glVertex2f(curX + charW * 0.5f, y + charH * 0.8f);
                glVertex2f(curX + charW * 0.15f, y + charH * 0.5f); glVertex2f(curX + charW * 0.85f, y + charH * 0.5f);
                break;
            case '&':
                glVertex2f(curX + charW * 0.8f, y); glVertex2f(curX + charW * 0.2f, y + charH * 0.6f);
                glVertex2f(curX + charW * 0.2f, y + charH * 0.6f); glVertex2f(curX + charW * 0.5f, y + charH);
                glVertex2f(curX + charW * 0.5f, y + charH); glVertex2f(curX + charW * 0.8f, y + charH * 0.6f);
                glVertex2f(curX + charW * 0.8f, y + charH * 0.6f); glVertex2f(curX, y);
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case '|':
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.5f, y + charH);
                break;
            case '%':
                glVertex2f(curX, y); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW * 0.25f, y + charH * 0.75f); glVertex2f(curX + charW * 0.25f, y + charH * 0.75f + 1.0f);
                glVertex2f(curX + charW * 0.75f, y + charH * 0.25f); glVertex2f(curX + charW * 0.75f, y + charH * 0.25f + 1.0f);
                break;
            case ',':
                glVertex2f(curX + charW * 0.5f, y + charH * 0.15f); glVertex2f(curX + charW * 0.5f, y);
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.25f, y - charH * 0.15f);
                break;
            case '.':
                glVertex2f(curX + charW * 0.35f, y); glVertex2f(curX + charW * 0.65f, y);
                glVertex2f(curX + charW * 0.5f, y); glVertex2f(curX + charW * 0.5f, y + charH * 0.15f);
                break;
            case '\'':
                glVertex2f(curX + charW * 0.5f, y + charH); glVertex2f(curX + charW * 0.5f, y + charH * 0.65f);
                break;
            case '\"':
                glVertex2f(curX + charW * 0.3f, y + charH); glVertex2f(curX + charW * 0.3f, y + charH * 0.65f);
                glVertex2f(curX + charW * 0.7f, y + charH); glVertex2f(curX + charW * 0.7f, y + charH * 0.65f);
                break;
            case '?':
                glVertex2f(curX, y + charH); glVertex2f(curX + charW, y + charH);
                glVertex2f(curX + charW, y + charH); glVertex2f(curX + charW, y + charH * 0.55f);
                glVertex2f(curX + charW, y + charH * 0.55f); glVertex2f(curX + charW * 0.5f, y + charH * 0.55f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.55f); glVertex2f(curX + charW * 0.5f, y + charH * 0.3f);
                glVertex2f(curX + charW * 0.5f, y + charH * 0.12f); glVertex2f(curX + charW * 0.5f, y);
                break;
            case '_':
                glVertex2f(curX, y); glVertex2f(curX + charW, y);
                break;
            case ' ':
                break;
            default:
                // Unknown character: do not draw slashes
                break;
        }
        curX += charW + (3.0f * scale);
    }
    glEnd();
}

// ============================================================================
// PRACTICAL NO. 06 / TASK 3 (CO6): TEXTURE MAPPING, LIGHTING & COLOR EFFECTS
// ============================================================================

void Renderer::beginLighting() {
    lightingEngine.beginSceneLighting();
}

void Renderer::endLighting() {
    lightingEngine.endSceneLighting();
}

void Renderer::setCombatLight(float x, float y, float r, float g, float b, float intensity) {
    lightingEngine.triggerCombatLight(x, y, r, g, b, intensity);
}

void Renderer::setMaterial(float specR, float specG, float specB, float shininess, float emitR, float emitG, float emitB) {
    lightingEngine.setMaterial(specR, specG, specB, shininess, emitR, emitG, emitB);
}

void Renderer::resetMaterial() {
    lightingEngine.resetMaterial();
}

void Renderer::applyFighterMaterial(int characterType, float healthPercent, bool isAttacking, bool isFlinching) {
    CG_CO6::FighterColorEffects::applyFighterMaterial(lightingEngine, characterType, healthPercent, isAttacking, isFlinching);
}

void Renderer::drawTexturedRect(float x, float y, float w, float h, CG_CO6::TextureType texType,
                                float repeatS, float repeatT, float r, float g, float b, float a) 
{
    if (!texturesEnabled) {
        drawRect(x, y, w, h, r, g, b, a);
        return;
    }

    glEnable(GL_TEXTURE_2D);
    textureManager.bind(texType);
    glColor4f(r, g, b, a);
    glNormal3f(0.0f, 0.0f, 1.0f);

    glBegin(GL_QUADS);
        glTexCoord2f(0.0f,    0.0f);    glVertex2f(x,     y);
        glTexCoord2f(repeatS, 0.0f);    glVertex2f(x + w, y);
        glTexCoord2f(repeatS, repeatT); glVertex2f(x + w, y + h);
        glTexCoord2f(0.0f,    repeatT); glVertex2f(x,     y + h);
    glEnd();

    textureManager.unbind();
    glDisable(GL_TEXTURE_2D);
}

void Renderer::drawTexturedFloor(float x, float y, float w, float h) {
    if (!texturesEnabled) {
        drawRect(x, y, w, h, 0.10f, 0.12f, 0.18f);
        drawRect(x, y + h - 4.0f, w, 4.0f, 0.2f, 0.8f, 0.95f);
        return;
    }

    // High-tech periodic tiled metallic grid
    float repeatS = w / 64.0f; // 64-pixel periodic repeat
    float repeatT = h / 64.0f;
    drawTexturedRect(x, y, w, h, CG_CO6::TEX_FLOOR_GRID, repeatS, repeatT, 0.85f, 0.92f, 1.0f, 1.0f);

    // Glowing neon top border edge
    drawRect(x, y + h - 5.0f, w, 5.0f, 0.25f, 0.9f, 1.0f);
    drawRect(x, y + h - 2.0f, w, 2.0f, 0.95f, 0.98f, 1.0f);
}

void Renderer::drawTexturedPillar(float x, float y, float w, float h) {
    if (!texturesEnabled) {
        drawRect(x, y, w, h, 0.08f, 0.1f, 0.16f, 0.85f);
        drawRectOutline(x, y, w, h, 0.2f, 0.6f, 0.9f, 2.0f);
        return;
    }

    float repeatS = 1.0f;
    float repeatT = h / 128.0f;
    drawTexturedRect(x, y, w, h, CG_CO6::TEX_CYBER_PILLAR, repeatS, repeatT, 0.9f, 0.95f, 1.0f, 0.95f);
    drawRectOutline(x, y, w, h, 0.3f, 0.75f, 1.0f, 2.0f);
}

void Renderer::drawTexturedPlatform(float x, float y, float w, float h) {
    if (!texturesEnabled) {
        drawRect(x, y, w, h, 0.14f, 0.16f, 0.24f);
        drawRectOutline(x, y, w, h, 0.95f, 0.75f, 0.2f, 2.0f);
        return;
    }

    float repeatS = w / 128.0f;
    float repeatT = 1.0f;
    drawTexturedRect(x, y, w, h, CG_CO6::TEX_ENERGY_PLATFORM, repeatS, repeatT, 1.0f, 0.95f, 0.85f, 1.0f);
    drawRectOutline(x, y, w, h, 1.0f, 0.85f, 0.25f, 2.0f);
}

void Renderer::drawTexturedCube(float centerX, float centerY, float size) {
    if (!texturesEnabled) {
        drawSpinning3DCube(centerX, centerY, size);
        return;
    }

    glPushMatrix();
        glTranslatef(centerX, centerY, 0.0f);
        glRotatef(cubeSpinAngle, 1.0f, 1.0f, 0.5f);
        glScalef(size, size, size);

        glEnable(GL_TEXTURE_2D);
        textureManager.bind(CG_CO6::TEX_CIRCUIT_CUBE);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glBegin(GL_QUADS);
            // Front Face (Normal pointing +Z)
            glNormal3f(0.0f, 0.0f, 1.0f);
            glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f, -0.5f,  0.5f);
            glTexCoord2f(1.0f, 0.0f); glVertex3f( 0.5f, -0.5f,  0.5f);
            glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f,  0.5f);
            glTexCoord2f(0.0f, 1.0f); glVertex3f(-0.5f,  0.5f,  0.5f);

            // Back Face (Normal pointing -Z)
            glNormal3f(0.0f, 0.0f, -1.0f);
            glTexCoord2f(1.0f, 0.0f); glVertex3f(-0.5f, -0.5f, -0.5f);
            glTexCoord2f(1.0f, 1.0f); glVertex3f(-0.5f,  0.5f, -0.5f);
            glTexCoord2f(0.0f, 1.0f); glVertex3f( 0.5f,  0.5f, -0.5f);
            glTexCoord2f(0.0f, 0.0f); glVertex3f( 0.5f, -0.5f, -0.5f);

            // Top Face (Normal pointing +Y)
            glNormal3f(0.0f, 1.0f, 0.0f);
            glTexCoord2f(0.0f, 1.0f); glVertex3f(-0.5f,  0.5f, -0.5f);
            glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f,  0.5f,  0.5f);
            glTexCoord2f(1.0f, 0.0f); glVertex3f( 0.5f,  0.5f,  0.5f);
            glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f, -0.5f);

            // Bottom Face (Normal pointing -Y)
            glNormal3f(0.0f, -1.0f, 0.0f);
            glTexCoord2f(1.0f, 1.0f); glVertex3f(-0.5f, -0.5f, -0.5f);
            glTexCoord2f(0.0f, 1.0f); glVertex3f( 0.5f, -0.5f, -0.5f);
            glTexCoord2f(0.0f, 0.0f); glVertex3f( 0.5f, -0.5f,  0.5f);
            glTexCoord2f(1.0f, 0.0f); glVertex3f(-0.5f, -0.5f,  0.5f);

            // Right Face (Normal pointing +X)
            glNormal3f(1.0f, 0.0f, 0.0f);
            glTexCoord2f(1.0f, 0.0f); glVertex3f( 0.5f, -0.5f, -0.5f);
            glTexCoord2f(1.0f, 1.0f); glVertex3f( 0.5f,  0.5f, -0.5f);
            glTexCoord2f(0.0f, 1.0f); glVertex3f( 0.5f,  0.5f,  0.5f);
            glTexCoord2f(0.0f, 0.0f); glVertex3f( 0.5f, -0.5f,  0.5f);

            // Left Face (Normal pointing -X)
            glNormal3f(-1.0f, 0.0f, 0.0f);
            glTexCoord2f(0.0f, 0.0f); glVertex3f(-0.5f, -0.5f, -0.5f);
            glTexCoord2f(1.0f, 0.0f); glVertex3f(-0.5f, -0.5f,  0.5f);
            glTexCoord2f(1.0f, 1.0f); glVertex3f(-0.5f,  0.5f,  0.5f);
            glTexCoord2f(0.0f, 1.0f); glVertex3f(-0.5f,  0.5f, -0.5f);
        glEnd();

        textureManager.unbind();
        glDisable(GL_TEXTURE_2D);
    glPopMatrix();
}

void Renderer::drawHexagonalShield(float centerX, float centerY, float radius, float pulseAlpha, float r, float g, float b) {
    if (!colorEffectsEnabled) return;

    glPushMatrix();
        glTranslatef(centerX, centerY, 0.0f);

        if (texturesEnabled) {
            glEnable(GL_TEXTURE_2D);
            textureManager.bind(CG_CO6::TEX_HEX_SHIELD);
            glColor4f(r, g, b, pulseAlpha);

            float sz = radius * 1.3f;
            glBegin(GL_QUADS);
                glTexCoord2f(0.0f, 0.0f); glVertex2f(-sz, -sz);
                glTexCoord2f(1.0f, 0.0f); glVertex2f( sz, -sz);
                glTexCoord2f(1.0f, 1.0f); glVertex2f( sz,  sz);
                glTexCoord2f(0.0f, 1.0f); glVertex2f(-sz,  sz);
            glEnd();

            textureManager.unbind();
            glDisable(GL_TEXTURE_2D);
        } else {
            // Untextured geometric hexagon fallback
            glColor4f(r, g, b, pulseAlpha * 0.45f);
            glBegin(GL_POLYGON);
            for (int i = 0; i < 6; i++) {
                float angle = i * 3.14159265f / 3.0f;
                glVertex2f(cosf(angle) * radius, sinf(angle) * radius);
            }
            glEnd();
        }

        // Glowing outer outline
        glColor4f(r * 1.2f, g * 1.2f, b * 1.2f, pulseAlpha * 0.9f);
        glLineWidth(2.5f);
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < 6; i++) {
            float angle = i * 3.14159265f / 3.0f;
            glVertex2f(cosf(angle) * radius, sinf(angle) * radius);
        }
        glEnd();
        glLineWidth(1.0f);
    glPopMatrix();
}

void Renderer::toggleLighting() {
    lightingEngine.cycleMode();
}

void Renderer::toggleTextures() {
    texturesEnabled = !texturesEnabled;
}

void Renderer::toggleColorEffects() {
    colorEffectsEnabled = !colorEffectsEnabled;
}

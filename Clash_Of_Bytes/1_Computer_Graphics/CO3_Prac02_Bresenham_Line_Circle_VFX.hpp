#ifndef CO3_PRAC02_BRESENHAM_LINE_CIRCLE_VFX_HPP
#define CO3_PRAC02_BRESENHAM_LINE_CIRCLE_VFX_HPP

#include <cmath>
#include <GL/gl.h>

/*
 * Subject: Computer Graphics
 * Course Outcome: CO3 | Practical 02
 * Topic: Bresenham's Line & 8-Way Symmetric Circle Drawing Algorithm
 * In-Game Application: Dynamic hitsparks, slash rays, and circular shield barriers
 * operating entirely on integer decision parameters without trigonometric functions!
 */
class BresenhamVFX {
public:
    // Integer-based Bresenham Line Drawing
    static void drawLine(int x1, int y1, int x2, int y2, float r, float g, float b) {
        int dx = std::abs(x2 - x1);
        int dy = std::abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        glColor3f(r, g, b);
        glBegin(GL_POINTS);
        while (true) {
            glVertex2i(x1, y1);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
        glEnd();
    }

    // Integer-based 8-Way Symmetric Bresenham Circle Drawing
    static void drawCircle(int xc, int yc, int radius, float r, float g, float b) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius; // Initial decision parameter

        glColor3f(r, g, b);
        glBegin(GL_POINTS);
        while (y >= x) {
            // Plot all 8 octants
            glVertex2i(xc + x, yc + y);
            glVertex2i(xc - x, yc + y);
            glVertex2i(xc + x, yc - y);
            glVertex2i(xc - x, yc - y);
            glVertex2i(xc + y, yc + x);
            glVertex2i(xc - y, yc + x);
            glVertex2i(xc + y, yc - x);
            glVertex2i(xc - y, yc - x);

            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            x++;
        }
        glEnd();
    }
};

#endif // CO3_PRAC02_BRESENHAM_LINE_CIRCLE_VFX_HPP

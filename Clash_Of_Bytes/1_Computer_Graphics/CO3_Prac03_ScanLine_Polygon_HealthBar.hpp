#ifndef CO3_PRAC03_SCANLINE_POLYGON_HEALTHBAR_HPP
#define CO3_PRAC03_SCANLINE_POLYGON_HEALTHBAR_HPP

#include <vector>
#include <algorithm>
#include <GL/gl.h>

/*
 * Subject: Computer Graphics
 * Course Outcome: CO3 | Practical 03
 * Topic: Scan-Line Polygon Filling Algorithm with Parity Checking
 * In-Game Application: Rendering dynamic player health bar gauges line by line
 * with edge-intersection sorting and span filling!
 */
class ScanLinePolygonFill {
public:
    static void fillPolygon(const std::vector<std::pair<int, int>>& vertices, float r, float g, float b) {
        if (vertices.size() < 3) return;

        int minY = vertices[0].second;
        int maxY = vertices[0].second;
        for (const auto& v : vertices) {
            if (v.second < minY) minY = v.second;
            if (v.second > maxY) maxY = v.second;
        }

        glColor3f(r, g, b);
        glBegin(GL_LINES);

        // Cast scanline from minY to maxY
        for (int y = minY; y <= maxY; y++) {
            std::vector<int> intersections;
            size_t n = vertices.size();

            for (size_t i = 0; i < n; i++) {
                size_t j = (i + 1) % n;
                int y1 = vertices[i].second;
                int y2 = vertices[j].second;
                int x1 = vertices[i].first;
                int x2 = vertices[j].first;

                if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
                    // Compute intersection point X
                    float x = x1 + (float)(y - y1) / (float)(y2 - y1) * (x2 - x1);
                    intersections.push_back((int)x);
                }
            }

            // Sort intersection points
            std::sort(intersections.begin(), intersections.end());

            // Fill pixel spans between pairs of intersections (Parity checking)
            for (size_t k = 0; k + 1 < intersections.size(); k += 2) {
                glVertex2i(intersections[k], y);
                glVertex2i(intersections[k + 1], y);
            }
        }

        glEnd();
    }
};

#endif // CO3_PRAC03_SCANLINE_POLYGON_HEALTHBAR_HPP

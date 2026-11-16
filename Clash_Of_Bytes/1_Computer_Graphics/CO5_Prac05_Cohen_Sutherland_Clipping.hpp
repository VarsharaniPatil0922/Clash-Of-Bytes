#ifndef CO5_PRAC05_COHEN_SUTHERLAND_CLIPPING_HPP
#define CO5_PRAC05_COHEN_SUTHERLAND_CLIPPING_HPP

#include <iostream>
#include <iomanip>

/*
 * Subject: Computer Graphics
 * Course Outcome: CO5 | Practical 05
 * Topic: Cohen-Sutherland 2D Line Clipping Algorithm
 * Purpose: Clips a 2D line segment against a rectangular viewport using 4-bit region outcodes.
 */

class CohenSutherlandClipping {
public:
    // 4-bit region outcodes
    static const int INSIDE = 0; // 0000
    static const int LEFT   = 1; // 0001
    static const int RIGHT  = 2; // 0010
    static const int BOTTOM = 4; // 0100
    static const int TOP    = 8; // 1000

    // Compute 4-bit region code for point (x, y) relative to clipping window [xmin, ymin, xmax, ymax]
    static int computeOutCode(double x, double y, double xmin, double ymin, double xmax, double ymax) {
        int code = INSIDE;

        if (x < xmin) {
            code |= LEFT;
        } else if (x > xmax) {
            code |= RIGHT;
        }

        if (y < ymin) {
            code |= BOTTOM;
        } else if (y > ymax) {
            code |= TOP;
        }

        return code;
    }

    // Cohen-Sutherland line clipping algorithm
    // Clips line segment from (x0, y0) to (x1, y1) against clipping window
    // Returns true if any portion of the line is visible (accepted), false if completely outside (rejected)
    static bool clipLine(double& x0, double& y0, double& x1, double& y1,
                         double xmin, double ymin, double xmax, double ymax) {
        int code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
        int code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
        bool accept = false;

        while (true) {
            if ((code0 | code1) == 0) {
                // Trivial accept: both endpoints are inside viewport
                accept = true;
                break;
            } else if ((code0 & code1) != 0) {
                // Trivial reject: both endpoints share an outside zone
                break;
            } else {
                // Line crosses the clipping boundary; calculate intersection point
                double x = 0.0, y = 0.0;
                // Pick an endpoint that is outside the clipping window
                int codeOut = code0 ? code0 : code1;

                if (codeOut & TOP) {
                    // Point is above the clipping window
                    x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
                    y = ymax;
                } else if (codeOut & BOTTOM) {
                    // Point is below the clipping window
                    x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
                    y = ymin;
                } else if (codeOut & RIGHT) {
                    // Point is to the right of clipping window
                    y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
                    x = xmax;
                } else if (codeOut & LEFT) {
                    // Point is to the left of clipping window
                    y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
                    x = xmin;
                }

                // Update the outside endpoint coordinates and recompute its outcode
                if (codeOut == code0) {
                    x0 = x;
                    y0 = y;
                    code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
                } else {
                    x1 = x;
                    y1 = y;
                    code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
                }
            }
        }

        return accept;
    }

    // Standalone unit test verifying algorithm correctness with a line crossing the viewport
    static void testClipping() {
        std::cout << "\n=======================================================\n";
        std::cout << "  COMPUTER GRAPHICS - PRACTICAL 05: COHEN-SUTHERLAND\n";
        std::cout << "  Line Clipping Algorithm Test Verification\n";
        std::cout << "=======================================================\n";

        double xmin = 100.0, ymin = 100.0, xmax = 400.0, ymax = 300.0;
        std::cout << "Clipping Viewport: [" << xmin << ", " << ymin << "] to [" << xmax << ", " << ymax << "]\n\n";

        // Test 1: Line crossing viewport
        double x0 = 50.0, y0 = 50.0, x1 = 450.0, y1 = 350.0;
        std::cout << "Test 1 - Line Crossing Viewport:\n";
        std::cout << "  Original Endpoints: (" << x0 << ", " << y0 << ") -> (" << x1 << ", " << y1 << ")\n";

        bool visible = clipLine(x0, y0, x1, y1, xmin, ymin, xmax, ymax);
        std::cout << "  Clipping Result   : " << (visible ? "ACCEPTED (Visible)" : "REJECTED (Invisible)") << "\n";
        if (visible) {
            std::cout << "  Clipped Endpoints : (" << std::fixed << std::setprecision(1) << x0 
                      << ", " << y0 << ") -> (" << x1 << ", " << y1 << ")\n";
        }

        // Test 2: Completely outside line
        double ox0 = 10.0, oy0 = 10.0, ox1 = 50.0, oy1 = 80.0;
        std::cout << "\nTest 2 - Line Completely Outside (Trivial Reject):\n";
        std::cout << "  Original Endpoints: (" << ox0 << ", " << oy0 << ") -> (" << ox1 << ", " << oy1 << ")\n";
        bool outVisible = clipLine(ox0, oy0, ox1, oy1, xmin, ymin, xmax, ymax);
        std::cout << "  Clipping Result   : " << (outVisible ? "ACCEPTED" : "TRIVIAL REJECT") << "\n";

        std::cout << "=======================================================\n\n";
    }
};

#endif // CO5_PRAC05_COHEN_SUTHERLAND_CLIPPING_HPP

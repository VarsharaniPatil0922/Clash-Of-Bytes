#ifndef CO2_PRAC04_TRANSFORMATION_MATRICES_3DCUBE_HPP
#define CO2_PRAC04_TRANSFORMATION_MATRICES_3DCUBE_HPP

#include <GL/gl.h>

/*
 * Subject: Computer Graphics
 * Course Outcome: CO2 | Practical 04
 * Topic: OpenGL Transformation Matrices (glPushMatrix, glRotatef, glTranslatef, glScalef)
 * In-Game Application: Continuous 3D spinning color cube on the arena stage floor
 * and dynamic rotating ninja shuriken weapon projectiles!
 */
class TransformationMatrix3DCube {
public:
    static void drawSpinningCube(float centerX, float centerY, float size, float angle) {
        glPushMatrix();
        glTranslatef(centerX, centerY, 0.0f);
        glRotatef(angle, 1.0f, 1.0f, 0.5f);
        glScalef(size, size, size);

        glBegin(GL_QUADS);
        // Front Face (Red)
        glColor3f(0.9f, 0.2f, 0.2f);
        glVertex3f(-0.5f, -0.5f,  0.5f);
        glVertex3f( 0.5f, -0.5f,  0.5f);
        glVertex3f( 0.5f,  0.5f,  0.5f);
        glVertex3f(-0.5f,  0.5f,  0.5f);

        // Back Face (Green)
        glColor3f(0.2f, 0.9f, 0.3f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f,  0.5f, -0.5f);
        glVertex3f( 0.5f,  0.5f, -0.5f);
        glVertex3f( 0.5f, -0.5f, -0.5f);

        // Top Face (Blue)
        glColor3f(0.2f, 0.4f, 0.95f);
        glVertex3f(-0.5f,  0.5f, -0.5f);
        glVertex3f(-0.5f,  0.5f,  0.5f);
        glVertex3f( 0.5f,  0.5f,  0.5f);
        glVertex3f( 0.5f,  0.5f, -0.5f);

        // Bottom Face (Yellow)
        glColor3f(0.95f, 0.9f, 0.2f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f( 0.5f, -0.5f, -0.5f);
        glVertex3f( 0.5f, -0.5f,  0.5f);
        glVertex3f(-0.5f, -0.5f,  0.5f);

        // Right Face (Magenta)
        glColor3f(0.9f, 0.2f, 0.9f);
        glVertex3f( 0.5f, -0.5f, -0.5f);
        glVertex3f( 0.5f,  0.5f, -0.5f);
        glVertex3f( 0.5f,  0.5f,  0.5f);
        glVertex3f( 0.5f, -0.5f,  0.5f);

        // Left Face (Cyan)
        glColor3f(0.2f, 0.9f, 0.9f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f,  0.5f);
        glVertex3f(-0.5f,  0.5f,  0.5f);
        glVertex3f(-0.5f,  0.5f, -0.5f);
        glEnd();

        glPopMatrix();
    }
};

#endif // CO2_PRAC04_TRANSFORMATION_MATRICES_3DCUBE_HPP

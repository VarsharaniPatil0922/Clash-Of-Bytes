#ifndef VECTOR2D_HPP
#define VECTOR2D_HPP

#include <cmath>

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Topic: Mathematical 2D Vector Physics Utility
 */
struct Vector2D {
    float x;
    float y;

    Vector2D() : x(0.0f), y(0.0f) {}
    Vector2D(float xVal, float yVal) : x(xVal), y(yVal) {}

    Vector2D operator+(const Vector2D& other) const { return Vector2D(x + other.x, y + other.y); }
    Vector2D operator-(const Vector2D& other) const { return Vector2D(x - other.x, y - other.y); }
    Vector2D operator*(float scalar) const { return Vector2D(x * scalar, y * scalar); }
    Vector2D& operator+=(const Vector2D& other) { x += other.x; y += other.y; return *this; }
    Vector2D& operator-=(const Vector2D& other) { x -= other.x; y -= other.y; return *this; }

    float length() const { return std::sqrt(x * x + y * y); }
};

#endif // VECTOR2D_HPP

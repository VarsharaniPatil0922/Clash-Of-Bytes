#ifndef WINDOW_HPP
#define WINDOW_HPP

#include <windows.h>
#include <string>

class Window {
private:
    HWND hwnd;
    HDC hdc;
    HGLRC hglrc;
    int width;
    int height;
    bool shouldClose;

public:
    Window(const std::string& title, int w, int h);
    ~Window();

    bool processMessages();
    void swapBuffers();
    bool isClosing() const { return shouldClose; }

    HWND getHWND() const { return hwnd; }
    HDC getHDC() const { return hdc; }
    int getWidth() const { return width; }
    int getHeight() const { return height; }

private:
    static LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
    void initOpenGL();
};

#endif // WINDOW_HPP

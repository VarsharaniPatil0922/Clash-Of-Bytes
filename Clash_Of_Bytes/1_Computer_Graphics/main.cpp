/*
 * Project: Clash of Bytes - Practicals 1 to 4 Integration
 * Subjects: Computer Graphics, DSA, COA (Real Assembly) & OOP Java
 * Topic: Application WinMain Entry Point, 60 FPS QPC Game Loop & Window Management
 */
#include "OpenGL_Window.hpp"
#include "CO1_Prac01_OpenGL_Gouraud_Renderer.hpp"
#include "InputManager.hpp"
#include "GameMatch.hpp"

#ifndef GL_BGR_EXT
#define GL_BGR_EXT 0x80E0
#endif

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Dynamically resolve SetProcessDPIAware to remain compatible across all Windows SDKs/compilers
    typedef BOOL (WINAPI *SetProcessDPIAwareFunc)();
    HMODULE hUser32 = GetModuleHandleA("user32.dll");
    if (hUser32) {
        SetProcessDPIAwareFunc setDPIAware = (SetProcessDPIAwareFunc)GetProcAddress(hUser32, "SetProcessDPIAware");
        if (setDPIAware) setDPIAware();
    }

    try {
        const int WINDOW_WIDTH = 1280;
        const int WINDOW_HEIGHT = 720;

        Window window("CLASH OF BYTES [Milestone 1 - Practicals 1 to 4 Edition]", WINDOW_WIDTH, WINDOW_HEIGHT);
        Renderer renderer;
        renderer.init(WINDOW_WIDTH, WINDOW_HEIGHT);

        InputManager inputManager;

        // Parse command line arguments for player character selection & game mode
        int p1Type = 0;
        int p2Type = 1;
        GameMode initialMode = GameMode::TUTORIAL;

        std::string fullCmd = GetCommandLineA();
        if (lpCmdLine && strlen(lpCmdLine) > 0) {
            fullCmd += " ";
            fullCmd += lpCmdLine;
        }

        // Look for --p1 or -p1
        size_t p1Pos = fullCmd.find("--p1");
        if (p1Pos == std::string::npos) p1Pos = fullCmd.find("-p1");
        if (p1Pos != std::string::npos) {
            size_t numStart = fullCmd.find_first_of("0123456789", p1Pos);
            if (numStart != std::string::npos) {
                p1Type = std::atoi(fullCmd.c_str() + numStart);
            }
        }

        // Look for --p2 or -p2
        size_t p2Pos = fullCmd.find("--p2");
        if (p2Pos == std::string::npos) p2Pos = fullCmd.find("-p2");
        if (p2Pos != std::string::npos) {
            size_t numStart = fullCmd.find_first_of("0123456789", p2Pos);
            if (numStart != std::string::npos) {
                p2Type = std::atoi(fullCmd.c_str() + numStart);
            }
        }

        // Look for --mode
        size_t modePos = fullCmd.find("--mode");
        if (modePos == std::string::npos) modePos = fullCmd.find("-mode");
        if (modePos != std::string::npos) {
            std::string modeSub = fullCmd.substr(modePos);
            for (char &c : modeSub) c = (char)std::tolower((unsigned char)c);
            if (modeSub.find("fight") != std::string::npos || modeSub.find("real") != std::string::npos) {
                initialMode = GameMode::REAL_FIGHT;
            } else if (modeSub.find("tutorial") != std::string::npos) {
                initialMode = GameMode::TUTORIAL;
            }
        }

        // Look for --capture or -capture for automated screenshot verification
        std::string capturePath = "";
        int captureAfterFrames = -1;
        size_t capPos = fullCmd.find("--capture");
        if (capPos == std::string::npos) capPos = fullCmd.find("-capture");
        if (capPos != std::string::npos) {
            capturePath = "screenshot_cg_task3.bmp";
            captureAfterFrames = 45; // Capture after 45 frames of active rendering
        }

        GameMatch match(p1Type, p2Type, initialMode);
        if (fullCmd.find("--syllabus") != std::string::npos || fullCmd.find("-syllabus") != std::string::npos) {
            match.setShowSyllabusMonitor(true);
        }

        // High-Precision QPC Delta Timing
        LARGE_INTEGER frequency, frameStart, frameEnd;
        QueryPerformanceFrequency(&frequency);
        QueryPerformanceCounter(&frameStart);

        const double TARGET_FRAME_TIME = 1.0 / 60.0;
        int frameCount = 0;

        while (!window.isClosing()) {
            if (!window.processMessages()) break;

            QueryPerformanceCounter(&frameEnd);
            double frameTime = static_cast<double>(frameEnd.QuadPart - frameStart.QuadPart) / frequency.QuadPart;
            frameStart = frameEnd;

            // Clamping delta time
            if (frameTime <= 0.0001 || frameTime > 0.1) {
                frameTime = TARGET_FRAME_TIME;
            }
            float dt = static_cast<float>(frameTime);

            inputManager.update();
            renderer.update(dt);
            match.update(dt, inputManager, renderer);

            renderer.clear(0.04f, 0.04f, 0.07f);
            renderer.beginFrame();
            match.render(renderer);

            // Screenshot Capture (F9 or --capture flag)
            static bool f9WasDown = false;
            bool f9Down = (GetAsyncKeyState(VK_F9) & 0x8000) != 0;
            if ((f9Down && !f9WasDown) || (captureAfterFrames > 0 && frameCount >= captureAfterFrames)) {
                std::string outBmp = capturePath.empty() ? "screenshot_gameplay.bmp" : capturePath;
                std::vector<unsigned char> pixels(WINDOW_WIDTH * WINDOW_HEIGHT * 3);
                glPixelStorei(GL_PACK_ALIGNMENT, 1);
                glReadPixels(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, GL_BGR_EXT, GL_UNSIGNED_BYTE, pixels.data());

                BITMAPFILEHEADER bfh;
                BITMAPINFOHEADER bih;
                memset(&bfh, 0, sizeof(bfh));
                memset(&bih, 0, sizeof(bih));

                bfh.bfType = 0x4D42; // "BM"
                bfh.bfSize = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER) + WINDOW_WIDTH * WINDOW_HEIGHT * 3;
                bfh.bfOffBits = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);

                bih.biSize = sizeof(BITMAPINFOHEADER);
                bih.biWidth = WINDOW_WIDTH;
                bih.biHeight = WINDOW_HEIGHT;
                bih.biPlanes = 1;
                bih.biBitCount = 24;
                bih.biCompression = BI_RGB;
                bih.biSizeImage = WINDOW_WIDTH * WINDOW_HEIGHT * 3;

                FILE* f = fopen(outBmp.c_str(), "wb");
                if (f) {
                    fwrite(&bfh, sizeof(bfh), 1, f);
                    fwrite(&bih, sizeof(bih), 1, f);
                    fwrite(pixels.data(), 1, WINDOW_WIDTH * WINDOW_HEIGHT * 3, f);
                    fclose(f);
                }

                if (captureAfterFrames > 0) {
                    renderer.endFrame(window.getHDC());
                    break; // Automated capture complete
                }
            }
            f9WasDown = f9Down;
            frameCount++;

            renderer.endFrame(window.getHDC());

            // 60 FPS frame throttle
            QueryPerformanceCounter(&frameEnd);
            double workTime = static_cast<double>(frameEnd.QuadPart - frameStart.QuadPart) / frequency.QuadPart;
            if (workTime < TARGET_FRAME_TIME) {
                DWORD sleepMs = static_cast<DWORD>((TARGET_FRAME_TIME - workTime) * 1000.0);
                if (sleepMs > 0) Sleep(sleepMs);
            }
        }
    } catch (const std::exception& ex) {
        MessageBoxA(NULL, ex.what(), "Game Runtime Error", MB_ICONERROR | MB_OK);
        return 1;
    }

    return 0;
}

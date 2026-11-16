#ifndef INPUTMANAGER_HPP
#define INPUTMANAGER_HPP

#include <windows.h>
#if defined(__has_include)
    #if __has_include(<xinput.h>)
        #include <xinput.h>
        #define HAS_XINPUT_HEADER 1
    #endif
#endif

#ifndef HAS_XINPUT_HEADER
typedef struct _XINPUT_GAMEPAD {
    WORD  wButtons;
    BYTE  bLeftTrigger;
    BYTE  bRightTrigger;
    SHORT sThumbLX;
    SHORT sThumbLY;
    SHORT sThumbRX;
    SHORT sThumbRY;
} XINPUT_GAMEPAD, *PXINPUT_GAMEPAD;

typedef struct _XINPUT_STATE {
    DWORD          dwPacketNumber;
    XINPUT_GAMEPAD Gamepad;
} XINPUT_STATE, *PXINPUT_STATE;

#define XINPUT_GAMEPAD_DPAD_UP          0x0001
#define XINPUT_GAMEPAD_DPAD_DOWN        0x0002
#define XINPUT_GAMEPAD_DPAD_LEFT        0x0004
#define XINPUT_GAMEPAD_DPAD_RIGHT       0x0008
#define XINPUT_GAMEPAD_START            0x0010
#define XINPUT_GAMEPAD_BACK             0x0020
#define XINPUT_GAMEPAD_LEFT_THUMB       0x0040
#define XINPUT_GAMEPAD_RIGHT_THUMB      0x0080
#define XINPUT_GAMEPAD_A                0x1000
#define XINPUT_GAMEPAD_B                0x2000
#define XINPUT_GAMEPAD_X                0x4000
#define XINPUT_GAMEPAD_Y                0x8000
#endif

typedef DWORD (WINAPI *PFN_XInputGetState)(DWORD dwUserIndex, XINPUT_STATE* pState);

class Fighter;

class InputManager {
private:
    HMODULE hXInputDLL;
    PFN_XInputGetState pfnXInputGetState;
    bool p1GamepadActive;
    bool p2GamepadActive;

public:
    InputManager();
    ~InputManager();

    void update();
    void applyInputToFighter(int playerIndex, Fighter* fighter);
    bool isPausePressed() const;
    bool isDebugTogglePressed() const;
    bool isSyllabusTogglePressed() const;
    bool isHowToPlayTogglePressed() const;
    bool isModeShiftPressed() const;
    bool isTutorialSelected() const;
    bool isFightSelected() const;
};

#endif // INPUTMANAGER_HPP

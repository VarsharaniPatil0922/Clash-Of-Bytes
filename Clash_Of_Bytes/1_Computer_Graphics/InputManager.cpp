#include "InputManager.hpp"
#include "Fighter.hpp"

InputManager::InputManager() 
    : hXInputDLL(NULL), pfnXInputGetState(NULL), p1GamepadActive(false), p2GamepadActive(false) 
{
    const char* dllNames[] = {"xinput1_4.dll", "xinput1_3.dll", "xinput9_1_0.dll"};
    for (const char* name : dllNames) {
        hXInputDLL = LoadLibraryA(name);
        if (hXInputDLL) {
            pfnXInputGetState = (PFN_XInputGetState)GetProcAddress(hXInputDLL, "XInputGetState");
            if (pfnXInputGetState) break;
            FreeLibrary(hXInputDLL);
            hXInputDLL = NULL;
        }
    }
}

InputManager::~InputManager() {
    if (hXInputDLL) FreeLibrary(hXInputDLL);
}

void InputManager::update() {
    if (pfnXInputGetState) {
        XINPUT_STATE state;
        p1GamepadActive = (pfnXInputGetState(0, &state) == ERROR_SUCCESS);
        p2GamepadActive = (pfnXInputGetState(1, &state) == ERROR_SUCCESS);
    }
}

void InputManager::applyInputToFighter(int playerIndex, Fighter* fighter) {
    if (!fighter) return;

    bool left = false, right = false, jump = false, crouch = false;
    bool punch = false, kick = false, block = false, special = false;

    if (playerIndex == 0) {
        // Player 1 Keyboard: WASD + F(Punch), G(Kick), H(Block), R(Special)
        if (GetAsyncKeyState('A') & 0x8000) left = true;
        if (GetAsyncKeyState('D') & 0x8000) right = true;
        if (GetAsyncKeyState('W') & 0x8000) jump = true;
        if (GetAsyncKeyState('S') & 0x8000) crouch = true;
        if (GetAsyncKeyState('F') & 0x8000) punch = true;
        if (GetAsyncKeyState('G') & 0x8000) kick = true;
        if (GetAsyncKeyState('H') & 0x8000) block = true;
        if (GetAsyncKeyState('R') & 0x8000) special = true;

        // XInput Controller 0 fallback
        if (pfnXInputGetState && p1GamepadActive) {
            XINPUT_STATE state;
            if (pfnXInputGetState(0, &state) == ERROR_SUCCESS) {
                SHORT lx = state.Gamepad.sThumbLX;
                SHORT ly = state.Gamepad.sThumbLY;
                if (lx < -12000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_LEFT)) left = true;
                if (lx > 12000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_RIGHT)) right = true;
                if (ly > 16000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_UP)) jump = true;
                if (ly < -16000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_DOWN)) crouch = true;
                if (state.Gamepad.wButtons & XINPUT_GAMEPAD_X) punch = true;
                if (state.Gamepad.wButtons & XINPUT_GAMEPAD_A) kick = true;
                if (state.Gamepad.bLeftTrigger > 50) block = true;
                if (state.Gamepad.wButtons & XINPUT_GAMEPAD_Y) special = true;
            }
        }
    } else {
        // Player 2 Keyboard: Arrow keys + J/Num1(Punch), K/Num2(Kick), L/Num3(Block), U/Num5(Special)
        if (GetAsyncKeyState(VK_LEFT) & 0x8000) left = true;
        if (GetAsyncKeyState(VK_RIGHT) & 0x8000) right = true;
        if (GetAsyncKeyState(VK_UP) & 0x8000) jump = true;
        if (GetAsyncKeyState(VK_DOWN) & 0x8000) crouch = true;
        if ((GetAsyncKeyState('J') & 0x8000) || (GetAsyncKeyState(VK_NUMPAD1) & 0x8000)) punch = true;
        if ((GetAsyncKeyState('K') & 0x8000) || (GetAsyncKeyState(VK_NUMPAD2) & 0x8000)) kick = true;
        if ((GetAsyncKeyState('L') & 0x8000) || (GetAsyncKeyState(VK_NUMPAD3) & 0x8000)) block = true;
        if ((GetAsyncKeyState('U') & 0x8000) || (GetAsyncKeyState(VK_NUMPAD5) & 0x8000)) special = true;

        // XInput Controller 1 fallback
        if (pfnXInputGetState && p2GamepadActive) {
            XINPUT_STATE state;
            if (pfnXInputGetState(1, &state) == ERROR_SUCCESS) {
                SHORT lx = state.Gamepad.sThumbLX;
                SHORT ly = state.Gamepad.sThumbLY;
                if (lx < -12000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_LEFT)) left = true;
                if (lx > 12000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_RIGHT)) right = true;
                if (ly > 16000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_UP)) jump = true;
                if (ly < -16000 || (state.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_DOWN)) crouch = true;
                if (state.Gamepad.wButtons & XINPUT_GAMEPAD_X) punch = true;
                if (state.Gamepad.wButtons & XINPUT_GAMEPAD_A) kick = true;
                if (state.Gamepad.bLeftTrigger > 50) block = true;
                if (state.Gamepad.wButtons & XINPUT_GAMEPAD_Y) special = true;
            }
        }
    }

    fighter->handleInput(left, right, jump, crouch, punch, kick, block, special);
}

bool InputManager::isPausePressed() const {
    static bool wasPressed = false;
    bool isPressed = (GetAsyncKeyState(VK_ESCAPE) & 0x8000) != 0;
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

bool InputManager::isDebugTogglePressed() const {
    static bool wasPressed = false;
    bool isPressed = (GetAsyncKeyState(VK_F1) & 0x8000) != 0;
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

bool InputManager::isSyllabusTogglePressed() const {
    static bool wasPressed = false;
    bool isPressed = (GetAsyncKeyState(VK_TAB) & 0x8000) != 0;
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

bool InputManager::isHowToPlayTogglePressed() const {
    static bool wasPressed = false;
    bool isPressed = (GetAsyncKeyState(VK_F2) & 0x8000) != 0;
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

bool InputManager::isModeShiftPressed() const {
    static bool wasPressed = false;
    bool isPressed = ((GetAsyncKeyState('M') & 0x8000) != 0) || ((GetAsyncKeyState(VK_F3) & 0x8000) != 0);
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

bool InputManager::isTutorialSelected() const {
    static bool wasPressed = false;
    bool isPressed = ((GetAsyncKeyState('1') & 0x8000) != 0) || ((GetAsyncKeyState('T') & 0x8000) != 0) || ((GetAsyncKeyState(VK_NUMPAD1) & 0x8000) != 0);
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

bool InputManager::isFightSelected() const {
    static bool wasPressed = false;
    bool isPressed = ((GetAsyncKeyState('2') & 0x8000) != 0) || ((GetAsyncKeyState(VK_RETURN) & 0x8000) != 0) || ((GetAsyncKeyState(VK_NUMPAD2) & 0x8000) != 0);
    bool result = isPressed && !wasPressed;
    wasPressed = isPressed;
    return result;
}

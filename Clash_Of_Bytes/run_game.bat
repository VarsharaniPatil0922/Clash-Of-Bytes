@echo off
cd /d "%~dp0"

if not exist "clash_of_bytes.exe" (
    echo [*] Game executable not found. Building first...
    call .\build.bat
)

echo [*] Launching Clash of Bytes [Milestone 1 - Practicals 1 to 4 Edition]...
echo [*] Controls:
echo     Player 1: W/A/S/D = Move/Jump, F = Punch, G = Kick, H = Block, R = Special
echo     Player 2: Arrow Keys = Move/Jump, J/Num1 = Punch, K/Num2 = Kick, L/Num3 = Block, U/Num5 = Special
echo [*] In-Game Shortcuts:
echo     [1] / [T]   = Select Tutorial Mode (Full movement & action guides for both sides)
echo     [2] / [ENTER] = Select Real Fight Mode (Authentic match - 0 indicators)
echo     [M] / [F3]  = Shift between Tutorial Mode and Real Fight Mode anytime
echo     [F4]        = Cycle Player 1 Character (Warrior, Ninja, Cyber, Paladin, Berserker, Gladiator)
echo     [F5]        = Cycle Player 2 Character
echo     [F2]        = Toggle Mode Selection & Guidelines Menu
echo     [TAB]       = Toggle 4-Course Milestone 1 Syllabus Monitor
echo     [F1]        = Toggle Real-Time Hitbox Outlines
echo     [ESC]       = Restart Current Match
echo.
start "" ".\clash_of_bytes.exe" %*

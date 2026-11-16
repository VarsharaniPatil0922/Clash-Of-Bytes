; ==============================================================================
; Subject: Computer Organization & Architecture
; Course Outcome: CO3 | Practical 04 / Task 3
; Topic: 64-bit Algorithmic Arithmetic & String Processing (NASM Syntax)
; Target Architecture: x86-64 (AMD64 / Intel 64)
; Application: "Clash of Bytes" Combat Damage Scaling & Log Processing Engine
; ==============================================================================

section .data
    ; Combat damage inputs for Shift-and-Add algorithmic multiplication
    base_strike_damage  dq 28              ; Multiplicand (Base strike damage)
    combo_scaling_factor dq 5              ; Multiplier (Combo chain multiplier)
    calculated_product  dq 0              ; Resulting burst damage
    
    ; Combat debuff inputs for Booth's signed algorithmic multiplication
    booth_multiplicand  dq -5              ; Multiplicand M (Fighter debuff: -5 HP/tick)
    booth_multiplier    dq 4               ; Multiplier Q (Duration: 4 combat ticks)
    booth_product_low   dq 0               ; Resulting health modifier (-20 HP)
    booth_product_high  dq 0               ; Sign-extension / high 64 bits
    
    ; Test string for 64-bit hardware string processing (repne scasb & search)
    combat_log_event    db "[COMBAT] BLAZE_WARRIOR HIT SHADOW_NINJA! CRITICAL HIT! COMBO X3! CRITICAL HIT!", 0
    token_to_search     db "CRITICAL", 0
    token_match_count   dq 0
    computed_str_len    dq 0

section .bss
    saved_rflags        resq 1             ; Stores hardware CPU flags (ZF, CF, SF, OF)

section .text
    global _start
    global shift_add_multiply
    global booth_multiply
    global count_string_length
    global count_token_matches

_start:
    ; ==========================================================================
    ; PART 1: 64-BIT SHIFT-AND-ADD MULTIPLICATION ALGORITHM
    ; Implements hardware-level binary multiplication on CPU registers:
    ;   RAX = Accumulator (Product)
    ;   RDX = Multiplicand (Shifted Left: RDX << 1)
    ;   RBX = Multiplier   (Shifted Right: RBX >> 1)
    ; ==========================================================================
    mov rdx, [base_strike_damage]          ; Multiplicand in RDX
    mov rbx, [combo_scaling_factor]        ; Multiplier in RBX
    xor rax, rax                           ; Clear Accumulator (Product = 0)
    
    test rbx, rbx                          ; Check if multiplier is zero
    jz .mult_complete

.mult_loop:
    test rbx, 1                            ; Test Least Significant Bit (LSB) of multiplier
    jz .skip_addition                      ; If LSB is 0, skip addition step
    add rax, rdx                           ; If LSB is 1, Accumulator += Multiplicand

.skip_addition:
    shl rdx, 1                             ; Multiplicand <<= 1 (Shift left by 1 bit)
    shr rbx, 1                             ; Multiplier >>= 1   (Shift right by 1 bit)
    jnz .mult_loop                         ; If Multiplier != 0, continue loop

.mult_complete:
    mov [calculated_product], rax          ; Store computed scaled damage
    pushfq                                 ; Push 64-bit RFLAGS to stack
    pop qword [saved_rflags]               ; Capture hardware status flags

    ; ==========================================================================
    ; PART 2: 64-BIT BOOTH'S SIGNED MULTIPLICATION ALGORITHM
    ; Implements hardware-level signed two's complement multiplication:
    ;   RAX = Accumulator (A) [Upper 64 bits of product, starts at 0]
    ;   RBX = Multiplier (Q)  [Lower 64 bits of product, starts at multiplier]
    ;   RDX = Multiplicand (M)[Signed 64-bit operand]
    ;   RSI = Q_-1            [Previous bit shifted out of Q, starts at 0]
    ;   RCX = Cycle Counter   (64 iterations)
    ;   R8  = Temporary register for Q0 bit inspection
    ;   Pairs: 01 -> A=A+M, 10 -> A=A-M, 00/11 -> NOP, followed by ARS [A, Q, Q_-1]
    ; ==========================================================================
booth_multiply:
    mov rdx, [booth_multiplicand]          ; Multiplicand M in RDX (-5)
    mov rbx, [booth_multiplier]            ; Multiplier Q in RBX (4)
    xor rax, rax                           ; A = 0 (Accumulator)
    xor rsi, rsi                           ; Q_-1 = 0
    mov rcx, 64                            ; 64 cycles for 64-bit registers

.booth_loop:
    mov r8, rbx                            ; R8 = Q
    and r8, 1                              ; R8 = Q0 (LSB of multiplier)
    cmp r8, rsi                            ; Compare Q0 and Q_-1
    je .booth_shift                        ; If Q0 == Q_-1 (00 or 11), skip add/sub

    test r8, r8                            ; Test Q0
    jz .booth_add                          ; If Q0 == 0 (so Q_-1 == 1, pair 01): A = A + M
    sub rax, rdx                           ; If Q0 == 1 (so Q_-1 == 0, pair 10): A = A - M
    jmp .booth_shift

.booth_add:
    add rax, rdx                           ; Pair 01: A = A + M

.booth_shift:
    mov rsi, r8                            ; Q_-1 = old Q0
    shrd rbx, rax, 1                       ; Arithmetic Right Shift [A, Q]: RBX >>= 1, LSB of RAX into MSB of RBX
    sar rax, 1                             ; Arithmetic Right Shift A (preserves sign bit)
    dec rcx                                ; Decrement cycle counter
    jnz .booth_loop                        ; Loop 64 times

    mov [booth_product_low], rbx           ; Lower 64 bits (Signed Product = -20)
    mov [booth_product_high], rax          ; Upper 64 bits (Sign Extension = -1)

    ; ==========================================================================
    ; PART 3: 64-BIT HARDWARE STRING PROCESSING (repne scasb)
    ; Calculates string length using processor string scan instruction:
    ;   RDI = String pointer
    ;   AL  = 0 (Search for null-terminator byte)
    ;   RCX = -1 (Maximum scan boundary)
    ; ==========================================================================
    mov rdi, combat_log_event              ; Load string address into RDI
    xor al, al                             ; Target byte: null terminator (0x00)
    mov rcx, -1                            ; Initial counter: 0xFFFFFFFFFFFFFFFF
    cld                                    ; Clear direction flag (scan forward)
    repne scasb                            ; Scan bytes until byte == AL
    not rcx                                ; Invert bits of RCX
    dec rcx                                ; Length = (~RCX) - 1
    mov [computed_str_len], rcx            ; Store computed string length

    ; ==========================================================================
    ; PART 3: EXIT PROGRAM
    ; ==========================================================================
    mov rax, 60                            ; sys_exit opcode
    xor rdi, rdi                           ; Exit code 0
    syscall

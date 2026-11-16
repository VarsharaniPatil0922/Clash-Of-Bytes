; ==============================================================================
; Subject: Computer Organization & Architecture
; Course Outcome: CO2 | Practical 03
; Topic: 64-bit Arithmetic Assembly Language Program (NASM Syntax)
; ==============================================================================

section .data
    current_health      dq 100
    base_damage         dq 18
    defense_stat        dq 4
    combo_multiplier    dq 2
    calculated_damage   dq 0
    final_health        dq 0

section .bss
    saved_rflags        resq 1

section .text
    global _start

_start:
    ; 1. Load Attack Damage into RAX
    mov rax, [base_damage]

    ; 2. Subtract Defense from Attack (RAX = RAX - RBX)
    mov rbx, [defense_stat]
    sub rax, rbx

    ; 3. If net damage is less than 4, set to minimum 4
    cmp rax, 4
    jge .apply_multiplier
    mov rax, 4

.apply_multiplier:
    ; 4. 64-bit Integer Multiplication for Combo Multiplier (RAX = RAX * RDX)
    mov rdx, [combo_multiplier]
    imul rax, rdx
    mov [calculated_damage], rax

    ; 5. Subtract Damage from Current Health
    mov rbx, [current_health]
    sub rbx, rax
    mov [final_health], rbx

    ; 6. Push 64-bit EFLAGS to stack to capture ZF, CF, SF
    pushfq
    pop qword [saved_rflags]

    ; 7. Exit (Linux sys_exit or Windows exit)
    mov rax, 60         ; sys_exit opcode
    xor rdi, rdi        ; status code 0
    syscall

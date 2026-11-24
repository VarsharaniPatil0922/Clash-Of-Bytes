# ==============================================================================
# Subject: Computer Organization & Architecture
# Course Outcome: CO3 | Practical 04 / Task 3
# Topic: 64-bit Algorithmic Multiplication & String Processing (GAS / GCC Syntax)
# Architecture: x86-64 (AMD64 / Intel 64)
# Calling Convention: Microsoft x64 ABI (RCX, RDX, R8, R9)
# ==============================================================================

.intel_syntax noprefix
.text
.globl shift_add_multiply_gas
.globl booth_multiply_gas
.globl string_length_gas
.globl count_token_occurrences_gas

# ------------------------------------------------------------------------------
# 1. 64-BIT SHIFT-AND-ADD MULTIPLICATION
# Signature: extern "C" uint64_t shift_add_multiply_gas(uint64_t multiplicand, uint64_t multiplier);
# RCX = multiplicand (M), RDX = multiplier (Q)
# ------------------------------------------------------------------------------
shift_add_multiply_gas:
    xor rax, rax                # Clear Accumulator (Product = 0)
    mov r8, rcx                 # R8 = Multiplicand
    mov r9, rdx                 # R9 = Multiplier

    test r9, r9                 # Test if multiplier is zero
    jz .sa_done

.sa_loop:
    test r9, 1                  # Test LSB of multiplier
    jz .sa_skip
    add rax, r8                 # If LSB == 1, add multiplicand to product

.sa_skip:
    shl r8, 1                   # Multiplicand <<= 1
    shr r9, 1                   # Multiplier >>= 1
    jnz .sa_loop

.sa_done:
    ret

# ------------------------------------------------------------------------------
# 2. 64-BIT BOOTH'S MULTIPLICATION ALGORITHM (SIGNED)
# Signature: extern "C" int64_t booth_multiply_gas(int64_t multiplicand, int64_t multiplier);
# RCX = multiplicand (M), RDX = multiplier (Q)
# ------------------------------------------------------------------------------
booth_multiply_gas:
    push rbx                    # Preserve non-volatile RBX
    xor rax, rax                # RAX = Accumulator A = 0
    xor r8, r8                  # R8 = Q_-1 = 0
    mov r10, rcx                # R10 = Multiplicand M
    mov rbx, rdx                # RBX = Multiplier Q
    mov r11, 64                 # R11 = 64 cycle counter

.bm_loop:
    mov r9, rbx
    and r9, 1                   # R9 = Q0 (LSB of multiplier)
    cmp r9, r8                  # Compare Q0 and Q_-1
    je .bm_shift

    test r9, r9
    jz .bm_add                  # If Q0 == 0 (pair 01): A = A + M
    sub rax, r10                # If Q0 == 1 (pair 10): A = A - M
    jmp .bm_shift

.bm_add:
    add rax, r10

.bm_shift:
    mov r8, r9                  # Q_-1 = old Q0
    shrd rbx, rax, 1            # Double-precision shift: RBX >>= 1 with LSB of RAX into MSB
    sar rax, 1                  # Arithmetic right shift on A
    dec r11
    jnz .bm_loop

    mov rax, rbx                # Return lower 64-bit product Q
    pop rbx                     # Restore RBX
    ret

# ------------------------------------------------------------------------------
# 3. 64-BIT HARDWARE STRING LENGTH (repne scasb)
# Signature: extern "C" uint64_t string_length_gas(const char* str);
# RCX = string address
# ------------------------------------------------------------------------------
string_length_gas:
    push rdi                    # Preserve non-volatile RDI
    test rcx, rcx
    jz .sl_null

    mov rdi, rcx                # RDI = string buffer
    xor rax, rax                # AL = 0x00 (null byte)
    mov rcx, -1                 # Max scan count
    cld
    repne scasb
    not rcx
    dec rcx
    mov rax, rcx
    pop rdi                     # Restore RDI
    ret

.sl_null:
    xor rax, rax
    pop rdi                     # Restore RDI
    ret

# ------------------------------------------------------------------------------
# 4. 64-BIT TOKEN OCCURRENCE COUNT
# Signature: extern "C" uint64_t count_token_occurrences_gas(const char* text, uint64_t textLen, const char* token, uint64_t tokenLen);
# RCX = text, RDX = textLen, R8 = token, R9 = tokenLen
# ------------------------------------------------------------------------------
count_token_occurrences_gas:
    push rbx
    push rsi
    push rdi
    push r12
    push r13
    push r14
    push r15

    xor rax, rax                # Match count = 0
    test rcx, rcx
    jz .ct_done
    test r8, r8
    jz .ct_done
    cmp r9, 0
    je .ct_done
    cmp rdx, r9
    jb .ct_done

    mov rsi, rcx                # RSI = text pointer
    mov rdi, r8                 # RDI = token pointer
    mov r10, rdx                # R10 = textLen
    mov r11, r9                 # R11 = tokenLen
    xor r12, r12                # R12 = outer index i

.ct_outer:
    mov r13, r10
    sub r13, r11                # R13 = textLen - tokenLen
    cmp r12, r13
    ja .ct_done

    xor r14, r14                # R14 = inner index j
.ct_inner:
    cmp r14, r11
    je .ct_matched

    mov r15, r12
    add r15, r14
    movzx ebx, byte ptr [rsi + r15]
    movzx edx, byte ptr [rdi + r14]
    cmp bl, dl
    jne .ct_mismatch

    inc r14
    jmp .ct_inner

.ct_matched:
    inc rax                     # Match count++

.ct_mismatch:
    inc r12                     # i++
    jmp .ct_outer

.ct_done:
    pop r15
    pop r14
    pop r13
    pop r12
    pop rdi
    pop rsi
    pop rbx
    ret

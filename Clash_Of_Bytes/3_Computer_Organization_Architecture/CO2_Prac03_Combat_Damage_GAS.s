# ==============================================================================
# Subject: Computer Organization & Architecture
# Course Outcome: CO2 | Practical 03
# Topic: 64-bit Hardware Assembly Implementation (GNU Assembler / GAS Syntax)
# Calling Convention: Microsoft x64 ABI (RCX = Attack, RDX = Defense, R8 = Combo)
# ==============================================================================

.intel_syntax noprefix
.text
.globl calculate_combat_damage_gas
.globl calculate_combat_damage_with_flags_gas

# Function: calculate_combat_damage_gas
# Signature: extern "C" uint64_t calculate_combat_damage_gas(uint64_t atk, uint64_t def, uint64_t combo);
calculate_combat_damage_gas:
    # 1. Load Attack Power (RCX) into RAX
    mov rax, rcx

    # 2. Subtract Defense Rating (RDX) from Attack: RAX = RAX - RDX
    sub rax, rdx

    # 3. Minimum Damage Clamp: If net damage < 4, clamp to 4
    cmp rax, 4
    jge 1f
    mov rax, 4

1:
    # 4. Multiply net damage by Combo Multiplier (R8): RAX = RAX * R8
    imul rax, r8

    # 5. Return result in RAX
    ret

# Function: calculate_combat_damage_with_flags_gas
# Signature: extern "C" uint64_t calculate_combat_damage_with_flags_gas(uint64_t atk, uint64_t def, uint64_t combo, uint64_t* outRflags);
calculate_combat_damage_with_flags_gas:
    mov rax, rcx
    sub rax, rdx
    
    # Capture RFLAGS immediately after subtraction
    pushfq
    pop r10
    test r9, r9
    jz 2f
    mov [r9], r10        # Store captured RFLAGS to *outRflags pointer in R9

2:
    cmp rax, 4
    jge 3f
    mov rax, 4

3:
    imul rax, r8
    ret

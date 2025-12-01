# NES Emulator Implementation Plan - Phase 4: Logical Instructions

## Goal Description
Implement the logical instructions `AND` (Bitwise AND), `ORA` (Bitwise OR), `EOR` (Bitwise Exclusive OR), and `BIT` (Bit Test). These instructions modify the Accumulator and Status Flags (Zero, Negative, Overflow).

## User Review Required
- None.

## Proposed Changes

### CPU Core
#### [MODIFY] [Cpu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/cpu/Cpu.java)
- Implement `AND(int addr)`:
    - A = A & M
    - Set Z, N.
- Implement `ORA(int addr)`:
    - A = A | M
    - Set Z, N.
- Implement `EOR(int addr)`:
    - A = A ^ M
    - Set Z, N.
- Implement `BIT(int addr)`:
    - Test bits in memory with Accumulator.
    - Z = (A & M) == 0
    - N = M & 0x80
    - V = M & 0x40

## Verification Plan

### Automated Tests
- **LogicalTest**:
    - Test `AND`, `ORA`, `EOR` with various patterns.
    - Test `BIT` specifically for flag setting (N and V come directly from memory, Z comes from result).
- **Command**: `mvn test`

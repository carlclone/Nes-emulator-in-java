# NES Emulator Implementation Plan - Phase 6: Stack Instructions

## Goal Description
Implement instructions that manipulate the stack and stack pointer. These are used for saving state and transferring data between registers and the stack.

## User Review Required
- **PHP/PLP Behavior**: The Break (B) flag and Unused (U) flag handling during push/pull is specific.
    - `PHP` pushes status with B (bit 4) and U (bit 5) set to 1.
    - `PLP` pulls status but ignores bits 4 and 5 from the stack (they are not physically stored in the status register).

## Proposed Changes

### CPU Core
#### [MODIFY] [Cpu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/cpu/Cpu.java)
- Implement `PHA(int addr)`: Push A.
- Implement `PHP(int addr)`: Push Status | B | U.
- Implement `PLA(int addr)`: Pull A, Set Z, N.
- Implement `PLP(int addr)`: Pull Status, ignore B and U bits.
- Implement `TSX(int addr)`: X = SP, Set Z, N.
- Implement `TXS(int addr)`: SP = X.

## Verification Plan

### Automated Tests
- **StackTest**:
    - Test `PHA`/`PLA` round trip.
    - Test `PHP`/`PLP` flag handling (verify B flag behavior).
    - Test `TSX`/`TXS` transfers.
- **Command**: `mvn test`

# NES Emulator Implementation Plan - Phase 9: System Instructions

## Goal Description
Implement system control instructions: `BRK` (Force Interrupt), `NOP` (No Operation), `RTI` (Return from Interrupt), and Flag manipulation instructions (`CLC`, `SEC`, `CLI`, `SEI`, `CLV`, `CLD`, `SED`).

## User Review Required
- **BRK Behavior**: `BRK` pushes PC+2 to stack (padding byte after opcode), then pushes Status with B flag set. Then jumps to IRQ vector (0xFFFE/F).
- **RTI Behavior**: Pulls Status (ignores B/U bits), then pulls PC.

## Proposed Changes

### CPU Core
#### [MODIFY] [Cpu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/cpu/Cpu.java)
- Implement Flag Instructions:
    - `CLC`, `SEC` (Carry)
    - `CLI`, `SEI` (Interrupt Disable)
    - `CLV` (Overflow - Clear only)
    - `CLD`, `SED` (Decimal - Unused but flags change)
- Implement `NOP`: Do nothing.
- Implement `BRK`:
    - Push PC + 1 (or +2 depending on implementation details, usually PC increments during fetch, so need to check).
    - Push Status | B | U.
    - Set I flag.
    - Load PC from 0xFFFE/F.
- Implement `RTI`:
    - Pull Status.
    - Pull PC.

## Verification Plan

### Automated Tests
- **SystemTest**:
    - Test Flag instructions.
    - Test `BRK` sequence (stack content, PC jump).
    - Test `RTI` (restore state).
- **Command**: `mvn test`

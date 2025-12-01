# NES Emulator Implementation Plan - Phase 3: Arithmetic Instructions

## Goal Description
Implement the arithmetic instructions `ADC` (Add with Carry) and `SBC` (Subtract with Carry). These are critical for mathematical operations and require careful handling of Status Flags (Carry, Zero, Overflow, Negative).

## User Review Required
- **Decimal Mode**: The NES CPU (Ricoh 2A03) does not support Decimal Mode. I will strictly implement binary arithmetic.

## Proposed Changes

### CPU Core
#### [MODIFY] [Cpu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/cpu/Cpu.java)
- Implement `ADC(int addr)`:
    - Fetch data from address.
    - Result = A + Data + Carry.
    - Set Carry (C) if result > 255.
    - Set Zero (Z) if result & 0xFF == 0.
    - Set Negative (N) if bit 7 is set.
    - Set Overflow (V) if sign of result is incorrect (signed overflow).
- Implement `SBC(int addr)`:
    - Fetch data.
    - Invert data (Data ^ 0xFF).
    - Execute `ADC` logic with inverted data.

## Verification Plan

### Automated Tests
- **ArithmeticTest**:
    - Test `ADC` with various inputs (positive, negative, carry in/out).
    - Test `SBC` with various inputs.
    - Verify Overflow (V) flag logic specifically, as it's error-prone.
- **Command**: `mvn test`

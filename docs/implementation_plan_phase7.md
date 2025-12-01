# NES Emulator Implementation Plan - Phase 7: Transfers & Inc/Dec

## Goal Description
Implement instructions for moving data between registers (TAX, TAY, TXA, TYA) and incrementing/decrementing values (INC, DEC, INX, DEX, INY, DEY).

## User Review Required
- None.

## Proposed Changes

### CPU Core
#### [MODIFY] [Cpu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/cpu/Cpu.java)
- Implement Register Transfers:
    - `TAX`: Transfer A to X. Set Z, N.
    - `TAY`: Transfer A to Y. Set Z, N.
    - `TXA`: Transfer X to A. Set Z, N.
    - `TYA`: Transfer Y to A. Set Z, N.
- Implement Increment/Decrement:
    - `INX`: Increment X. Set Z, N.
    - `INY`: Increment Y. Set Z, N.
    - `DEX`: Decrement X. Set Z, N.
    - `DEY`: Decrement Y. Set Z, N.
    - `INC(int addr)`: Increment Memory. Set Z, N.
    - `DEC(int addr)`: Decrement Memory. Set Z, N.

## Verification Plan

### Automated Tests
- **TransferTest**:
    - Verify data transfer and flag updates.
- **IncDecTest**:
    - Verify increment/decrement logic and flag updates (especially wrapping 0xFF -> 0x00).
- **Command**: `mvn test`

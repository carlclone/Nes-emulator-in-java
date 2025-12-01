# NES Emulator Implementation Plan - Phase 12: Memory Map

## Goal Description
Implement the full CPU memory map, routing read/write operations to the appropriate components (RAM, PPU, APU, Cartridge) with correct mirroring.

## User Review Required
- **Component Stubs**: I will create `Ppu` and `Apu` classes as stubs to handle register access. They won't have full functionality yet, just read/write methods.

## Proposed Changes

### Components
#### [NEW] [Ppu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/Ppu.java)
- Stub class.
- `cpuRead(int addr)`: Handle 0x2000-0x2007 (mirrored).
- `cpuWrite(int addr, byte data)`: Handle 0x2000-0x2007 (mirrored).

#### [NEW] [Apu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/Apu.java)
- Stub class.
- `cpuRead(int addr)`: Handle 0x4000-0x4017.
- `cpuWrite(int addr, byte data)`: Handle 0x4000-0x4017.

### Bus Integration
#### [MODIFY] [Bus.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/Bus.java)
- Add `Ppu` and `Apu` fields.
- Update `read/write` logic:
    - **0x0000 - 0x1FFF**: RAM (Mirrored).
    - **0x2000 - 0x3FFF**: PPU Registers (Mirrored every 8 bytes).
    - **0x4000 - 0x4017**: APU Registers.
    - **0x4018 - 0x401F**: APU/IO Test (Disabled).
    - **0x4020 - 0xFFFF**: Cartridge.

## Verification Plan

### Automated Tests
- **MemoryMapTest**:
    - Verify RAM mirroring (e.g., writing to 0x0000 reads back at 0x0800).
    - Verify PPU mirroring (e.g., writing to 0x2000 reads back at 0x2008).
    - Verify routing to PPU/APU stubs.
- **Command**: `mvn test`

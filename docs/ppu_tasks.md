# NES Emulator - PPU & Display Implementation Tasks

## Phase 1: PPU Foundation
- [x] PPU Registers Implementation
    - [x] PPUCTRL (0x2000) - Control register
    - [x] PPUMASK (0x2001) - Mask register
    - [x] PPUSTATUS (0x2002) - Status register
    - [x] OAMADDR (0x2003) - OAM address
    - [x] OAMDATA (0x2004) - OAM data
    - [x] PPUSCROLL (0x2005) - Scroll position
    - [x] PPUADDR (0x2006) - VRAM address
    - [x] PPUDATA (0x2007) - VRAM data
- [x] PPU Memory
    - [x] Pattern Tables (CHR-ROM access)
    - [x] Nametables (2KB VRAM)
    - [x] Palette RAM (32 bytes)
    - [x] OAM (256 bytes for sprites)
- [x] PPU Timing
    - [x] Scanline counter (262 scanlines)
    - [x] Cycle counter (341 cycles per scanline)
    - [x] Frame timing
    - [x] VBlank generation

## Phase 2: Background Rendering
- [x] Tile Fetching
    - [x] Nametable byte fetch
    - [x] Attribute byte fetch
    - [x] Pattern table tile fetch (low/high)
- [x] Background Rendering Pipeline
    - [x] Shift registers for tile data
    - [x] Palette selection
    - [x] Pixel rendering
- [x] Scrolling
    - [x] Horizontal scroll
    - [x] Vertical scroll
    - [x] Nametable mirroring

## Phase 3: Sprite Rendering
- [x] Sprite Evaluation
    - [x] Find sprites on current scanline
    - [x] Sprite 0 hit detection
- [x] Sprite Rendering
    - [x] 8x8 sprite rendering
    - [x] 8x16 sprite rendering (optional)
    - [x] Sprite priority
    - [x] Sprite flipping (H/V)

## Phase 4: Display Window
- [x] GUI Framework Setup
    - [x] Choose framework (Swing/JavaFX)
    - [x] Create window (256x240)
    - [x] Frame buffer implementation
- [x] Rendering Loop
    - [x] 60 FPS timing
    - [x] Frame buffer to screen
    - [x] Double buffering

## Phase 5: Controller Input
- [x] Controller Registers
    - [x] 0x4016 (Controller 1)
    - [x] 0x4017 (Controller 2)
- [x] Input Mapping
    - [x] Keyboard to NES buttons
    - [x] Button state tracking
- [x] Input Polling
    - [x] Strobe mechanism
    - [x] Serial read

## Phase 6: Integration & Testing
- [x] PPU-CPU Synchronization
    - [x] Proper timing (3 PPU cycles per CPU cycle)
    - [x] NMI on VBlank
- [x] Complete Opcode Table
    - [x] Add remaining official opcodes
    - [x] Test with real games
- [x] Game Testing
    - [x] Test with simple games (e.g., Donkey Kong)
    - [x] Debug rendering issues
    - [x] Performance optimization

## Phase 7: Polish & Features
- [ ] APU (Audio) - Optional
    - [ ] Pulse channels
    - [ ] Triangle channel
    - [ ] Noise channel
- [ ] Additional Features
    - [ ] Save states
    - [ ] Debugging tools
    - [ ] Game Genie codes
    - [ ] Screenshot capability

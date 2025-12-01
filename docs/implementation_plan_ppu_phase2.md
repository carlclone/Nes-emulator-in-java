# NES Emulator Implementation Plan - PPU Phase 2: Background Rendering

## Goal
Implement the PPU background rendering pipeline to draw tiles from nametables to a frame buffer, enabling visual output of NES games.

## Background
The NES PPU renders backgrounds using:
- **Nametables**: 32x30 grid of tile indices (960 bytes)
- **Attribute tables**: 8x8 grid defining 2-bit palette selection for 4x4 tile regions (64 bytes)
- **Pattern tables**: 8x8 pixel tile graphics in CHR-ROM (2 bits per pixel)
- **Palettes**: 4-color palettes for background and sprites

The rendering happens in scanlines (0-239), fetching and rendering 8 pixels at a time.

## User Review Required

> [!IMPORTANT]
> **Design Decisions:**
> - We'll implement a simplified rendering model (render per-scanline, not per-pixel initially)
> - Frame buffer will be 256x240 pixels (standard NES resolution)
> - Colors will be represented as RGB integers for now
> - We'll use a standard NES palette lookup table

> [!WARNING]
> **Breaking Changes:**
> - `Ppu.java` will add ~200-300 lines for rendering logic
> - New frame buffer array `int[] frameBuffer` (256 * 240 pixels)
> - Need to add NES color palette constants

## Proposed Changes

### PPU Rendering

#### [MODIFY] [Ppu.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/Ppu.java)
**Add rendering components:**

**Frame Buffer:**
- `int[] frameBuffer = new int[256 * 240]`: RGB pixel buffer
- `getFrameBuffer()`: Accessor for display

**Background Rendering:**
- `renderBackgroundScanline()`: Render one scanline of background
- `fetchNametableByte()`: Get tile index from nametable
- `fetchAttributeByte()`: Get palette selection
- `fetchPatternByte()`: Get tile graphics from CHR-ROM
- `renderPixel(int x, int y, int color)`: Draw pixel to frame buffer

**Palette System:**
- `NES_PALETTE[]`: 64-color NES palette as RGB values
- `getBackgroundColor(int palette, int pixel)`: Get final RGB color

**Shift Registers (for proper scrolling):**
- `int bgShiftPatternLo, bgShiftPatternHi`: Pattern data
- `int bgShiftAttribLo, bgShiftAttribHi`: Attribute data

#### [MODIFY] [Bus.java](file:///c:/Users/lin/Desktop/my_nes2/src/main/java/com/nes/Bus.java)
**Add frame buffer access:**
- `getFrameBuffer()`: Return PPU frame buffer for display

### Testing

#### [MODIFY] [PpuTest.java](file:///c:/Users/lin/Desktop/my_nes2/src/test/java/com/nes/PpuTest.java)
**Add rendering tests:**
- Test nametable/attribute fetching
- Test pattern table decoding
- Test palette color lookup
- Verify frame buffer updates

## Verification Plan

### Automated Tests
- **PpuTest**: Verify tile fetching and palette lookups
- **Command**: `mvn test -Dtest=PpuTest`

### Manual Verification
- Run emulator with a simple ROM
- Verify frame buffer contains non-zero data
- Check that rendering happens during visible scanlines

## Implementation Notes

**NES Color Palette:**
The NES has a fixed 64-color palette. We'll use standard RGB values for each color.

**Tile Fetching Sequence (per 8 pixels):**
1. Fetch nametable byte (tile index)
2. Fetch attribute byte (palette selection)
3. Fetch pattern table low byte
4. Fetch pattern table high byte
5. Combine to render 8 pixels

**Scrolling:**
Uses fine X scroll and VRAM address registers to determine which tiles to fetch.

## Next Steps
After Phase 2:
- **Phase 3**: Sprite rendering
- **Phase 4**: Display window (GUI)
- **Phase 5**: Controller input

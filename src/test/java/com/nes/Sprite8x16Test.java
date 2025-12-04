package com.nes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Sprite8x16Test {

    private Ppu ppu;
    private Cartridge cartridge;

    @BeforeEach
    public void setUp() {
        ppu = new Ppu();
        // Create a dummy cartridge (Mapper 0)
        // 16KB PRG, 8KB CHR
        cartridge = new Cartridge(new byte[16384], new byte[8192], 0, 0);
        ppu.connectCartridge(cartridge);
    }

    @Test
    public void testSprite8x16Rendering() {
        // 1. Configure PPU for 8x16 sprites (Bit 5 of PPUCTRL)
        // PPUCTRL = 0x20 (0010 0000)
        ppu.cpuWrite(0x2000, (byte) 0x20);
        
        // 2. Enable Sprites and Background (PPUMASK = 0x18)
        ppu.cpuWrite(0x2001, (byte) 0x18);
        
        // 3. Set up a sprite in OAM
        // Sprite 0: Y=10, Tile=2, Attr=0, X=50
        // Tile 2 is even, so it uses Pattern Table 0 (0x0000)
        // Top half = Tile 2, Bottom half = Tile 3
        ppu.cpuWrite(0x2003, (byte) 0x00); // OAMADDR = 0
        ppu.cpuWrite(0x2004, (byte) 10);   // Y
        ppu.cpuWrite(0x2004, (byte) 2);    // Tile
        ppu.cpuWrite(0x2004, (byte) 0);    // Attr
        ppu.cpuWrite(0x2004, (byte) 50);   // X
        
        // 4. Write Pattern Data to CHR-RAM
        // Tile 2 (Top Half): Full opaque (Pixel 1)
        // Address 0x0020 (Tile 2 * 16 bytes)
        for (int i = 0; i < 8; i++) {
            ppu.ppuWrite(0x0020 + i, (byte) 0xFF); // Plane 0
            ppu.ppuWrite(0x0028 + i, (byte) 0x00); // Plane 1
        }
        
        // Tile 3 (Bottom Half): Full opaque (Pixel 2)
        // Address 0x0030 (Tile 3 * 16 bytes)
        for (int i = 0; i < 8; i++) {
            ppu.ppuWrite(0x0030 + i, (byte) 0x00); // Plane 0
            ppu.ppuWrite(0x0038 + i, (byte) 0xFF); // Plane 1
        }
        
        // 5. Set Palette
        // Sprite Palette 0 (0x3F11-0x3F13)
        // Index 1 = Red (0x16), Index 2 = Blue (0x12)
        ppu.ppuWrite(0x3F11, (byte) 0x16);
        ppu.ppuWrite(0x3F12, (byte) 0x12);
        
        // 6. Run PPU to render the frame
        // We need to advance to the scanline where the sprite appears.
        // Sprite Y=10, so it appears on scanline 11.
        
        // Run until Scanline 11, Cycle 260 (just after rendering)
        while (ppu.getScanline() < 11 || (ppu.getScanline() == 11 && ppu.getCycle() < 260)) {
            ppu.clock();
        }
        
        // 7. Verify Pixel Output
        // X=50. So at Index 50 (0-indexed) of the scanline buffer.
        int[] buffer = ppu.getFrameBuffer();
        int pixelIndex = 11 * 256 + 50;
        
        // We expect Pixel 1 (Red) because it's the top half (Tile 2)
        // Wait, scanline 11 is the FIRST line of the sprite (row 0).
        // Row 0 comes from Tile 2.
        // Tile 2 has 0xFF in Plane 0, 0x00 in Plane 1 -> Color Index 1.
        // Palette Index 1 is 0x16 (Red).
        // NES_PALETTE[0x16] is the color value.
        // We can just check if it's NOT background color.
        
        int pixelColor = buffer[pixelIndex];
        assertNotEquals(0, pixelColor, "Pixel at X=50 should not be black");
        
        // Now check bottom half
        // Run until Scanline 19 (11 + 8)
        while (ppu.getScanline() < 19 || (ppu.getScanline() == 19 && ppu.getCycle() < 260)) {
            ppu.clock();
        }
        
        pixelIndex = 19 * 256 + 50;
        // Row 8 (relative to sprite top) comes from Tile 3.
        // Tile 3 has 0x00 in Plane 0, 0xFF in Plane 1 -> Color Index 2.
        // Palette Index 2 is 0x12 (Blue).
        
        pixelColor = buffer[pixelIndex];
        assertNotEquals(0, pixelColor, "Pixel at X=50 (bottom half) should not be black");
    }
}

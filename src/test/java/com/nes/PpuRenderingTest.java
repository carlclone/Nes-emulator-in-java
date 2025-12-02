package com.nes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PpuRenderingTest {

    private Ppu ppu;
    private Bus bus;
    private Cartridge cartridge;

    @BeforeEach
    public void setUp() {
        bus = new Bus();
        ppu = bus.getPpu();
        
        // Create a dummy cartridge
        // 16KB PRG, 8KB CHR
        byte[] prg = new byte[16384];
        byte[] chr = new byte[8192];
        
        // Pre-fill CHR with Pattern Data for Tile 1 (Solid Color 1)
        // Tile 1 starts at 0x0010
        // Plane 0: 0x0010-0x0017 (All 1s)
        // Plane 1: 0x0018-0x001F (All 0s)
        for (int i = 0; i < 8; i++) {
            chr[0x0010 + i] = (byte) 0xFF;
        }
        
        cartridge = new Cartridge(prg, chr, 0);
        
        ppu.connectCartridge(cartridge);
        ppu.reset();
    }

    @Test
    public void testBackgroundRendering() {
        // 1. Setup Palette
        // Palette 0, Color 1 = 0x30 (White)
        ppu.cpuWrite(0x2006, (byte) 0x3F);
        ppu.cpuWrite(0x2006, (byte) 0x01);
        ppu.cpuWrite(0x2007, (byte) 0x30); // White
        
        // 2. Setup Nametable (Tile 1 at top-left)
        ppu.cpuWrite(0x2006, (byte) 0x20);
        ppu.cpuWrite(0x2006, (byte) 0x00);
        ppu.cpuWrite(0x2007, (byte) 0x01); // Tile ID 0x01
        
        // 3. Pattern Table is already pre-filled in setUp()
        // Tile 1 has 0xFF in low plane and 0x00 in high plane -> Color Index 1
        
        // 4. Enable Background Rendering
        // PPUCTRL: Background table 0 (0x0000)
        ppu.cpuWrite(0x2000, (byte) 0x00); 
        // PPUMASK: Show background (Bit 3), Show background in left 8 pixels (Bit 1)
        ppu.cpuWrite(0x2001, (byte) 0x0A); 
        
        // 5. Run PPU to prime the shifters
        // The PPU needs the pre-render scanline (261) to fetch the first tiles for scanline 0.
        // Since reset() sets scanline=0, we should run a full frame or at least get to the end of the pre-render line.
        
        // Run until we hit the pre-render scanline
        while (ppu.getScanline() != 261) {
            ppu.clock();
        }
        
        // Now run through the pre-render scanline until we hit scanline 0 again
        while (ppu.getScanline() != 0) {
            ppu.clock();
        }
        
        // Run through Scanline 0
        // First 8 cycles will fetch the first tile.
        // Pixels should start appearing after that.
        for (int i = 0; i < 20; i++) {
            ppu.clock();
        }
        
        // 6. Check Frame Buffer
        // Pixel at (0,0) should be Color 1 (White -> 0x30 -> RGB value)
        int[] buffer = ppu.getFrameBuffer();
        int pixel = buffer[0];
        
        System.out.println("Pixel at 0,0: " + String.format("0x%06X", pixel));
        
        // NES Palette 0x30 is usually white/bright (approx 0xFFFFFF or similar)
        // It should NOT be black (0x000000) or the default grey.
        assertNotEquals(0, pixel, "Pixel should not be black");
        assertNotEquals(0xFF000000, pixel, "Pixel should not be transparent black");
    }
}

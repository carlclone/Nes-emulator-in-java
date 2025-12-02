package com.nes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PpuSpriteTest {

    private Ppu ppu;
    private Bus bus;
    private Cartridge cartridge;

    @BeforeEach
    public void setUp() {
        bus = new Bus();
        ppu = bus.getPpu();
        
        // Create a dummy cartridge
        byte[] prg = new byte[16384];
        byte[] chr = new byte[8192];
        
        // Pre-fill CHR with Pattern Data for Tile 1 (Solid White)
        // Tile 1 starts at 0x0010
        // Plane 0: 0x0010-0x0017
        // Plane 1: 0x0018-0x001F
        for (int i = 0; i < 16; i++) {
            chr[0x0010 + i] = (byte) 0xFF;
        }
        
        cartridge = new Cartridge(prg, chr, 0);
        
        ppu.connectCartridge(cartridge);
        ppu.reset();
    }

    @Test
    public void testSpriteEvaluation() {
        // Setup OAM
        // Sprite 0: Y=9 (appears on line 10), Tile=1, Attr=0, X=50
        ppu.cpuWrite(0x2003, (byte) 0x00);
        ppu.cpuWrite(0x2004, (byte) 9);
        ppu.cpuWrite(0x2004, (byte) 1);
        ppu.cpuWrite(0x2004, (byte) 0);
        ppu.cpuWrite(0x2004, (byte) 50);
        
        // Enable Sprites
        ppu.cpuWrite(0x2001, (byte) 0x10); // Show sprites
        
        // Run until Scanline 10 (Pre-render is 261, then 0..9)
        while (ppu.getScanline() != 9) {
            ppu.clock();
        }
        
        // Clock through scanline 9. At cycle 257, it should evaluate sprites for line 10.
        while (ppu.getCycle() < 300) {
            ppu.clock();
        }
        
        // We can't easily check internal state without reflection or getters, 
        // but we can check if it renders on the NEXT scanline (10).
        
        // Advance to Scanline 10, Cycle 50 (Sprite X)
        while (ppu.getScanline() != 10) {
            ppu.clock();
        }
        
        // Setup Palette 4 (Sprite Palette 0)
        // 0x3F11 (Color 1) = 0x30 (White)
        ppu.cpuWrite(0x2006, (byte) 0x3F);
        ppu.cpuWrite(0x2006, (byte) 0x11);
        ppu.cpuWrite(0x2007, (byte) 0x30);
        
        // Clock through the sprite pixels (X=50 to 57)
        while (ppu.getCycle() < 60) {
            ppu.clock();
        }
        
        // Check Frame Buffer at (50, 10)
        int[] buffer = ppu.getFrameBuffer();
        int pixel = buffer[10 * 256 + 50];
        
        // Should be white (from palette 0x30)
        assertNotEquals(0, pixel, "Sprite pixel should be rendered");
        assertNotEquals(0xFF000000, pixel, "Sprite pixel should not be black");
    }
    
    @Test
    public void testSpriteZeroHit() {
        // Setup OAM Sprite 0 at (1, 2) -> Appears on line 2
        ppu.cpuWrite(0x2003, (byte) 0x00);
        ppu.cpuWrite(0x2004, (byte) 1);  // Y
        ppu.cpuWrite(0x2004, (byte) 1);  // Tile
        ppu.cpuWrite(0x2004, (byte) 0);  // Attr
        ppu.cpuWrite(0x2004, (byte) 2);  // X
        
        // Setup Background at (2, 2)
        // Nametable 0, Tile 0 (0x2000) = Tile ID 1
        ppu.cpuWrite(0x2006, (byte) 0x20);
        ppu.cpuWrite(0x2006, (byte) 0x00);
        ppu.cpuWrite(0x2007, (byte) 0x01);
        
        // Enable BG and Sprites
        ppu.cpuWrite(0x2001, (byte) 0x1E); // Show BG, Show Sprites, Show in left 8px
        
        // Run to Scanline 2
        while (ppu.getScanline() != 2) {
            ppu.clock();
        }
        
        // Run past X=2
        while (ppu.getCycle() < 10) {
            ppu.clock();
        }
        
        // Check PPUSTATUS (0x2002) for Sprite 0 Hit (Bit 6)
        byte status = ppu.cpuRead(0x2002);
        assertTrue((status & 0x40) != 0, "Sprite 0 Hit should be set");
    }
}

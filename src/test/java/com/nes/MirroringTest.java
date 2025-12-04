package com.nes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MirroringTest {

    private Ppu ppu;
    private Cartridge cartridge;

    @BeforeEach
    public void setUp() {
        ppu = new Ppu();
    }

    @Test
    public void testHorizontalMirroring() {
        // Mode 0 = Horizontal
        cartridge = new Cartridge(new byte[16384], new byte[8192], 0, 0);
        ppu.connectCartridge(cartridge);
        
        // NT0 ($2000) and NT1 ($2400) should map to VRAM A
        ppu.ppuWrite(0x2000, (byte) 0xAA);
        assertEquals((byte) 0xAA, ppu.ppuRead(0x2000), "Read back from NT0");
        assertEquals((byte) 0xAA, ppu.ppuRead(0x2400), "NT1 should mirror NT0");
        
        // NT2 ($2800) and NT3 ($2C00) should map to VRAM B
        ppu.ppuWrite(0x2800, (byte) 0xBB);
        assertEquals((byte) 0xBB, ppu.ppuRead(0x2800), "Read back from NT2");
        assertEquals((byte) 0xBB, ppu.ppuRead(0x2C00), "NT3 should mirror NT2");
        
        // Verify they are distinct
        assertEquals((byte) 0xAA, ppu.ppuRead(0x2000), "NT0 should be unchanged");
        assertNotEquals(ppu.ppuRead(0x2000), ppu.ppuRead(0x2800), "NT0 and NT2 should be distinct");
    }

    @Test
    public void testVerticalMirroring() {
        // Mode 1 = Vertical
        cartridge = new Cartridge(new byte[16384], new byte[8192], 0, 1);
        ppu.connectCartridge(cartridge);
        
        // NT0 ($2000) and NT2 ($2800) should map to VRAM A
        ppu.ppuWrite(0x2000, (byte) 0xCC);
        assertEquals((byte) 0xCC, ppu.ppuRead(0x2000), "Read back from NT0");
        assertEquals((byte) 0xCC, ppu.ppuRead(0x2800), "NT2 should mirror NT0");
        
        // NT1 ($2400) and NT3 ($2C00) should map to VRAM B
        ppu.ppuWrite(0x2400, (byte) 0xDD);
        assertEquals((byte) 0xDD, ppu.ppuRead(0x2400), "Read back from NT1");
        assertEquals((byte) 0xDD, ppu.ppuRead(0x2C00), "NT3 should mirror NT1");
        
        // Verify they are distinct
        assertEquals((byte) 0xCC, ppu.ppuRead(0x2000), "NT0 should be unchanged");
        assertNotEquals(ppu.ppuRead(0x2000), ppu.ppuRead(0x2400), "NT0 and NT1 should be distinct");
    }
}
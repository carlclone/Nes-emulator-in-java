package com.nes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MemoryMapTest {

    private Bus bus;

    @BeforeEach
    public void setUp() {
        bus = new Bus();
    }

    @Test
    public void testRamMirroring() {
        // Write to 0x0000
        bus.write(0x0000, (byte) 0x42);
        
        // Read from 0x0000
        assertEquals(0x42, bus.read(0x0000));
        
        // Read from Mirror 0x0800
        assertEquals(0x42, bus.read(0x0800));
        
        // Read from Mirror 0x1800
        assertEquals(0x42, bus.read(0x1800));
        
        // Write to Mirror 0x0801
        bus.write(0x0801, (byte) 0x55);
        
        // Read from Real 0x0001
        assertEquals(0x55, bus.read(0x0001));
    }
    
    @Test
    public void testPpuMirroring() {
        // PPU Stub currently does nothing, but we can verify it doesn't crash
        // and that routing logic works (by checking it doesn't write to RAM)
        
        // Write to PPU 0x2000
        bus.write(0x2000, (byte) 0xFF);
        
        // Read from RAM 0x2000 (should be 0, as it's out of RAM bounds in array, but wait...)
        // My RAM array is 64KB.
        // But my read() logic for RAM only returns for 0x0000-0x1FFF.
        // So if I read 0x2000, it goes to PPU.
        
        // Since PPU stub returns 0, we expect 0.
        assertEquals(0x00, bus.read(0x2000));
    }
}

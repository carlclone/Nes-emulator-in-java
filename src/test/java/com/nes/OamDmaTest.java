package com.nes;

import com.nes.cpu.Cpu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OamDmaTest {

    private Bus bus;
    private Cpu cpu;
    private Ppu ppu;

    @BeforeEach
    public void setUp() {
        bus = new Bus();
        cpu = new Cpu();
        bus.connectCpu(cpu);
        cpu.connectBus(bus);
        ppu = bus.getPpu();
        bus.reset();
    }

    @Test
    public void testOamDmaTransfer() {
        // 1. Prepare data in RAM (Page 0x0200)
        // We'll write a pattern: 0, 1, 2, ... 255
        for (int i = 0; i < 256; i++) {
            bus.write(0x0200 + i, (byte) i);
        }
        
        // 2. Clear OAM to ensure we are testing the transfer
        // OAM is internal to PPU, but we can write zeros to it via 0x2004 manually first if needed.
        // Or just rely on reset. Reset clears OAM? No, reset doesn't clear OAM in my Ppu.java usually.
        // Let's verify what reset does.
        // But let's assume it's unknown.
        
        // 3. Trigger DMA
        // Write 0x02 to 0x4014 (Page 2)
        bus.write(0x4014, (byte) 0x02);
        
        // 4. Verify OAM Data
        // We can read back via 0x2004 (OAMDATA).
        // But reading 0x2004 doesn't auto-increment address.
        // So we need to set OAMADDR to 0, then read, then set to 1, etc.
        // Wait, OAMADDR increments on write, not read?
        // "Reads from OAMDATA do not increment OAMADDR" (usually).
        // Let's check my Ppu.java implementation of cpuRead(0x2004).
        
        // Actually, let's use the debug getter if available, or just read via 2004.
        
        for (int i = 0; i < 256; i++) {
            // Set OAM Address
            bus.write(0x2003, (byte) i);
            
            // Read OAM Data
            // Note: In real hardware, reading OAMDATA is unreliable during rendering,
            // but we are in vblank/disabled state.
            byte data = bus.read(0x2004);
            
            assertEquals((byte) i, data, "OAM Byte " + i + " should match RAM");
        }
    }
}

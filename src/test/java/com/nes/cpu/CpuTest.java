package com.nes.cpu;

import com.nes.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CpuTest {

    private Cpu cpu;
    private Bus bus;

    @BeforeEach
    public void setUp() {
        cpu = new Cpu();
        bus = new Bus();
        cpu.connectBus(bus);
    }

    @Test
    public void testReset() {
        // Set reset vector
        bus.write(0xFFFC, (byte) 0x00);
        bus.write(0xFFFD, (byte) 0x80);

        cpu.reset();

        assertEquals(0x8000, cpu.pc, "PC should be loaded from reset vector");
        assertEquals(0, cpu.a, "Accumulator should be 0");
        assertEquals(0, cpu.x, "X register should be 0");
        assertEquals(0, cpu.y, "Y register should be 0");
        assertEquals((byte) 0xFD, cpu.sp, "Stack Pointer should be 0xFD");
        
        // Check Unused flag is set
        assertEquals(1, cpu.getFlag(Cpu.U), "Unused flag should be set");
    }
}

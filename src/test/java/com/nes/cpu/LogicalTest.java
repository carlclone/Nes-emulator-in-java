package com.nes.cpu;

import com.nes.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LogicalTest {

    private Cpu cpu;
    private Bus bus;

    @BeforeEach
    public void setUp() {
        cpu = new Cpu();
        bus = new Bus();
        cpu.connectBus(bus);
        
        // Initialize Reset Vector
        bus.write(0xFFFC, (byte) 0x00);
        bus.write(0xFFFD, (byte) 0x80);
        
        cpu.reset();
    }

    @Test
    public void testAND() {
        cpu.a = (byte) 0xFF; // 1111 1111
        bus.write(0x1000, (byte) 0x0F); // 0000 1111
        cpu.AND(0x1000);
        
        assertEquals(0x0F, cpu.a);
        assertEquals(0, cpu.getFlag(Cpu.Z));
        assertEquals(0, cpu.getFlag(Cpu.N));
        
        // Test Zero
        cpu.a = (byte) 0xF0;
        bus.write(0x1001, (byte) 0x0F);
        cpu.AND(0x1001);
        assertEquals(0x00, cpu.a);
        assertEquals(1, cpu.getFlag(Cpu.Z));
    }

    @Test
    public void testORA() {
        cpu.a = (byte) 0xF0; // 1111 0000
        bus.write(0x1000, (byte) 0x0F); // 0000 1111
        cpu.ORA(0x1000);
        
        assertEquals((byte) 0xFF, cpu.a);
        assertEquals(0, cpu.getFlag(Cpu.Z));
        assertEquals(1, cpu.getFlag(Cpu.N));
    }

    @Test
    public void testEOR() {
        cpu.a = (byte) 0xFF; // 1111 1111
        bus.write(0x1000, (byte) 0x0F); // 0000 1111
        cpu.EOR(0x1000);
        
        assertEquals((byte) 0xF0, cpu.a); // 1111 0000
        assertEquals(0, cpu.getFlag(Cpu.Z));
        assertEquals(1, cpu.getFlag(Cpu.N));
    }

    @Test
    public void testBIT() {
        cpu.a = (byte) 0x0F; // 0000 1111
        // Memory: 1100 0000 (N=1, V=1)
        bus.write(0x1000, (byte) 0xC0); 
        
        cpu.BIT(0x1000);
        
        // A should not change
        assertEquals(0x0F, cpu.a);
        
        // (0x0F & 0xC0) == 0 -> Z=1
        assertEquals(1, cpu.getFlag(Cpu.Z));
        
        // M bit 7 is 1 -> N=1
        assertEquals(1, cpu.getFlag(Cpu.N));
        
        // M bit 6 is 1 -> V=1
        assertEquals(1, cpu.getFlag(Cpu.V));
    }
}

package com.nes.cpu;

import com.nes.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StackTest {

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
    public void testPHA_PLA() {
        cpu.a = 0x42;
        cpu.PHA(0);
        
        cpu.a = 0x00;
        cpu.PLA(0);
        
        assertEquals(0x42, cpu.a);
        assertEquals(0, cpu.getFlag(Cpu.Z));
        assertEquals(0, cpu.getFlag(Cpu.N));
    }

    @Test
    public void testPHP_PLP() {
        cpu.status = (byte) (Cpu.Z | Cpu.C); // Z=1, C=1
        
        cpu.PHP(0);
        
        // Check stack content manually
        // Stack is at 0x0100 + SP. SP was decremented.
        // SP starts at 0xFD. After push, SP=0xFC. Data at 0x1FD.
        byte pushedStatus = bus.read(0x01FD);
        
        // Pushed status should have B(4) and U(5) set.
        // Z(1) | C(0) | B(4) | U(5) = 0011 0011 = 0x33
        assertEquals(0x33, pushedStatus & 0xFF);
        
        cpu.status = 0;
        cpu.PLP(0);
        
        // Restored status should have Z and C set. B should be ignored (0). U should be 1.
        // 0010 0011 = 0x23
        assertEquals(0x23, cpu.status & 0xFF);
    }
    
    @Test
    public void testTSX_TXS() {
        cpu.sp = (byte) 0xF0;
        cpu.TSX(0);
        
        assertEquals((byte) 0xF0, cpu.x);
        assertEquals(1, cpu.getFlag(Cpu.N)); // 0xF0 is negative
        
        cpu.x = (byte) 0x88;
        cpu.TXS(0);
        assertEquals((byte) 0x88, cpu.sp);
    }
}

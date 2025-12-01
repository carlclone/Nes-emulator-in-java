package com.nes.cpu;

import com.nes.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SystemTest {

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
    public void testFlags() {
        cpu.SEC(0);
        assertEquals(1, cpu.getFlag(Cpu.C));
        cpu.CLC(0);
        assertEquals(0, cpu.getFlag(Cpu.C));
        
        cpu.SEI(0);
        assertEquals(1, cpu.getFlag(Cpu.I));
        cpu.CLI(0);
        assertEquals(0, cpu.getFlag(Cpu.I));
    }

    @Test
    public void testBRK_RTI() {
        // Setup IRQ vector
        bus.write(0xFFFE, (byte) 0x00);
        bus.write(0xFFFF, (byte) 0x90); // Jump to 0x9000
        
        // PC at 0x8000
        // Simulate fetch of BRK opcode
        cpu.pc++; 
        
        // Execute BRK
        cpu.BRK(0);
        
        assertEquals(0x9000, cpu.pc);
        assertEquals(1, cpu.getFlag(Cpu.I));
        
        // Check Stack
        // Pushed PC+1 (0x8001 -> 0x8002). 
        // Pushed Status.
        
        // Now RTI
        cpu.RTI(0);
        
        // Should return to 0x8002
        assertEquals(0x8002, cpu.pc);
    }
}

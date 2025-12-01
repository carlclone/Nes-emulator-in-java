package com.nes.cpu;

import com.nes.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArithmeticTest {

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
    public void testADC_NoCarry() {
        cpu.a = 10;
        bus.write(0x1000, (byte) 20);
        cpu.ADC(0x1000);
        
        assertEquals(30, cpu.a);
        assertEquals(0, cpu.getFlag(Cpu.C));
        assertEquals(0, cpu.getFlag(Cpu.V));
    }

    @Test
    public void testADC_CarryOut() {
        cpu.a = (byte) 250;
        bus.write(0x1000, (byte) 10);
        cpu.ADC(0x1000);
        
        // 250 + 10 = 260 -> 4 with Carry
        assertEquals(4, cpu.a);
        assertEquals(1, cpu.getFlag(Cpu.C));
        assertEquals(0, cpu.getFlag(Cpu.V));
    }

    @Test
    public void testADC_Overflow_PosPosNeg() {
        // 127 + 1 = 128 (-128 in signed byte) -> Overflow
        cpu.a = 127;
        bus.write(0x1000, (byte) 1);
        cpu.ADC(0x1000);
        
        assertEquals((byte) -128, cpu.a);
        assertEquals(1, cpu.getFlag(Cpu.V));
        assertEquals(1, cpu.getFlag(Cpu.N));
    }

    @Test
    public void testSBC_Simple() {
        // 10 - 5 = 5
        // SBC requires Carry to be set for "no borrow"
        // A - M - (1-C)
        // If C=1, A - M - 0
        cpu.status |= Cpu.C; 
        cpu.a = 10;
        bus.write(0x1000, (byte) 5);
        cpu.SBC(0x1000);
        
        assertEquals(5, cpu.a);
        assertEquals(1, cpu.getFlag(Cpu.C)); // Carry set means no borrow occurred (result >= 0)
    }

    @Test
    public void testSBC_Borrow() {
        // 10 - 20 = -10
        cpu.status |= Cpu.C;
        cpu.a = 10;
        bus.write(0x1000, (byte) 20);
        cpu.SBC(0x1000);
        
        assertEquals((byte) -10, cpu.a);
        assertEquals(0, cpu.getFlag(Cpu.C)); // Carry clear means borrow occurred
        assertEquals(1, cpu.getFlag(Cpu.N));
    }
}

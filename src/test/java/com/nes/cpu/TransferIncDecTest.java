package com.nes.cpu;

import com.nes.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransferIncDecTest {

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
    public void testTransfers() {
        cpu.a = 0x42;
        cpu.TAX(0);
        assertEquals(0x42, cpu.x);
        assertEquals(0, cpu.getFlag(Cpu.Z));
        
        cpu.a = 0x00;
        cpu.TAY(0);
        assertEquals(0x00, cpu.y);
        assertEquals(1, cpu.getFlag(Cpu.Z));
        
        cpu.x = (byte) 0x80;
        cpu.TXA(0);
        assertEquals((byte) 0x80, cpu.a);
        assertEquals(1, cpu.getFlag(Cpu.N));
    }

    @Test
    public void testIncDecRegister() {
        cpu.x = 0x00;
        cpu.DEX(0);
        assertEquals((byte) 0xFF, cpu.x);
        assertEquals(1, cpu.getFlag(Cpu.N));
        
        cpu.x = (byte) 0xFF;
        cpu.INX(0);
        assertEquals(0x00, cpu.x);
        assertEquals(1, cpu.getFlag(Cpu.Z));
    }

    @Test
    public void testIncDecMemory() {
        bus.write(0x1000, (byte) 0x05);
        
        cpu.INC(0x1000);
        assertEquals(0x06, bus.read(0x1000));
        
        cpu.DEC(0x1000);
        assertEquals(0x05, bus.read(0x1000));
    }
}

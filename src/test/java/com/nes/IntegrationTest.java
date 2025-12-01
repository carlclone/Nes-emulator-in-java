package com.nes;

import com.nes.cpu.Cpu;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete NES emulator.
 * Tests ROM loading, instruction execution, and system integration.
 */
public class IntegrationTest {

    @Test
    public void testDummyRomExecution() {
        // Create a dummy cartridge with a simple program
        // 0x8000: A9 55 (LDA #$55)
        // 0x8002: 8D 00 02 (STA $0200)
        // 0x8005: A9 AA (LDA #$AA)
        // 0x8007: 8D 01 02 (STA $0201)
        // 0x800A: 4C 0A 80 (JMP $800A) - infinite loop
        
        byte[] prg = new byte[16384];
        prg[0] = (byte) 0xA9; prg[1] = (byte) 0x55;  // LDA #$55
        prg[2] = (byte) 0x8D; prg[3] = (byte) 0x00; prg[4] = (byte) 0x02;  // STA $0200
        prg[5] = (byte) 0xA9; prg[6] = (byte) 0xAA;  // LDA #$AA
        prg[7] = (byte) 0x8D; prg[8] = (byte) 0x01; prg[9] = (byte) 0x02;  // STA $0201
        prg[10] = (byte) 0x4C; prg[11] = (byte) 0x0A; prg[12] = (byte) 0x80; // JMP $800A
        
        // Reset Vector at 0xFFFC (offset 0x3FFC in 16KB ROM)
        prg[0x3FFC] = (byte) 0x00;
        prg[0x3FFD] = (byte) 0x80;
        
        byte[] chr = new byte[8192];
        Cartridge cart = new Cartridge(prg, chr, 0);
        
        Bus bus = new Bus();
        Cpu cpu = new Cpu();
        
        bus.connectCpu(cpu);
        bus.insertCartridge(cart);
        cpu.connectBus(bus);
        
        cpu.reset();
        
        // Verify reset
        assertEquals(0x8000, cpu.pc);
        assertEquals(0, cpu.a);
        
        // Run enough cycles to execute the program
        for (int i = 0; i < 200; i++) {
            bus.clock();
        }
        
        // Verify execution results
        assertEquals((byte) 0xAA, cpu.a);  // Last LDA should have loaded 0xAA
        assertEquals((byte) 0x55, bus.read(0x0200));  // First STA
        assertEquals((byte) 0xAA, bus.read(0x0201));  // Second STA
        assertEquals(0x800A, cpu.pc);  // Should be at the JMP target
    }
    
    @Test
    public void testArithmeticProgram() {
        // Test a program with arithmetic operations
        // 0x8000: A9 10 (LDA #$10)
        // 0x8002: 69 05 (ADC #$05)  -> A = 0x15
        // 0x8004: 8D 00 02 (STA $0200)
        // 0x8007: A9 20 (LDA #$20)
        // 0x8009: E9 08 (SBC #$08)  -> A = 0x17 (with borrow)
        // 0x800B: 8D 01 02 (STA $0201)
        
        byte[] prg = new byte[16384];
        prg[0] = (byte) 0xA9; prg[1] = (byte) 0x10;  // LDA #$10
        prg[2] = (byte) 0x69; prg[3] = (byte) 0x05;  // ADC #$05
        prg[4] = (byte) 0x8D; prg[5] = (byte) 0x00; prg[6] = (byte) 0x02;  // STA $0200
        prg[7] = (byte) 0xA9; prg[8] = (byte) 0x20;  // LDA #$20
        prg[9] = (byte) 0xE9; prg[10] = (byte) 0x08; // SBC #$08
        prg[11] = (byte) 0x8D; prg[12] = (byte) 0x01; prg[13] = (byte) 0x02; // STA $0201
        
        // Reset Vector
        prg[0x3FFC] = (byte) 0x00;
        prg[0x3FFD] = (byte) 0x80;
        
        byte[] chr = new byte[8192];
        Cartridge cart = new Cartridge(prg, chr, 0);
        
        Bus bus = new Bus();
        Cpu cpu = new Cpu();
        
        bus.connectCpu(cpu);
        bus.insertCartridge(cart);
        cpu.connectBus(bus);
        
        cpu.reset();
        
        // Run program
        for (int i = 0; i < 200; i++) {
            bus.clock();
        }
        
        // Verify results
        assertEquals((byte) 0x15, bus.read(0x0200));  // 0x10 + 0x05
        assertEquals((byte) 0x17, bus.read(0x0201));  // 0x20 - 0x08 - 1 (no carry initially)
    }
    
    @Test
    public void testBranchingProgram() {
        // Test conditional branching
        // 0x8000: A9 00 (LDA #$00)  -> Z flag set
        // 0x8002: F0 04 (BEQ $8008) -> Branch taken
        // 0x8004: A9 FF (LDA #$FF)  -> Skipped
        // 0x8006: 00 00 (BRK BRK)   -> Skipped
        // 0x8008: A9 42 (LDA #$42)  -> Executed
        // 0x800A: 8D 00 02 (STA $0200)
        
        byte[] prg = new byte[16384];
        prg[0] = (byte) 0xA9; prg[1] = (byte) 0x00;  // LDA #$00
        prg[2] = (byte) 0xF0; prg[3] = (byte) 0x04;  // BEQ +4
        prg[4] = (byte) 0xA9; prg[5] = (byte) 0xFF;  // LDA #$FF (skipped)
        prg[6] = (byte) 0x00; prg[7] = (byte) 0x00;  // BRK BRK (skipped)
        prg[8] = (byte) 0xA9; prg[9] = (byte) 0x42;  // LDA #$42
        prg[10] = (byte) 0x8D; prg[11] = (byte) 0x00; prg[12] = (byte) 0x02; // STA $0200
        
        // Reset Vector
        prg[0x3FFC] = (byte) 0x00;
        prg[0x3FFD] = (byte) 0x80;
        
        byte[] chr = new byte[8192];
        Cartridge cart = new Cartridge(prg, chr, 0);
        
        Bus bus = new Bus();
        Cpu cpu = new Cpu();
        
        bus.connectCpu(cpu);
        bus.insertCartridge(cart);
        cpu.connectBus(bus);
        
        cpu.reset();
        
        // Run program
        for (int i = 0; i < 200; i++) {
            bus.clock();
        }
        
        // Verify branch was taken and correct value stored
        assertEquals((byte) 0x42, cpu.a);
        assertEquals((byte) 0x42, bus.read(0x0200));
    }
    
    @Test
    public void testMemoryMirroring() {
        // Test RAM mirroring
        Bus bus = new Bus();
        
        // Write to base RAM
        bus.write(0x0000, (byte) 0x12);
        
        // Read from mirrors
        assertEquals((byte) 0x12, bus.read(0x0000));
        assertEquals((byte) 0x12, bus.read(0x0800));
        assertEquals((byte) 0x12, bus.read(0x1000));
        assertEquals((byte) 0x12, bus.read(0x1800));
        
        // Write to mirror
        bus.write(0x0801, (byte) 0x34);
        
        // Read from base
        assertEquals((byte) 0x34, bus.read(0x0001));
    }
    
    @Test
    public void testCartridgeMirroring() {
        // Test 16KB ROM mirroring (Mapper 0)
        byte[] prg = new byte[16384];  // 16KB
        prg[0] = (byte) 0xAB;
        prg[16383] = (byte) 0xCD;
        
        byte[] chr = new byte[8192];
        Cartridge cart = new Cartridge(prg, chr, 0);
        
        Bus bus = new Bus();
        bus.insertCartridge(cart);
        
        // Read from 0x8000 (base)
        assertEquals((byte) 0xAB, bus.read(0x8000));
        
        // Read from 0xC000 (mirror)
        assertEquals((byte) 0xAB, bus.read(0xC000));
        
        // Read from end
        assertEquals((byte) 0xCD, bus.read(0xBFFF));
        assertEquals((byte) 0xCD, bus.read(0xFFFF));
    }
}

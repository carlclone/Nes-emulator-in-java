package com.nes.cpu;

import com.nes.Bus;

/**
 * Emulates the MOS 6502 CPU.
 */
public class Cpu {

    // Registers
    public byte a = 0x00;      // Accumulator
    public byte x = 0x00;      // X Register
    public byte y = 0x00;      // Y Register
    public byte sp = (byte) 0xFD; // Stack Pointer
    public int pc = 0x0000;    // Program Counter
    public byte status = 0x00; // Status Register

    // Status Flags
    public static final byte C = (byte) (1 << 0); // Carry Bit
    public static final byte Z = (byte) (1 << 1); // Zero
    public static final byte I = (byte) (1 << 2); // Disable Interrupts
    public static final byte D = (byte) (1 << 3); // Decimal Mode (unused in NES)
    public static final byte B = (byte) (1 << 4); // Break
    public static final byte U = (byte) (1 << 5); // Unused
    public static final byte V = (byte) (1 << 6); // Overflow
    public static final byte N = (byte) (1 << 7); // Negative

    private Bus bus;

    public void connectBus(Bus bus) {
        this.bus = bus;
    }

    /**
     * Reset the CPU to its initial state.
     */
    public void reset() {
        a = 0;
        x = 0;
        y = 0;
        sp = (byte) 0xFD;
        status = (byte) (0x00 | U); // Unused bit is always 1

        // Read reset vector
        int lo = bus.read(0xFFFC) & 0xFF;
        int hi = bus.read(0xFFFD) & 0xFF;
        pc = (hi << 8) | lo;

        // Reset takes time
        // cycles = 8;
    }

    /**
     * Read a byte from the bus.
     */
    public byte read(int addr) {
        return bus.read(addr);
    }

    /**
     * Write a byte to the bus.
     */
    public void write(int addr, byte data) {
        bus.write(addr, data);
    }
    
    // Helper to set flags based on result
    private void setFlag(byte flag, boolean v) {
        if (v) {
            status |= flag;
        } else {
            status &= ~flag;
        }
    }

    public byte getFlag(byte flag) {
        return (byte) ((status & flag) > 0 ? 1 : 0);
    }
}

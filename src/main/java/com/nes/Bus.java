package com.nes;

/**
 * Represents the NES Bus.
 * Connects the CPU, PPU, and other components to memory.
 */
public class Bus {
    // 64KB RAM
    private final byte[] ram = new byte[64 * 1024];

    // Connected CPU
    // private Cpu cpu; // Will be added later when Cpu is ready

    public Bus() {
        // Initialize RAM to 0
        for (int i = 0; i < ram.length; i++) {
            ram[i] = 0;
        }
    }

    /**
     * Read a byte from the bus.
     * @param addr The 16-bit address to read from.
     * @return The byte at the address.
     */
    public byte read(int addr) {
        // Mirroring and mapping logic will go here later.
        // For now, just read from the 64KB RAM array.
        if (addr >= 0 && addr < ram.length) {
            return ram[addr];
        }
        return 0x00;
    }

    /**
     * Write a byte to the bus.
     * @param addr The 16-bit address to write to.
     * @param data The byte to write.
     */
    public void write(int addr, byte data) {
        // Mirroring and mapping logic will go here later.
        if (addr >= 0 && addr < ram.length) {
            ram[addr] = data;
        }
    }
}

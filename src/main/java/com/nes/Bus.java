package com.nes;

import com.nes.cpu.Cpu;

/**
 * Represents the NES Bus.
 * Connects the CPU, PPU, and other components to memory.
 */
public class Bus {
    // Connected CPU
    private Cpu cpu;

    // 64KB RAM
    private final byte[] ram = new byte[64 * 1024];
    
    private Cartridge cartridge;
    private Ppu ppu = new Ppu();
    private Apu apu = new Apu();
    
    // Controllers
    private Controller[] controllers = new Controller[2];
    
    private long systemClockCounter = 0;

    public Bus() {
        // Initialize RAM to 0
        for (int i = 0; i < ram.length; i++) {
            ram[i] = 0;
        }
        
        // Initialize Controllers
        controllers[0] = new Controller();
        controllers[1] = new Controller();
    }
    
    public void connectCpu(Cpu cpu) {
        this.cpu = cpu;
        ppu.connectBus(this);
    }
    
    public void insertCartridge(Cartridge cartridge) {
        this.cartridge = cartridge;
        ppu.connectCartridge(cartridge);
    }
    
    public void reset() {
        if (cpu != null) cpu.reset();
        ppu.reset();
        systemClockCounter = 0;
    }
    
    public void nmi() {
        if (cpu != null) {
            cpu.nmi();
        }
    }
    
    public void clock() {
        // PPU runs 3 times faster than CPU
        ppu.clock();
        
        // CPU runs once every 3 system ticks
        if (systemClockCounter % 3 == 0) {
            if (cpu != null) {
                cpu.clock();
            }
        }
        
        systemClockCounter++;
    }

    /**
     * Read a byte from the bus.
     * @param addr The 16-bit address to read from.
     * @return The byte at the address.
     */
    public byte read(int addr) {
        // Cartridge Address Range (0x4020 - 0xFFFF)
        if (addr >= 0x8000 && addr <= 0xFFFF) {
            if (cartridge != null) {
                return cartridge.cpuRead(addr);
            }
            // Fallback for testing: read from RAM if no cartridge
            return ram[addr];
        }
        
        // RAM (0x0000 - 0x1FFF) - Mirrored every 2KB
        if (addr >= 0x0000 && addr <= 0x1FFF) {
            return ram[addr & 0x07FF];
        }

        // PPU Registers (0x2000 - 0x3FFF) - Mirrored every 8 bytes
        if (addr >= 0x2000 && addr <= 0x3FFF) {
            return ppu.cpuRead(addr & 0x2007);
        }
        
        // Controller 1 (0x4016)
        if (addr == 0x4016) {
            return controllers[0].cpuRead();
        }
        
        // Controller 2 (0x4017)
        if (addr == 0x4017) {
            return controllers[1].cpuRead();
        }
        
        // APU Registers (0x4000 - 0x4017)
        if (addr >= 0x4000 && addr <= 0x4017) {
            return apu.cpuRead(addr);
        }

        return 0x00;
    }

    /**
     * Write a byte to the bus.
     * @param addr The 16-bit address to write to.
     * @param data The byte to write.
     */
    public void write(int addr, byte data) {
        // Cartridge Address Range
        if (addr >= 0x8000 && addr <= 0xFFFF) {
            if (cartridge != null) {
                cartridge.cpuWrite(addr, data);
                return;
            }
            // Fallback for testing: write to RAM if no cartridge
            ram[addr] = data;
            return;
        }
        
        // RAM (0x0000 - 0x1FFF) - Mirrored every 2KB
        if (addr >= 0x0000 && addr <= 0x1FFF) {
            ram[addr & 0x07FF] = data;
            return;
        }
        
        // PPU Registers (0x2000 - 0x3FFF) - Mirrored every 8 bytes
        if (addr >= 0x2000 && addr <= 0x3FFF) {
            ppu.cpuWrite(addr & 0x2007, data);
            return;
        }
        
        // Controller Strobe (0x4016)
        // Writing to 0x4016 affects BOTH controllers
        if (addr == 0x4016) {
            controllers[0].cpuWrite(data);
            controllers[1].cpuWrite(data);
            return;
        }
        
        // APU Registers (0x4000 - 0x4017)
        if (addr >= 0x4000 && addr <= 0x4017) {
            apu.cpuWrite(addr, data);
            return;
        }
    }
    
    public int[] getFrameBuffer() {
        return ppu.getFrameBuffer();
    }
    
    public Ppu getPpu() {
        return ppu;
    }
    
    public Controller getController(int index) {
        return controllers[index];
    }
}

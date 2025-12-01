package com.nes;

import com.nes.cpu.Cpu;

public class Main {
    public static void main(String[] args) {
        System.out.println("NES Emulator Started");
        
        Bus bus = new Bus();
        Cpu cpu = new Cpu();
        
        cpu.connectBus(bus);
        
        // Hardcode reset vector for testing
        bus.write(0xFFFC, (byte)0x00);
        bus.write(0xFFFD, (byte)0x80);
        
        cpu.reset();
        
        System.out.println("CPU Reset Complete. PC: " + String.format("0x%04X", cpu.pc));
    }
}

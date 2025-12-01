package com.nes;

/**
 * NES Picture Processing Unit (PPU) - 2C02
 * Handles all graphics rendering for the NES.
 */
public class Ppu {
    
    // PPU Registers (CPU-accessible via 0x2000-0x2007)
    private byte ppuCtrl = 0x00;      // 0x2000 - Control register
    private byte ppuMask = 0x00;      // 0x2001 - Mask register
    private byte ppuStatus = 0x00;    // 0x2002 - Status register
    private byte oamAddr = 0x00;      // 0x2003 - OAM address
    
    // Internal registers
    private int vramAddr = 0x0000;    // Current VRAM address (15 bits)
    private int tempVramAddr = 0x0000; // Temporary VRAM address
    private byte fineX = 0x00;        // Fine X scroll (3 bits)
    private boolean writeToggle = false; // First/second write toggle for PPUSCROLL and PPUADDR
    
    // Data buffer for PPUDATA reads
    private byte dataBuffer = 0x00;
    
    // Memory
    private byte[] vram = new byte[2048];      // 2KB nametable memory
    private byte[] paletteRam = new byte[32];  // 32 bytes palette memory
    private byte[] oam = new byte[256];        // 256 bytes OAM (sprite memory)
    
    // Frame buffer (256x240 pixels, RGB format)
    private int[] frameBuffer = new int[256 * 240];
    
    // Background rendering state
    private int bgNextTileId = 0;
    private int bgNextTileAttrib = 0;
    private int bgNextTileLsb = 0;
    private int bgNextTileMsb = 0;
    private int bgShifterPatternLo = 0;
    private int bgShifterPatternHi = 0;
    private int bgShifterAttribLo = 0;
    private int bgShifterAttribHi = 0;
    
    // Timing
    private int scanline = 0;   // Current scanline (0-261)
    private int cycle = 0;      // Current cycle (0-340)
    private long frame = 0;     // Frame counter
    
    // Flags
    private boolean nmiOccurred = false;
    private boolean nmiOutput = false;
    
    // Reference to cartridge for CHR-ROM access
    private Cartridge cartridge;
    
    // Reference to bus for NMI triggering
    private Bus bus;
    
    // NES Color Palette (64 colors in RGB format)
    private static final int[] NES_PALETTE = {
        0x666666, 0x002A88, 0x1412A7, 0x3B00A4, 0x5C007E, 0x6E0040, 0x6C0600, 0x561D00,
        0x333500, 0x0B4800, 0x005200, 0x004F08, 0x00404D, 0x000000, 0x000000, 0x000000,
        0xADADAD, 0x155FD9, 0x4240FF, 0x7527FE, 0xA01ACC, 0xB71E7B, 0xB53120, 0x994E00,
        0x6B6D00, 0x388700, 0x0C9300, 0x008F32, 0x007C8D, 0x000000, 0x000000, 0x000000,
        0xFFFEFF, 0x64B0FF, 0x9290FF, 0xC676FF, 0xF36AFF, 0xFE6ECC, 0xFE8170, 0xEA9E22,
        0xBCBE00, 0x88D800, 0x5CE430, 0x45E082, 0x48CDDE, 0x4F4F4F, 0x000000, 0x000000,
        0xFFFEFF, 0xC0DFFF, 0xD3D2FF, 0xE8C8FF, 0xFBC2FF, 0xFEC4EA, 0xFECCC5, 0xF7D8A5,
        0xE4E594, 0xCFEF96, 0xBDF4AB, 0xB3F3CC, 0xB5EBF2, 0xB8B8B8, 0x000000, 0x000000
    };
    
    public void connectCartridge(Cartridge cartridge) {
        this.cartridge = cartridge;
    }
    
    public void connectBus(Bus bus) {
        this.bus = bus;
    }
    
    /**
     * Reset PPU to initial state
     */
    public void reset() {
        ppuCtrl = 0x00;
        ppuMask = 0x00;
        ppuStatus = 0x00;
        oamAddr = 0x00;
        
        vramAddr = 0x0000;
        tempVramAddr = 0x0000;
        fineX = 0x00;
        writeToggle = false;
        dataBuffer = 0x00;
        
        scanline = 0;
        cycle = 0;
        frame = 0;
        
        nmiOccurred = false;
        nmiOutput = false;
    }
    
    /**
     * CPU reads from PPU registers (0x2000-0x2007, mirrored)
     */
    public byte cpuRead(int addr) {
        byte data = 0x00;
        
        switch (addr & 0x0007) {
            case 0x0000: // PPUCTRL - Write only
                break;
                
            case 0x0001: // PPUMASK - Write only
                break;
                
            case 0x0002: // PPUSTATUS
                // Read status register
                data = (byte) ((ppuStatus & 0xE0) | (dataBuffer & 0x1F));
                
                // Clear VBlank flag
                ppuStatus &= ~0x80;
                
                // Reset write toggle
                writeToggle = false;
                break;
                
            case 0x0003: // OAMADDR - Write only
                break;
                
            case 0x0004: // OAMDATA
                data = oam[oamAddr & 0xFF];
                break;
                
            case 0x0005: // PPUSCROLL - Write only
                break;
                
            case 0x0006: // PPUADDR - Write only
                break;
                
            case 0x0007: // PPUDATA
                // Read from VRAM
                data = dataBuffer;
                dataBuffer = ppuRead(vramAddr);
                
                // Palette reads are not buffered
                if ((vramAddr & 0x3FFF) >= 0x3F00) {
                    data = dataBuffer;
                }
                
                // Increment VRAM address
                vramAddr += ((ppuCtrl & 0x04) != 0) ? 32 : 1;
                vramAddr &= 0x3FFF;
                break;
        }
        
        return data;
    }
    
    /**
     * CPU writes to PPU registers (0x2000-0x2007, mirrored)
     */
    public void cpuWrite(int addr, byte data) {
        switch (addr & 0x0007) {
            case 0x0000: // PPUCTRL
                ppuCtrl = data;
                
                // Update NMI output
                nmiOutput = (ppuCtrl & 0x80) != 0;
                
                // t: ...BA.. ........ = d: ......BA
                tempVramAddr = (tempVramAddr & 0xF3FF) | ((data & 0x03) << 10);
                break;
                
            case 0x0001: // PPUMASK
                ppuMask = data;
                break;
                
            case 0x0002: // PPUSTATUS - Read only
                break;
                
            case 0x0003: // OAMADDR
                oamAddr = data;
                break;
                
            case 0x0004: // OAMDATA
                oam[oamAddr & 0xFF] = data;
                oamAddr++;
                break;
                
            case 0x0005: // PPUSCROLL
                if (!writeToggle) {
                    // First write: X scroll
                    // t: ....... ...HGFED = d: HGFED...
                    // x:              CBA = d: .....CBA
                    tempVramAddr = (tempVramAddr & 0xFFE0) | ((data & 0xFF) >> 3);
                    fineX = (byte) (data & 0x07);
                    writeToggle = true;
                } else {
                    // Second write: Y scroll
                    // t: CBA..HG FED..... = d: HGFEDCBA
                    tempVramAddr = (tempVramAddr & 0x8FFF) | ((data & 0x07) << 12);
                    tempVramAddr = (tempVramAddr & 0xFC1F) | ((data & 0xF8) << 2);
                    writeToggle = false;
                }
                break;
                
            case 0x0006: // PPUADDR
                if (!writeToggle) {
                    // First write: High byte
                    // t: .FEDCBA ........ = d: ..FEDCBA
                    // t: X...... ........ = 0
                    tempVramAddr = (tempVramAddr & 0x80FF) | ((data & 0x3F) << 8);
                    writeToggle = true;
                } else {
                    // Second write: Low byte
                    // t: ....... HGFEDCBA = d: HGFEDCBA
                    // v                   = t
                    tempVramAddr = (tempVramAddr & 0xFF00) | (data & 0xFF);
                    vramAddr = tempVramAddr;
                    writeToggle = false;
                }
                break;
                
            case 0x0007: // PPUDATA
                // Write to VRAM
                ppuWrite(vramAddr, data);
                
                // Increment VRAM address
                vramAddr += ((ppuCtrl & 0x04) != 0) ? 32 : 1;
                vramAddr &= 0x3FFF;
                break;
        }
    }
    
    /**
     * PPU reads from its own address space
     */
    private byte ppuRead(int addr) {
        addr &= 0x3FFF;
        
        // Pattern tables (0x0000-0x1FFF) - CHR-ROM/RAM
        if (addr < 0x2000) {
            if (cartridge != null) {
                return cartridge.ppuRead(addr);
            }
            return 0x00;
        }
        // Nametables (0x2000-0x3EFF)
        else if (addr < 0x3F00) {
            addr &= 0x0FFF;
            
            // Apply mirroring
            if (cartridge != null) {
                int mirrorMode = cartridge.getMirrorMode();
                if (mirrorMode == 0) { // Horizontal
                    if (addr >= 0x0400 && addr < 0x0800) addr -= 0x0400;
                    if (addr >= 0x0C00) addr -= 0x0400;
                } else if (mirrorMode == 1) { // Vertical
                    if (addr >= 0x0800) addr -= 0x0800;
                }
            }
            
            return vram[addr & 0x07FF];
        }
        // Palette RAM (0x3F00-0x3FFF)
        else {
            addr &= 0x001F;
            
            // Mirroring: 0x3F10/14/18/1C mirror 0x3F00/04/08/0C
            if (addr == 0x0010) addr = 0x0000;
            if (addr == 0x0014) addr = 0x0004;
            if (addr == 0x0018) addr = 0x0008;
            if (addr == 0x001C) addr = 0x000C;
            
            return paletteRam[addr];
        }
    }
    
    /**
     * PPU writes to its own address space
     */
    private void ppuWrite(int addr, byte data) {
        addr &= 0x3FFF;
        
        // Pattern tables (0x0000-0x1FFF) - CHR-ROM/RAM
        if (addr < 0x2000) {
            if (cartridge != null) {
                cartridge.ppuWrite(addr, data);
            }
        }
        // Nametables (0x2000-0x3EFF)
        else if (addr < 0x3F00) {
            addr &= 0x0FFF;
            
            // Apply mirroring
            if (cartridge != null) {
                int mirrorMode = cartridge.getMirrorMode();
                if (mirrorMode == 0) { // Horizontal
                    if (addr >= 0x0400 && addr < 0x0800) addr -= 0x0400;
                    if (addr >= 0x0C00) addr -= 0x0400;
                } else if (mirrorMode == 1) { // Vertical
                    if (addr >= 0x0800) addr -= 0x0800;
                }
            }
            
            vram[addr & 0x07FF] = data;
        }
        // Palette RAM (0x3F00-0x3FFF)
        else {
            addr &= 0x001F;
            
            // Mirroring
            if (addr == 0x0010) addr = 0x0000;
            if (addr == 0x0014) addr = 0x0004;
            if (addr == 0x0018) addr = 0x0008;
            if (addr == 0x001C) addr = 0x000C;
            
            paletteRam[addr] = data;
        }
    }
    
    private void fetchNametableByte() {
        int addr = 0x2000 | (vramAddr & 0x0FFF);
        bgNextTileId = ppuRead(addr) & 0xFF;
    }
    
    private void fetchAttributeByte() {
        int addr = 0x23C0 | (vramAddr & 0x0C00) | ((vramAddr >> 4) & 0x38) | ((vramAddr >> 2) & 0x07);
        bgNextTileAttrib = ppuRead(addr) & 0xFF;
        if ((vramAddr & 0x0040) != 0) bgNextTileAttrib >>= 4;
        if ((vramAddr & 0x0002) != 0) bgNextTileAttrib >>= 2;
        bgNextTileAttrib &= 0x03;
    }
    
    private void fetchPatternLow() {
        int fineY = (vramAddr >> 12) & 0x07;
        int table = (ppuCtrl & 0x10) != 0 ? 0x1000 : 0x0000;
        int addr = table + (bgNextTileId << 4) + fineY;
        bgNextTileLsb = ppuRead(addr) & 0xFF;
    }
    
    private void fetchPatternHigh() {
        int fineY = (vramAddr >> 12) & 0x07;
        int table = (ppuCtrl & 0x10) != 0 ? 0x1000 : 0x0000;
        int addr = table + (bgNextTileId << 4) + fineY + 8;
        bgNextTileMsb = ppuRead(addr) & 0xFF;
    }
    
    private void incrementScrollX() {
        if ((ppuMask & 0x18) == 0) return;
        if ((vramAddr & 0x001F) == 31) {
            vramAddr &= ~0x001F;
            vramAddr ^= 0x0400;
        } else {
            vramAddr++;
        }
    }
    
    private void incrementScrollY() {
        if ((ppuMask & 0x18) == 0) return;
        if ((vramAddr & 0x7000) != 0x7000) {
            vramAddr += 0x1000;
        } else {
            vramAddr &= ~0x7000;
            int y = (vramAddr & 0x03E0) >> 5;
            if (y == 29) {
                y = 0;
                vramAddr ^= 0x0800;
            } else if (y == 31) {
                y = 0;
            } else {
                y++;
            }
            vramAddr = (vramAddr & ~0x03E0) | (y << 5);
        }
    }
    
    /**
     * Advance PPU by one cycle
     */
    public void clock() {
        if (scanline < 240 || scanline == 261) {
            if ((cycle >= 1 && cycle <= 256) || (cycle >= 321 && cycle <= 336)) {
                updateShifters();
                switch ((cycle - 1) % 8) {
                    case 0: loadBackgroundShifters(); fetchNametableByte(); break;
                    case 2: fetchAttributeByte(); break;
                    case 4: fetchPatternLow(); break;
                    case 6: fetchPatternHigh(); break;
                    case 7: incrementScrollX(); break;
                }
            }
            if (cycle == 256) incrementScrollY();
            if (cycle == 257 && (ppuMask & 0x18) != 0) vramAddr = (vramAddr & 0xFBE0) | (tempVramAddr & 0x041F);
            if (scanline == 261 && cycle >= 280 && cycle <= 304 && (ppuMask & 0x18) != 0) vramAddr = (vramAddr & 0x841F) | (tempVramAddr & 0x7BE0);
            if (scanline < 240 && cycle >= 1 && cycle <= 256) renderPixel();
        }
        if (scanline == 241 && cycle == 1) {
            ppuStatus |= 0x80;
            if (nmiOutput && bus != null) bus.nmi();
        }
        if (scanline == 261 && cycle == 1) ppuStatus &= ~0xE0;
        cycle++;
        if (cycle >= 341) {
            cycle = 0;
            scanline++;
            if (scanline >= 262) {
                scanline = 0;
                frame++;
            }
        }
    }
    
    // Getters for debugging
    public int getScanline() { return scanline; }
    public int getCycle() { return cycle; }
    public long getFrame() { return frame; }
    public int[] getFrameBuffer() { return frameBuffer; }
    
    /**
     * Get color from palette RAM
     */
    private int getColorFromPalette(int palette, int pixel) {
        int paletteIndex = (palette << 2) | pixel;
        int colorIndex = paletteRam[paletteIndex] & 0x3F;
        return NES_PALETTE[colorIndex];
    }
    
    /**
     * Render a single pixel to the frame buffer
     */
    private void renderPixel() {
        int x = cycle - 1;
        int y = scanline;
        
        if (x < 0 || x >= 256 || y < 0 || y >= 240) {
            return;
        }
        
        // Background pixel
        int bgPixel = 0;
        int bgPalette = 0;
        
        if ((ppuMask & 0x08) != 0) { // Show background
            int bitMux = 0x8000 >> fineX;
            
            int p0Pixel = (bgShifterPatternLo & bitMux) > 0 ? 1 : 0;
            int p1Pixel = (bgShifterPatternHi & bitMux) > 0 ? 1 : 0;
            bgPixel = (p1Pixel << 1) | p0Pixel;
            
            int bgPal0 = (bgShifterAttribLo & bitMux) > 0 ? 1 : 0;
            int bgPal1 = (bgShifterAttribHi & bitMux) > 0 ? 1 : 0;
            bgPalette = (bgPal1 << 1) | bgPal0;
        }
        
        // Get final color
        int color = getColorFromPalette(bgPalette, bgPixel);
        
        // Draw to frame buffer
        frameBuffer[y * 256 + x] = color;
    }
    
    /**
     * Load background shifters
     */
    private void loadBackgroundShifters() {
        bgShifterPatternLo = (bgShifterPatternLo & 0xFF00) | bgNextTileLsb;
        bgShifterPatternHi = (bgShifterPatternHi & 0xFF00) | bgNextTileMsb;
        
        bgShifterAttribLo = (bgShifterAttribLo & 0xFF00) | ((bgNextTileAttrib & 0x01) != 0 ? 0xFF : 0x00);
        bgShifterAttribHi = (bgShifterAttribHi & 0xFF00) | ((bgNextTileAttrib & 0x02) != 0 ? 0xFF : 0x00);
    }
    
    /**
     * Update background shifters
     */
    private void updateShifters() {
        if ((ppuMask & 0x08) != 0) { // Show background
            bgShifterPatternLo <<= 1;
            bgShifterPatternHi <<= 1;
            bgShifterAttribLo <<= 1;
            bgShifterAttribHi <<= 1;
        }
    }
}


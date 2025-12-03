package com.nes;

/**
 * Represents a standard NES Controller.
 * Handles input state, strobe mechanism, and serial reporting.
 */
public class Controller {

    // Button Bitmasks
    public static final int BUTTON_A      = 0x80;
    public static final int BUTTON_B      = 0x40;
    public static final int BUTTON_SELECT = 0x20;
    public static final int BUTTON_START  = 0x10;
    public static final int BUTTON_UP     = 0x08;
    public static final int BUTTON_DOWN   = 0x04;
    public static final int BUTTON_LEFT   = 0x02;
    public static final int BUTTON_RIGHT  = 0x01;

    // Current state of buttons (1 = Pressed)
    // Layout: [A] [B] [Select] [Start] [Up] [Down] [Left] [Right]
    private int controllerState = 0x00;
    
    // Latched state for serial reading
    private int controllerStateLatched = 0x00;
    
    // Strobe state
    // If strobe is high, state is continuously re-latched.
    private boolean strobe = false;

    public Controller() {
    }

    /**
     * Set the state of a specific button.
     * @param buttonMask The bitmask of the button (e.g., BUTTON_A)
     * @param pressed True if pressed, false otherwise
     */
    public void setButtonPressed(int buttonMask, boolean pressed) {
        if (pressed) {
            controllerState |= buttonMask;
        } else {
            controllerState &= ~buttonMask;
        }
    }

    /**
     * Write to the controller register (0x4016).
     * Controls the strobe mechanism.
     * @param data Byte written (only bit 0 matters)
     */
    public void cpuWrite(byte data) {
        // If bit 0 is set, strobe is high
        boolean newStrobe = (data & 0x01) != 0;
        
        // If strobe goes high, or is high, we latch the state
        // Actually, while strobe is high, the shift register is constantly reloaded with current state.
        // When strobe goes low, the state is "latched" and can be shifted out.
        
        if (strobe && !newStrobe) {
            // Falling edge of strobe: Latch state
            // But wait, standard behavior:
            // Strobe = 1: Output is always Button A (first bit)
            // Strobe = 0: Output shifts through buttons
            // So we just track strobe state.
        }
        
        strobe = newStrobe;
        
        if (strobe) {
            controllerStateLatched = controllerState;
        }
    }

    /**
     * Read from the controller register (0x4016/0x4017).
     * Returns the next bit of the button state.
     * @return Byte with bit 0 set if button is pressed.
     */
    public byte cpuRead() {
        byte data = 0;
        
        if (strobe) {
            // If strobe is high, we always return the state of button A
            // (and reload the latch, effectively)
            controllerStateLatched = controllerState;
            data = (byte) ((controllerStateLatched & BUTTON_A) != 0 ? 1 : 0);
        } else {
            // If strobe is low, we shift out the bits
            // The standard order is A, B, Select, Start, Up, Down, Left, Right.
            // My mask is A=0x80 (top bit). So I need to read MSB first.
            
            data = (byte) ((controllerStateLatched & 0x80) != 0 ? 1 : 0);
            
            // Shift the latch left to get the next button in MSB position
            controllerStateLatched <<= 1;
        }
        
        return data;
    }
}

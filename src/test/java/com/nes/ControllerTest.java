package com.nes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {

    private Controller controller;

    @BeforeEach
    public void setUp() {
        controller = new Controller();
    }

    @Test
    public void testButtonState() {
        // Set A button
        controller.setButtonPressed(Controller.BUTTON_A, true);
        
        // Strobe to latch
        controller.cpuWrite((byte) 1);
        controller.cpuWrite((byte) 0);
        
        // Read first bit (A)
        assertEquals(1, controller.cpuRead(), "Button A should be pressed");
        
        // Read next (B) - should be 0
        assertEquals(0, controller.cpuRead(), "Button B should be released");
    }

    @Test
    public void testSerialRead() {
        // Set A, Select, Up, Right
        controller.setButtonPressed(Controller.BUTTON_A, true);
        controller.setButtonPressed(Controller.BUTTON_SELECT, true);
        controller.setButtonPressed(Controller.BUTTON_UP, true);
        controller.setButtonPressed(Controller.BUTTON_RIGHT, true);
        
        // Latch
        controller.cpuWrite((byte) 1);
        controller.cpuWrite((byte) 0);
        
        // Expected sequence: A, B, Select, Start, Up, Down, Left, Right
        //                    1, 0, 1,      0,     1,  0,    0,    1
        
        assertEquals(1, controller.cpuRead(), "Read 1: A");
        assertEquals(0, controller.cpuRead(), "Read 2: B");
        assertEquals(1, controller.cpuRead(), "Read 3: Select");
        assertEquals(0, controller.cpuRead(), "Read 4: Start");
        assertEquals(1, controller.cpuRead(), "Read 5: Up");
        assertEquals(0, controller.cpuRead(), "Read 6: Down");
        assertEquals(0, controller.cpuRead(), "Read 7: Left");
        assertEquals(1, controller.cpuRead(), "Read 8: Right");
    }

    @Test
    public void testStrobeMode() {
        // While strobe is high, it should always return A
        controller.setButtonPressed(Controller.BUTTON_A, true);
        controller.setButtonPressed(Controller.BUTTON_B, true);
        
        controller.cpuWrite((byte) 1); // Strobe High
        
        assertEquals(1, controller.cpuRead(), "Should return A");
        assertEquals(1, controller.cpuRead(), "Should still return A");
        assertEquals(1, controller.cpuRead(), "Should still return A");
        
        controller.cpuWrite((byte) 0); // Strobe Low
        
        assertEquals(1, controller.cpuRead(), "Should return A (latched)");
        assertEquals(1, controller.cpuRead(), "Should return B");
    }
}

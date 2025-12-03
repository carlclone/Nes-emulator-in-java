# Controller Implementation Documentation

## Overview
The NES controller implementation allows users to interact with the emulator using a standard keyboard. It emulates the behavior of the standard NES 8-button controller (A, B, Select, Start, Up, Down, Left, Right).

## Architecture

### 1. Controller Class (`com.nes.Controller`)
The `Controller` class encapsulates the state and logic of a single NES controller.

- **State Management**: Uses an integer bitmask (`controllerState`) to track the current state of all 8 buttons (1 = Pressed, 0 = Released).
- **Strobe Mechanism**:
    - When the CPU writes `1` to `0x4016`, the controller enters "strobe mode". In this mode, the shift register is continuously reloaded with the current button state.
    - When the CPU writes `0` to `0x4016`, the current state is "latched" into the shift register (`controllerStateLatched`).
- **Serial Read**:
    - Each read from `0x4016` (Controller 1) or `0x4017` (Controller 2) returns the least significant bit (LSB) of the shift register.
    - After each read, the shift register is shifted to the right (or left, depending on implementation detail - in our case, we shift the latch to expose the next button).
    - The standard button order for reading is: A, B, Select, Start, Up, Down, Left, Right.

### 2. Bus Integration (`com.nes.Bus`)
The `Bus` class manages two instances of `Controller`:
- `controllers[0]` (Player 1)
- `controllers[1]` (Player 2)

**Memory Mapping:**
- **Write 0x4016**: Writes to both controllers (triggers strobe).
- **Read 0x4016**: Reads from Controller 1.
- **Read 0x4017**: Reads from Controller 2.

### 3. Input Handling (`com.nes.EmulatorWindow`)
The `EmulatorWindow` uses a `KeyListener` to capture keyboard events and update the `Controller` state directly.

**Key Mapping:**
| NES Button | Keyboard Key |
|------------|--------------|
| A          | X            |
| B          | Z            |
| Select     | A            |
| Start      | S            |
| Up         | Up Arrow     |
| Down       | Down Arrow   |
| Left       | Left Arrow   |
| Right      | Right Arrow  |

## Usage
The controller is automatically initialized when the `Bus` is created. No additional configuration is required. The `EmulatorWindow` must have focus to capture keyboard input.

## Future Improvements
- Support for USB gamepads (via a library like JInput or LWJGL).
- Configurable key mappings.
- Turbo button support.

package com.pi4j.usbbridge.mcp2221;

import java.util.Arrays;
import java.util.function.Consumer;

import com.pi4j.io.IO;
import com.pi4j.io.IOConfig;
import com.pi4j.io.IOType;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import org.hid4java.*;

import com.pi4j.usbbridge.DirectContextBase;

public class Mcp2221 extends DirectContextBase {
    final HidDevice device;
    final byte[] transferBuffer = new byte[64];
    final IO[] openIOs = new IO[4];

    static HidDevice findMcp2221() {
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        hidServicesSpecification.setAutoStart(false);
        HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);

        for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            if (hidDevice.getVendorId() == 0x04d8 && hidDevice.getProductId() == 0xdd) {
                return hidDevice;
            }
        }

        throw new IllegalStateException("MCP2221 Not found");
    }

    // TODO: Support multiple devices
    public Mcp2221() {
        device = findMcp2221();
        device.open();
    }

    void transfer(int commandCode, Consumer<byte[]>  input, Consumer<byte[]> output) {
        synchronized (lock) {
            Arrays.fill(transferBuffer, (byte) 0);
            transferBuffer[0] = (byte) commandCode;
            if (input != null) {
                input.accept(transferBuffer);
            }
            device.write(transferBuffer, 64, (byte) 0);
            device.read(transferBuffer);
            if (output != null) {
                output.accept(transferBuffer);
            }
        }
    }

    void send(int commandCode, Consumer<byte[]> input) {
        transfer(commandCode, input, null);
    }

    void receive(int commandCode, Consumer<byte[]> output) {
        transfer(commandCode, null, output);
    }

    void setGpioConfiguration(int pin, PinMode mode) {
        byte[] modes = new byte[4];
        receive(Command.GET_SRAM_SETTINGS,
                receivedBuffer -> {
                    System.arraycopy(receivedBuffer, 22, modes, 0, 4);
                });

        modes[pin] = (byte) mode.ordinal();

        send(Command.SET_SRAM_SETTINGS,
            sendBuffer -> {
                sendBuffer[7] = (byte) 0b1000_0000;
                System.arraycopy(modes, 0, sendBuffer, 8, 4);
            });
    }


    void setGpioDirection(int pin, GpioDirection direction) {
        send(Command.SET_GPIO_OUTPUT_VALUES, sendBuffer -> {
            sendBuffer[4 * pin + 4] = 1;
            sendBuffer[4 * pin + 5] = (byte) direction.ordinal();
        });
    }

    void setGpioValue(int pin, boolean value) {
        send(Command.SET_GPIO_OUTPUT_VALUES, sendBuffer -> {
            sendBuffer[4 * pin + 2] = 1;
            sendBuffer[4 * pin + 3] = value ? (byte) 1 : (byte) 0;
        });
    }


    @Override
    public I2C create(I2CConfig config) {
        return new Mcp2221I2C(this, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected IO createImpl(IOConfig ioConfig, IOType ioType) {
        return switch (ioType) {
            case I2C -> new Mcp2221I2C(this, (I2CConfig) ioConfig);
            case DIGITAL_OUTPUT -> new Mcp2221DigitalOutput(this, (DigitalOutputConfig) ioConfig);
            // TODO: Add Digital IO based on gpio methods.
            default -> throw new UnsupportedOperationException("Unsupported IO type: " + ioType);
        };
    }

    enum PinMode {
        GPIO, DEDICATED, ALT_0, ALT_1, ALT_2
    }

    enum GpioDirection {
        OUTPUT, INPUT
    }
}

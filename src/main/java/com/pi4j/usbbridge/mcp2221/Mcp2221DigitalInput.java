package com.pi4j.usbbridge.mcp2221;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;

public class Mcp2221DigitalInput extends DigitalInputBase {
    private final Mcp2221 bridge;
    private final int pin;

    public Mcp2221DigitalInput(Mcp2221 bridge, DigitalInputConfig config) {
        super(null, config);
        this.bridge = bridge;
        this.pin = config.getBcm();
        if (pin < 0 || pin > 3) {
            throw new IllegalArgumentException("Invalid pin number: " + pin);
        }
        if (bridge.openIOs[pin] != null) {
            throw new IllegalStateException("Pin " + pin + " is already in use");
        }
        bridge.setGpioConfiguration(pin, Mcp2221.PinMode.GPIO);
        bridge.setGpioDirection(pin, Mcp2221.GpioDirection.OUTPUT);
    }

    @Override
    public DigitalState state() {
        return null;
    }

    @Override
    public DigitalInput shutdownInternal(Context context) {
        super.shutdownInternal(context);
        bridge.openIOs[pin] = null;
        return this;
    }
}

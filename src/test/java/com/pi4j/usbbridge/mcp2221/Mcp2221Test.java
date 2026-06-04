package com.pi4j.usbbridge.mcp2221;

import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver;
import com.pi4j.io.SerialCircuitIO;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.util.Delay;

public class Mcp2221Test {

    static void main() {
        Mcp2221 bridge = new Mcp2221();

        SerialCircuitIO i2c = bridge.create(I2CConfig.newBuilder(bridge).bus(0).device(Bmx280Driver.ADDRESS_BME_280_PRIMARY).build());

        bridge.setGpioConfiguration(Mcp2221.PinMode.GPIO, Mcp2221.PinMode.GPIO, Mcp2221.PinMode.GPIO, Mcp2221.PinMode.GPIO);


        Bmx280Driver driver = new Bmx280Driver(i2c);
        System.out.println(driver.readMeasurement());

        bridge.setGpioDirection(0, Mcp2221.GpioDirection.OUTPUT);
        bridge.setGpioDirection(1, Mcp2221.GpioDirection.OUTPUT);
        bridge.setGpioDirection(2, Mcp2221.GpioDirection.OUTPUT);
        bridge.setGpioDirection(3, Mcp2221.GpioDirection.OUTPUT);

        Delay delay = new Delay();

        while (true) {
        bridge.setGpioValue(1, true);

        delay.setMillis(2000).materialize();
        bridge.setGpioValue(2, true);

        delay.setMillis(1000).materialize();
        bridge.setGpioValue(1, false);
        bridge.setGpioValue(2, false);
        bridge.setGpioValue(3, true);

        delay.setMillis(2000).materialize();
            bridge.setGpioValue(2, true);
            bridge.setGpioValue(3, false);

            delay.setMillis(1000).materialize();
            bridge.setGpioValue(2, false);
        }
    }
}

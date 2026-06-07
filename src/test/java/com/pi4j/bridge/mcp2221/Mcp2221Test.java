package com.pi4j.bridge.mcp2221;

import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver;
import com.pi4j.drivers.sensor.environment.scd4x.Scd4xDriver;
import com.pi4j.io.SerialCircuitIO;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.util.Delay;

public class Mcp2221Test {

    static void main() {
        Mcp2221 bridge = new Mcp2221();

        DigitalOutput red = bridge.create(DigitalOutputConfig.newBuilder(bridge).bcm(1).build());
        DigitalOutput yellow = bridge.create(DigitalOutputConfig.newBuilder(bridge).bcm(2).build());
        DigitalOutput green = bridge.create(DigitalOutputConfig.newBuilder(bridge).bcm(3).build());

        SerialCircuitIO bmx280i2c = bridge.create(I2CConfig.newBuilder(bridge).bus(0).device(Bmx280Driver.ADDRESS_BME_280_PRIMARY).build());
        Bmx280Driver bmx280Driver = new Bmx280Driver(bmx280i2c);
        System.out.println(bmx280Driver.readMeasurement());

        I2C scd4xI2c = bridge.create(I2CConfig.newBuilder(bridge).bus(0).device(Scd4xDriver.I2C_ADDRESS).build());
        Scd4xDriver scd4xDriver = new Scd4xDriver(scd4xI2c);
        System.out.println(scd4xDriver.readMeasurement());

        Delay delay = new Delay();

        while (true) {
            bridge.setGpioValue(1, true);
            red.setState(true);

            delay.setMillis(2000).materialize();

            yellow.setState(true);

            delay.setMillis(1000).materialize();
            red.setState(false);
            yellow.setState(false);
            green.setState(true);

            delay.setMillis(2000).materialize();

            bridge.setGpioValue(2, true);
            bridge.setGpioValue(3, false);

            delay.setMillis(1000).materialize();
            bridge.setGpioValue(2, false);
        }
    }
}

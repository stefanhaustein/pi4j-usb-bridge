package com.pi4j.usbbridge.mcp2221;

class Command {
    static final byte GET_GPIO_VALUES = (byte) 0x51;
    static final byte GET_SRAM_SETTINGS = (byte) 0x61;

    static final byte I2C_GET_DATA = (byte) 0x40;
    static final byte I2C_WRITE_DATA = (byte) 0x90;
    static final byte I2C_READ_DATA = (byte) 0x91;

    static final byte SET_GPIO_OUTPUT_VALUES = (byte) 0x50;
    static final byte SET_SRAM_SETTINGS = (byte) 0x60;

    static final byte STATUS_SET_PARAMETERS = 0x10;
}

package org.pi4j.usbbridge.mcp2221;

import com.pi4j.io.SerialCircuitIO;

class Mcp2221I2c implements SerialCircuitIO {
    private static final int I2C_CHUNK_SIZE = 60;

    private static final byte I2C_WRITE_DATA = (byte) 0x90;
    private static final byte I2C_READ_DATA = (byte) 0x91;
    private static final byte I2C_GET_DATA = (byte) 0x40;

    private final Mcp2221 bridge;
    private final int address;
    byte[] transferBuffer = new byte[64];

    Mcp2221I2c(Mcp2221 bridge, int address) {
        this.bridge = bridge;
        this.address = address;
    }

    @Override
    public void writeThenRead(byte[] writeBuffer, int writeOffset, int writeLength, int readDelayNanos, byte[] readBuffer, int readOffset, int readLength) {
        if (writeLength > 0) {
            if (writeLength > I2C_CHUNK_SIZE) {
                throw new IllegalArgumentException("TODO: Support > 60 byte write");
            }
            transferBuffer[0] = I2C_WRITE_DATA;
            transferBuffer[1] = (byte) writeLength;
            transferBuffer[2] = (byte) (writeLength >>> 8);
            transferBuffer[3] = (byte) ((address << 1) | 0);
            System.arraycopy(writeBuffer, writeOffset, transferBuffer, 4, writeLength);

            bridge.device.write(transferBuffer, 64, (byte) 0);
            bridge.device.read(transferBuffer);

            if (transferBuffer[0] != I2C_WRITE_DATA) {
                throw new IllegalStateException("Command echo failed; was: " + transferBuffer[0]);
            }
            if (transferBuffer[1] != 0) {
                throw new IllegalStateException("Unexpected response code " + transferBuffer[1]);
            }
        }

        if (readLength > 0) {
            transferBuffer[0] = I2C_READ_DATA;
            transferBuffer[1] = (byte) readLength;
            transferBuffer[2] = (byte) (readLength >>> 8);
            transferBuffer[3] = (byte) ((address << 1) | 1);

            bridge.device.write(transferBuffer, 64, (byte) 0);
            bridge.device.read(transferBuffer);

            if (transferBuffer[0] != I2C_READ_DATA) {
                throw new IllegalStateException("Command echo failed; was: " + transferBuffer[0]);
            }
            if (transferBuffer[1] != 0) {
                throw new IllegalStateException("Unexpected response code " + transferBuffer[1]);
            }

            int readCount = 0;
            while (readCount < readLength) {
                transferBuffer[0] = I2C_GET_DATA;
                bridge.device.write(transferBuffer, 64, (byte) 0);
                bridge.device.read(transferBuffer);
                if (transferBuffer[0] != I2C_GET_DATA) {
                    throw new IllegalStateException("Command echo failed; was: " + transferBuffer[0]);
                }
                if (transferBuffer[1] != 0) {
                    throw new IllegalStateException("Unexpected response code " + transferBuffer[1]);
                }
                int count = transferBuffer[3] & 0xff;
                if (count > 60) {
                    throw new IllegalStateException("Unexpected byte count " + count);
                }
                System.arraycopy(transferBuffer, 4, readBuffer, readOffset + readCount, count);
                readCount += count;
            }
        }
    }

    @Override
    public void close() throws Exception {

    }
}

package org.pi4j.usbbridge.mcp2221;

import java.util.Arrays;
import org.hid4java.*;
import com.pi4j.io.SerialCircuitIO;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Mcp2221 {
    final HidDevice device;

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
        System.out.println("Found I2C device: " + device);
        device.open();

        byte[] request = new byte[64];
        byte[] response = new byte[64];
        request[0] = 0x10;

        device.write(request, 64, (byte) 0);
        device.read(response);

        System.out.println("MCP2221 Configuration: " + Arrays.toString(response));
    }


    public SerialCircuitIO openI2c(int address) {
        return new Mcp2221I2c(this, address);
    }



/*

public static UsbAlternateInterface selectHid(UsbDevice device) {
        for (UsbInterface iface : device.getInterfaces()) {
            System.out.println("- Interface: " + iface);
            for (UsbAlternateInterface alt : iface.getAlternates()) {
                System.out.println("  - Alternate: " + alt + " class code: " + alt.getClassCode());
                if (alt.getClassCode() == 3) {
                    device.claimInterface(iface.getNumber());
                    device.selectAlternateSetting(iface.getNumber(), alt.getNumber());
                    return alt;
                }
            }
        }
        throw new IllegalArgumentException("No HID interface found");
    }


    public static void main(String[] args) {
        var device = Usb.findDevice(0x04d8, 0x00dd).orElseThrow();

        System.out.println("Device: " + device + " class code: " + device.getClassCode() + " interfaces: " + device.getInterfaces());

        device.open();
        UsbAlternateInterface hid = selectHid(device);

        for (UsbEndpoint endpoint : hid.getEndpoints()) {
            System.out.println("- endpoint: " + endpoint + " #" + endpoint.getNumber() + " dir: " + endpoint.getDirection()  + " packetSize: " + endpoint.getPacketSize() + " transferType: " + endpoint.getTransferType());
        }


        byte[] data = new byte[64];
        data[0] = 0x10;

        device.transferOut(3, data);
        byte[] result = device.transferIn(3);

        System.out.println("Transfer result: " + Arrays.toString(result));

    }
*/
}

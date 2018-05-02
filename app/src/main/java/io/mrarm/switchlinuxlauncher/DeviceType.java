package io.mrarm.switchlinuxlauncher;

import android.hardware.usb.UsbDevice;

public class DeviceType {

    public static boolean isDeviceRCM(UsbDevice dev) {
        return (dev.getVendorId() == 0x0955 && dev.getProductId() == 0x7321);
    }

    public static boolean isDeviceUBoot(UsbDevice dev) {
        return (dev.getVendorId() == 0x0955 && dev.getProductId() == 0x701a);
    }

    public static boolean isSupportedDevice(UsbDevice dev) {
        return isDeviceRCM(dev) || isDeviceUBoot(dev);
    }

}

package io.mrarm.switchlinuxlauncher;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import io.mrarm.switchlinuxlauncher.log.LogProxy;
import io.mrarm.switchlinuxlauncher.log.Logger;

public class ImxUsbLoader {

    private LogProxy log;
    private UsbDeviceConnection conn;
    private UsbInterface deviceInterface;

    public ImxUsbLoader(Logger logger, UsbDevice device,  UsbDeviceConnection conn) {
        log = new LogProxy(logger, "ImxUsbLoader");
        this.conn = conn;
        deviceInterface = device.getInterface(0);
    }

    public void claimInterface() {
        if (!conn.claimInterface(deviceInterface, true))
            throw new RuntimeException("Claiming in the interface failed");
    }

    public void releaseInterface() {
        conn.releaseInterface(deviceInterface);
    }

    public boolean load(String confPath) {
        boolean hidMode = deviceInterface.getInterfaceClass() == UsbConstants.USB_CLASS_HID;
        log.i("hid mode " + hidMode);
        return nativeLoad(log, conn.getFileDescriptor(), hidMode, confPath);
    }

    static {
        System.loadLibrary("switchlauncher");
    }

    private static native boolean nativeLoad(
            LogProxy log, int fd, boolean hidMode, String confPath);

}

package io.mrarm.switchlinuxlauncher;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

import io.mrarm.switchlinuxlauncher.log.LogProxy;
import io.mrarm.switchlinuxlauncher.log.Logger;

public class ShofEL2 {

    private static final int TIMEOUT = 2000;

    private LogProxy log;
    private UsbDevice device;
    private UsbDeviceConnection conn;
    private UsbInterface deviceInterface;
    private UsbEndpoint eIn;
    private UsbEndpoint eOut;

    public ShofEL2(Logger logger, UsbDevice device, UsbDeviceConnection conn) {
        this.log = new LogProxy(logger, "ShofEL2");
        this.device = device;
        this.conn = conn;
        deviceInterface = device.getInterface(0);
        if (!conn.claimInterface(deviceInterface, true))
            throw new RuntimeException("Claiming in the interface failed");
        eIn = deviceInterface.getEndpoint(0);
        eOut = deviceInterface.getEndpoint(1);
    }

    private void readInitMsg() {
        byte[] buf = new byte[0x10];
        int len = conn.bulkTransfer(eIn, buf, buf.length, 20);
        if (len >= 0)
            log.i("Init message: " + HexString.encode(buf, 0, len));
        else
            log.i("No init message");
    }

    private void sanityCheck(int srcBase, int dstBase) {
        byte[] buf = new byte[0x1000];
        int len = conn.controlTransfer(0x82, 0, 0, 0, buf, buf.length, 0);
        if (len != 0x1000)
            throw new RuntimeException("Read error");
        int curSrc = BinaryReader.readInt32(buf, 0xc);
        int curDst = BinaryReader.readInt32(buf, 0x14);
        if (curSrc != srcBase || curDst != dstBase)
            throw new RuntimeException("Sanity check failed (curSrc = " + curSrc +
                    ", curDst = " + curDst + ")");
    }

    public void run() {
        final int srcBase = 0x4000fc84;
        final int target = srcBase - 0xc - 2 * 4 - 2 * 4;
        final int dstBase = 0x40009000;
        final int overrideLen = target - dstBase;
        final int payloadBase = 0x40010000;

        readInitMsg();
        sanityCheck(srcBase, dstBase);
    }

}

package io.mrarm.switchlinuxlauncher;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.mrarm.switchlinuxlauncher.log.LogProxy;
import io.mrarm.switchlinuxlauncher.log.Logger;
import io.mrarm.switchlinuxlauncher.util.BinaryReader;
import io.mrarm.switchlinuxlauncher.util.BinaryWriter;
import io.mrarm.switchlinuxlauncher.util.HexString;

public class ShofEL2 {

    public static final String PAYLOAD_FILENAME = "cbfs.bin";
    public static final String COREBOOT_FILENAME = "coreboot.rom";


    private LogProxy log;
    private UsbDevice device;
    private UsbDeviceConnection conn;
    private UsbInterface deviceInterface;
    private UsbEndpoint eIn;
    private UsbEndpoint eOut;
    private File payloadDir;

    public ShofEL2(Logger logger, UsbDevice device, UsbDeviceConnection conn, File payloadDir) {
        this.log = new LogProxy(logger, "ShofEL2");
        this.device = device;
        this.conn = conn;
        this.payloadDir = payloadDir;
        deviceInterface = device.getInterface(0);
        eIn = deviceInterface.getEndpoint(0);
        eOut = deviceInterface.getEndpoint(1);
    }

    public void claimInterface() {
        if (!conn.claimInterface(deviceInterface, true))
            throw new RuntimeException("Claiming in the interface failed");
    }

    public void releaseInterface() {
        conn.releaseInterface(deviceInterface);
    }

    private void readAdditionalFile(String name, OutputStream toStream) throws IOException {
        InputStream is = new FileInputStream(new File(payloadDir, name));
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0)
            toStream.write(buf, 0, n);
        is.close();
    }

    private void write(byte[] data, int offset, int len) {
        int ret = conn.bulkTransfer(eOut, data, offset, len, 0);
        if (ret < len)
            throw new RuntimeException("Write failed (ret = " + ret + ", expected = " + len + ")");
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

    public void run() throws IOException {
        final int srcBase = 0x4000fc84;
        final int target = srcBase - 0xc - 2 * 4 - 2 * 4;
        final int dstBase = 0x40009000;
        final int overrideLen = target - dstBase;
        final int payloadBase = 0x40010000;

        // rom is in rcm_send_chip_id_and_version
        // unblock it
        readInitMsg();

        // now in rcm_recv_buf
        sanityCheck(srcBase, dstBase);

        // need to build payload buffer
        // write header
        {
            byte[] header = new byte[4 + 0x2a4];
            BinaryWriter.writeInt32(header, 0, 0x30008);
            write(header, 0, header.length);
        }
        // write payload
        final int xferLen = 0x1000;
        {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            byte[] buf = new byte[0x1a3a * 4];
            payload.write(buf);
            int entry = payloadBase + payload.size() + 4;
            entry |= 1;
            BinaryWriter.writeInt32(buf, 0, entry);
            payload.write(buf, 0, 4);
            readAdditionalFile(PAYLOAD_FILENAME, payload);
            byte[] payloadData = payload.toByteArray();
            for (int i = 0; i < payloadData.length; i += xferLen)
                write(payloadData, i, Math.min(xferLen, payloadData.length - i));
        }

        try {
            sanityCheck(srcBase, dstBase);
        } catch (RuntimeException e) {
            log.i("Throwing more data");
            byte[] data = new byte[xferLen];
            write(data, 0, data.length);
        }

        log.i("Performing hax...");
        nativeControlReadUnbounded(log, conn.getFileDescriptor(), overrideLen);

        byte[] buf = new byte[4096];
        while (true) {
            int len = conn.bulkTransfer(eIn, buf, buf.length, 0);
            if (len < 0)
                continue;
            String cmd = new String(buf, 0, len);
            cmd = cmd.trim();
            log.i("In: " + cmd);
            if (cmd.equals("CBFS")) {
                cbfs();
                log.i("You have been served");
                break;
            }
        }
    }

    private void cbfs() throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        readAdditionalFile(COREBOOT_FILENAME, dataStream);
        byte[] data = dataStream.toByteArray();
        if (data.length < 20 * 1024)
            throw new RuntimeException("Invalid coreboot.rom");
        byte[] inBuf = new byte[8];
        while (true) {
            int inLen = conn.bulkTransfer(eIn, inBuf, 8, 0);
            if (inLen < 8)
                throw new RuntimeException("Read error");
            int offset = BinaryReader.readInt32BE(inBuf, 0);
            int length = BinaryReader.readInt32BE(inBuf, 4);
            if (offset + length == 0)
                return;
            log.i("Sending 0x" + Integer.toString(length, 16) + " bytes @0x" +
                    Integer.toString(offset, 16));
            while (length > 0) {
                int l = length;
                if (l > 32 * 1024)
                    l = 32 * 1024;
                int ret = conn.bulkTransfer(eOut, data, offset, l, 0);
                if (ret < 0)
                    log.e("Transfer error = " + ret);
                offset += ret;
                length -= ret;
            }
        }
    }

    static {
        System.loadLibrary("switchlauncher");
    }

    private native static void nativeControlReadUnbounded(LogProxy log, int fd, int size);

}

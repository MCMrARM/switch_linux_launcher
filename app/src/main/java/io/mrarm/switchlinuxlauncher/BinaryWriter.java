package io.mrarm.switchlinuxlauncher;

public class BinaryWriter {

    public static void writeInt32(byte[] buf, int off, int i) {
        buf[off] = (byte) (i & 0xff);
        buf[off + 1] = (byte) ((i >> 8) & 0xff);
        buf[off + 2] = (byte) ((i >> 16) & 0xff);
        buf[off + 3] = (byte) ((i >> 24) & 0xff);
    }

}

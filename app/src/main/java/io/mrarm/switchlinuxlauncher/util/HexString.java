package io.mrarm.switchlinuxlauncher.util;

public class HexString {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String encode(byte[] buf, int off, int length) {
        StringBuffer str = new StringBuffer(length * 2);
        for (int i = off; i < off + length; i++) {
            byte b = buf[i];
            str.append(HEX_CHARS[(b >> 4) & 0xf]);
            str.append(HEX_CHARS[b & 0xf]);
        }
        return str.toString();
    }

}

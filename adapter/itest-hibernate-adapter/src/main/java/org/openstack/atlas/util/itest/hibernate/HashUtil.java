package org.openstack.atlas.util.itest.hibernate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashUtil {

    private static final char[] nibble2hexMap;

    static {
        nibble2hexMap = new char[]{'0', '1', '2', '3',
                    '4', '5', '6', '7',
                    '8', '9', 'a', 'b',
                    'c', 'd', 'e', 'f'
                };
    }

    public static byte[] sha1sum(byte[] data, Integer startIdxInc, Integer stopIdxExc)
            throws NoSuchAlgorithmException {
        byte[] out;
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        out = md.digest(data);
        if (startIdxInc != null && stopIdxExc != null) {
            out = Arrays.copyOfRange(out, startIdxInc, stopIdxExc);
        }
        return out;
    }

    public static byte[] sha1sum(byte[] data) throws NoSuchAlgorithmException {
        return sha1sum(data, null, null);
    }

    public static String sha1sumHex(byte[] data, Integer startIdxInc, Integer stopIdxExc) throws NoSuchAlgorithmException {
        String out;
        byte[] sum;
        sum = sha1sum(data, startIdxInc, stopIdxExc);
        out = bytes2hex(sum);
        return out;
    }

    public static String sha1sumHex(byte[] data) throws NoSuchAlgorithmException {
        return sha1sumHex(data, null, null);
    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte curByte = bytes[i];
            int val = (curByte < 0) ? (int) curByte + 256 : (int) curByte;
            int hi = (val >> 4) & 0x0f;
            int lo = val & 0x0f;
            sb.append(nibble2hexMap[hi]).append(nibble2hexMap[lo]);
        }
        return sb.toString();
    }
}

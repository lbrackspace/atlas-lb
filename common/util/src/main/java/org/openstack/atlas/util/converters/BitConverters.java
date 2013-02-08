package org.openstack.atlas.util.converters;

import java.util.HashMap;
import java.util.Map;

public class BitConverters {

    private static final byte[] hexmap = "0123456789abcdef".getBytes();

    public static String int16bit2hex(int i16) {
        String out;
        byte[] nibbles = new byte[4];
        int i;
        if (i16 < 0x0000 || i16 > 0xffff) {
            return null;
        }
        nibbles[0] = BitConverters.int2Nibble((i16 & 0xf000) >> 12);
        nibbles[1] = BitConverters.int2Nibble((i16 & 0x0f00) >> 8);
        nibbles[2] = BitConverters.int2Nibble((i16 & 0x00f0) >> 4);
        nibbles[3] = BitConverters.int2Nibble((i16 & 0x000f) >> 0);
        out = new String(nibbles);
        return out;
    }

    public static int hex16bit2int(String in) {
        int i;
        int last;
        int base;
        int v;
        byte[] hex;
        int out;

        out = 0;
        base = 1;
        if (in == null) {
            return -1;
        }
        hex = in.getBytes();
        if (hex.length > 4) {
            return -1;
        }
        last = hex.length - 1;
        for (i = last; i >= 0; i--) {
            v = BitConverters.nibble2Int(hex[i]);
            if (v == -1) {
                return -1;
            }
            out += v * base;
            base *= 16;
        }

        return out;
    }

    public static int nibble2Int(byte nibble) {
        int out;
        switch (nibble) {
            case '0':
                out = 0;
                break;
            case '1':
                out = 1;
                break;
            case '2':
                out = 2;
                break;
            case '3':
                out = 3;
                break;
            case '4':
                out = 4;
                break;
            case '5':
                out = 5;
                break;
            case '6':
                out = 6;
                break;
            case '7':
                out = 7;
                break;
            case '8':
                out = 8;
                break;
            case '9':
                out = 9;
                break;
            case 'a':
                out = 10;
                break;
            case 'b':
                out = 11;
                break;
            case 'c':
                out = 12;
                break;
            case 'd':
                out = 13;
                break;
            case 'e':
                out = 14;
                break;
            case 'f':
                out = 15;
                break;
            case 'A':
                out = 10;
                break;
            case 'B':
                out = 11;
                break;
            case 'C':
                out = 12;
                break;
            case 'D':
                out = 13;
                break;
            case 'E':
                out = 14;
                break;
            case 'F':
                out = 15;
                break;
            default:
                out = -1;
                break;
        }
        return out;
    }

    public static byte int2Nibble(int in) {
        return (in < 0 || in > 15) ? (byte) -1 : hexmap[in];
    }

    public static int ubyte2int(byte in) {
        return (in >= 0) ? (int) in : (int) in + 256;
    }

    public static byte int2ubyte(int in) {
        in &= 0xff;
        return (in < 128) ? (byte) in : (byte) (in - 256);
    }

    public static String byte2hex(byte ubyte) {
        byte[] nibbleBytes;
        String out;
        int num;
        int lo;
        int hi;
        num = ubyte2int(ubyte);
        lo = ubyte & 0x0f;
        hi = (ubyte & 0xf0) >> 4;
        nibbleBytes = new byte[]{int2Nibble(hi), int2Nibble(lo)};
        out = new String(nibbleBytes);
        return out;
    }

    // Only positive ints though. BigEndian
    public static byte[] uint2bytes(int in) {
        byte out[] = new byte[4];
        if (in < 0) {
            in = 0 - in;// Don't put up with sign bits;
        }

        out[3] = int2ubyte(in & 0xff);
        in >>= 8;
        out[2] = int2ubyte(in & 0xff);
        in >>= 8;
        out[1] = int2ubyte(in & 0xff);
        in >>= 8;
        out[0] = int2ubyte(in & 0xff);
        return out;

    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int i;
        int lo;
        int hi;
        for (i = 0; i < bytes.length; i++) {
            sb.append(byte2hex(bytes[i]));
        }
        return sb.toString();
    }
}

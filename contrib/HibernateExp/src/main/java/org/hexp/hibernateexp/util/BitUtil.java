package org.hexp.hibernateexp.util;

public class BitUtil {

    public static final String[] hexchars = "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f".split(",");

    public static int ubyte2int(byte in) {
        return (in > 0) ? (int) in : (int) in + 256;
    }

    public static byte int2ubyte(int in) {
        in &= 0xff;
        return (in < 128) ? (byte) in : (byte) (in - 256);
    }

    public static int nibble2int(byte nibble) {
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

    public static String byte2hex(byte ubyte) {
        String out;
        int num;
        int lo;
        int hi;
        num = ubyte2int(ubyte);
        lo = ubyte & 0x0f;
        hi = (ubyte & 0xf0) >> 4;
        out = String.format("%s%s", hexchars[hi], hexchars[lo]);
        return out;
    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb;
        int i;
        int lo;
        int hi;
        sb = new StringBuilder();
        for (i = 0; i < bytes.length; i++) {
            sb.append(byte2hex(bytes[i]));
        }
        return sb.toString();
    }

    public static byte[] bitOp(byte[] a, byte[] b, BitOp op, Integer start, Integer stop) {
        byte[] out;
        int i;
        if (start == null || stop == null) {
            start = 0;
            stop = a.length;
        }
        out = new byte[a.length];
        for (i = 0; i < a.length; i++) {
            out[i] = a[i];
        }

        for (i = start; i < stop; i++) {
            switch (op) {
                case AND:
                    out[i] &= b[i];
                    break;
                case OR:
                    out[i] |= b[i];
                    break;
                case XOR:
                    out[i] ^= b[i];
                    break;
                case INV:
                    out[i] ^= 0xff;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown bit Op");
            }
        }
        return out;
    }

    public static enum BitOp {

        AND, OR, XOR, INV
    };
}

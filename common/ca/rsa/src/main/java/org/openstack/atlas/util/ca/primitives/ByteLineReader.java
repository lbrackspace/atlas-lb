package org.openstack.atlas.util.ca.primitives;

public class ByteLineReader {

    public static final byte CR = 13;
    public static final byte LF = 10;
    private byte[] bytes;
    int bi;

    public ByteLineReader(byte[] bytes) {
        this.bi = 0;
        this.bytes = bytes;
    }

    public int bytesAvailable() {
        return bytes.length - bi;
    }

    public byte[] readLine(boolean chop) {
        byte[] out = null;
        int beg = bi;
        int end;
        int length;
        int i;
        if (bytesAvailable() <= 0) {
            return out;
        }
        for (i = bi; i < bytes.length; i++) {
            if (bytes[i] == LF) {
                i++;
                break;
            }
        }
        end = i;
        bi = i;
        length = end - beg;
        if (!chop) {
            return copyBytes(bytes, beg, end);
        }
        if (length == 0) {
            return copyBytes(bytes, beg, end);
        }
        if (length == 1) {
            if (bytes[end - 1] == CR || bytes[end - 1] == LF) {
                return copyBytes(bytes, beg, end - 1);
            }
            return copyBytes(bytes, beg, end);
        }
        if (length >= 2) {
            if (bytes[end - 1] == LF && bytes[end - 2] == CR) {
                return copyBytes(bytes, beg, end - 2);
            }
            if (bytes[end - 1] == LF) {
                return copyBytes(bytes, beg, end - 1);
            }
            return copyBytes(bytes, beg, end);
        }

        return out;
    }

    public byte[] readLine() {
        return readLine(false);
    }

    public static byte[] copyBytes(byte[] in, int beg, int end) {
        byte[] out = null;
        int i;
        int length = end - beg;
        out = new byte[length];
        for (i = 0; i < length; i++) {
            out[i] = in[beg + i];
        }
        return out;
    }

    public static byte[] copyBytes(byte[] in) {
        return copyBytes(in, 0, in.length);
    }

    public static boolean cmpBytes(byte[] a, byte[] b) {
        int i;
        if (a.length != b.length) {
            return false;
        }

        for (i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] appendLF(byte[] bytesIn){
        int i;
        byte[] bytesOut = new byte[bytesIn.length + 1];
        for(i=0;i<bytesIn.length;i++){
            bytesOut[i] = bytesIn[i];
        }
        bytesOut[i]=LF;
        return bytesOut;
    }
}

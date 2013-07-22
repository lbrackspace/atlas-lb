package org.rackspace.capman.tools.ca.primitives;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.rackspace.capman.tools.ca.StringUtils;
import org.rackspace.capman.tools.util.StaticHelpers;

public class ByteLineReader {

    private static final byte CR = 13;
    private static final byte LF = 10;
    private static final int PAGESIZE = 4096;
    private ByteArrayInputStream inStream;
    private int totalBytes;

    public ByteLineReader(byte[] bytes) {
        inStream = new ByteArrayInputStream(bytes);
        totalBytes = inStream.available();
    }

    public int getBytesRead() {
        return totalBytes - inStream.available();
    }

    public int bytesAvailable() {
        return inStream.available();
    }

    public byte[] readLine(boolean chop) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(PAGESIZE);
        while (inStream.available() > 0) {
            int ch = inStream.read();
            if (ch < 0) {
                break;
            }
            if (ch == CR) { // Skip Carriage Return Nonsense.
                continue;
            }
            if (ch == LF) {
                if (chop) {
                    break;
                } else {
                    outStream.write(ch);
                    break;
                }
            }
            outStream.write(ch);
        }
        byte[] line = outStream.toByteArray();
        return line;
    }

    public byte[] readLine() {
        return readLine(false);
    }

    public static boolean cmpBytes(byte[] a, byte[] b) {
        return Arrays.equals(a, b);
    }

    public static byte[] appendLF(byte[] bytesIn) {
        byte[] bytesOut = Arrays.copyOf(bytesIn, bytesIn.length + 1);
        bytesOut[bytesOut.length - 1] = LF;
        return bytesOut;
    }

    public static byte[] copyBytes(byte[] inBytes) {
        byte[] outBytes = Arrays.copyOf(inBytes, inBytes.length);
        return outBytes;
    }

    public static byte[] chopLine(byte[] lineIn) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(PAGESIZE);
        for (int i = 0; i < lineIn.length; i++) {
            int ch = StaticHelpers.ubyte2int(lineIn[i]);
            if (ch < 0) {
                break;
            }
            if (ch == CR) { // Skip Carriage Return Nonsense.
                continue;
            }
            if (ch == LF) {
                break; // We are chopping off the newline
            }
            outStream.write(ch);
        }
        byte[] line = outStream.toByteArray();
        return line;
    }

    public static byte[] trim(byte[] lineIn) {
        byte[] out;
        int hi;
        int ti;
        int outSize;
        String lineInStr = StringUtils.asciiString(lineIn);
        for (hi = 0; hi < lineIn.length; hi++) {
            if (!StaticHelpers.isByteWhiteSpace(lineIn[hi])) {
                break;
            }
        }
        for (ti = lineIn.length - 1; ti >= 0; ti--) {
            if (!StaticHelpers.isByteWhiteSpace(lineIn[ti])) {
                break;
            }
        }

        outSize = ti - hi + 1;
        if (outSize <= 0) {
            return new byte[0];
        }
        out = new byte[outSize];
        for (int i = 0; i < outSize; i++) {
            out[i] = lineIn[hi + i];
        }
        String outStr = StringUtils.asciiString(out);
        return out;
    }
}

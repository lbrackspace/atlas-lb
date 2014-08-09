package org.openstack.atlas.util.b64aes;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class Base64 {
    // static vars

    public static final int PAGESIZE = 4096;
    public static final int MAX_FILTER_ENTRY_SIZE = 4096;
    public static final int[] charToSix;
    public static final int[] sixToChar;
    private static String[] hexmap = {"0", "1", "2", "3",
        "4", "5", "6", "7",
        "8", "9", "A", "B",
        "C", "D", "E", "F"};
    // instance vars

    static {
        int ch;
        sixToChar = new int[64];
        charToSix = new int[256];
        for (int i = 0; i < 256; i++) {
            charToSix[i] = -1;
        }

        for (int i = 0; i < 64; i++) {
            sixToChar[i] = 46;
        }

        for (int i = 0; i < 64; i++) {
            if (i < 26) {
                ch = (int) ((byte) 'A' + i - 0);
            } else if (i < 52) {
                ch = (int) ((byte) 'a' + i - 26);
            } else if (i < 62) {
                ch = (int) ((byte) '0' + i - 52);
            } else if (i == 62) { // '+' = 43
                ch = 43;
            } else if (i == 63) { // '/' = 47
                ch = 47;
            } else {
                ch = -1;
            }
            sixToChar[i] = ch;
            charToSix[ch] = i;
        }

        charToSix[61] = 0; // Treat '=' as padding
    }

    // for jython
    public static String decode(String strIn) throws PaddingException, UnsupportedEncodingException {
        byte[] bytesIn = strIn.getBytes("utf-8");
        byte[] bytesOut = decode(bytesIn, bytesIn.length);
        return new String(bytesOut);
    }

    public static byte[] decode(byte[] unfilteredBytes, int unFilterLen) throws PaddingException {
        int[] map = charToSix;
        byte[] bi = filterPemBytes(unfilteredBytes, unFilterLen);
        byte[] bo;
        if (bi.length == 0) {
            return new byte[0];
        }

        int ilen = bi.length;
        int olen;
        int ibp = 0;
        int obp = 0;

        byte b0;
        byte b1;
        byte b2;
        byte b3;
        int ib0;
        int ib1;
        int ib2;
        int ib3;

        if (ilen % 4 != 0) {
            throw new PaddingException(String.format("b64 bytes was %d bytes long was expecting a multiple of 4", ilen));
        }
        olen = ((ilen / 4) - 1) * 3;
        // Compute length based on padding
        if (bi[ilen - 2] == '=') {
            olen += 1;
        } else if (bi[ilen - 1] == '=') {
            olen += 2;
        } else {
            olen += 3;
        }
        bo = new byte[olen];

        while (ilen - ibp > 4) { // Encode all but the last bytes
            b0 = bi[ibp + 0];
            b1 = bi[ibp + 1];
            b2 = bi[ibp + 2];
            b3 = bi[ibp + 3];
            ib0 = (b0 >= 0) ? (int) b0 : (int) b0 + 256;
            ib1 = (b1 >= 0) ? (int) b1 : (int) b1 + 256;
            ib2 = (b2 >= 0) ? (int) b2 : (int) b2 + 256;
            ib3 = (b3 >= 0) ? (int) b3 : (int) b3 + 256;

            bo[obp + 0] = (byte) (map[ib0] << 2 | map[ib1] >> 4);
            bo[obp + 1] = (byte) (map[ib1] << 4 & 0xf0 | map[ib2] >> 2);
            bo[obp + 2] = (byte) (map[ib2] << 6 & 0xc0 | map[ib3]);
            ibp += 4;
            obp += 3;
        }
        b0 = bi[ibp + 0];
        b1 = bi[ibp + 1];
        b2 = bi[ibp + 2];
        b3 = bi[ibp + 3];
        ib0 = (b0 >= 0) ? (int) b0 : (int) b0 + 256;
        ib1 = (b1 >= 0) ? (int) b1 : (int) b1 + 256;
        ib2 = (b2 >= 0) ? (int) b2 : (int) b2 + 256;
        ib3 = (b3 >= 0) ? (int) b3 : (int) b3 + 256;
        switch (olen % 3) {
            case 0:
                b0 = bi[ibp + 0];
                b1 = bi[ibp + 1];
                b2 = bi[ibp + 2];
                b3 = bi[ibp + 3];
                ib0 = (b0 >= 0) ? (int) b0 : (int) b0 + 256;
                ib1 = (b1 >= 0) ? (int) b1 : (int) b1 + 256;
                ib2 = (b2 >= 0) ? (int) b2 : (int) b2 + 256;
                ib3 = (b3 >= 0) ? (int) b3 : (int) b3 + 256;
                // No padding
                bo[obp + 0] = (byte) (map[ib0] << 2 | map[ib1] >> 4);
                bo[obp + 1] = (byte) (map[ib1] << 4 & 0xf0 | map[ib2] >> 2);
                bo[obp + 2] = (byte) (map[ib2] << 6 & 0xc0 | map[ib3]);
                break;
            case 1:
                // use '=' padding
                bo[obp + 0] = (byte) (map[ib0] << 2 | map[ib1] >> 4);
                break;
            case 2:
                // use '==' padding
                bo[obp + 0] = (byte) (map[ib0] << 2 | map[ib1] >> 4);
                bo[obp + 1] = (byte) (map[ib1] << 4 & 0xf0 | map[ib2] >> 2);
                break;
        }
        return bo;
    }

    public static byte[] filterPemBytes(byte[] bi, int blen) {
        int[] map = charToSix;
        int maxEntrySize = MAX_FILTER_ENTRY_SIZE;
        byte[] filteredInput;
        byte[] out;
        ByteBufferList bb = new ByteBufferList();
        List<ByteBuffer> entries = bb.getByteBufferEntries();
        byte[] entry;
        entry = new byte[maxEntrySize];
        int used = 0;

        for (int i = 0; i < blen; i++) {
            byte currByte = bi[i];
            int b = (currByte >= 0) ? (int) currByte : (int) currByte + 256;
            if (map[b] == -1) {
                continue;
            }
            if (used >= maxEntrySize) {
                entries.add(new ByteBuffer(entry, used));
                used = 0;
                entry = new byte[maxEntrySize];
            }
            entry[used] = (byte) b;
            used++;
        }
        if (used > 0) {
            entries.add(new ByteBuffer(entry, used));
        }
        filteredInput = bb.getAllBytes();
        return filteredInput;
    }

    public static String encode(String utf8Str) throws UnsupportedEncodingException {
        byte[] bytes = toUTF8Bytes(utf8Str);
        byte[] encoded = encode(bytes, bytes.length);
        return toUTF8String(encoded);
    }

    public static byte[] encode(byte[] bi, int ilen) {
        final int[] map = sixToChar;
        int olen = ((ilen + 2) / 3) * 4;
        byte[] bo = new byte[olen];
        byte currByte;
        int ibp = 0;
        int obp = 0;
        byte b0;
        byte b1;
        byte b2;
        int ib0;
        int ib1;
        int ib2;
        while (ibp < ilen) {
            switch (ilen - ibp) {
                case 1:
                    b0 = bi[ibp + 0];
                    ib0 = (b0 >= 0) ? (int) b0 : (int) b0 + 256;

                    bo[obp + 0] = (byte) map[ib0 >> 2];
                    bo[obp + 1] = (byte) map[ib0 << 4 & 0x30];
                    bo[obp + 2] = '=';
                    bo[obp + 3] = '=';
                    break;
                case 2:
                    b0 = bi[ibp + 0];
                    b1 = bi[ibp + 1];
                    ib0 = (b0 >= 0) ? (int) b0 : (int) b0 + 256;
                    ib1 = (b1 >= 0) ? (int) b1 : (int) b1 + 256;

                    bo[obp + 0] = (byte) map[ib0 >> 2];
                    bo[obp + 1] = (byte) map[ib0 << 4 & 0x30 | ib1 >> 4];
                    bo[obp + 2] = (byte) map[ib1 << 2 & 0x3c];
                    bo[obp + 3] = '=';
                    break;
                default:
                    b0 = bi[ibp + 0];
                    b1 = bi[ibp + 1];
                    b2 = bi[ibp + 2];
                    ib0 = (b0 >= 0) ? (int) b0 : (int) b0 + 256;
                    ib1 = (b1 >= 0) ? (int) b1 : (int) b1 + 256;
                    ib2 = (b2 >= 0) ? (int) b2 : (int) b2 + 256;

                    bo[obp + 0] = (byte) map[ib0 >> 2];
                    bo[obp + 1] = (byte) map[ib0 << 4 & 0x30 | ib1 >> 4];
                    bo[obp + 2] = (byte) map[ib1 << 2 & 0x3c | ib2 >> 6];
                    bo[obp + 3] = (byte) map[ib2 & 0x3f];
                    break;
            }
            ibp += 3;
            obp += 4;
        }
        return bo;
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(PAGESIZE);
        int bl = bytes.length;
        for (int i = 0; i < bl; i++) {
            int val = (int) bytes[i];
            if (val < 0) {
                val += 256;
            }
            sb.append(hexmap[val >> 4]);
            sb.append(hexmap[val & 0x0f]);
        }
        return sb.toString();
    }

    // For jython
    public static byte[] toUTF8Bytes(String strIn) throws UnsupportedEncodingException {
        byte[] out = strIn.getBytes("utf-8");
        return out;
    }

    public static String toUTF8String(byte[] bytes) throws UnsupportedEncodingException {
        String out = new String(bytes, "utf-8");
        return out;

    }

    private static int utoi(byte in) {
        return (in >= 0) ? (int) in : (int) in + 256;
    }

    private static byte itou(int in) {
        in &= 0xff;
        return (in < 128) ? (byte) in : (byte) (in - 256);
    }
}

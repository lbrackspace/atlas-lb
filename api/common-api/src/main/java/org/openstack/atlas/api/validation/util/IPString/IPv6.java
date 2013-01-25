package org.openstack.atlas.api.validation.util.IPString;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IPv6 {

    private String ippatternstr;
    private String ip;
    private static final Pattern ipPattern;

    static {
        String ippatternstr = "^(.*::)([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})$|"
                + "^(.*):([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})$";

        ipPattern = Pattern.compile(ippatternstr);
    }

    public static int[] splitvals(String ipStr) {
        int i;
        int length;
        int val;
        String[] words;
        int[] out;
        if (ipStr == null) {
            return null;
        }
        words = ipStr.split(":", -1);
        length = words.length;
        if (length <= 0) {
            return null;
        }
        out = new int[length];
        for (i = 0; i < length; i++) {
            if (words[i].length() == 0) {
                out[i] = -1;
                continue;
            }
            val = IPUtils.hex16bit2int(words[i]);
            if (val == -1) {
                return null;
            }
            out[i] = val;
        }

        return out;
    }

    public static int countnegatives(int[] in) {
        int i;
        int out = 0;
        for (i = 0; i < in.length; i++) {
            if (in[i] < 0) {
                out += 1;
            }
        }
        return out;
    }

    public static String expand(String ipStr, int nwords) throws IPStringConversionException {
        StringBuilder sb;
        String hex;
        int[] vals;
        int[] expanded_vals;
        int i;
        sb = new StringBuilder();
        vals = splitvals(ipStr);
        if (vals == null) {
            throw new IPStringConversionException("Error converting hex to binary in IPv6 ip");
        }
        expanded_vals = expand(vals, nwords);
        for (i = 0; i < nwords - 1; i++) {
            hex = String.format("%s:", IPUtils.int16bit2hex(expanded_vals[i]));
            sb.append(hex);
        }
        hex = String.format("%s", IPUtils.int16bit2hex(expanded_vals[nwords - 1]));
        sb.append(hex);
        return sb.toString();
    }

    public String expand() throws IPStringConversionException {
        return bytes2IpString(IpString2bytes(ip)); // Silly but just used the inversion to force expansion
    }

    public static int[] expand(int[] vals, int nwords) throws IPStringConversionException {
        int[] out;
        int i;
        int j;
        int nvals;
        int negatives;

        if (vals.length < 3 || vals.length > nwords) {
            throw new IPStringConversionException("Invalid Number of 16bit words in IPv6 address");
        }

        nvals = vals.length;
        negatives = countnegatives(vals);

        out = new int[nwords];
        for (i = 0; i < nwords; i++) {
            out[i] = 0;
        }

        if (negatives > 3) {
            throw new IPStringConversionException("Invalid IPv6 Zero compression");
        }

        if (negatives == 3 && nvals == 3) {
            return out; // All Zero compression;
        }

        if (negatives == 1 && (vals[0] != -1 && vals[nvals - 1] != -1)) { // Middle Compression
            // Do left expand
            j = nvals - 1;
            i = nwords - 1;
            while (vals[j] != -1) {
                out[i] = vals[j];
                i--;
                j--;
            }

            // Then do right Expand
            j = 0;
            i = 0;
            while (vals[i] != -1) {
                out[i] = vals[j];
                i++;
                j++;
            }
            return out;
        }

        if (negatives == 2 && vals[0] == -1 && vals[1] == -1) { // Left Compression
            j = nvals - 1;
            i = nwords - 1;
            while (vals[j] != -1) {
                out[i] = vals[j];
                i--;
                j--;
            }
            return out;
        }

        if (negatives == 2 && vals[nvals - 1] == -1 && vals[nvals - 2] == -1) { // Right Compression
            j = 0;
            i = 0;
            while (vals[i] != -1) {
                out[i] = vals[j];
                i++;
                j++;
            }
            return out;
        }
        if (negatives == 0 && nvals == nwords) { // No Compression Straight copy;
            for (i = 0; i < nwords; i++) {
                out[i] = vals[i];
            }
            return out;
        }

        throw new IPStringConversionException("Invapid IPv6 ip");
    }

    public static byte[] IpString2bytes(String ipStr) throws IPStringConversionException {
        int i;
        int j;
        int val;
        byte[] out;
        String expanded_ipStr;

        Matcher ipMatch = ipPattern.matcher(ipStr);
        if (ipMatch.find()) { // Found a RFC4291 2.2.3 ipv4 mixed address
            String hex_part = ipMatch.group(1);
            String ip4_part = ipMatch.group(2);
            if (hex_part == null && ip4_part == null) {
                hex_part = ipMatch.group(3);
                ip4_part = ipMatch.group(4);
            }
            IPv4 ipv4 = new IPv4(ip4_part);
            byte[] ipv4_bytes = ipv4.getBytes();
            byte[] hex_bytes = hex_part.getBytes();
            int last = hex_bytes.length - 1;
            int hi_word = (IPUtils.ubyte2int(ipv4_bytes[0]) << 8) + (IPUtils.ubyte2int(ipv4_bytes[1]));
            int lo_word = (IPUtils.ubyte2int(ipv4_bytes[2]) << 8) + (IPUtils.ubyte2int(ipv4_bytes[3]));
            String hi_str = IPUtils.int16bit2hex(hi_word);
            String lo_str = IPUtils.int16bit2hex(lo_word);
            expanded_ipStr = expand(hex_part, 6) + String.format(":%s:%s", hi_str, lo_str);

        } else {
            String hex_part = ipStr;
            expanded_ipStr = expand(hex_part, 8);
        }
        i = 0;
        out = new byte[16];
        for (String word : expanded_ipStr.split(":")) {
            val = IPUtils.hex16bit2int(word);
            out[i] = IPUtils.int2ubyte((val & 0xff00) >> 8);
            out[i + 1] = IPUtils.int2ubyte((val & 0x00ff));
            i += 2;
        }
        return out;
    }

    public static String bytes2IpString(byte[] in) throws IPStringConversionException {
        int i;
        int hi;
        int lo;
        StringBuilder sb;
        String hex;

        if (in.length != 16) {
            String msg = "Error IPv6 requires byte array of length 16";
            throw new IPStringConversionException(msg);
        }
        sb = new StringBuilder();
        for (i = 0; i < 14; i += 2) {
            hi = IPUtils.ubyte2int(in[i]) << 8;
            lo = IPUtils.ubyte2int(in[i + 1]);
            hex = String.format("%s:", IPUtils.int16bit2hex(hi | lo));
            sb.append(hex);
        }
        hi = IPUtils.ubyte2int(in[14]) << 8;
        lo = IPUtils.ubyte2int(in[15]);
        sb.append(String.format("%s", IPUtils.int16bit2hex(hi | lo)));
        return sb.toString();
    }

    public IPv6() {
    }

    public IPv6(String ip) {
        this.ip = ip;
    }

    public IPv6(byte[] in) throws IPStringConversionException {
        ip = bytes2IpString(in);
    }

    public void setIp(byte[] in) throws IPStringConversionException {
        ip = bytes2IpString(in);
    }

    public String getString() {
        return ip;
    }

    public byte[] getBytes() throws IPStringConversionException {
        return IpString2bytes(this.ip);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}

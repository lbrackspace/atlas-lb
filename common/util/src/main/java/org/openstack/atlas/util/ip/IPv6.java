package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.converters.BitConverters;
import org.openstack.atlas.util.crypto.HashUtil;
import org.openstack.atlas.util.ip.exception.AccountUnHashableException;
import org.openstack.atlas.util.ip.exception.IPBigIntegerConversionException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IPv6 implements Comparable<IPv6> {

    private String ip;
    private static final BigInteger maxIp;
    private static final Pattern ipPattern;
    private static final BigInteger byte255;
    private static final BigInteger max32bit;
    private static final BigInteger vipOctetMask;

    static {
        String ippatternstr = "^(.*::)([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})$|"
                + "^(.*):([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})$";
        ipPattern = Pattern.compile(ippatternstr);
        maxIp = new BigInteger("340282366920938463463374607431768211455");
        byte255 = new BigInteger("255");
        max32bit = new BigInteger("2147483648");
        vipOctetMask = new BigInteger("18446744073709551615");
    }

    private static int[] splitvals(String ipStr) {
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
            val = BitConverters.hex16bit2int(words[i]);
            if (val == -1) {
                return null;
            }
            out[i] = val;
        }

        return out;
    }

    private static int countnegatives(int[] in) {
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
        StringBuilder sb = new StringBuilder();
        String hex;
        int[] vals;
        int[] expanded_vals;
        int i;
        vals = splitvals(ipStr);
        if (vals == null) {
            throw new IPStringConversionException("Error converting hex to binary in IPv6 ip");
        }
        expanded_vals = expand(vals, nwords);
        for (i = 0; i < nwords - 1; i++) {
            hex = String.format("%s:", BitConverters.int16bit2hex(expanded_vals[i]));
            sb.append(hex);
        }
        hex = String.format("%s", BitConverters.int16bit2hex(expanded_vals[nwords - 1]));
        sb.append(hex);
        return sb.toString();
    }

    public String expand() throws IPStringConversionException {
        return bytes2IpString(IpString2bytes(ip)); // Silly but just used the inversion to force expansion
    }

    private static int[] expand(int[] vals, int nwords) throws IPStringConversionException {
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

        throw new IPStringConversionException("Invalid IPv6 ip");
    }

    public BigInteger toBigInteger() throws IPStringConversionException {
        BigInteger out;
        out = new BigInteger(1, this.getBytes());
        return out;
    }

    public IPv6(BigInteger in) throws IPBigIntegerConversionException {
        this.ip = bigInteger2IPv6(in).getString();
    }

    public static IPv6 bigInteger2IPv6(BigInteger in) throws IPBigIntegerConversionException {
        IPv6 out;
        int byteInt;
        int i;
        int j;
        byte[] bytesOut = new byte[16];
        BigInteger bigInt = new BigInteger(in.toByteArray());
        if (bigInt.compareTo(BigInteger.ZERO) == -1 || bigInt.compareTo(maxIp) == 1) {
            throw new IPBigIntegerConversionException("Big Integer out of IPv6 Range");
        }

        for (i = 15; i >= 0; i--) {
            byteInt = bigInt.and(byte255).intValue();
            bytesOut[i] = BitConverters.int2ubyte(byteInt);
            bigInt = bigInt.shiftRight(8);
        }
        try {
            out = new IPv6(bytesOut);
        } catch (IPStringConversionException ex) {
            throw new IPBigIntegerConversionException("Impossible Exception Conditions where pre checked");
        }

        return out;
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
            int hi_word = (BitConverters.ubyte2int(ipv4_bytes[0]) << 8) + (BitConverters.ubyte2int(ipv4_bytes[1]));
            int lo_word = (BitConverters.ubyte2int(ipv4_bytes[2]) << 8) + (BitConverters.ubyte2int(ipv4_bytes[3]));
            String hi_str = BitConverters.int16bit2hex(hi_word);
            String lo_str = BitConverters.int16bit2hex(lo_word);
            expanded_ipStr = expand(hex_part, 6) + String.format(":%s:%s", hi_str, lo_str);

        } else {
            String hex_part = ipStr;
            expanded_ipStr = expand(hex_part, 8);
        }
        i = 0;
        out = new byte[16];
        for (String word : expanded_ipStr.split(":")) {
            val = BitConverters.hex16bit2int(word);
            out[i] = BitConverters.int2ubyte((val & 0xff00) >> 8);
            out[i + 1] = BitConverters.int2ubyte((val & 0x00ff));
            i += 2;
        }
        return out;
    }

    public static String bytes2IpString(byte[] in) throws IPStringConversionException {
        int i;
        int hi;
        int lo;
        StringBuilder sb = new StringBuilder();
        String hex;

        if (in.length != 16) {
            String msg = "Error IPv6 requires byte array of length 16";
            throw new IPStringConversionException(msg);
        }
        for (i = 0; i < 14; i += 2) {
            hi = BitConverters.ubyte2int(in[i]) << 8;
            lo = BitConverters.ubyte2int(in[i + 1]);
            hex = String.format("%s:", BitConverters.int16bit2hex(hi | lo));
            sb.append(hex);
        }
        hi = BitConverters.ubyte2int(in[14]) << 8;
        lo = BitConverters.ubyte2int(in[15]);
        sb.append(String.format("%s", BitConverters.int16bit2hex(hi | lo)));
        return sb.toString();
    }

    public IPv6() {
    }

    public IPv6(String ip) {
        this.ip = ip;
    }

    public IPv6(byte[] in) throws IPStringConversionException {
        this.ip = bytes2IpString(in);
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

    public void insertInteger(int in, int startBit) throws IPStringConversionException {
        int size = 32;
        byte[] intBytes;
        BigInteger bigInt;
        intBytes = BitConverters.uint2bytes(in);
        bigInt = new BigInteger(1, intBytes);
        this.insertBigInteger(bigInt, startBit, size);
    }

    public void insertBigInteger(BigInteger in, int startBit, int size) throws IPStringConversionException {
        BigInteger bigInt;
        BigInteger thisIpInt;
        BigInteger mask;
        BigInteger invMask;
        bigInt = in.or(BigInteger.ZERO);
        thisIpInt = this.toBigInteger();
        mask = maxIp.shiftRight(128 - size);
        mask = mask.shiftLeft(128 - startBit - size);
        invMask = mask.xor(maxIp);
        bigInt = bigInt.shiftLeft(128 - startBit - size);
        thisIpInt = thisIpInt.and(invMask);
        bigInt = bigInt.and(mask);
        thisIpInt = thisIpInt.or(bigInt);
        try {
            this.ip = new IPv6(thisIpInt).expand();

        } catch (IPBigIntegerConversionException ex) {
            throw new IPStringConversionException("Impossible exception conditions where already checked");

        }
    }

    public void setAccountPartition(int accountId) throws IPStringConversionException {
        byte[] accountBytes = String.format("%s", accountId).getBytes();
        byte[] accountSha1;
        try {
            accountSha1 = HashUtil.sha1sum(accountBytes, 0, 4);
        } catch (NoSuchAlgorithmException ex) {
            throw new AccountUnHashableException("Account was not sha1 Sum Hashable", ex);
        }
        BigInteger sha1Int = new BigInteger(1, accountSha1);
        this.insertBigInteger(sha1Int, 64, 32);
    }

    public void setVipOctets(int lo) throws IPStringConversionException {
        this.insertInteger(lo, 96);
    }

    public int getVipOctets() throws IPStringConversionException {
        Integer out;
        BigInteger bigInt = this.toBigInteger();
        bigInt = bigInt.and(vipOctetMask);
        out = bigInt.intValue();
        if (out < 0) {
            throw new IPStringConversionException("Negative vipOctet found");
        }
        return out;
    }

    public void setClusterPartition(IPv6Cidr clusterCidr) throws IPStringConversionException {
        String newIp;
        BigInteger ipDelta = this.toBigInteger();
        BigInteger mask = new BigInteger(1, clusterCidr.getMaskBytes());
        BigInteger cluster = new BigInteger(1, clusterCidr.getIpBytes());
        BigInteger invMask = mask.xor(maxIp);
        ipDelta = ipDelta.and(invMask);
        cluster = cluster.and(mask);
        ipDelta = ipDelta.or(cluster);
        try {
            this.ip = new IPv6(ipDelta).getString();
        } catch (IPBigIntegerConversionException ex) {
            throw new IPStringConversionException("Unknown failure converting string. BigNum should have already been trimmed");
        }

    }

    @Override
    public String toString() {
        String out;

        try {
            return this.expand();
        } catch (IPStringConversionException ex) {
            return String.format("{%s:%s}", ex.getClass().getName(), ex.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;

        }
        final IPv6 that = (IPv6) obj;

        String thisIp;
        String thatIp;
        try {
            thisIp = this.ip == null ? null : this.expand();
        } catch (IPStringConversionException ex) {
            thisIp = this.ip;
        }
        try {
            thatIp = that.ip == null ? null : that.expand();
        } catch (IPStringConversionException ex) {
            thatIp = that.ip;
        }

        if ((thisIp == null) ? (thatIp != null) : !thisIp.equals(thatIp)) {
            return false;

        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        String thisIp;
        try {
            thisIp = this.ip == null ? null : this.expand();

        } catch (IPStringConversionException ex) {
            thisIp = this.ip;
        }
        hash = 97 * hash + (this.ip != null ? this.ip.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(IPv6 that) {
        BigInteger thisInt;
        BigInteger thatInt;
        int out;
        try {
            thisInt = this.toBigInteger();
            thatInt = that.toBigInteger();
        } catch (IPStringConversionException ex) {
            throw new IllegalArgumentException("One IP address was invalid");
        }
        out = thisInt.compareTo(thatInt);
        return out;
    }
}

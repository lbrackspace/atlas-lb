package org.openstack.atlas.util.ip;


import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.openstack.atlas.util.converters.BitConverters;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;

public class IPUtils {   
    private static final Pattern subnetPattern = Pattern.compile("^(.*)/(.*)$");
    private static Random rnd = new Random();

    public static boolean isValidIpv6String(String in) {
        IPv6 ip = new IPv6(in);
        try {
            ip.getBytes();
        } catch (IPStringConversionException ex) {
            return false;
        }
        return true;
    }

    public static final boolean isValidIpv4Subnet(String in) {
        String ip;
        String subnet;
        Matcher ipMatcher;
        if (in == null) {
            return false;
        }

        ipMatcher = subnetPattern.matcher(in);
        int subnetint;
        if (ipMatcher.find()) {
            ip = ipMatcher.group(1);
            subnet = ipMatcher.group(2);
            try {
                subnetint = Integer.parseInt(subnet);
                if (subnetint < 0 || subnetint > 32 || !isValidIpv4String(ip)) {
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }

        }
        return false;
    }

    public static boolean isValidIpv6Subnet(String in) {
        String ip;
        String subnet;
        Matcher ipMatcher;

        if (in == null) {
            return false;
        }
        ipMatcher = subnetPattern.matcher(in);
        int subnetint;
        if (ipMatcher.find()) {
            ip = ipMatcher.group(1);
            subnet = ipMatcher.group(2);
            try {
                subnetint = Integer.parseInt(subnet);
                if (subnetint < 0 || subnetint > 128 || !isValidIpv6String(ip)) {
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isValidIpv4String(String in) {
        IPv4 ip = new IPv4(in);
        try {
            ip.getBytes();
        } catch (IPStringConversionException ex) {
            return false;
        }
        return true;
    }


    public static byte[] invBytes(byte[] mask) {
        int i;
        byte[] out = new byte[mask.length];
        for (i = 0; i < mask.length; i++) {
            out[i] = BitConverters.int2ubyte((~mask[i]) & 0xff);
        }
        return out;
    }

    public static byte[] opBytes(byte[] a1, byte[] a2, ByteStreamOperation bop) throws IpTypeMissMatchException, IPStringConversionException {
        byte[] out;
        int i;
        if (a1.length != a2.length) {
            throw new IpTypeMissMatchException("Incompatible IP Operation");
        }
        if (bop == null) {
            throw new IPStringConversionException("No ByteStrteamOperation defined");
        }
        out = new byte[a1.length];
        for (i = 0; i < a1.length; i++) {
            switch (bop) {
                case AND:
                    out[i] = BitConverters.int2ubyte(BitConverters.ubyte2int(a1[i]) & BitConverters.ubyte2int(a2[i]));
                    break;
                case OR:
                    out[i] = BitConverters.int2ubyte(BitConverters.ubyte2int(a1[i]) | BitConverters.ubyte2int(a2[i]));
                    break;
                case XOR:
                    out[i] = BitConverters.int2ubyte(BitConverters.ubyte2int(a1[i]) ^ BitConverters.ubyte2int(a2[i]));
                    break;
            }
        }
        return out;
    }

    public static byte[] rollMask(int mask, int addrSize) {
        byte[] out = new byte[addrSize];
        int i;
        int bit_i;
        int byte_i;
        for (i = 0; i < addrSize * 8; i++) {
            byte_i = i / 8;
            bit_i = i % 8;
            if (mask > 0) {
                out[byte_i] |= BitConverters.int2ubyte(1 << (7 - bit_i));
                mask--;
            } else {
                out[byte_i] &= BitConverters.int2ubyte((~(1 << (7 - bit_i))) & 0xff);
            }
        }
        return out;
    }

    public static boolean bytesEqual(byte[] a1, byte[] a2) {
        int i;
        if (a1.length != a2.length) {
            return false;
        }
        for (i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

}

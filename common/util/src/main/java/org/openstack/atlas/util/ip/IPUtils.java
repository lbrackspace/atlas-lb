package org.openstack.atlas.util.ip;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.openstack.atlas.util.converters.BitConverters;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;

public class IPUtils {

    public static final int HOST_NAME = 1;
    public static final int IPv4 = 4;
    public static final int IPv6 = 6;
    private static final Pattern subnetPattern = Pattern.compile("^(.*)/(.*)$");
    private static final int MAX_HOST_LENGTH = 128;
    private static Random rnd = new Random();
    private static final Pattern hostNamePattern = Pattern.compile("^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?$");

    public static boolean isValidHostName(String in) {
        if (in == null) {
            return false;
        }
        if (in.length() > MAX_HOST_LENGTH) {
            return false;
        }
        Matcher hostNameMatcher = hostNamePattern.matcher(in);

        if (hostNameMatcher.find()) {
            return true;
        }
        return false;
    }

    public static boolean isValidIpv6String(String in) {
        if (in == null) {
            return false;
        }
        IPv6 ip = new IPv6(in);
        try {
            ip.getBytes();
        } catch (IPStringConversionException ex) {
            return false;
        }
        return true;
    }

    public static boolean isValidIpv4Subnet(String in) {
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
        if (in == null) {
            return false;
        }
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

    public static int getIPType(String ip) {
        if (isValidIpv4String(ip)) {
            return IPv4;
        } else if (isValidIpv6String(ip)) {
            return IPv6;
        } else if (isValidHostName(ip)) {
            return HOST_NAME;
        }
        return 0;
    }

    public static boolean ipEquals(String ipa, String ipb) {
        if (ipa == null || ipb == null) {
            return false;
        }

        // See if its an IPv4 match
        try {
            IPv4 a4 = new IPv4(ipa);
            IPv4 b4 = new IPv4(ipb);
            byte[] aBytes = a4.getBytes();
            byte[] bBytes = b4.getBytes();
            for (int i = 0; i < 4; i++) {
                if (aBytes[i] != bBytes[i]) {
                    return false;
                }
            }
            return true;
        } catch (IPStringConversionException ex) {
        }

        // See if there matching IPv6 addresses
        try {
            IPv6 a6 = new IPv6(ipa);
            IPv6 b6 = new IPv6(ipb);
            byte[] aBytes = a6.getBytes();
            byte[] bBytes = b6.getBytes();
            b6.getBytes();
            for (int i = 0; i < 16; i++) {
                if (aBytes[i] != bBytes[i]) {
                    return false;
                }
            }
            return true;
        } catch (IPStringConversionException ex) {
        }

        // See if there matching hostnames
        int aType = getIPType(ipa);
        int bType = getIPType(ipb);
        if (aType == bType && bType == HOST_NAME) {
            return ipa.equalsIgnoreCase(ipb);
        }
        return false;
    }

    public static String canonicalIp(String ip) throws IPStringConversionException {

        // If its IPv4 its already cononical
        try {
            IPv4 ip4 = new IPv4(ip);
            ip4.getBytes();
            return ip;
        } catch (IPStringConversionException ex) {
        }

        try {
            IPv6 ip6 = new IPv6(ip);
            String ip6Expanded = ip6.expand();
            return ip6Expanded;
        } catch (IPStringConversionException ex) {
        }
        if (isValidHostName(ip)) {
            return ip.toLowerCase();
        }
        String msg = String.format("Error converting ip %s to cononical form", ip);
        throw new IPStringConversionException(msg);
    }
}

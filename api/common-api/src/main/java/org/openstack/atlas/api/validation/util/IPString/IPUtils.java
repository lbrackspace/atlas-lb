package org.openstack.atlas.api.validation.util.IPString;


import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;

public class IPUtils {

    private static final byte[] hexmap = "0123456789ABCDEF".getBytes();
    private static final Pattern subnetPattern = Pattern.compile("^(.*)/(.*)$");
    private static final String ipv4PatternStr = "^((25[0-5]|2[0-4]\\d|1\\d\\d"
            + "|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)(/(3[0-2]|[0-2][0-9]|[0-9]))?$";
    private static final String ipv6PatternStr = "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))"
            + "|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
            + "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})"
            + "|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))"
            + "|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
            + "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*"
            + "(/([0-1][0-2][0-8]|1[0-1][0-9]|[0-9][0-9]|[0-9]))?$";
    private static final Pattern ipv4Pattern = Pattern.compile(ipv4PatternStr);
    private static final Pattern ipv6Pattern = Pattern.compile(ipv6PatternStr);
    private static final Pattern ipvbothPattern = Pattern.compile(ipv4PatternStr + "|" + ipv6PatternStr);
    private static Random rnd = new Random();

    public static Integer rndInt(int lo, int hi) {
        int ri = rnd.nextInt();
        ri = ri < 0 ? 0 - ri : ri;
        return new Integer(ri % (hi - lo + 1) + lo);
    }

    public static Double rndDouble(double lo, double hi) {
        double d = rnd.nextDouble();
        return (Double) (hi - lo) * d + lo;
    }

    public static Integer rndPosInt(int lo, int hi) {
        int ri = rnd.nextInt();
        ri = ri < 0 ? 0 - ri : ri;
        return new Integer(ri % (hi - lo + 1) + lo);
    }

    public static String rndIp() {
        String out = String.format("%s.%s.%s.%s", rndPosInt(0, 255), rndPosInt(0, 255), rndPosInt(0, 255), rndPosInt(0, 255));
        return out;
    }

    public static Object rndChoice(List oList) {
        int ri = rndPosInt(0, oList.size() - 1);
        return oList.get(ri);
    }

    public static Object rndChoice(Object[] oArray) {
        int ri = rndPosInt(0, oArray.length) - 1;
        return oArray[ri];
    }

    public static String rndperc() {
        return String.format("%s%%", rndPosInt(0, 100).toString());
    }

    public static final int ubyte2int(byte in) {
        return (in < 0) ? (int) in + 256 : (int) in;
    }

    /* Strips off extra bits and negative sign */
    public static final byte int2ubyte(int in) {
        in = ((in < 0) ? in = 0 - in : in) % 256;
        return (in > 127) ? (byte) (in - 256) : (byte) in;
    }

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

    public static final byte int2nibble(int in) {
        return (in < 0 || in > 15) ? (byte) -1 : hexmap[in];
    }

    public static String int16bit2hex(int i16) {
        String out;
        byte[] nibbles = new byte[4];
        int i;
        if (i16 < 0x0000 || i16 > 0xffff) {
            return null;
        }
        nibbles[0] = int2nibble((i16 & 0xf000) >> 12);
        nibbles[1] = int2nibble((i16 & 0x0f00) >> 8);
        nibbles[2] = int2nibble((i16 & 0x00f0) >> 4);
        nibbles[3] = int2nibble((i16 & 0x000f) >> 0);
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
            v = nibble2int(hex[i]);
            if (v == -1) {
                return -1;
            }
            out += v * base;
            base *= 16;
        }

        return out;
    }

    public static boolean IP4RegEx(String ip) {
        return ipv4Pattern.matcher(ip).matches();
    }

    public static boolean IP6RegEx(String ip) {
        return ipv6Pattern.matcher(ip).matches();
    }

    public static boolean IPRegEx(String ip) {
        return ipvbothPattern.matcher(ip).matches();
    }
}

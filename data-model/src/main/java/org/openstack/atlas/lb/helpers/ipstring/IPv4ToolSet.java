package org.openstack.atlas.lb.helpers.ipstring;

import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPCidrBlockOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;


import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/* Just testing */
public class IPv4ToolSet {

    public static String long2ip(long ipLong) {
        long o4 = (ipLong >> 24) & 0xFF;
        long o3 = (ipLong >> 16) & 0xFF;
        long o2 = (ipLong >> 8) & 0xFF;
        long o1 = (ipLong >> 0) & 0xFF;
        return String.format("%d.%d.%d.%d", o4, o3, o2, o1);
    }

    public static long mask_bits(int bits) throws IPStringConversionException {
        long mask = 0;
        long rolling_bit = 1L << 32;
        int i;
        String parseError = "Error parsing integer";
        if (bits < 0 || bits > 32) {
            throw new IPStringConversionException(parseError);
        }
        for (i = 1; i <= bits; i++) {
            rolling_bit >>= 1;
            mask |= rolling_bit;
            //System.out.printf("rolling bit = %d mask = %s\n",rolling_bit,mask);
        }
        return mask;
    }

    public static List<String> ipv4BlockToIpStrings(String blockString) throws IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        long ip;
        long lo;
        long hi;
        List<String> ips = new ArrayList<String>();
        IPv4Range ipRange = ipv4BlockToRange(blockString);
        lo = ipRange.getLo();
        hi = ipRange.getHi();

        for (ip = lo; ip <= hi; ip++) {
            ips.add(long2ip(ip));
        }
        return ips;
    }

    public static List<String> ipv4BlocksToIpStrings(List<String> ipBlocks) throws IPBlocksOverLapException, IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        List<String> ipStrings = new ArrayList();
        IPv4Ranges ranges = new IPv4Ranges();
        for (String ipBlock : ipBlocks) {
            ranges.add(ipBlock); // Will throw an overlap exception if an overlap occurs
            ipStrings.addAll(ipv4BlockToIpStrings(ipBlock));
        }
        return ipStrings;
    }

    public static IPv4Range ipv4BlockToRange(String blockString) throws IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        long hi;
        long lo;
        long ipLong;
        long mask;
        long i;
        int mask_bits = 0;
        final int loMaskLimit = 0;
        final int hiMaskLimit = 32;

        List<String> ips = new ArrayList<String>();
        Pattern blockPattern = Pattern.compile("^(.*)/(.*)$");
        Matcher blockMatch = blockPattern.matcher(blockString);
        String parseError = String.format("Could not parse IP block \"%s\"", blockString);
        if (!blockMatch.find()) {
            throw new IPStringConversionException(parseError);
        }
        String ipString = blockMatch.group(1);
        try {
            mask_bits = Integer.parseInt(blockMatch.group(2));
            if (mask_bits < loMaskLimit || mask_bits > hiMaskLimit) {
                String format = "/%d must be in range of %d and %d";
                throw new IPCidrBlockOutOfRangeException(String.format(format, mask_bits, loMaskLimit, hiMaskLimit));
            }
            mask = mask_bits(mask_bits);
            ipLong = ip2long(ipString);
        } catch (IllegalArgumentException e) {
            String errorMsg = String.format("could not convert \"%s\" to an integer in block \"%s\"",
                    blockMatch.group(2), blockString);
            throw new IPStringConversionException(errorMsg);
        }
        lo = ipLong & mask;
        hi = (ipLong | ~mask) & 0xFFFFFFFFL;
        return new IPv4Range(lo, hi, blockString);
    }

    public static boolean isValid(String ip) {
        long ipLong;
        if(ip==null) {
            return false;
        }
        try {
            ipLong = ip2long(ip);
        } catch (IPStringException ex) {
            return false;
        }
        return true;
    }

    public static long ip2long(String ip) throws IPStringConversionException, IPOctetOutOfRangeException {
        long out = 0;
        String ippatternstr = "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$";
        Pattern ipPattern = Pattern.compile(ippatternstr);
        Matcher ipMatch = ipPattern.matcher(ip);
        String parseError = String.format("Could not parse IP string \"%s\"", ip);
        int i = 1;
        if (ipMatch.find()) {
            try {
                for (i = 1; i <= 4; i++) {
                    long octet = Long.parseLong(ipMatch.group(i));
                    if (octet < 0 || octet > 255) {
                        String errMsg = String.format("Octet %s out of range 0-255 in ip %s",
                                ipMatch.group(i), ip);
                        throw new IPOctetOutOfRangeException(errMsg);
                    }
                    out += octet << (8 * -i + 32);
                }
            } catch (NumberFormatException e) {
                String errMsg = String.format("Could not convert %s to integer in ip \"%s\"", ipMatch.group(i), ip);
                throw new IPStringConversionException(errMsg);
            }
        } else {
            throw new IPStringConversionException(parseError);
        }
        return out;
    }

    //TODO: refactor this, temp hack to make zeus happy...
    public static boolean rejectUnwantedIps(String ip) {
        List<String> badIps = new ArrayList<String>();
        badIps.add("0.0.0.0");
        for (String ipString : badIps) {
            if (ipString.equals(ip)) {
                return true;
            }
        }
        return false;
    }
}

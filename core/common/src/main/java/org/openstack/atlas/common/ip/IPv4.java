/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.common.ip;


import org.openstack.atlas.common.converters.BitConverters;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4 {

    private String ip;

    public static String bytes2IpString(byte[] in) throws IPStringConversionException1 {
        String ipStr;
        if (in.length != 4) {
            String msg = "Error IPv4 requires byte array of length 4";
            throw new IPStringConversionException1(msg);
        }
        ipStr = String.format("%d.%d.%d.%d",
                BitConverters.ubyte2int(in[0]),
                BitConverters.ubyte2int(in[1]),
                BitConverters.ubyte2int(in[2]),
                BitConverters.ubyte2int(in[3]));
        return ipStr;
    }

    public IPv4() {
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setIp(byte[] in) throws IPStringConversionException1 {
        ip = bytes2IpString(in);
    }

    public IPv4(String ip) {
        setIp(ip);
    }

    public IPv4(byte[] in) throws IPStringConversionException1 {
        setIp(in);
    }

    public String getIp() {
        return ip;
    }

    public String getString() {
        return ip;
    }

    public byte[] getBytes() throws IPStringConversionException1 {
        byte[] out = new byte[4];
        int octet;
        int i;

        if (ip == null) {
            throw new IPStringConversionException1("Error ip address is null");
        }

        String ippatternstr = "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$";
        Pattern ipPattern = Pattern.compile(ippatternstr);
        Matcher ipMatch = ipPattern.matcher(ip);
        if (ipMatch.find()) {
            for (i = 1; i <= 4; i++) {
                try {
                    octet = Integer.parseInt(ipMatch.group(i));
                } catch (NumberFormatException e) {
                    String format = "Error ipv4 octet[%d] is not a valid integer in ip %s";
                    String msg = String.format(format, i, ip);
                    throw new IPStringConversionException1(msg);
                }
                if (octet < 0 || octet > 255) {
                    String format = "Error ipv4 octet[%d](%d) is not in range 0 - 255 in ip %s";
                    String msg = String.format(format, i, octet, ip);
                    throw new IPStringConversionException1(msg);
                }
                out[i - 1] = BitConverters.int2ubyte((int) (octet % 256));
            }
        } else {
            String err = String.format("Error %s is not a valid IPv4 string", ip);
            throw new IPStringConversionException1(err);
        }

        return out;
    }
}

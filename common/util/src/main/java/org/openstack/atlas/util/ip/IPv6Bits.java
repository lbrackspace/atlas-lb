package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.converters.BitConverters;
import org.openstack.atlas.util.ip.exception.IPBigIntegerConversionException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import java.math.BigInteger;

public class IPv6Bits extends IPv6{
    private BigInteger ipAsBigInt;
    private static final BigInteger maxIp;
    private static final BigInteger byte255;

    static {
        maxIp = new BigInteger("340282366920938463463374607431768211455");
        byte255 = new BigInteger("255");
    }

    public IPv6Bits(){
        super();
    }

        public static IPv6 BigInteger2IPv6(BigInteger in) throws IPBigIntegerConversionException {
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
}

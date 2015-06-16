package org.openstack.atlas.util.crypto;

import java.io.UnsupportedEncodingException;
import org.openstack.atlas.util.converters.BitConverters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashUtil {

    public static byte[] sha1sum(byte[] data, Integer startIdxInc, Integer stopIdxExc)
            throws NoSuchAlgorithmException {
        byte[] out;
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        out = md.digest(data);
        if (startIdxInc != null && stopIdxExc != null) {
            out = Arrays.copyOfRange(out, startIdxInc, stopIdxExc);
        }
        return out;
    }

    public static byte[] sha1sum(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException{
        return sha1sum(data.getBytes("UTF=8"));
    }

    public static byte[] sha1sum(byte[] data) throws NoSuchAlgorithmException {
        return sha1sum(data, null, null);
    }

    public static String sha1sumHex(byte[] data, Integer startIdxInc, Integer stopIdxExc) throws NoSuchAlgorithmException {
        String out;
        byte[] sum;
        sum = sha1sum(data, startIdxInc, stopIdxExc);
        out = BitConverters.bytes2hex(sum);
        return out;
    }

    public static String sha1sumHex(byte[] data) throws NoSuchAlgorithmException {
        return sha1sumHex(data, null, null);
    }

    public static String sha1sumHex(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException{
        return sha1sumHex(data.getBytes("UTF-8"),null,null);
    }
}

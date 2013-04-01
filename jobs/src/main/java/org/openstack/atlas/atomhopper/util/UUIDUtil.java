package org.openstack.atlas.atomhopper.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDUtil {

    /**
     *
     * @param uuidString the string used to generate the UUID with SHA256 encoding
     * @return the generated UUID
     * @throws NoSuchAlgorithmException
     */
    public static UUID genUUIDSHA256(String uuidString) throws NoSuchAlgorithmException {
        byte[] sum = sha256(uuidString);
        return UUID.nameUUIDFromBytes(sum);
    }

    /**
     *
     * @param uuidString the string used to generate the UUID with MD5 encoding
     * @return the generated UUID
     * @throws NoSuchAlgorithmException
     */
    public static UUID genUUIDMD5Hash(String uuidString) throws NoSuchAlgorithmException {
        byte[] sum = md5(uuidString);
        return UUID.nameUUIDFromBytes(sum);
    }

    /**
     *
     * @param str_in: the string to sha
     * @return the byte array
     * @throws java.security.NoSuchAlgorithmException
     */
    public static byte[] sha256(String str_in) throws java.security.NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(str_in.getBytes());
        byte digest[] = sha256.digest();
        return digest;
    }

    /**
     *
     * @param str_in: the string to md5
     * @return the byte array
     * @throws java.security.NoSuchAlgorithmException
     */
    public static byte[] md5(String str_in) throws java.security.NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(str_in.getBytes());
        byte digest[] = md5.digest();
        return digest;
    }
}

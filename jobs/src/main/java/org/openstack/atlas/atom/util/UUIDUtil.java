package org.openstack.atlas.atom.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDUtil {

    /**
     *
     * @param uuidString the string used to generate the UUID
     * @return the generated UUID
     * @throws NoSuchAlgorithmException
     */
    public static UUID genUUID(String uuidString) throws NoSuchAlgorithmException {
        byte[] sum = sha256(uuidString);
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
}

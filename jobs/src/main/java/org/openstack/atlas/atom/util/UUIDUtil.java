package org.openstack.atlas.atom.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDUtil {

    public static UUID genUUID(String uuidString) throws NoSuchAlgorithmException {
        byte[] sum = sha256(uuidString);
        return UUID.nameUUIDFromBytes(sum);
    }

    public static byte[] sha256(String str_in) throws java.security.NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(str_in.getBytes());
        byte digest[] = sha256.digest();
        return digest;
    }
}

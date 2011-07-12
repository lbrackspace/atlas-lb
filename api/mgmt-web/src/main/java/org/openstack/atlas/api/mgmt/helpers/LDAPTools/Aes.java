package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.InvalidAlgorithmParameterException;

public class Aes {

    private static final byte[] iv = new byte[]{111, -120, 13, -78,
        123, 21, 81, -13,
        12, -49, 124, 63,
        13, 96, -54, 32};

    public static SecretKey getAesKey(String key_str) throws
            NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKey secret = new SecretKeySpec(md5sum(key_str), "AES");
        return secret;
    }

    public static String b64encrypt(byte[] ptext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException {
        SecretKey key = getAesKey(key_str);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameterSpec ps = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ps);
        byte ctext[] = cipher.doFinal(ptext);
        return new String(Base64Coder.encode(ctext));
    }

    public static String bytes2str(byte[] bytes) {
        return new String(bytes);
    }

    public static byte[] b64decrypt(String b64ctext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException {
        SecretKey key = getAesKey(key_str);
        byte[] ctext = Base64Coder.decode(b64ctext);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameterSpec ps = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ps);
        return cipher.doFinal(ctext);
    }

    private static byte[] sha256(String str_in) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(str_in.getBytes());
        byte digest[] = sha256.digest();
        return digest;
    }

    private static byte[] md5sum(String str_in) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(str_in.getBytes());
        byte digest[] = md5.digest();
        return digest;
    }
}

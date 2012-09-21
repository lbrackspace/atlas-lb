package org.openstack.atlas.util.b64aes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import java.util.Random;

public class Aes {

    private static final int rivSize = 4;
    private static final int PAGESIZE = 4096;
    private static Random rnd = new Random();
    private static final byte[] iv = new byte[]{111, -120, 13, -78,
        123, 21, 81, -13,
        12, -49, 124, 63,
        13, 96, -54, 32};

    public static SecretKey getAesKey(String key_str) throws
            NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        SecretKey secret = new SecretKeySpec(md5sum(key_str), "AES");
        return secret;
    }

    public static String b64encrypt(byte[] ptext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        byte[] ctext = encrypt(ptext, sha256(key_str));
        return new String(Base64.encode(ctext, ctext.length));
    }

    public static byte[] encrypt(byte[] ptextBytes, byte[] keyBytes)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IOException, IllegalBlockSizeException, BadPaddingException {
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameterSpec algoParamSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, algoParamSpec);
        byte[] ptextWithRandomVector = injectVector(ptextBytes, rivSize);
        return cipher.doFinal(ptextWithRandomVector);
    }

    public static byte[] b64decrypt(String b64ctext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            PaddingException, IOException {
        byte[] ctextBytes = b64ctext.getBytes("UTF-8");
        byte[] ctext = Base64.decode(ctextBytes, ctextBytes.length);
        return decrypt(ctext, sha256(key_str));
    }

    public static byte[] decrypt(byte[] ctextBytes, byte[] keyBytes)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            PaddingException, IOException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameterSpec algoPramSpec = new IvParameterSpec(iv);
        SecretKey key = new SecretKeySpec(keyBytes,"AES");
        cipher.init(Cipher.DECRYPT_MODE, key, algoPramSpec);
        byte[] ptextBytes = cipher.doFinal(ctextBytes);
        ptextBytes = removeVector(ptextBytes, rivSize);
        return ptextBytes;
    }

    public static String b64decrypt_str(String b64ctext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            PaddingException, IOException {
        return new String(b64decrypt(b64ctext, key_str),"utf-8");
    }

    public static byte[] sha256(String str_in) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        return sha256(str_in.getBytes("UTF-8"));
    }

    public static byte[] sha256(byte[] bytesIn) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(bytesIn);
        return sha256.digest();
    }

    private static byte[] md5sum(String str_in) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return md5sum(str_in.getBytes("UTF-8"));
    }

    private static byte[] md5sum(byte[] bytesIn) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(bytesIn);
        return md5.digest();
    }

    private static byte[] injectVector(byte[] bytes, int vLength) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream(PAGESIZE);
        byte[] vector = new byte[vLength];
        rnd.nextBytes(vector);
        bas.write(vector);
        bas.write(bytes);
        return bas.toByteArray();
    }

    private static byte[] removeVector(byte[] bytes, int vLength) throws IOException {
        byte[] out;
        int olen = bytes.length - vLength;
        if (olen <= 0) {
            return new byte[0];
        }
        out = new byte[olen];
        int ilen = bytes.length;
        for (int i = vLength; i < ilen; i++) {
            out[i - vLength] = bytes[i];
        }
        return out;

    }
}

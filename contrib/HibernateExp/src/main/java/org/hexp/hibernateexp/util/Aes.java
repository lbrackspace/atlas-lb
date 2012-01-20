package org.hexp.hibernateexp.util;

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
import java.security.SecureRandom;

public class Aes {

    private static SecureRandom sr;
    private SecretKey aesKey;
    private static final int PADDINGSIZE = 32;

    static {
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            sr = new SecureRandom();
        }
    }

    public Aes(String keyStr) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        aesKey = new SecretKeySpec(md5sum(keyStr), "AES");
    }

    public Aes(byte[] keyBytes) throws NoSuchAlgorithmException {
        aesKey = new SecretKeySpec(md5sum(keyBytes), "AES");
    }

    public static String bytes2str(byte[] bytes) {
        return new String(bytes);
    }
    private static final byte[] iv = new byte[]{111, -120, 13, -78,
        123, 21, 81, -13,
        12, -49, 124, 63,
        13, 96, -54, 32};

    public String b64Encrypt(byte[] ptext)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException {
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameterSpec ps = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ps);
        byte[] padded = new byte[PADDINGSIZE];
        sr.nextBytes(padded);
        byte[] ctext = cipher.doFinal(appendBytes(padded, ptext));
        return new String(Base64Coder.encode(ctext));
    }

    public String encryptString(String pString) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException {
        byte[] pBytes = pString.getBytes("UTF-8");
        return b64Encrypt(pBytes);
    }

    public String decryptString(String ctext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        byte[] pBytes = b64Decrypt(ctext);
        String out = new String(pBytes, "UTF-8");
        return out;
    }

    public byte[] b64Decrypt(String b64ctext)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException {
        byte[] ctext = Base64Coder.decode(b64ctext);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameterSpec ps = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ps);
        byte[] padded = cipher.doFinal(ctext);
        byte[] ptext = stripBytes(padded,PADDINGSIZE);
        return ptext;
    }

    public static byte[] sha256(String strIn) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] bytesOut = strIn.getBytes("UTF-8");
        return sha256(bytesOut);
    }

    public static byte[] md5sum(String strIn) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] bytesOut = strIn.getBytes("UTF-8");
        return md5sum(bytesOut);
    }

    public static byte[] sha256(byte[] bytesIn) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(bytesIn);
        byte digest[] = sha256.digest();
        return digest;
    }

    public static byte[] md5sum(byte[] bytesIn) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(bytesIn);
        byte digest[] = md5.digest();
        return digest;
    }

    private static  byte[] stripBytes(byte[] bytes,int len){
        byte[] out;
        int i;
        int li;
        int j;
        li = bytes.length - len;
        j= len;
        out = new byte[li];
        for(i=0;i<li;i++,j++){
            out[i] = bytes[j];
        }
        return out;
    }

    private static byte[] appendBytes(byte[] bytes, byte[] appendBytes){
        int i=0;
        int j=0;

        byte[] out = new byte[bytes.length + appendBytes.length];
        for(i=0;i<bytes.length;i++,j++){
            out[j] = bytes[i];
        }
        for(i=0;i<appendBytes.length;i++,j++){
            out[j] = appendBytes[i];
        }
        return out;
    }
}

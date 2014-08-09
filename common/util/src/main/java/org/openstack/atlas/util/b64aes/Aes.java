package org.openstack.atlas.util.b64aes;

import java.io.ByteArrayInputStream;
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
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.openstack.atlas.util.converters.BitConverters;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class Aes {

    private static final String ALGO;
    private static final String UTF8;
    private static final int rivSize;
    private static final int PAGESIZE;
    private static SecureRandom rnd;
    private static final byte[] iv = new byte[]{111, -120, 13, -78,
        123, 21, 81, -13,
        12, -49, 124, 63,
        13, 96, -54, 32};
    private static String[] hexMap = {
        "0", "1", "2", "3",
        "4", "5", "6", "7",
        "8", "9", "A", "B",
        "C", "D", "E", "F"};

    static {
        ALGO = "AES/CBC/PKCS5Padding";
        UTF8 = "UTF-8";
        rivSize = 16;
        PAGESIZE = 4096;
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            sr = new SecureRandom();
        }
        rnd = sr;
        try {
            // Try to force load the crypto classes with this classes classloader.
            String key = "test";
            String ctext = Aes.b64encrypt_str("data", key);
            String ptext = Aes.b64decrypt_str(ctext, key);
        } catch (Exception ex) {
        }
    }

    private static byte[] rndBytes(int nBytes) {
        int i;
        byte[] out = new byte[nBytes];
        rnd.nextBytes(out);
        return out;
    }

    public static SecretKey getAesKey(String key_str) throws
            NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        int i;
        byte[] shabits = sha256(key_str);
        byte[] keybits = new byte[16];
        for (i = 0; i < 16; i++) {
            keybits[i] = (byte) ((int) shabits[i] ^ (int) shabits[i + 16]);
        }
        SecretKey secret = new SecretKeySpec(keybits, "AES");
        return secret;
    }

    public static String b64encrypt_str(String ptext_str, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        return b64encrypt(ptext_str.getBytes(UTF8), key_str);
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
        Cipher cipher = Cipher.getInstance(ALGO);
        AlgorithmParameterSpec algoParamSpec = new IvParameterSpec(iv);
        ClassLoader AESCipherClass = com.sun.crypto.provider.AESCipher.class.getClassLoader();
        ClassLoader cipherSpi = javax.crypto.CipherSpi.class.getClassLoader();

        cipher.init(Cipher.ENCRYPT_MODE, key, algoParamSpec);
        byte[] compressedBytes = StaticFileUtils.compressBytes(ptextBytes);
        byte[] ptextWithRandomVector = injectVector(compressedBytes, rivSize);
        return cipher.doFinal(ptextWithRandomVector);
    }

    public static byte[] b64decrypt(String b64ctext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            PaddingException, IOException {
        byte[] ctextBytes = b64ctext.getBytes(UTF8);
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
        Cipher cipher = Cipher.getInstance(ALGO);
        AlgorithmParameterSpec algoPramSpec = new IvParameterSpec(iv);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, algoPramSpec);
        byte[] ptextBytes = cipher.doFinal(ctextBytes);
        ptextBytes = removeVector(ptextBytes, rivSize);
        byte[] decompressedBytes = StaticFileUtils.decompressBytes(ptextBytes);
        return decompressedBytes;
    }

    public static String b64decrypt_str(String b64ctext, String key_str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            PaddingException, IOException {
        return new String(b64decrypt(b64ctext, key_str), UTF8);
    }

    public static byte[] sha256(String str_in) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        return sha256(str_in.getBytes(UTF8));
    }

    public static byte[] sha256(byte[] bytesIn) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(bytesIn);
        return sha256.digest();
    }

    private static byte[] md5sum_str(String str_in) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return md5sum(str_in.getBytes(UTF8));
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

    public static String stringToHex(String str)
            throws UnsupportedEncodingException {
        return bytesToHex(str.getBytes(UTF8));
    }

    public static String bytesToHex(byte[] bytesIn) {
        StringBuilder sb = new StringBuilder();
        int i;
        int nBytes = bytesIn.length;
        for (i = 0; i < nBytes; i++) {
            int byteInt = (int) bytesIn[i];
            if (byteInt < 0) {
                byteInt += 256;
            }
            int hi = byteInt >> 4;
            int lo = byteInt & 15;
            sb.append(hexMap[hi]).append(hexMap[lo]);
        }
        return sb.toString();
    }
}

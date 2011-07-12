package org.openstack.atlas.util.crypto;

import org.openstack.atlas.util.config.MossoConfig;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.util.crypto.exception.EncryptException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * A utility class providing static encryption and decryption methods.
 */
public final class CryptoUtil {

    public static final String ENCODING = "UTF-8";

    private static final Logger LOGGER = Logger.getLogger(CryptoUtil.class);

    private CryptoUtil() {
    }


    /**
     * Decrypts an encrypted String containing sensitive data using an AES/ECB/NoPadding transformation.
     *
     * @param encrypted The encrypted input string
     * @return A plaintext string
     * @throws DecryptException
     */
    public static String decrypt(String encrypted) throws DecryptException {
        String s = null;
        if (encrypted != null) {
            byte[] b = decryptAES(MossoConfig.getCryptoKeySpec(),
                    CryptoUtilValues.TRANSFORMATION_MODE_ECB,
                    CryptoUtilValues.TRANSFORMATION_PADDING_NO_PADDING, CryptoUtil.asBytes(encrypted,
                            CryptoUtilValues.CRYPTO_PAD_BYTE));
            try {
                s = new String(b, ENCODING);
            } catch (UnsupportedEncodingException ex) {
                // This shouldn't happen with UTF-8
                s = new String(b);
            }
            if (s != null) {
                s = s.trim();
            }
        }
        return s;
    }

    /**
     * Encrypts a String containing sensitive data using an AES/ECB/NoPadding transformation
     *
     * @param decrypted The plaintext string to be encrypted
     * @return An encrypted string, or null if "decrypted" is null
     * @throws EncryptException If there are any errors
     */
    public static String encrypt(String decrypted) throws EncryptException {
        if (decrypted == null) {
            return null;
        } else {
            return asHex(encryptAES(MossoConfig.getCryptoKeySpec(), CryptoUtilValues.TRANSFORMATION_MODE_ECB,
                    CryptoUtilValues.TRANSFORMATION_PADDING_NO_PADDING, padString(decrypted)));
        }
    }

    /**
     * Transforms a hex string into a byte array, and removes any padding.
     *
     * @param hexString The hex string to transform
     * @param padByte   The padding byte to remove
     * @return A byte array
     */
    protected static byte[] asBytes(String hexString, byte padByte) throws DecryptException {
        byte[] number = new byte[hexString.length() / 2];

        try {
            for (int i = 0; i < hexString.length(); i += 2) {
                int j = Integer.parseInt(hexString.substring(i, i + 2), 16);
                number[i / 2] = (byte) (j & 0x000000ff);
            }
        } catch (StringIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            throw new DecryptException("Invalid hex string");
        }

        return number;
    }

    /**
     * Transforms a byte array into a hex string.
     *
     * @param bytes An array of bytes to convert
     * @return A hex string
     */
    protected static String asHex(byte bytes[]) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] & 0xff) < 0x10) {
                sb.append("0");
            }

            sb.append(Long.toString(bytes[i] & 0xff, 16));
        }

        return sb.toString();
    }

    /**
     * Decrypts an encrypted byte array using a random key spec with the specified key length.
     *
     * @param keySize       The key size
     * @param cipherMode    The mode to use for the transformation
     * @param cipherPadding The paddint to use for the transformation
     * @param encrypted     The encrypted bytes
     * @return The decrypted bytes
     * @throws DecryptException
     */
    protected static byte[] decryptAES(int keySize, String cipherMode, String cipherPadding, byte[] encrypted)
            throws DecryptException {
        try {
            // Initialize the key generator
            KeyGenerator kgen = KeyGenerator.getInstance(CryptoUtilValues.TRANSFORMATION_ALG_AES);
            kgen.init(keySize);

            // Generate the secret key specs.
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();

            SecretKeySpec skeySpec = new SecretKeySpec(raw, CryptoUtilValues.TRANSFORMATION_ALG_AES);

            return decryptAES(skeySpec, cipherMode, cipherPadding, encrypted);
        } catch (NoSuchAlgorithmException e) {

            LOGGER.error("Could not instantiate keygen for AES decryption", e);
            throw new DecryptException("Could not instantiate keygen", e);
        }
    }

    /**
     * Decrypts an encrypted byte array using the given key spec.
     *
     * @param key           The key spec to use
     * @param cipherMode    The mode to use for the transformation
     * @param cipherPadding The paddint to use for the transformation
     * @param encrypted
     * @return The unencrypted bytes
     * @throws DecryptException
     */
    protected static byte[] decryptAES(SecretKeySpec key, String cipherMode, String cipherPadding,
                                       byte[] encrypted) throws DecryptException {
        try {
            // Build the transformation string
            StringBuilder transformation = new StringBuilder(CryptoUtilValues.TRANSFORMATION_ALG_AES);

            // Add the cipher mode if specified
            if (StringUtils.isNotEmpty(cipherMode)) {
                transformation.append("/").append(cipherMode);
            }

            // Add the cipher padding if specified
            if (StringUtils.isNotEmpty(cipherPadding)) {
                transformation.append("/").append(cipherPadding);
            }

            // Initialize the cipher
            Cipher c = Cipher.getInstance(transformation.toString());
            c.init(Cipher.DECRYPT_MODE, key);

            return c.doFinal(encrypted);

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not instantiate cipher for AES decryption", e);
            throw new DecryptException("Could not instantiate cipher", e);

        } catch (NoSuchPaddingException e) {
            LOGGER.error("Could not instantiate cipher for AES decryption", e);
            throw new DecryptException("Could not instantiate cipher", e);

        } catch (InvalidKeyException e) {
            LOGGER.error("Invalid key for AES encryption", e);
            throw new DecryptException("Invalid key for AES decryption", e);

        } catch (IllegalBlockSizeException e) {
            LOGGER.error("Could not perform AES decryption on: " + new String(encrypted), e);
            throw new DecryptException("Could not perform AES decryption on: " + new String(encrypted), e);

        } catch (BadPaddingException e) {
            LOGGER.error("Could not perform AES decryption on: " + new String(encrypted), e);
            throw new DecryptException("Could not perform AES decryption on: " + new String(encrypted), e);
        }
    }

    /**
     * Performs AES (Rijndael) encryption with a random key spec and specified key size on a string.
     *
     * @param keySize       The key size to use
     * @param cipherMode
     * @param cipherPadding
     * @param decrypted     The bytes to encrypt
     * @return An encrypted byte[]
     * @throws EncryptException If there are any errors
     */
    protected static byte[] encryptAES(int keySize, String cipherMode, String cipherPadding, byte[] decrypted)
            throws EncryptException {

        try {
            // set up the key generator for use
            KeyGenerator kgen = KeyGenerator.getInstance(CryptoUtilValues.TRANSFORMATION_ALG_AES);
            kgen.init(keySize);

            // Generate the secret key specs
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();

            SecretKeySpec skeySpec = new SecretKeySpec(raw, CryptoUtilValues.TRANSFORMATION_ALG_AES);

            return encryptAES(skeySpec, cipherMode, cipherPadding, decrypted);

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not decrypt data", e);
            throw new EncryptException("Could not decrypt data", e);

        } catch (Exception e) {
            LOGGER.error("Could not decrypt data", e);
            throw new EncryptException("Could not decrypt data", e);
        }
    }

    /**
     * Performs AES (Rijndael) encryption with the specified key spec on a string.
     *
     * @param key
     * @param cipherMode
     * @param cipherPadding
     * @param decrypted     The bytes to encrypt
     * @return An encrypted byte[]
     * @throws EncryptException Thrown if anything goes wrong during encryption
     */
    protected static byte[] encryptAES(SecretKeySpec key, String cipherMode, String cipherPadding,
                                       byte[] decrypted) throws EncryptException {

        try {
            // Build the transformation string
            StringBuilder transformation = new StringBuilder(CryptoUtilValues.TRANSFORMATION_ALG_AES);

            // Add the cipher mode if specified
            if (StringUtils.isNotEmpty(cipherMode)) {
                transformation.append("/").append(cipherMode);
            }

            // Add the cipher padding if specified
            if (StringUtils.isNotEmpty(cipherPadding)) {
                transformation.append("/").append(cipherPadding);
            }

            // Initialize the cipher
            Cipher c = Cipher.getInstance(transformation.toString());
            c.init(Cipher.ENCRYPT_MODE, key);

            return c.doFinal(decrypted);

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not encrypt data", e);
            throw new EncryptException("Could not encrypt data", e);

        } catch (NoSuchPaddingException e) {
            LOGGER.error("Could not encrypt data", e);
            throw new EncryptException("Could not encrypt data", e);

        } catch (InvalidKeyException e) {
            LOGGER.error("Could not encrypt data", e);
            throw new EncryptException("Could not encrypt data", e);

        } catch (IllegalBlockSizeException e) {
            LOGGER.error("Could not encrypt data", e);
            throw new EncryptException("Could not encrypt data", e);

        } catch (BadPaddingException e) {
            LOGGER.error("Could not encrypt data", e);
            throw new EncryptException("Could not encrypt data", e);

        } catch (Exception e) {
            LOGGER.error("Could not encrypt data", e);
            throw new EncryptException("Could not encrypt data", e);
        }
    }

    /**
     * Pads a string for AES encryption so that compatibility with PHP is preserved, returning the result to
     * the caller as a byte array.
     *
     * @param in The input string to be padded
     * @return A byte array
     */
    protected static byte[] padString(String in) {

        int inputLength = 0;
        try {
            inputLength = in.getBytes(ENCODING).length;
        } catch (UnsupportedEncodingException ex) {
            // This should never happen with UTF-8 encoding, but none the less.
            inputLength = in.getBytes().length;
        }

        int extraBytes = inputLength % CryptoUtilValues.CRYPTO_PAD_BLOCK_SIZE;

        byte[] ret = null;

        if (extraBytes > 0) {
            int bytesNeeded = CryptoUtilValues.CRYPTO_PAD_BLOCK_SIZE - extraBytes;

            StringBuilder sb = new StringBuilder(inputLength + bytesNeeded);
            sb.append(in);

            for (int i = 0; i < bytesNeeded; i++) {
                sb.append(" ");
            }

            try {
                ret = sb.toString().getBytes(ENCODING);
            } catch (UnsupportedEncodingException ex) {
                // This should never happen with UTF-8 encoding, but none the less.
                ret = sb.toString().getBytes();
            }
        } else {
            try {
                ret = in.getBytes(ENCODING);
            } catch (UnsupportedEncodingException ex) {
                // This should never happen with UTF-8 encoding, but none the less.
                ret = in.getBytes();
            }
        }

        return ret;
    }
}


package org.openstack.atlas.util.crypto;

/**
 * Defines public static final values usable by CryptoUtil.
 * 
 * This interface should not be implemented by anyone. All values should be referenced staticly, like so:
 * CryptoUtilValues.CRYPTO_PAD_BYTE.
 * 
 * @author Kaan Erdener
 */
public interface CryptoUtilValues {

    /**
     * The byte used for cryptography padding
     */
    byte CRYPTO_PAD_BYTE = 0x00;

    /**
     * The block size used to pad bytes
     */
    int CRYPTO_PAD_BLOCK_SIZE = 16;

    /**
     * An AES algorithm (Rijndael)
     */
    String TRANSFORMATION_ALG_AES = "AES";

    /**
     * ECB transformation mode (Electronic Codebook Mode)
     */
    String TRANSFORMATION_MODE_ECB = "ECB";

    /**
     * Specifies that padding should not be used in a transformation
     */
    String TRANSFORMATION_PADDING_NO_PADDING = "NoPadding";
}

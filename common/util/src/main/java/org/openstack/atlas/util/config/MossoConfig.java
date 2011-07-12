package org.openstack.atlas.util.config;

import org.openstack.atlas.util.crypto.CryptoUtilValues;

import javax.crypto.spec.SecretKeySpec;

/**
 * Mosso configuration class. Looks for a file named "hm.properties" in order to initalize log4j and some
 * other components within Mosso.
 * 
 * The contents of the properties file are then made available for other uses as an ExtendedProperties object.
 */
public final class MossoConfig {

    /**
     * The key spec to use for sensitive data
     */
    private static SecretKeySpec cryptoSecretKeySpec;

    /**
     * Returns the secret key spec for sensitive data
     * 
     * @return The secret key spec for sensitive data
     */
    public static SecretKeySpec getCryptoKeySpec() {

        org.openstack.atlas.cfg.Configuration config = new LbConfiguration();
        String key = config.getString(MossoConfigValues.hm_crypto_key);
        MossoConfig.cryptoSecretKeySpec = new SecretKeySpec(key.getBytes(),
                CryptoUtilValues.TRANSFORMATION_ALG_AES);
        return cryptoSecretKeySpec;
    }

}
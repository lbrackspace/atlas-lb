package org.openstack.atlas.common.config;

/**
 * Defines a set of public and protected configuration names and values. Used by MossoConfig.
 */
public enum MossoConfigValues implements ConfigurationKey {

    /**
     * The alt crypto key for data cryptography
     */
    hm_crypto_key,

    /**
     * The crypto key for sensitive data cryptography
     */
    hm_crypto_key_alt;
}

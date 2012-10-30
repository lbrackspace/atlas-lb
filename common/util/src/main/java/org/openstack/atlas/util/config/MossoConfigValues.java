package org.openstack.atlas.util.config;

import org.openstack.atlas.cfg.ConfigurationKey;

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
    hm_crypto_key_alt,
    /**
     * The base url for this services public endpoint
     */
    base_uri,
    /**
     * The crypto key used to encrypt the rdns_admin_passwd
     */
    rdns_crypto_key,
    /**
     * The endpoint for the management dns service. Used to delete PTR records
     */
    rdns_admin_url,
    /**
     * The endpoint for the public dns service. Used to delete PTR records
     */
    rdns_public_url,
    /**
     * The management user for the rDNS service
     */
    rdns_admin_user,
    /**
     * The base64 encrypted password for the rDNS user
     */
    rdns_admin_passwd,
    /**
     * Auth management url
     */
    auth_management_uri,

    /**
     * The Auth public endpoint
     */
    auth_public_uri,

    /**
     * the admin auth key
     */
    basic_auth_key,

    /**
     * The admin auth user account
     */
    basic_auth_user;
}

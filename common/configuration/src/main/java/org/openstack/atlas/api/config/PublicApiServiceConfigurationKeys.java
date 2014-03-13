package org.openstack.atlas.api.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum PublicApiServiceConfigurationKeys implements ConfigurationKey {

    access_log_file_location,
    adapter_soap_rest,
    allow_bypassauth,
    allow_internal_auth,
    auth_callback_uri,
    auth_management_uri,
    auth_public_uri,
    base_uri,
    basic_auth_key,
    basic_auth_user,
    db_host,
    db_name,
    db_passwd,
    db_port,
    db_user,
    esb_queue_name,
    health_check,
    identity_auth_url,
    identity_pass,
    identity_user,
    memcached_servers,
    rdns_admin_passwd,
    rdns_admin_url,
    rdns_admin_user,
    rdns_crypto_key,
    rdns_public_url,
    repose_via_key,
    rest_port,
    root_ca_file,
    service_bus_endpoint_uri,
    ssl_termination,
    stats,
    ttl,
    usage_deletion_limit,
    usage_poller_log_all_counters,
    usage_timezone_code
}

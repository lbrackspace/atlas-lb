package org.openstack.atlas.util.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum MossoConfigValues implements ConfigurationKey {
    hm_crypto_key,
    hm_crypto_key_alt,
    base_uri,
    rdns_crypto_key,
    rdns_admin_url,
    rdns_public_url,
    rdns_admin_user,
    rdns_admin_passwd,
    auth_management_uri,
    auth_public_uri,
    basic_auth_key,
    basic_auth_user,
    ahusl_region,
    allow_ahusl,
    ahusl_data_center,
    atom_hopper_endpoint,
    ahusl_max_total_connections,
    ahusl_time_out,
    ahusl_max_redirects,
    http_protocol,
    ahusl_pool_task_count,
    ahusl_pool_max_size,
    ahusl_pool_core_size,
    ahusl_pool_conn_timeout,
    ahusl_auth_username,
    ahusl_auth_password,
    ahusl_log_requests,
    ahusl_num_attempts,
    ahusl_run_failed_entries,
    rdns_use_service_admin
}

package org.openstack.atlas.restclients.atomhopper.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum AtomHopperConfigurationKeys implements ConfigurationKey {
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
    ahusl_run_failed_entries
}

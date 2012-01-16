package org.openstack.atlas.api.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum PublicApiServiceConfigurationKeys implements ConfigurationKey {
    auth_callback_uri,
    auth_management_uri,
    basic_auth_user,
    basic_auth_key,
    base_uri,
    esb_queue_name,
    service_bus_endpoint_uri,
    db_host,
    db_user,
    db_passwd,
    db_name,
    db_port,
    access_log_file_location,
    usage_timezone_code,
    health_check,
    stats,
    ttl,
    ssl_termination,
    memcached_servers;
}

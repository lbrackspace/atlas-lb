package org.openstack.atlas.api.config;

import org.openstack.atlas.common.config.ConfigurationKey;

public enum PublicApiServiceConfigurationKeys implements ConfigurationKey {
    auth_callback_uri,
    auth_management_uri,
    auth_username,
    auth_password,
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
    adapter,
    extensions;
}

package org.openstack.atlas.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum LbLogsConfigurationKeys implements ConfigurationKey {
    auth_management_uri,
    basic_auth_key,
    basic_auth_user,

    rawlogs_cache_dir,
    rawlogs_backup_dir,

    mapreduce_input_prefix,
    mapreduce_output_prefix,
    filesystem_root_dir,

    basemapreduce_log_suffix,
}
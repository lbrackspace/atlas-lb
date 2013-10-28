package org.openstack.atlas.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum LbLogsConfigurationKeys implements ConfigurationKey {
    auth_management_uri,
    basic_auth_key,
    basic_auth_user,
    files_region,
    rawlogs_cache_dir,
    rawlogs_backup_dir,
    mapreduce_input_prefix,
    mapreduce_output_prefix,
    filesystem_root_dir,
    basemapreduce_log_suffix,
    job_jar_path,
    hdfs_user_name,
    hadoop_xml_file,
    hdfs_job_jar_path,
    num_reducers,
    logs_use_service_admin
}

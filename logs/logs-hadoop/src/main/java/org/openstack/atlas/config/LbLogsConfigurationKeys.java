package org.openstack.atlas.config;

import org.openstack.atlas.cfg.ConfigurationKey;

public enum LbLogsConfigurationKeys implements ConfigurationKey {
    auth_management_uri,
    basemapreduce_log_suffix,
    basic_auth_key,
    basic_auth_user,
    cloud_files_lzo_account,
    cloud_files_lzo_auth_api_endpoint,
    cloud_files_days_of_lzos_to_keep,
    cloud_files_lzo_segment_size,
    cloud_files_lzo_key,
    cloud_files_lzo_user,
    cloud_files_lzo_storage_api_endpoint,
    cloud_files_lzo_file_name_append,
    days_of_lzos_to_keep,
    days_of_zips_to_keep,
    files_region,
    filesystem_root_dir,
    hadoop_xml_file,
    hdfs_block_size_megs,
    hdfs_job_jar_path,
    hdfs_user_name,
    job_jar_path,
    logs_use_service_admin,
    mapreduce_input_prefix,
    mapreduce_output_prefix,
    num_reducers,
    rawlogs_backup_dir,
    rawlogs_cache_dir,
    replication_count,
}

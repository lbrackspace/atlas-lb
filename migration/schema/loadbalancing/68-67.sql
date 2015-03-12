use loadbalancing;
drop table cloud_files_lzo;
drop table hdfs_lzo;

update `meta` set `meta_value` = '67' where `meta_key`='version';

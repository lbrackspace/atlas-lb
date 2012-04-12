use loadbalancing_usage;

ALTER TABLE `lb_usage` ADD COLUMN `avg_concurrent_conns_ssl` double NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `cum_bandwidth_bytes_in_ssl` bigint(20) NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `cum_bandwidth_bytes_out_ssl` bigint(20) NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `last_bandwidth_bytes_in_ssl` bigint(20) NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `last_bandwidth_bytes_out_ssl` bigint(20) NOT NULL default '0';

update `meta` set `meta_value` = '40' where `meta_key`='version';

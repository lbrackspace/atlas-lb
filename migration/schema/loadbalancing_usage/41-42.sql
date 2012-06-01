USE `loadbalancing_usage`;

ALTER TABLE `lb_usage_event` ADD COLUMN `last_bandwidth_bytes_in` bigint(20) DEFAULT NULL;
ALTER TABLE `lb_usage_event` ADD COLUMN `last_bandwidth_bytes_out` bigint(20) DEFAULT NULL;
ALTER TABLE `lb_usage_event` ADD COLUMN `last_concurrent_conns` int(11) DEFAULT NULL;
ALTER TABLE `lb_usage_event` ADD COLUMN `last_bandwidth_bytes_in_ssl` bigint(20) DEFAULT NULL;
ALTER TABLE `lb_usage_event` ADD COLUMN `last_bandwidth_bytes_out_ssl` bigint(20) DEFAULT NULL;
ALTER TABLE `lb_usage_event` ADD COLUMN `last_concurrent_conns_ssl` int(11) DEFAULT NULL;

update `meta` set `meta_value` = '42' where `meta_key`='version';
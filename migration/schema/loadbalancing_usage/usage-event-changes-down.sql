USE `loadbalancing_usage`;

ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_in`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_out`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_concurrent_conns`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_in_ssl`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_out_ssl`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_concurrent_conns_ssl`;

update `loadbalancerMeta` set `meta_value` = '?' where `meta_key`='version';
USE `loadbalancing_usage`;

ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_in`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_out`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_concurrent_conns`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_in_ssl`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_bandwidth_bytes_out_ssl`;
ALTER TABLE `lb_usage_event` DROP COLUMN `last_concurrent_conns_ssl`;

update `meta` set `meta_value` = '41' where `meta_key`='version';
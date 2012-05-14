use loadbalancing_usage;

ALTER TABLE `lb_usage` DROP COLUMN `avg_concurrent_conns_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `cum_bandwidth_bytes_in_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `cum_bandwidth_bytes_out_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `last_bandwidth_bytes_in_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `last_bandwidth_bytes_out_ssl`;

update `meta` set `meta_value` = '39' where `meta_key`='version';

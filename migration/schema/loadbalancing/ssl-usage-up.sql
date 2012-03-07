use loadbalancing;

ALTER TABLE `lb_usage` ADD COLUMN `avg_concurrent_conns_ssl` double NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `bandwidth_in_ssl` bigint(20) NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `bandwidth_out_ssl` bigint(20) NOT NULL default '0';

update `meta` set `meta_value` = '?' where `meta_key`='version';

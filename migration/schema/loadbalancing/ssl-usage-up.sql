use loadbalancing;

ALTER TABLE `lb_usage` ADD COLUMN `avg_concurrent_conns_ssl` double NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `bandwidth_in_ssl` bigint(20) NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `bandwidth_out_ssl` bigint(20) NOT NULL default '0';

INSERT INTO `event_type`(`name`, `description`) VALUES('SSL_ONLY_ON', 'Only the ssl virtual server is enabled in Zeus');
INSERT INTO `event_type`(`name`, `description`) VALUES('SSL_MIXED_ON', 'Both the ssl virtual server and non-ssl virtual server are enabled in Zeus');

update `meta` set `meta_value` = '?' where `meta_key`='version';

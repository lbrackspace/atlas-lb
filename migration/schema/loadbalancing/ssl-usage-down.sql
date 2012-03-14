use loadbalancing;

DELETE FROM `event_type` WHERE `name` = 'SSL_MIXED_ON';
DELETE FROM `event_type` WHERE `name` = 'SSL_ONLY_ON';

ALTER TABLE `lb_usage` DROP COLUMN `avg_concurrent_conns_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `bandwidth_in_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `bandwidth_out_ssl`;

update `meta` set `meta_value` = '?' where `meta_key`='version';
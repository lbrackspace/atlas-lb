USE `loadbalancing_usage`;

ALTER TABLE `lb_usage` DROP COLUMN `event_type`;

DROP TABLE IF EXISTS `event_type`;

update `meta` set `meta_value` = '5' where `meta_key`='version';


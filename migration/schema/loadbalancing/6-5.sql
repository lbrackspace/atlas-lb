USE `loadbalancing`;

ALTER TABLE `lb_usage` DROP COLUMN `event_type`;

DROP TABLE IF EXISTS `event_type`;

update `loadbalancerMeta` set `meta_value` = '5' where `meta_key`='version';


use `loadbalancing_usage`;

DROP TABLE IF EXISTS `lb_usage_event`;
DROP TABLE IF EXISTS `event_type`;
DROP TABLE IF EXISTS `lb_usage`;

UPDATE `meta` SET `meta_value` = '64' WHERE `meta_key`='version';


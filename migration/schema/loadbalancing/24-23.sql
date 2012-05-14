USE `loadbalancing`;

ALTER TABLE `virtual_ip_ipv4` RENAME TO `virtual_ip`;

DROP TABLE IF EXISTS `access_list_event`;
DROP TABLE IF EXISTS `alert`;
DROP TABLE IF EXISTS `alert_status`;
DROP TABLE IF EXISTS `category_type`;
DROP TABLE IF EXISTS `connection_limit_event`;
DROP TABLE IF EXISTS `event_severity`;
DROP TABLE IF EXISTS `health_monitor_event`;
DROP TABLE IF EXISTS `loadbalancer_event`;
DROP TABLE IF EXISTS `loadbalancer_service_event`;
DROP TABLE IF EXISTS `node_event`;
DROP TABLE IF EXISTS `session_persistence_event`;
DROP TABLE IF EXISTS `state`;
DROP TABLE IF EXISTS `virtual_ip_event`;

CREATE TABLE `allocated_virtual_ip` (
  `id` int(11) NOT NULL auto_increment,
  `virtual_ip_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `virtual_ip_id` (`virtual_ip_id`),
  CONSTRAINT `virtual_ip_idfk_1` FOREIGN KEY (`virtual_ip_id`) REFERENCES `virtual_ip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `allocated_virtual_ip`(virtual_ip_id) SELECT id FROM virtual_ip WHERE is_allocated = TRUE;
ALTER TABLE `virtual_ip` ADD COLUMN `last_allocation` timestamp NULL DEFAULT NULL;
ALTER TABLE `virtual_ip` ADD COLUMN `ip_version` varchar(32) default 'IPV4';
ALTER TABLE `virtual_ip` ADD CONSTRAINT `fk_ip_version` FOREIGN KEY (`ip_version`) REFERENCES `ip_version` (`name`);
UPDATE `virtual_ip` SET `last_allocation` = now() WHERE id IN (SELECT virtual_ip_id FROM allocated_virtual_ip);
ALTER TABLE `virtual_ip` DROP COLUMN `is_allocated`;

DELETE FROM `event_type` WHERE `name` = 'CREATE_ACCESS_LIST';
DELETE FROM `event_type` WHERE `name` = 'CREATE_CONNECTION_THROTTLE';
DELETE FROM `event_type` WHERE `name` = 'CREATE_HEALTH_MONITOR';
DELETE FROM `event_type` WHERE `name` = 'CREATE_NODE';
DELETE FROM `event_type` WHERE `name` = 'CREATE_SESSION_PERSISTENCE';
DELETE FROM `event_type` WHERE `name` = 'DELETE_ACCESS_LIST';
DELETE FROM `event_type` WHERE `name` = 'DELETE_CONNECTION_THROTTLE';
DELETE FROM `event_type` WHERE `name` = 'DELETE_HEALTH_MONITOR';
DELETE FROM `event_type` WHERE `name` = 'DELETE_NETWORK_ITEM';
DELETE FROM `event_type` WHERE `name` = 'DELETE_NODE';
DELETE FROM `event_type` WHERE `name` = 'DELETE_SESSION_PERSISTENCE';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_ACCESS_LIST';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_CONNECTION_THROTTLE';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_HEALTH_MONITOR';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_LOADBALANCER';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_NODE';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_SESSION_PERSISTENCE';
DELETE FROM `event_type` WHERE `name` = 'UPDATE_CONNECTION_LOGGING';

update `meta` set `meta_value` = '23' where `meta_key`='version';

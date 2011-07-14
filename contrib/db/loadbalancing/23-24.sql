USE `loadbalancing`;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `virtual_ip` ADD COLUMN `is_allocated` boolean NOT NULL DEFAULT FALSE;
UPDATE `virtual_ip` SET is_allocated = TRUE WHERE id IN (SELECT virtual_ip_id FROM allocated_virtual_ip);
DROP TABLE `allocated_virtual_ip`;

ALTER TABLE `virtual_ip` DROP FOREIGN KEY `fk_ip_version`;
ALTER TABLE `virtual_ip` DROP COLUMN `ip_version`;

CREATE TABLE IF NOT EXISTS `access_list_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `access_list_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svale_fk` (`severity`),
  KEY `tyale_fk` (`type`),
  KEY `ctale_fk` (`category`),
  CONSTRAINT `ale_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `ale_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `ale_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `alert` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) default NULL,
  `loadbalancer_id` int(11) default NULL,
  `alert_type` varchar(32) NOT NULL,
  `message` text NOT NULL,
  `message_name` varchar(256) NOT NULL,
  `status` varchar(32) NOT NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `alert_status_fk` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `alert_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `category_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `connection_limit_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `connection_limit_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svcle_fk` (`severity`),
  KEY `tycle_fk` (`type`),
  KEY `ctcle_fk` (`category`),
  CONSTRAINT `cle_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `cle_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `cle_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `event_severity` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `event_type` VALUES('CREATE_ACCESS_LIST', 'Created Access List');
INSERT INTO `event_type` VALUES('CREATE_CONNECTION_THROTTLE', 'Created Connection Throttle');
INSERT INTO `event_type` VALUES('CREATE_HEALTH_MONITOR', 'Created Health Monitor');
INSERT INTO `event_type` VALUES('CREATE_NODE', 'Node Created');
INSERT INTO `event_type` VALUES('CREATE_SESSION_PERSISTENCE', 'Created Session Persisitence');
INSERT INTO `event_type` VALUES('DELETE_ACCESS_LIST', 'Deleted Access List');
INSERT INTO `event_type` VALUES('DELETE_CONNECTION_THROTTLE', 'Deleted Connection Throttle');
INSERT INTO `event_type` VALUES('DELETE_HEALTH_MONITOR', 'Deleted Health Monitor');
INSERT INTO `event_type` VALUES('DELETE_NETWORK_ITEM', 'Deleted Network Item');
INSERT INTO `event_type` VALUES('DELETE_NODE', 'Node deleted');
INSERT INTO `event_type` VALUES('DELETE_SESSION_PERSISTENCE', 'Deleted Session Persistence');
INSERT INTO `event_type` VALUES('UPDATE_ACCESS_LIST', 'Update Access List');
INSERT INTO `event_type` VALUES('UPDATE_CONNECTION_THROTTLE', 'Update Connection Throttle');
INSERT INTO `event_type` VALUES('UPDATE_HEALTH_MONITOR', 'Updated Health Monitor');
INSERT INTO `event_type` VALUES('UPDATE_LOADBALANCER', 'Loadbalancer updated');
INSERT INTO `event_type` VALUES('UPDATE_NODE', 'Node updated');
INSERT INTO `event_type` VALUES('UPDATE_SESSION_PERSISTENCE', 'Updated Session Persisitence');
INSERT INTO `event_type` VALUES('UPDATE_CONNECTION_LOGGING', 'Updated Connection Logging');

CREATE TABLE IF NOT EXISTS `health_monitor_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `health_monitor_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svhme_fk` (`severity`),
  KEY `tylhme_fk` (`type`),
  KEY `cthme_fk` (`category`),
  CONSTRAINT `hme_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `hme_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `hme_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `loadbalancer_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svle_fk` (`severity`),
  KEY `tyle_fk` (`type`),
  KEY `ctle_fk` (`category`),
  CONSTRAINT `le_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `le_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `le_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `loadbalancer_service_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svlse_fk` (`severity`),
  KEY `tylse_fk` (`type`),
  KEY `ctlse_fk` (`category`),
  CONSTRAINT `lse_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `lse_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `lse_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `node_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `node_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svne_fk` (`severity`),
  KEY `tyne_fk` (`type`),
  KEY `ctne_fk` (`category`),
  CONSTRAINT `ne_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `ne_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `ne_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `session_persistence_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svspe_fk` (`severity`),
  KEY `tyspe_fk` (`type`),
  KEY `ctspe_fk` (`category`),
  CONSTRAINT `spe_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `spe_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `spe_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `state` (
  `id` int(11) NOT NULL auto_increment,
  `state` varchar(50) default NULL,
  `jobname` varchar(1000) default NULL,
  `inputpath` varchar(200) default NULL,
  `start_time` datetime default NULL,
  `end_time` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `virtual_ip_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `virtual_ip_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `svvie_fk` (`severity`),
  KEY `tyvie_fk` (`type`),
  KEY `ctvie_fk` (`category`),
  CONSTRAINT `vie_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `vie_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `vie_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `virtual_ip` RENAME TO `virtual_ip_ipv4`;

update `meta` set `meta_value` = '24' where `meta_key`='version';

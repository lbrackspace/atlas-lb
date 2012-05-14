/*
 Navicat Premium Data Transfer

 Source Server         : lbaas-dev
 Source Server Type    : MySQL
 Source Server Version : 50051
 Source Host           : 173.203.200.79
 Source Database       : loadbalancing

 Target Server Type    : MySQL
 Target Server Version : 50051
 File Encoding         : iso-8859-1

 Date: 11/08/2010 15:45:10 PM
*/

SET NAMES `utf8`;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `loadbalancing` /*!40100 DEFAULT CHARACTER SET latin1 */;
use `loadbalancing`;

-- ----------------------------
--  Table structure for `access_list`
-- ----------------------------
DROP TABLE IF EXISTS `access_list`;
CREATE TABLE `access_list` (
  `id` int(11) NOT NULL auto_increment,
  `ip_address` varchar(39) default NULL,
  `ip_version` varchar(255) default NULL,
  `type` varchar(32) default NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FKC27372991210C19B` (`loadbalancer_id`),
  KEY `access_list_type_fk` (`type`),
  CONSTRAINT `access_list_ibfk_1` FOREIGN KEY (`type`) REFERENCES `access_list_type` (`name`),
  CONSTRAINT `FKC27372991210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `access_list_type`
-- ----------------------------
DROP TABLE IF EXISTS `access_list_type`;
CREATE TABLE `access_list_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `cluster`
-- ----------------------------
DROP TABLE IF EXISTS `cluster`;
CREATE TABLE `cluster` (
  `id` int(11) NOT NULL auto_increment,
  `description` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `username` varchar(32) NOT NULL,
  `password` varchar(32) NOT NULL,
  `data_center` varchar(32) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `data_center` (`data_center`),
  CONSTRAINT `cluster_ibfk_1` FOREIGN KEY (`data_center`) REFERENCES `lb_data_center` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `connection_limit`
-- ----------------------------
DROP TABLE IF EXISTS `connection_limit`;
CREATE TABLE `connection_limit` (
  `id` int(11) NOT NULL auto_increment,
  `max_connectionrate` int(11) NOT NULL,
  `max_connection` int(11) NOT NULL,
  `min_connections` int(11) NOT NULL,
  `rate_interval` int(11) NOT NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK47D50F1A1210C19B` (`loadbalancer_id`),
  CONSTRAINT `FK47D50F1A1210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `health_monitor`
-- ----------------------------
DROP TABLE IF EXISTS `health_monitor`;
CREATE TABLE `health_monitor` (
  `id` int(11) NOT NULL auto_increment,
  `attempts_before_deactivation` int(11) default NULL,
  `body_regex` varchar(128) default NULL,
  `delay` int(11) default NULL,
  `path` varchar(128) default NULL,
  `status_regex` varchar(128) default NULL,
  `timeout` int(11) default NULL,
  `type` varchar(32) NOT NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK59EB4A771210C19B` (`loadbalancer_id`),
  KEY `health_monitor_type_fk` (`type`),
  CONSTRAINT `FK59EB4A771210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`),
  CONSTRAINT `health_monitor_ibfk_1` FOREIGN KEY (`type`) REFERENCES `health_monitor_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `health_monitor_type`
-- ----------------------------
DROP TABLE IF EXISTS `health_monitor_type`;
CREATE TABLE `health_monitor_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `host`
-- ----------------------------
DROP TABLE IF EXISTS `host`;
CREATE TABLE `host` (
  `id` int(11) NOT NULL auto_increment,
  `core_device_id` varchar(64) NOT NULL,
  `host_status` varchar(32) NOT NULL,
  `management_ip` varchar(255) NOT NULL,
  `max_concurrent_connections` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `endpoint` varchar(255) NOT NULL,
  `target_host` varchar(255) NOT NULL,
  `cluster_id` int(11) NOT NULL,
  `zone` varchar(4) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `target_host` (`target_host`),
  KEY `FK30F5A8D56C3599` (`cluster_id`),
  KEY `host_status_fk` (`host_status`),
  KEY `host_ibfk_3` (`zone`),
  CONSTRAINT `FK30F5A8D56C3599` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`),
  CONSTRAINT `host_ibfk_1` FOREIGN KEY (`host_status`) REFERENCES `host_status` (`name`),
  CONSTRAINT `host_ibfk_3` FOREIGN KEY (`zone`) REFERENCES `lb_zone` (`zone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `host_backup`
-- ----------------------------
DROP TABLE IF EXISTS `host_backup`;
CREATE TABLE `host_backup` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `backup_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `host_id` int(11) NOT NULL,
  `lb_zone` varchar(4) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name_host_id` (`name`,`host_id`),
  KEY `hb_host_id_fk` (`host_id`),
  CONSTRAINT `host_backup_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `host_status`
-- ----------------------------
DROP TABLE IF EXISTS `host_status`;
CREATE TABLE `host_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_algorithm`
-- ----------------------------
DROP TABLE IF EXISTS `lb_algorithm`;
CREATE TABLE `lb_algorithm` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_data_center`
-- ----------------------------
DROP TABLE IF EXISTS `lb_data_center`;
CREATE TABLE `lb_data_center` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_protocol`
-- ----------------------------
DROP TABLE IF EXISTS `lb_protocol`;
CREATE TABLE `lb_protocol` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `port` int(11) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_rate_limit`
-- ----------------------------
DROP TABLE IF EXISTS `lb_rate_limit`;
CREATE TABLE `lb_rate_limit` (
  `id` int(11) NOT NULL auto_increment,
  `ticket_id` int(11) NOT NULL,
  `expiration_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `max_requests_per_second` int(11) NOT NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `rl_loadbalancer_id_fk` (`loadbalancer_id`),
  CONSTRAINT `rate_limit_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_session_persistence`
-- ----------------------------
DROP TABLE IF EXISTS `lb_session_persistence`;
CREATE TABLE `lb_session_persistence` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_status`
-- ----------------------------
DROP TABLE IF EXISTS `lb_status`;
CREATE TABLE `lb_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_suspension`
-- ----------------------------
DROP TABLE IF EXISTS `lb_suspension`;
CREATE TABLE `lb_suspension` (
  `id` int(11) NOT NULL auto_increment,
  `ticket_id` int(11) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `user` varchar(255) default NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `suspension_fk_1` (`loadbalancer_id`),
  CONSTRAINT `suspension_fk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_usage`
-- ----------------------------
DROP TABLE IF EXISTS `lb_usage`;
CREATE TABLE `lb_usage` (
  `id` int(11) NOT NULL auto_increment,
  `loadbalancer_id` int(11) NOT NULL,
  `avg_concurrent_conns` double NOT NULL default '0',
  `bandwidth_in` bigint(20) NOT NULL,
  `bandwidth_out` bigint(20) NOT NULL,
  `start_time` timestamp NULL default NULL,
  `end_time` timestamp NULL default NULL,
  `num_polls` int(11) NOT NULL default '0',
  `num_vips` int(11) NOT NULL default '1',
  `tags_bitmask` int(3) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FKBBE47D981210C19B` (`loadbalancer_id`),
  KEY `start_time` (`start_time`),
  KEY `end_time` (`end_time`),
  CONSTRAINT `FKBBE47D981210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------                                                                      --  Table structure for `account_usage`                                                              -- ----------------------------                                                                                                   
DROP TABLE IF EXISTS `account_usage`;
CREATE TABLE `account_usage` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `num_loadbalancers` int(11) NOT NULL default '0',
  `num_public_vips` int(11) NOT NULL default '0',
  `num_servicenet_vips` int(11) NOT NULL default '0',
  `start_time` timestamp NULL default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `account_usage_account_id` (`account_id`),
  KEY `account_usage_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `lb_zone`
-- ----------------------------
DROP TABLE IF EXISTS `lb_zone`;
CREATE TABLE `lb_zone` (
  `zone` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`zone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `loadbalancer`
-- ----------------------------
DROP TABLE IF EXISTS `loadbalancer`;
CREATE TABLE `loadbalancer` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `name` varchar(128) default NULL,
  `protocol` varchar(32) NOT NULL,
  `port` int(11) NOT NULL,
  `algorithm` varchar(32) NOT NULL,
  `connection_logging` tinyint(1) NOT NULL default '0',
  `sessionPersistence` varchar(255) NOT NULL,
  `status` varchar(32) NOT NULL,
  `host_id` int(11) NOT NULL,
  `created` timestamp NULL default NULL,
  `updated` timestamp NULL default NULL,
  `is_sticky` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FKD5EFEBFC9789BEFB` (`host_id`),
  KEY `lb_algorithm_fk` (`algorithm`),
  KEY `lb_session_persistence_fk` (`sessionPersistence`),
  KEY `lb_protocol_fk` (`protocol`),
  KEY `lb_status_fk` (`status`),
  CONSTRAINT `FKD5EFEBFC9789BEFB` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `loadbalancer_ibfk_1` FOREIGN KEY (`algorithm`) REFERENCES `lb_algorithm` (`name`),
  CONSTRAINT `loadbalancer_ibfk_2` FOREIGN KEY (`sessionPersistence`) REFERENCES `lb_session_persistence` (`name`),
  CONSTRAINT `loadbalancer_ibfk_3` FOREIGN KEY (`protocol`) REFERENCES `lb_protocol` (`name`),
  CONSTRAINT `loadbalancer_ibfk_4` FOREIGN KEY (`status`) REFERENCES `lb_status` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `loadbalancer_virtualip`
-- ----------------------------
DROP TABLE IF EXISTS `loadbalancer_virtualip`;
CREATE TABLE `loadbalancer_virtualip` (
  `loadbalancer_id` int(11) NOT NULL,
  `virtualip_id` int(11) NOT NULL,
  PRIMARY KEY  (`loadbalancer_id`,`virtualip_id`),
  KEY `FKEDA4B0EF1210C19B` (`loadbalancer_id`),
  KEY `FKEDA4B0EF6D38D2F9` (`virtualip_id`),
  CONSTRAINT `FKEDA4B0EF1210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`),
  CONSTRAINT `FKEDA4B0EF6D38D2F9` FOREIGN KEY (`virtualip_id`) REFERENCES `virtual_ip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `node`
-- ----------------------------
DROP TABLE IF EXISTS `node`;
CREATE TABLE `node` (
  `id` int(11) NOT NULL auto_increment,
  `node_condition` varchar(32) default NULL,
  `ip_address` varchar(39) default NULL,
  `port` int(11) default NULL,
  `status` varchar(32) default NULL,
  `weight` int(11) NOT NULL default '1',
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK33AE021210C19B` (`loadbalancer_id`),
  KEY `node_condition_fk` (`node_condition`),
  KEY `node_status_fk` (`status`),
  CONSTRAINT `FK33AE021210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`),
  CONSTRAINT `node_ibfk_1` FOREIGN KEY (`node_condition`) REFERENCES `node_condition` (`name`),
  CONSTRAINT `node_ibfk_2` FOREIGN KEY (`status`) REFERENCES `node_status` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `node_condition`
-- ----------------------------
DROP TABLE IF EXISTS `node_condition`;
CREATE TABLE `node_condition` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `node_status`
-- ----------------------------
DROP TABLE IF EXISTS `node_status`;
CREATE TABLE `node_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `virtual_ip`
-- ----------------------------
DROP TABLE IF EXISTS `virtual_ip`;
CREATE TABLE `virtual_ip` (
  `id` int(11) NOT NULL auto_increment,
  `ip_address` varchar(39) NOT NULL,
  `last_allocation` timestamp NULL default NULL,
  `last_deallocation` timestamp NULL default NULL,
  `type` varchar(32) default NULL,
  `cluster_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `ip_address` (`ip_address`),
  KEY `FK71B4F2DBD56C3599` (`cluster_id`),
  KEY `virtual_ip_type_fk` (`type`),
  CONSTRAINT `FK71B4F2DBD56C3599` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`),
  CONSTRAINT `virtual_ip_ibfk_1` FOREIGN KEY (`type`) REFERENCES `virtual_ip_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `virtual_ip_type`
-- ----------------------------
DROP TABLE IF EXISTS `virtual_ip_type`;
CREATE TABLE `virtual_ip_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `allocated_virtual_ip`
-- ----------------------------
DROP TABLE IF EXISTS `allocated_virtual_ip`;
CREATE TABLE `allocated_virtual_ip` (
  `id` int(11) NOT NULL auto_increment,
  `virtual_ip_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `virtual_ip_id` (`virtual_ip_id`),
  CONSTRAINT `virtual_ip_idfk_1` FOREIGN KEY (`virtual_ip_id`) REFERENCES `virtual_ip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `meta`
-- ----------------------------
DROP TABLE IF EXISTS `meta`;
CREATE TABLE `meta` (
  `meta_key` varchar(128) NOT NULL,
  `meta_value` varchar(128) default NULL,
  UNIQUE KEY `meta_key` (`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `traffic_scripts`
-- ----------------------------
DROP TABLE IF EXISTS `traffic_scripts`;
CREATE TABLE `traffic_scripts` (
  `id` int(11) NOT NULL auto_increment,
  `host_id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `status` varchar(32)  NULL default NULL,
  `notes` varchar(256)  NULL default NULL,
  `created` timestamp NULL default NULL,
  `updated` timestamp NULL default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `lb_host_fk` (`host_id`),
  CONSTRAINT `ts_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Data for enumerations
--

INSERT INTO node_condition values('ENABLED', 'Indicates that the node is enabled');
INSERT INTO node_condition values('DISABLED', 'Indicates that the node is disabled');
INSERT INTO node_condition values('DRAINING', 'Indicates that the node is draining');

INSERT INTO node_status values('ONLINE', 'Indicates that the node is online');
INSERT INTO node_status values('OFFLINE', 'Indicates that the node is offline');

INSERT INTO access_list_type values('ALLOW', 'Indicates that the access list item is white-listed');
INSERT INTO access_list_type values('DENY', 'Indicates that access list item is black-listed');

INSERT INTO virtual_ip_type values('PUBLIC', 'Indicates that the virtual ip is exposed publicly');
INSERT INTO virtual_ip_type values('SERVICENET', 'Indicates that the virtual ip is used for servicing');

INSERT INTO host_status values('ACTIVE_TARGET', 'Indicates that the host is an active target');
INSERT INTO host_status values('BURN_IN', 'Indicates that the host is in the burn-in phase');
INSERT INTO host_status values('OFFLINE', 'Indicates that the host is in the offline status');
INSERT INTO host_status values('ACTIVE', 'Indicates that the host is in the active status');
INSERT INTO host_status values('FAILOVER', 'Indicates that the host is in failover status');

INSERT INTO lb_status values('ACTIVE', 'Indicates that the load balancer is active');
INSERT INTO lb_status values('BUILD', 'Indicates that the load balancer is building');
INSERT INTO lb_status values('PENDING_UPDATE', 'Indicates that the load balancer is pending an update.');
INSERT INTO lb_status values('PENDING_DELETE', 'Indicates that the load balancer is pending a deletion.');
INSERT INTO lb_status values('SUSPENDED', 'Indicates that the load balancer is suspended');
INSERT INTO lb_status values('DELETED', 'Indicates that the load balancer is deleted');
INSERT INTO lb_status values('ERROR', 'Indicates that the load balancer is in an error state.');

INSERT INTO lb_algorithm values('LEAST_CONNECTIONS', 'The node with the fewest number of connections will receive requests',true);
INSERT INTO lb_algorithm values('RANDOM', 'Backend servers are selected at random',true);
INSERT INTO lb_algorithm values('ROUND_ROBIN', 'Connections are routed to each of the backend servers in turn',true);
INSERT INTO lb_algorithm values('WEIGHTED_LEAST_CONNECTIONS', 'Assign each request to a node based on the number of concurrent connections to the node and its weight',true);
INSERT INTO lb_algorithm values('WEIGHTED_ROUND_ROBIN', 'A round robin algorithm, but with different proportions of traffic being directed to the backend nodes.  Weights must be defined as part of the load balancer\'s node configuration',true);

INSERT INTO lb_session_persistence values('NONE', 'Indicates that the load balancer does not have session persistence enabled',true);
INSERT INTO lb_session_persistence values('HTTP_COOKIE', 'Indicates that the load balancer uses HTTP_COOKIE session persistence',true);
INSERT INTO lb_session_persistence values('SOURCE_IP', 'Indicates that the load balancer uses SOURCE_IP session persistence',true);

INSERT INTO lb_protocol values('HTTP', 'The HTTP protocol', 80, TRUE);
INSERT INTO lb_protocol values('FTP', 'The FTP protocol', 21, TRUE);
INSERT INTO lb_protocol values('IMAPv4', 'The IMAPv4 protocol', 143, TRUE);
INSERT INTO lb_protocol values('POP3', 'The POP3 protocol', 110, TRUE);
INSERT INTO lb_protocol values('SMTP', 'The SMTP protocol', 25, TRUE);
INSERT INTO lb_protocol values('LDAP', 'The LDAP protocol', 389, TRUE);
INSERT INTO lb_protocol values('HTTPS', 'The HTTPS protocol', 443, TRUE);
INSERT INTO lb_protocol values('IMAPS', 'The IMAPS protocol', 993, TRUE);
INSERT INTO lb_protocol values('POP3S', 'The POP3S protocol', 995, TRUE);
INSERT INTO lb_protocol values('LDAPS', 'The LDAPS protocol', 636, TRUE);

INSERT INTO lb_data_center values('DFW','Dallas');
INSERT INTO lb_data_center values('ORD','Chicago');
INSERT INTO lb_data_center values('LON','London');

INSERT INTO lb_zone values('A', 'Zone A');
INSERT INTO lb_zone values('B', 'Zone B');

INSERT INTO health_monitor_type values('CONNECT','Indicates the healthmonitor is of type CONNECT');
INSERT INTO health_monitor_type values('HTTP','Indicates the healthmonitor is of type HTTP');
INSERT INTO health_monitor_type values('HTTPS','Indicates the healthmonitor is of type HTTPS');

INSERT INTO meta values('version', '1');


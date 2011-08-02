-- MySQL dump 10.13  Distrib 5.1.57, for apple-darwin10.6.0 (i386)
--
-- Host: 173.203.200.79    Database: loadbalancing
-- ------------------------------------------------------
-- Server version	5.0.51a-24+lenny4-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Table structure for table `access_list`
--

DROP TABLE IF EXISTS `access_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `access_list_event`
--

DROP TABLE IF EXISTS `access_list_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access_list_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `access_list_type`
--

DROP TABLE IF EXISTS `access_list_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access_list_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` int(11) NOT NULL,
  `sha1sum_ipv6` varchar(9) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `sha1sum_ipv6` (`sha1sum_ipv6`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_group`
--

DROP TABLE IF EXISTS `account_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_group` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `grp_id_fk` (`group_id`),
  CONSTRAINT `grp_id_fk` FOREIGN KEY (`group_id`) REFERENCES `group_rate_limit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_limits`
--

DROP TABLE IF EXISTS `account_limits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_limits` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `limit_amount` int(11) NOT NULL,
  `limit_type` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `account_id_limit_type` (`account_id`,`limit_type`),
  KEY `account_limits_account_id` (`account_id`),
  KEY `limit_type` (`limit_type`),
  CONSTRAINT `account_limits_ibfk_1` FOREIGN KEY (`limit_type`) REFERENCES `limit_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_usage`
--

DROP TABLE IF EXISTS `account_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `alert`
--

DROP TABLE IF EXISTS `alert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alert` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `alert_status`
--

DROP TABLE IF EXISTS `alert_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alert_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blacklist_item`
--

DROP TABLE IF EXISTS `blacklist_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blacklist_item` (
  `id` int(11) NOT NULL auto_increment,
  `cidr_block` varchar(64) NOT NULL,
  `ip_version` varchar(32) NOT NULL,
  `type` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `blacklist_type_fk` (`type`),
  CONSTRAINT `blacklist_ibfk_1` FOREIGN KEY (`type`) REFERENCES `blacklist_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blacklist_type`
--

DROP TABLE IF EXISTS `blacklist_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blacklist_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category_type`
--

DROP TABLE IF EXISTS `category_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `category_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster` (
  `id` int(11) NOT NULL auto_increment,
  `description` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `username` varchar(32) NOT NULL,
  `password` varchar(32) NOT NULL,
  `data_center` varchar(32) NOT NULL,
  `cluster_ipv6_cidr` varchar(44) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `data_center` (`data_center`),
  CONSTRAINT `cluster_ibfk_1` FOREIGN KEY (`data_center`) REFERENCES `lb_data_center` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `connection_limit`
--

DROP TABLE IF EXISTS `connection_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `connection_limit_event`
--

DROP TABLE IF EXISTS `connection_limit_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `connection_limit_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event_severity`
--

DROP TABLE IF EXISTS `event_severity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_severity` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event_type`
--

DROP TABLE IF EXISTS `event_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_rate_limit`
--

DROP TABLE IF EXISTS `group_rate_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_rate_limit` (
  `id` int(11) NOT NULL auto_increment,
  `group_name` varchar(32) NOT NULL,
  `group_desc` varchar(128) NOT NULL,
  `is_default` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`group_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_monitor`
--

DROP TABLE IF EXISTS `health_monitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_monitor_event`
--

DROP TABLE IF EXISTS `health_monitor_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `health_monitor_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_monitor_type`
--

DROP TABLE IF EXISTS `health_monitor_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `health_monitor_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `host`
--

DROP TABLE IF EXISTS `host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host` (
  `id` int(11) NOT NULL auto_increment,
  `core_device_id` varchar(64) NOT NULL,
  `host_status` varchar(32) NOT NULL,
  `management_ip` varchar(255) NOT NULL,
  `max_concurrent_connections` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `endpoint` varchar(255) NOT NULL,
  `host_name` varchar(255) NOT NULL,
  `cluster_id` int(11) NOT NULL,
  `zone` varchar(4) NOT NULL,
  `endpoint_active` tinyint(1) NOT NULL,
  `ipv6_servicenet` varchar(39) default NULL,
  `ipv4_servicenet` varchar(39) default NULL,
  `ipv4_public` varchar(39) default NULL,
  `ipv6_public` varchar(39) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `target_host` (`host_name`),
  KEY `FK30F5A8D56C3599` (`cluster_id`),
  KEY `host_status_fk` (`host_status`),
  KEY `host_ibfk_3` (`zone`),
  CONSTRAINT `FK30F5A8D56C3599` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`),
  CONSTRAINT `host_ibfk_1` FOREIGN KEY (`host_status`) REFERENCES `host_status` (`name`),
  CONSTRAINT `host_ibfk_3` FOREIGN KEY (`zone`) REFERENCES `lb_zone` (`zone`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `host_backup`
--

DROP TABLE IF EXISTS `host_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `host_status`
--

DROP TABLE IF EXISTS `host_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ip_version`
--

DROP TABLE IF EXISTS `ip_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ip_version` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_algorithm`
--

DROP TABLE IF EXISTS `lb_algorithm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_algorithm` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_data_center`
--

DROP TABLE IF EXISTS `lb_data_center`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_data_center` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_protocol`
--

DROP TABLE IF EXISTS `lb_protocol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_protocol` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `port` int(11) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_rate_limit`
--

DROP TABLE IF EXISTS `lb_rate_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_rate_limit` (
  `id` int(11) NOT NULL auto_increment,
  `expiration_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `max_requests_per_second` int(11) NOT NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `rl_loadbalancer_id_fk` (`loadbalancer_id`),
  CONSTRAINT `rate_limit_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_session_persistence`
--

DROP TABLE IF EXISTS `lb_session_persistence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_session_persistence` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_status`
--

DROP TABLE IF EXISTS `lb_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_suspension`
--

DROP TABLE IF EXISTS `lb_suspension`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_suspension` (
  `id` int(11) NOT NULL auto_increment,
  `reason` varchar(255) NOT NULL,
  `user` varchar(255) default NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `suspension_fk_1` (`loadbalancer_id`),
  CONSTRAINT `suspension_fk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_usage`
--

DROP TABLE IF EXISTS `lb_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  `event_type` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FKBBE47D981210C19B` (`loadbalancer_id`),
  KEY `start_time` (`start_time`),
  KEY `end_time` (`end_time`),
  CONSTRAINT `FKBBE47D981210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_zone`
--

DROP TABLE IF EXISTS `lb_zone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_zone` (
  `zone` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`zone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `limit_type`
--

DROP TABLE IF EXISTS `limit_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `limit_type` (
  `name` varchar(32) NOT NULL,
  `default_value` int(11) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loadbalancer`
--

DROP TABLE IF EXISTS `loadbalancer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  `max_concurrent_connections` int(32) default NULL,
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loadbalancer_event`
--

DROP TABLE IF EXISTS `loadbalancer_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loadbalancer_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loadbalancer_service_event`
--

DROP TABLE IF EXISTS `loadbalancer_service_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loadbalancer_service_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loadbalancer_virtualip`
--

DROP TABLE IF EXISTS `loadbalancer_virtualip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loadbalancer_virtualip` (
  `loadbalancer_id` int(11) NOT NULL,
  `virtualip_id` int(11) NOT NULL,
  `port` int(11) NOT NULL,
  PRIMARY KEY  (`loadbalancer_id`,`virtualip_id`),
  UNIQUE KEY `virtual_ip_port` (`virtualip_id`,`port`),
  KEY `FKEDA4B0EF1210C19B` (`loadbalancer_id`),
  KEY `FKEDA4B0EF6D38D2F9` (`virtualip_id`),
  CONSTRAINT `FKEDA4B0EF1210C19B` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`),
  CONSTRAINT `FKEDA4B0EF6D38D2F9` FOREIGN KEY (`virtualip_id`) REFERENCES `virtual_ip_ipv4` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loadbalancer_virtualipv6`
--

DROP TABLE IF EXISTS `loadbalancer_virtualipv6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loadbalancer_virtualipv6` (
  `loadbalancer_id` int(11) NOT NULL,
  `virtualip6_id` int(11) NOT NULL,
  `port` int(11) NOT NULL,
  PRIMARY KEY  (`loadbalancer_id`,`virtualip6_id`),
  UNIQUE KEY `virtualip6_port` (`virtualip6_id`,`port`),
  CONSTRAINT `loadbalancer_virtualipv6_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`),
  CONSTRAINT `loadbalancer_virtualipv6_ibfk_2` FOREIGN KEY (`virtualip6_id`) REFERENCES `virtual_ip_ipv6` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meta`
--

DROP TABLE IF EXISTS `meta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meta` (
  `meta_key` varchar(128) NOT NULL,
  `meta_value` varchar(128) default NULL,
  UNIQUE KEY `meta_key` (`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `node`
--

DROP TABLE IF EXISTS `node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `node_condition`
--

DROP TABLE IF EXISTS `node_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node_condition` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `node_event`
--

DROP TABLE IF EXISTS `node_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `node_status`
--

DROP TABLE IF EXISTS `node_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `session_persistence_event`
--

DROP TABLE IF EXISTS `session_persistence_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `session_persistence_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state`
--

DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `state` (
  `id` int(11) NOT NULL auto_increment,
  `state` varchar(50) default NULL,
  `jobname` varchar(1000) default NULL,
  `inputpath` varchar(200) default NULL,
  `start_time` datetime default NULL,
  `end_time` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ticket`
--

DROP TABLE IF EXISTS `ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ticket` (
  `id` int(11) NOT NULL auto_increment,
  `ticket_id` varchar(32) NOT NULL,
  `comment` text NOT NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `ticket_loadbalancer_id` (`loadbalancer_id`),
  CONSTRAINT `ticket_loadbalancer_id_fk` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `traffic_scripts`
--

DROP TABLE IF EXISTS `traffic_scripts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `traffic_scripts` (
  `id` int(11) NOT NULL auto_increment,
  `host_id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `status` varchar(32) default NULL,
  `notes` varchar(256) default NULL,
  `created` timestamp NULL default NULL,
  `updated` timestamp NULL default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `lb_host_fk` (`host_id`),
  CONSTRAINT `ts_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtual_ip_event`
--

DROP TABLE IF EXISTS `virtual_ip_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_ip_event` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtual_ip_ipv4`
--

DROP TABLE IF EXISTS `virtual_ip_ipv4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_ip_ipv4` (
  `id` int(11) NOT NULL auto_increment,
  `ip_address` varchar(39) NOT NULL,
  `last_allocation` timestamp NULL default NULL,
  `last_deallocation` timestamp NULL default NULL,
  `type` varchar(32) default NULL,
  `cluster_id` int(11) default NULL,
  `is_allocated` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `ip_address` (`ip_address`),
  KEY `FK71B4F2DBD56C3599` (`cluster_id`),
  KEY `virtual_ip_type_fk` (`type`),
  CONSTRAINT `FK71B4F2DBD56C3599` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`),
  CONSTRAINT `virtual_ip_ipv4_ibfk_1` FOREIGN KEY (`type`) REFERENCES `virtual_ip_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtual_ip_ipv6`
--

DROP TABLE IF EXISTS `virtual_ip_ipv6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_ip_ipv6` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `vip_octets` int(11) NOT NULL,
  `cluster_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `account_octets` (`account_id`,`vip_octets`),
  KEY `cluster_id` (`cluster_id`),
  KEY `account_id` (`account_id`),
  KEY `vip_octets` (`vip_octets`),
  CONSTRAINT `virtual_ip_ipv6_ibfk_1` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9000000 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtual_ip_type`
--

DROP TABLE IF EXISTS `virtual_ip_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_ip_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-07-12 11:49:22

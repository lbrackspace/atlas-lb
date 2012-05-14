-- MySQL dump 10.13  Distrib 5.1.57, for apple-darwin10.7.4 (i386)
--
-- Host: 173.203.200.79    Database: loadbalancing_usage
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
-- Table structure for table `host_usage`
--

DROP TABLE IF EXISTS `host_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_usage` (
  `id` int(11) NOT NULL auto_increment,
  `host_id` int(11) NOT NULL,
  `bandwidth_bytes_in` bigint(20) NOT NULL default '0',
  `bandwidth_bytes_out` bigint(20) NOT NULL default '0',
  `snapshot_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`),
  KEY `host_usage_host_key` (`host_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1745 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_usage`
--

DROP TABLE IF EXISTS `lb_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_usage` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `avg_concurrent_conns` double NOT NULL default '0',
  `cum_bandwidth_bytes_in` bigint(20) NOT NULL default '0',
  `cum_bandwidth_bytes_out` bigint(20) NOT NULL default '0',
  `last_bandwidth_bytes_in` bigint(20) NOT NULL default '0',
  `last_bandwidth_bytes_out` bigint(20) NOT NULL default '0',
  `start_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `end_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `num_polls` int(11) NOT NULL default '0',
  `num_vips` int(11) NOT NULL default '1',
  `tags_bitmask` int(3) NOT NULL default '0',
  `event_type` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `lb_usage_account_key` (`account_id`),
  KEY `lb_usage_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1778 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_usage_event`
--

DROP TABLE IF EXISTS `lb_usage_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_usage_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `start_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `num_vips` int(11) NOT NULL default '1',
  `event_type` varchar(32) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `lb_usage_event_account_key` (`account_id`),
  KEY `lb_usage_event_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=589 DEFAULT CHARSET=utf8;
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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-06-13 15:55:04

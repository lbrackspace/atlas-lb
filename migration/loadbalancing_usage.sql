-- MySQL dump 10.13  Distrib 5.1.61, for apple-darwin10.6.0 (i386)
--
-- Host: 10.12.99.14    Database: loadbalancing_usage
-- ------------------------------------------------------
-- Server version	5.1.61-rel13.2-log

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
-- Table structure for table `event_type`
--

DROP TABLE IF EXISTS `event_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `host_usage`
--

DROP TABLE IF EXISTS `host_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `host_id` int(11) NOT NULL,
  `bandwidth_bytes_in` bigint(20) NOT NULL DEFAULT '0',
  `bandwidth_bytes_out` bigint(20) NOT NULL DEFAULT '0',
  `snapshot_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `host_usage_host_key` (`host_id`)
) ENGINE=InnoDB AUTO_INCREMENT=504326 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_usage`
--

DROP TABLE IF EXISTS `lb_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `avg_concurrent_conns` double NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_in` bigint(20) NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_out` bigint(20) NOT NULL DEFAULT '0',
  `last_bandwidth_bytes_in` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out` bigint(20) DEFAULT NULL,
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `num_polls` int(11) NOT NULL DEFAULT '0',
  `num_vips` int(11) NOT NULL DEFAULT '1',
  `tags_bitmask` int(3) NOT NULL DEFAULT '0',
  `event_type` varchar(32) DEFAULT NULL,
  `avg_concurrent_conns_ssl` double NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_in_ssl` bigint(20) NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_out_ssl` bigint(20) NOT NULL DEFAULT '0',
  `last_bandwidth_bytes_in_ssl` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out_ssl` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lb_usage_account_key` (`account_id`),
  KEY `lb_usage_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=885279 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lb_usage_event`
--

DROP TABLE IF EXISTS `lb_usage_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_usage_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `num_vips` int(11) NOT NULL DEFAULT '1',
  `event_type` varchar(32) NOT NULL,
  `last_bandwidth_bytes_in` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out` bigint(20) DEFAULT NULL,
  `last_concurrent_conns` int(11) DEFAULT NULL,
  `last_bandwidth_bytes_in_ssl` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out_ssl` bigint(20) DEFAULT NULL,
  `last_concurrent_conns_ssl` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lb_usage_event_account_key` (`account_id`),
  KEY `lb_usage_event_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17271 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meta`
--

DROP TABLE IF EXISTS `meta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meta` (
  `meta_key` varchar(128) NOT NULL,
  `meta_value` varchar(128) DEFAULT NULL,
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

-- Dump completed on 2013-05-02 19:20:45

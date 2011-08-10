-- MySQL dump 10.13  Distrib 5.1.57, for apple-darwin10.7.4 (i386)
--
-- Host: localhost    Database: loadbalancing
-- ------------------------------------------------------
-- Server version	5.1.57

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
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_ipv6_cidr` varchar(43) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `connection_throttle`
--

DROP TABLE IF EXISTS `connection_throttle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `connection_throttle` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_connectionrate` int(11) NOT NULL,
  `max_connection` int(11) NOT NULL,
  `min_connections` int(11) NOT NULL,
  `rate_interval` int(11) NOT NULL,
  `loadbalancer_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK45EA176BD166A451` (`loadbalancer_id`),
  CONSTRAINT `FK45EA176BD166A451` FOREIGN KEY (`loadbalancer_id`) REFERENCES `load_balancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_monitor`
--

DROP TABLE IF EXISTS `health_monitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `health_monitor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attempts_before_deactivation` int(11) NOT NULL,
  `body_regex` varchar(128) DEFAULT NULL,
  `delay` int(11) NOT NULL,
  `path` varchar(128) DEFAULT NULL,
  `status_regex` varchar(128) DEFAULT NULL,
  `timeout` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `loadbalancer_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK59EB4A77D166A451` (`loadbalancer_id`),
  CONSTRAINT `FK59EB4A77D166A451` FOREIGN KEY (`loadbalancer_id`) REFERENCES `load_balancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_monitor_type`
--

DROP TABLE IF EXISTS `health_monitor_type`;
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `core_device_id` varchar(255) NOT NULL,
  `endpoint` varchar(255) NOT NULL,
  `endpoint_active` bit(1) DEFAULT NULL,
  `host_name` varchar(255) NOT NULL,
  `ipv4_public` varchar(255) DEFAULT NULL,
  `ipv4_servicenet` varchar(255) DEFAULT NULL,
  `ipv6_public` varchar(255) DEFAULT NULL,
  `ipv6_servicenet` varchar(255) DEFAULT NULL,
  `management_ip` varchar(255) NOT NULL,
  `max_concurrent_connections` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `cluster_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK30F5A85B94F923` (`cluster_id`),
  CONSTRAINT `FK30F5A85B94F923` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ip_version`
--

DROP TABLE IF EXISTS `ip_version`;
CREATE TABLE `ip_version` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `load_balancer`
--

DROP TABLE IF EXISTS `load_balancer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load_balancer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `algorithm` varchar(255) NOT NULL,
  `connection_logging` bit(1) NOT NULL,
  `created` datetime DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `port` int(11) NOT NULL,
  `protocol` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `host_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK5F4FFC4F28B0B7B1` (`host_id`),
  CONSTRAINT `FK5F4FFC4F28B0B7B1` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `lb_algorithm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lb_algorithm` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  `enabled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `load_balancer_usage`
--

DROP TABLE IF EXISTS `load_balancer_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load_balancer_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `transfer_bytes_in` bigint(20) NOT NULL,
  `transfer_bytes_out` bigint(20) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK63225B1D166A451` (`loadbalancer_id`),
  CONSTRAINT `FK63225B1D166A451` FOREIGN KEY (`loadbalancer_id`) REFERENCES `load_balancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  `port` int(11) DEFAULT NULL,
  PRIMARY KEY (`loadbalancer_id`,`virtualip_id`),
  KEY `FKEDA4B0EFD166A451` (`loadbalancer_id`),
  KEY `FKEDA4B0EFC3EDC03` (`virtualip_id`),
  CONSTRAINT `FKEDA4B0EFC3EDC03` FOREIGN KEY (`virtualip_id`) REFERENCES `virtual_ip_ipv4` (`id`),
  CONSTRAINT `FKEDA4B0EFD166A451` FOREIGN KEY (`loadbalancer_id`) REFERENCES `load_balancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  `port` int(11) DEFAULT NULL,
  PRIMARY KEY (`loadbalancer_id`,`virtualip6_id`),
  KEY `FK173C3FAFD166A451` (`loadbalancer_id`),
  KEY `FK173C3FAF475792F1` (`virtualip6_id`),
  CONSTRAINT `FK173C3FAF475792F1` FOREIGN KEY (`virtualip6_id`) REFERENCES `virtual_ip_ipv6` (`id`),
  CONSTRAINT `FK173C3FAFD166A451` FOREIGN KEY (`loadbalancer_id`) REFERENCES `load_balancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `node`
--

DROP TABLE IF EXISTS `node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `node_condition` varchar(255) DEFAULT NULL,
  `ip_address` varchar(39) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `weight` int(11) NOT NULL,
  `loadbalancer_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK33AE02D166A451` (`loadbalancer_id`),
  CONSTRAINT `FK33AE02D166A451` FOREIGN KEY (`loadbalancer_id`) REFERENCES `load_balancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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

--
-- Table structure for table `virtual_ip_ipv4`
--

DROP TABLE IF EXISTS `virtual_ip_ipv4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_ip_ipv4` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(39) NOT NULL,
  `is_allocated` bit(1) NOT NULL,
  `last_allocation` datetime DEFAULT NULL,
  `last_deallocation` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `cluster_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `ip_address` (`ip_address`),
  KEY `FKEEB3C4C95B94F923` (`cluster_id`),
  CONSTRAINT `FKEEB3C4C95B94F923` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtual_ip_ipv6`
--

DROP TABLE IF EXISTS `virtual_ip_ipv6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_ip_ipv6` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `vip_octets` int(11) NOT NULL,
  `cluster_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FKEEB3C4CB5B94F923` (`cluster_id`),
  CONSTRAINT `FKEEB3C4CB5B94F923` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtual_ip_type`
--

DROP TABLE IF EXISTS `virtual_ip_type`;
CREATE TABLE `virtual_ip_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-08-10 14:27:25

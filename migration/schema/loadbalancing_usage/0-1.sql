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

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `loadbalancing_usage` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `loadbalancing_usage`;

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
  `avg_concurrent_conns` double NOT NULL DEFAULT 0,
  `cum_bandwidth_bytes_in` bigint NOT NULL DEFAULT 0,
  `cum_bandwidth_bytes_out` bigint NOT NULL DEFAULT 0,
  `last_bandwidth_bytes_in` bigint NOT NULL DEFAULT 0,
  `last_bandwidth_bytes_out` bigint NOT NULL DEFAULT 0,
  `start_time` timestamp NOT NULL,
  `end_time` timestamp NOT NULL,
  `num_polls` int(11) NOT NULL DEFAULT 0,
  `num_vips` int(11) NOT NULL DEFAULT 1,
  `tags_bitmask` int(3) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_id_lb_id_start_time` (`account_id`, `loadbalancer_id`, `start_time`),
  KEY `lb_usage_account_key` (`account_id`),
  KEY `lb_usage_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

-- ----------------------------
--  Table structure for `meta`
-- ----------------------------
DROP TABLE IF EXISTS `meta`;
CREATE TABLE `meta` (
  `meta_key` varchar(128) NOT NULL,
  `meta_value` varchar(128) default NULL,
  UNIQUE KEY `meta_key` (`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO meta values('version', '1');


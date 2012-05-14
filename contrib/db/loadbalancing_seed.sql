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
-- Dumping data for table `access_list`
--

LOCK TABLES `access_list` WRITE;
/*!40000 ALTER TABLE `access_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `access_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `access_list_event`
--

LOCK TABLES `access_list_event` WRITE;
/*!40000 ALTER TABLE `access_list_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `access_list_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `access_list_type`
--

LOCK TABLES `access_list_type` WRITE;
/*!40000 ALTER TABLE `access_list_type` DISABLE KEYS */;
INSERT INTO `access_list_type` VALUES ('ALLOW','Indicates that the access list item is white-listed');
INSERT INTO `access_list_type` VALUES ('DENY','Indicates that access list item is black-listed');
/*!40000 ALTER TABLE `access_list_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account_group`
--

LOCK TABLES `account_group` WRITE;
/*!40000 ALTER TABLE `account_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account_limits`
--

LOCK TABLES `account_limits` WRITE;
/*!40000 ALTER TABLE `account_limits` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_limits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `account_usage`
--

LOCK TABLES `account_usage` WRITE;
/*!40000 ALTER TABLE `account_usage` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_usage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `alert`
--

LOCK TABLES `alert` WRITE;
/*!40000 ALTER TABLE `alert` DISABLE KEYS */;
/*!40000 ALTER TABLE `alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `alert_status`
--

LOCK TABLES `alert_status` WRITE;
/*!40000 ALTER TABLE `alert_status` DISABLE KEYS */;
/*!40000 ALTER TABLE `alert_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `blacklist_item`
--

LOCK TABLES `blacklist_item` WRITE;
/*!40000 ALTER TABLE `blacklist_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `blacklist_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `blacklist_type`
--

LOCK TABLES `blacklist_type` WRITE;
/*!40000 ALTER TABLE `blacklist_type` DISABLE KEYS */;
INSERT INTO `blacklist_type` VALUES ('ACCESSLIST','Blacklisted lists');
INSERT INTO `blacklist_type` VALUES ('NODE','Blacklisted Nodes');
/*!40000 ALTER TABLE `blacklist_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `category_type`
--

LOCK TABLES `category_type` WRITE;
/*!40000 ALTER TABLE `category_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `cluster`
--

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'Cluster Delta','My Cluster','lbaas_dev','1972d851ab4605cd124562bb38704d9e','DFW','fd24:f480:ce44:91bc::/64');
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `connection_limit`
--

LOCK TABLES `connection_limit` WRITE;
/*!40000 ALTER TABLE `connection_limit` DISABLE KEYS */;
/*!40000 ALTER TABLE `connection_limit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `connection_limit_event`
--

LOCK TABLES `connection_limit_event` WRITE;
/*!40000 ALTER TABLE `connection_limit_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `connection_limit_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `event_severity`
--

LOCK TABLES `event_severity` WRITE;
/*!40000 ALTER TABLE `event_severity` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_severity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `event_type`
--

LOCK TABLES `event_type` WRITE;
/*!40000 ALTER TABLE `event_type` DISABLE KEYS */;
INSERT INTO `event_type` VALUES ('CREATE_ACCESS_LIST','Created Access List');
INSERT INTO `event_type` VALUES ('CREATE_CONNECTION_THROTTLE','Created Connection Throttle');
INSERT INTO `event_type` VALUES ('CREATE_HEALTH_MONITOR','Created Health Monitor');
INSERT INTO `event_type` VALUES ('CREATE_LOADBALANCER','A load balancer was created');
INSERT INTO `event_type` VALUES ('CREATE_NODE','Node Created');
INSERT INTO `event_type` VALUES ('CREATE_SESSION_PERSISTENCE','Created Session Persisitence');
INSERT INTO `event_type` VALUES ('CREATE_VIRTUAL_IP','A virtual ip was created');
INSERT INTO `event_type` VALUES ('DELETE_ACCESS_LIST','Deleted Access List');
INSERT INTO `event_type` VALUES ('DELETE_CONNECTION_THROTTLE','Deleted Connection Throttle');
INSERT INTO `event_type` VALUES ('DELETE_HEALTH_MONITOR','Deleted Health Monitor');
INSERT INTO `event_type` VALUES ('DELETE_LOADBALANCER','A load balancer was deleted');
INSERT INTO `event_type` VALUES ('DELETE_NETWORK_ITEM','Deleted Network Item');
INSERT INTO `event_type` VALUES ('DELETE_NODE','Node deleted');
INSERT INTO `event_type` VALUES ('DELETE_SESSION_PERSISTENCE','Deleted Session Persistence');
INSERT INTO `event_type` VALUES ('DELETE_VIRTUAL_IP','A virtual ip was deleted');
INSERT INTO `event_type` VALUES ('SSL_OFF','SSL was turned off');
INSERT INTO `event_type` VALUES ('SSL_ON','SSL was turned on');
INSERT INTO `event_type` VALUES ('SUSPEND_LOADBALANCER','A load balancer was suspended');
INSERT INTO `event_type` VALUES ('UNSUSPEND_LOADBALANCER','A load balancer was unsuspended');
INSERT INTO `event_type` VALUES ('UPDATE_ACCESS_LIST','Update Access List');
INSERT INTO `event_type` VALUES ('UPDATE_CONNECTION_LOGGING','Updated Connection Logging');
INSERT INTO `event_type` VALUES ('UPDATE_CONNECTION_THROTTLE','Update Connection Throttle');
INSERT INTO `event_type` VALUES ('UPDATE_HEALTH_MONITOR','Updated Health Monitor');
INSERT INTO `event_type` VALUES ('UPDATE_LOADBALANCER','Loadbalancer updated');
INSERT INTO `event_type` VALUES ('UPDATE_NODE','Node updated');
INSERT INTO `event_type` VALUES ('UPDATE_SESSION_PERSISTENCE','Updated Session Persisitence');
/*!40000 ALTER TABLE `event_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `group_rate_limit`
--

LOCK TABLES `group_rate_limit` WRITE;
/*!40000 ALTER TABLE `group_rate_limit` DISABLE KEYS */;
INSERT INTO `group_rate_limit` VALUES (1,'customer_group','customer_limit_group',1);
/*!40000 ALTER TABLE `group_rate_limit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `health_monitor`
--

LOCK TABLES `health_monitor` WRITE;
/*!40000 ALTER TABLE `health_monitor` DISABLE KEYS */;
/*!40000 ALTER TABLE `health_monitor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `health_monitor_event`
--

LOCK TABLES `health_monitor_event` WRITE;
/*!40000 ALTER TABLE `health_monitor_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `health_monitor_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `health_monitor_type`
--

LOCK TABLES `health_monitor_type` WRITE;
/*!40000 ALTER TABLE `health_monitor_type` DISABLE KEYS */;
INSERT INTO `health_monitor_type` VALUES ('CONNECT','Indicates the healthmonitor is of type CONNECT');
INSERT INTO `health_monitor_type` VALUES ('HTTP','Indicates the healthmonitor is of type HTTP');
INSERT INTO `health_monitor_type` VALUES ('HTTPS','Indicates the healthmonitor is of type HTTPS');
/*!40000 ALTER TABLE `health_monitor_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `host`
--

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (1,'100','ACTIVE_TARGET','10.2.4.4',1500,'host1_cluster1','https://localhost:9090/soap','ztm-n01.dev.lbaas..com',1,'A',0,'4FDE:0000:0000:0002:0022:F376:FF3B:AB3F','10.2.2.6','172.1.1.1','4FDE:0000:0000:0002:0022:F376:FF3B:AB3F');
INSERT INTO `host` VALUES (2,'200','ACTIVE_TARGET','10.2.3.5',1600,'host2_cluster1','https://localhost:9090/soap','ztm-n02.dev.lbaas..com',1,'A',0,'4FDE:0000:0000:0002:0022:F376:FF3B:AB3F','10.3.4.5','172.2.2.5','4FDE:0000:0000:0002:0022:F376:FF3B:AB3F');
INSERT INTO `host` VALUES (3,'100','FAILOVER','10.4.4.5',1700,'host3_cluster1','https://localhost:9090/soap','ztm-n03.dev.lbaas..com',1,'B',1,'4FDE:0000:0000:0002:0022:F376:FF3B:AB3F','10.2.3.1','172.2.2.4','4FDE:0000:0000:0002:0022:F376:FF3B:AB3F');
INSERT INTO `host` VALUES (4,'400','FAILOVER','10.2.5.5',1800,'host3_cluster1','https://localhost:9090/soap','ztm-n04.dev.lbaas..com',1,'B',1,'4FDE:0000:0000:0002:0022:F376:FF3B:AB3F','10.1.3.1','172.1.3.4','4FDE:0000:0000:0002:0022:F376:FF3B:AB3F');
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `host_backup`
--

LOCK TABLES `host_backup` WRITE;
/*!40000 ALTER TABLE `host_backup` DISABLE KEYS */;
/*!40000 ALTER TABLE `host_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `host_status`
--

LOCK TABLES `host_status` WRITE;
/*!40000 ALTER TABLE `host_status` DISABLE KEYS */;
INSERT INTO `host_status` VALUES ('ACTIVE','Indicates that the host is in the active status');
INSERT INTO `host_status` VALUES ('ACTIVE_TARGET','Indicates that the host is an active target');
INSERT INTO `host_status` VALUES ('BURN_IN','Indicates that the host is in the burn-in phase');
INSERT INTO `host_status` VALUES ('FAILOVER','Indicates that the host is in failover status');
INSERT INTO `host_status` VALUES ('OFFLINE','Indicates that the host is in the offline status');
/*!40000 ALTER TABLE `host_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `ip_version`
--

LOCK TABLES `ip_version` WRITE;
/*!40000 ALTER TABLE `ip_version` DISABLE KEYS */;
INSERT INTO `ip_version` VALUES ('IPV4','A IPV4 ip address');
INSERT INTO `ip_version` VALUES ('IPV6','A IPV6 ip address');
/*!40000 ALTER TABLE `ip_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_algorithm`
--

LOCK TABLES `lb_algorithm` WRITE;
/*!40000 ALTER TABLE `lb_algorithm` DISABLE KEYS */;
INSERT INTO `lb_algorithm` VALUES ('LEAST_CONNECTIONS','The node with the fewest number of connections will receive requests',1);
INSERT INTO `lb_algorithm` VALUES ('RANDOM','Backend servers are selected at random',1);
INSERT INTO `lb_algorithm` VALUES ('ROUND_ROBIN','Connections are routed to each of the backend servers in turn',1);
INSERT INTO `lb_algorithm` VALUES ('WEIGHTED_LEAST_CONNECTIONS','Assign each request to a node based on the number of concurrent connections to the node and its weight',1);
INSERT INTO `lb_algorithm` VALUES ('WEIGHTED_ROUND_ROBIN','A round robin algorithm, but with different proportions of traffic being directed to the backend nodes.  Weights must be defined',1);
/*!40000 ALTER TABLE `lb_algorithm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_data_center`
--

LOCK TABLES `lb_data_center` WRITE;
/*!40000 ALTER TABLE `lb_data_center` DISABLE KEYS */;
INSERT INTO `lb_data_center` VALUES ('DFW','Dallas');
INSERT INTO `lb_data_center` VALUES ('LON','London');
INSERT INTO `lb_data_center` VALUES ('ORD','Chicago');
/*!40000 ALTER TABLE `lb_data_center` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_protocol`
--

LOCK TABLES `lb_protocol` WRITE;
/*!40000 ALTER TABLE `lb_protocol` DISABLE KEYS */;
INSERT INTO `lb_protocol` VALUES ('FTP','The FTP protocol',21,1);
INSERT INTO `lb_protocol` VALUES ('HTTP','The HTTP protocol',80,1);
INSERT INTO `lb_protocol` VALUES ('HTTPS','The HTTPS protocol',443,1);
INSERT INTO `lb_protocol` VALUES ('IMAPS','The IMAPS protocol',993,1);
INSERT INTO `lb_protocol` VALUES ('IMAPv2','The IMAPv2 protocol',143,1);
INSERT INTO `lb_protocol` VALUES ('IMAPv3','The IMAPv3 protocol',220,1);
INSERT INTO `lb_protocol` VALUES ('IMAPv4','The IMAPv4 protocol',143,1);
INSERT INTO `lb_protocol` VALUES ('LDAP','The LDAP protocol',389,1);
INSERT INTO `lb_protocol` VALUES ('LDAPS','The LDAPS protocol',636,1);
INSERT INTO `lb_protocol` VALUES ('POP3','The POP3 protocol',110,1);
INSERT INTO `lb_protocol` VALUES ('POP3S','The POP3S protocol',995,1);
INSERT INTO `lb_protocol` VALUES ('SMTP','The SMTP protocol',25,1);
INSERT INTO `lb_protocol` VALUES ('TCP','The TCP protocol',0,1);
/*!40000 ALTER TABLE `lb_protocol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_rate_limit`
--

LOCK TABLES `lb_rate_limit` WRITE;
/*!40000 ALTER TABLE `lb_rate_limit` DISABLE KEYS */;
/*!40000 ALTER TABLE `lb_rate_limit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_session_persistence`
--

LOCK TABLES `lb_session_persistence` WRITE;
/*!40000 ALTER TABLE `lb_session_persistence` DISABLE KEYS */;
INSERT INTO `lb_session_persistence` VALUES ('HTTP_COOKIE','Indicates that the load balancer uses HTTP_COOKIE session persistence',1);
INSERT INTO `lb_session_persistence` VALUES ('NONE','Indicates that the load balancer does not have session persistence enabled',1);
INSERT INTO `lb_session_persistence` VALUES ('SOURCE_IP','Indicates that the load balancer uses SOURCE_IP session persistence',1);
/*!40000 ALTER TABLE `lb_session_persistence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_status`
--

LOCK TABLES `lb_status` WRITE;
/*!40000 ALTER TABLE `lb_status` DISABLE KEYS */;
INSERT INTO `lb_status` VALUES ('ACTIVE','Indicates that the load balancer is active');
INSERT INTO `lb_status` VALUES ('BUILD','Indicates that the load balancer is building');
INSERT INTO `lb_status` VALUES ('DELETED','Indicates that the load balancer is deleted');
INSERT INTO `lb_status` VALUES ('ERROR','Indicates that the load balancer is in an error state.');
INSERT INTO `lb_status` VALUES ('PENDING_DELETE','Indicates that the load balancer is pending a deletion.');
INSERT INTO `lb_status` VALUES ('PENDING_UPDATE','Indicates that the load balancer is pending an update.');
INSERT INTO `lb_status` VALUES ('SUSPENDED','Indicates that the load balancer is suspended');
/*!40000 ALTER TABLE `lb_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_suspension`
--

LOCK TABLES `lb_suspension` WRITE;
/*!40000 ALTER TABLE `lb_suspension` DISABLE KEYS */;
/*!40000 ALTER TABLE `lb_suspension` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_usage`
--

LOCK TABLES `lb_usage` WRITE;
/*!40000 ALTER TABLE `lb_usage` DISABLE KEYS */;
/*!40000 ALTER TABLE `lb_usage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lb_zone`
--

LOCK TABLES `lb_zone` WRITE;
/*!40000 ALTER TABLE `lb_zone` DISABLE KEYS */;
INSERT INTO `lb_zone` VALUES ('A','Zone A');
INSERT INTO `lb_zone` VALUES ('B','Zone B');
/*!40000 ALTER TABLE `lb_zone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `limit_type`
--

LOCK TABLES `limit_type` WRITE;
/*!40000 ALTER TABLE `limit_type` DISABLE KEYS */;
INSERT INTO `limit_type` VALUES ('ACCESS_LIST_LIMIT',100,'Max number of items for an access list');
INSERT INTO `limit_type` VALUES ('BATCH_DELETE_LIMIT',10,'Max number of items that can be deleted for batch delete operations');
INSERT INTO `limit_type` VALUES ('IPV6_LIMIT',25,'Max number of IPv6 addresses for a load balancer');
INSERT INTO `limit_type` VALUES ('LOADBALANCER_LIMIT',25,'Max number of load balancers for an account');
INSERT INTO `limit_type` VALUES ('NODE_LIMIT',25,'Max number of nodes for a load balancer');
/*!40000 ALTER TABLE `limit_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `loadbalancer`
--

LOCK TABLES `loadbalancer` WRITE;
/*!40000 ALTER TABLE `loadbalancer` DISABLE KEYS */;
/*!40000 ALTER TABLE `loadbalancer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `loadbalancer_event`
--

LOCK TABLES `loadbalancer_event` WRITE;
/*!40000 ALTER TABLE `loadbalancer_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `loadbalancer_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `loadbalancer_service_event`
--

LOCK TABLES `loadbalancer_service_event` WRITE;
/*!40000 ALTER TABLE `loadbalancer_service_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `loadbalancer_service_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `loadbalancer_virtualip`
--

LOCK TABLES `loadbalancer_virtualip` WRITE;
/*!40000 ALTER TABLE `loadbalancer_virtualip` DISABLE KEYS */;
/*!40000 ALTER TABLE `loadbalancer_virtualip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `loadbalancer_virtualipv6`
--

LOCK TABLES `loadbalancer_virtualipv6` WRITE;
/*!40000 ALTER TABLE `loadbalancer_virtualipv6` DISABLE KEYS */;
/*!40000 ALTER TABLE `loadbalancer_virtualipv6` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `meta`
--

LOCK TABLES `meta` WRITE;
/*!40000 ALTER TABLE `meta` DISABLE KEYS */;
INSERT INTO `meta` VALUES ('version','28');
/*!40000 ALTER TABLE `meta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `node`
--

LOCK TABLES `node` WRITE;
/*!40000 ALTER TABLE `node` DISABLE KEYS */;
/*!40000 ALTER TABLE `node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `node_condition`
--

LOCK TABLES `node_condition` WRITE;
/*!40000 ALTER TABLE `node_condition` DISABLE KEYS */;
INSERT INTO `node_condition` VALUES ('DISABLED','Indicates that the node is disabled');
INSERT INTO `node_condition` VALUES ('DRAINING','Indicates that the node is draining');
INSERT INTO `node_condition` VALUES ('ENABLED','Indicates that the node is enabled');
/*!40000 ALTER TABLE `node_condition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `node_event`
--

LOCK TABLES `node_event` WRITE;
/*!40000 ALTER TABLE `node_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `node_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `node_status`
--

LOCK TABLES `node_status` WRITE;
/*!40000 ALTER TABLE `node_status` DISABLE KEYS */;
INSERT INTO `node_status` VALUES ('OFFLINE','Indicates that the node is offline');
INSERT INTO `node_status` VALUES ('ONLINE','Indicates that the node is online');
/*!40000 ALTER TABLE `node_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `session_persistence_event`
--

LOCK TABLES `session_persistence_event` WRITE;
/*!40000 ALTER TABLE `session_persistence_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `session_persistence_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `state`
--

LOCK TABLES `state` WRITE;
/*!40000 ALTER TABLE `state` DISABLE KEYS */;
/*!40000 ALTER TABLE `state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `ticket`
--

LOCK TABLES `ticket` WRITE;
/*!40000 ALTER TABLE `ticket` DISABLE KEYS */;
/*!40000 ALTER TABLE `ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `traffic_scripts`
--

LOCK TABLES `traffic_scripts` WRITE;
/*!40000 ALTER TABLE `traffic_scripts` DISABLE KEYS */;
/*!40000 ALTER TABLE `traffic_scripts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `virtual_ip_event`
--

LOCK TABLES `virtual_ip_event` WRITE;
/*!40000 ALTER TABLE `virtual_ip_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_ip_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `virtual_ip_ipv4`
--

LOCK TABLES `virtual_ip_ipv4` WRITE;
/*!40000 ALTER TABLE `virtual_ip_ipv4` DISABLE KEYS */;
INSERT INTO `virtual_ip_ipv4` VALUES (1,'172.16.0.1','2011-07-12 21:19:50',NULL,'PUBLIC',1,1);
INSERT INTO `virtual_ip_ipv4` VALUES (2,'172.16.0.2','2011-07-12 21:40:21',NULL,'PUBLIC',1,1);
INSERT INTO `virtual_ip_ipv4` VALUES (3,'172.16.0.3',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (4,'172.16.0.4',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (5,'172.16.0.5',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (6,'172.16.0.6',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (7,'172.16.0.7',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (8,'172.16.0.8',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (9,'172.16.0.9',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (10,'172.16.0.10',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (11,'172.16.0.11',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (12,'172.16.0.12',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (13,'172.16.0.13',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (14,'172.16.0.14',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (15,'172.16.0.15',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (16,'172.16.0.16',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (17,'172.16.0.17',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (18,'172.16.0.18',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (19,'172.16.0.19',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (20,'172.16.0.20',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (21,'172.16.0.21',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (22,'172.16.0.22',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (23,'172.16.0.23',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (24,'172.16.0.24',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (25,'172.16.0.25',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (26,'172.16.0.26',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (27,'172.16.0.27',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (28,'172.16.0.28',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (29,'172.16.0.29',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (30,'172.16.0.30',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (31,'172.16.0.31',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (32,'172.16.0.32',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (33,'172.16.0.33',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (34,'172.16.0.34',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (35,'172.16.0.35',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (36,'172.16.0.36',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (37,'172.16.0.37',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (38,'172.16.0.38',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (39,'172.16.0.39',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (40,'172.16.0.40',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (41,'172.16.0.41',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (42,'172.16.0.42',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (43,'172.16.0.43',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (44,'172.16.0.44',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (45,'172.16.0.45',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (46,'172.16.0.46',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (47,'172.16.0.47',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (48,'172.16.0.48',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (49,'172.16.0.49',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (50,'172.16.0.50',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (51,'172.16.0.51',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (52,'172.16.0.52',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (53,'172.16.0.53',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (54,'172.16.0.54',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (55,'172.16.0.55',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (56,'172.16.0.56',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (57,'172.16.0.57',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (58,'172.16.0.58',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (59,'172.16.0.59',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (60,'172.16.0.60',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (61,'172.16.0.61',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (62,'172.16.0.62',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (63,'172.16.0.63',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (64,'172.16.0.64',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (65,'172.16.0.65',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (66,'172.16.0.66',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (67,'172.16.0.67',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (68,'172.16.0.68',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (69,'172.16.0.69',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (70,'172.16.0.70',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (71,'172.16.0.71',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (72,'172.16.0.72',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (73,'172.16.0.73',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (74,'172.16.0.74',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (75,'172.16.0.75',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (76,'172.16.0.76',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (77,'172.16.0.77',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (78,'172.16.0.78',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (79,'172.16.0.79',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (80,'172.16.0.80',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (81,'172.16.0.81',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (82,'172.16.0.82',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (83,'172.16.0.83',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (84,'172.16.0.84',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (85,'172.16.0.85',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (86,'172.16.0.86',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (87,'172.16.0.87',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (88,'172.16.0.88',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (89,'172.16.0.89',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (90,'172.16.0.90',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (91,'172.16.0.91',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (92,'172.16.0.92',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (93,'172.16.0.93',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (94,'172.16.0.94',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (95,'172.16.0.95',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (96,'172.16.0.96',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (97,'172.16.0.97',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (98,'172.16.0.98',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (99,'172.16.0.99',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (100,'172.16.0.100',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (101,'172.16.0.101',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (102,'172.16.0.102',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (103,'172.16.0.103',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (104,'172.16.0.104',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (105,'172.16.0.105',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (106,'172.16.0.106',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (107,'172.16.0.107',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (108,'172.16.0.108',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (109,'172.16.0.109',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (110,'172.16.0.110',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (111,'172.16.0.111',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (112,'172.16.0.112',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (113,'172.16.0.113',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (114,'172.16.0.114',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (115,'172.16.0.115',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (116,'172.16.0.116',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (117,'172.16.0.117',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (118,'172.16.0.118',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (119,'172.16.0.119',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (120,'172.16.0.120',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (121,'172.16.0.121',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (122,'172.16.0.122',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (123,'172.16.0.123',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (124,'172.16.0.124',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (125,'172.16.0.125',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (126,'172.16.0.126',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (127,'172.16.0.127',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (128,'172.16.0.128',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (129,'172.16.0.129',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (130,'172.16.0.130',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (131,'172.16.0.131',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (132,'172.16.0.132',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (133,'172.16.0.133',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (134,'172.16.0.134',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (135,'172.16.0.135',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (136,'172.16.0.136',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (137,'172.16.0.137',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (138,'172.16.0.138',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (139,'172.16.0.139',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (140,'172.16.0.140',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (141,'172.16.0.141',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (142,'172.16.0.142',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (143,'172.16.0.143',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (144,'172.16.0.144',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (145,'172.16.0.145',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (146,'172.16.0.146',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (147,'172.16.0.147',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (148,'172.16.0.148',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (149,'172.16.0.149',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (150,'172.16.0.150',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (151,'172.16.0.151',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (152,'172.16.0.152',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (153,'172.16.0.153',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (154,'172.16.0.154',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (155,'172.16.0.155',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (156,'172.16.0.156',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (157,'172.16.0.157',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (158,'172.16.0.158',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (159,'172.16.0.159',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (160,'172.16.0.160',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (161,'172.16.0.161',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (162,'172.16.0.162',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (163,'172.16.0.163',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (164,'172.16.0.164',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (165,'172.16.0.165',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (166,'172.16.0.166',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (167,'172.16.0.167',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (168,'172.16.0.168',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (169,'172.16.0.169',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (170,'172.16.0.170',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (171,'172.16.0.171',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (172,'172.16.0.172',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (173,'172.16.0.173',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (174,'172.16.0.174',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (175,'172.16.0.175',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (176,'172.16.0.176',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (177,'172.16.0.177',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (178,'172.16.0.178',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (179,'172.16.0.179',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (180,'172.16.0.180',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (181,'172.16.0.181',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (182,'172.16.0.182',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (183,'172.16.0.183',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (184,'172.16.0.184',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (185,'172.16.0.185',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (186,'172.16.0.186',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (187,'172.16.0.187',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (188,'172.16.0.188',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (189,'172.16.0.189',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (190,'172.16.0.190',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (191,'172.16.0.191',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (192,'172.16.0.192',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (193,'172.16.0.193',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (194,'172.16.0.194',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (195,'172.16.0.195',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (196,'172.16.0.196',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (197,'172.16.0.197',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (198,'172.16.0.198',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (199,'172.16.0.199',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (200,'172.16.0.200',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (201,'172.16.0.201',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (202,'172.16.0.202',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (203,'172.16.0.203',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (204,'172.16.0.204',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (205,'172.16.0.205',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (206,'172.16.0.206',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (207,'172.16.0.207',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (208,'172.16.0.208',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (209,'172.16.0.209',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (210,'172.16.0.210',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (211,'172.16.0.211',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (212,'172.16.0.212',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (213,'172.16.0.213',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (214,'172.16.0.214',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (215,'172.16.0.215',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (216,'172.16.0.216',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (217,'172.16.0.217',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (218,'172.16.0.218',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (219,'172.16.0.219',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (220,'172.16.0.220',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (221,'172.16.0.221',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (222,'172.16.0.222',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (223,'172.16.0.223',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (224,'172.16.0.224',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (225,'172.16.0.225',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (226,'172.16.0.226',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (227,'172.16.0.227',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (228,'172.16.0.228',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (229,'172.16.0.229',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (230,'172.16.0.230',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (231,'172.16.0.231',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (232,'172.16.0.232',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (233,'172.16.0.233',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (234,'172.16.0.234',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (235,'172.16.0.235',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (236,'172.16.0.236',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (237,'172.16.0.237',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (238,'172.16.0.238',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (239,'172.16.0.239',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (240,'172.16.0.240',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (241,'172.16.0.241',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (242,'172.16.0.242',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (243,'172.16.0.243',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (244,'172.16.0.244',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (245,'172.16.0.245',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (246,'172.16.0.246',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (247,'172.16.0.247',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (248,'172.16.0.248',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (249,'172.16.0.249',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (250,'172.16.0.250',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (251,'172.16.0.251',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (252,'172.16.0.252',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (253,'172.16.0.253',NULL,NULL,'PUBLIC',1,0);
INSERT INTO `virtual_ip_ipv4` VALUES (254,'172.16.0.254',NULL,NULL,'PUBLIC',1,0);
/*!40000 ALTER TABLE `virtual_ip_ipv4` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

--
-- Dumping data for table `virtual_ip_ipv6`
--

LOCK TABLES `virtual_ip_ipv6` WRITE;
/*!40000 ALTER TABLE `virtual_ip_ipv6` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_ip_ipv6` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `virtual_ip_type`
--

LOCK TABLES `virtual_ip_type` WRITE;
/*!40000 ALTER TABLE `virtual_ip_type` DISABLE KEYS */;
INSERT INTO `virtual_ip_type` VALUES ('PUBLIC','Indicates that the virtual ip is exposed publicly');
INSERT INTO `virtual_ip_type` VALUES ('SERVICENET','Indicates that the virtual ip is used for servicing');
/*!40000 ALTER TABLE `virtual_ip_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-07-12 14:48:29

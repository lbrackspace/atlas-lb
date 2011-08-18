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
INSERT INTO `category_type` VALUES ('CREATE','Resource created'),('DELETE','Resource deleted'),('UPDATE','Resource updated');
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
INSERT INTO `event_severity` VALUES ('CRITICAL','critical'),('INFO','information'),('WARNING','warning');
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

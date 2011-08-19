LOCK TABLES `virtual_ipv6` WRITE;
/*!40000 ALTER TABLE `virtual_ipv6` DISABLE KEYS */;
ALTER TABLE `virtual_ipv6` AUTO_INCREMENT = 9000000;
/*!40000 ALTER TABLE `virtual_ipv6` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES ('CORE', 1,'fd24:f480:ce44:91bc::/64','Core Cluster 1','core-cluster-1','0d1131d28d76ba0a72f42f819d207c94','username');
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` (`vendor`, `id`, `core_device_id`, `host_status`, `management_ip`, `max_concurrent_connections`, `name`, `endpoint`, `cluster_id`, `endpoint_active`, `ipv6_service_net`, `ipv4_service_net`, `ipv4_public`, `ipv6_public`) VALUES ('CORE', 1,'100', 'ACTIVE_TARGET', '10.2.4.4', '1500', 'host1_cluster1','https://127.0.0.1:1234', '1', TRUE, '4FDE:0000:0000:0002:0022:F376:FF3B:AB3F', '10.2.2.6', '172.1.1.1', '4FDE:0000:0000:0002:0022:F376:FF3B:AB3F');
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `virtual_ipv4` WRITE;
/*!40000 ALTER TABLE `virtual_ipv4` DISABLE KEYS */;
INSERT INTO `virtual_ipv4` VALUES ('CORE', 1,'10.0.0.1', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 2,'10.0.0.2', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 3,'10.0.0.3', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 4,'10.0.0.4', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 5,'10.0.0.5', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 6,'10.0.0.6', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 7,'10.0.0.7', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 8,'10.0.0.8', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 9,'10.0.0.9', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 10,'10.0.0.10', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 11,'10.0.0.11', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 12,'10.0.0.12', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 13,'10.0.0.13', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 14,'10.0.0.14', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 15,'10.0.0.15', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 16,'10.0.0.16', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 17,'10.0.0.17', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 18,'10.0.0.18', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 19,'10.0.0.19', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 20,'10.0.0.20', FALSE, NULL, NULL ,'PRIVATE',1);
/*!40000 ALTER TABLE `virtual_ipv4` ENABLE KEYS */;
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

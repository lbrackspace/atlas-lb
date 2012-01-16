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
INSERT INTO `host` (`vendor`, `id`, `name`, `host_status`, `endpoint`, `endpoint_active`, `username`, `password`, `cluster_id`, `ipv6_service_net`, `ipv4_service_net`, `ipv4_public`, `ipv6_public`) VALUES ('CORE', 1, 'AtlasHost1', 'ACTIVE_TARGET', 'https://127.0.0.1:1234', TRUE, 'username', 'somehashedpassword', '1', '4FDE:0000:0000:0002:0022:F376:FF3B:AB3F', '10.2.2.6', '172.1.1.1', '4FDE:0000:0000:0002:0022:F376:FF3B:AB3F');
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `virtual_ipv4` WRITE;
/*!40000 ALTER TABLE `virtual_ipv4` DISABLE KEYS */;
INSERT INTO `virtual_ipv4` VALUES ('CORE', 21,'10.0.0.21', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 22,'10.0.0.22', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 23,'10.0.0.23', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 24,'10.0.0.24', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 25,'10.0.0.25', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 26,'10.0.0.26', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 27,'10.0.0.27', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 28,'10.0.0.28', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 29,'10.0.0.29', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 30,'10.0.0.110', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,('CORE', 31,'10.0.0.111', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 32,'10.0.0.112', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 33,'10.0.0.113', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 34,'10.0.0.114', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 35,'10.0.0.115', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 36,'10.0.0.116', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 37,'10.0.0.117', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 38,'10.0.0.118', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 39,'10.0.0.119', FALSE, NULL, NULL ,'PRIVATE',1)
                                  ,('CORE', 40,'10.0.0.200', FALSE, NULL, NULL ,'PRIVATE',1);
/*!40000 ALTER TABLE `virtual_ipv4` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `limit_type`
--

LOCK TABLES `limit_type` WRITE;
/*!40000 ALTER TABLE `limit_type` DISABLE KEYS */;
INSERT INTO `limit_type` VALUES ('CORE', 'LOADBALANCER_LIMIT',25,'Max number of load balancers for an account');
INSERT INTO `limit_type` VALUES ('CORE', 'NODE_LIMIT',25,'Max number of nodes for a load balancer');
INSERT INTO `limit_type` VALUES ('CORE', 'IPV6_LIMIT',25,'Max number of IPv6 addresses for a load balancer');
INSERT INTO `limit_type` VALUES ('CORE', 'BATCH_DELETE_LIMIT',10,'Max number of items that can be deleted for batch delete operations');
/*!40000 ALTER TABLE `limit_type` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `virtual_ipv6` WRITE;
/*!40000 ALTER TABLE `virtual_ipv6` DISABLE KEYS */;
ALTER TABLE `virtual_ipv6` AUTO_INCREMENT = 9000000;
/*!40000 ALTER TABLE `virtual_ipv6` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'fd24:f480:ce44:91bc::/64','Core Cluster 1','core-cluster-1','0d1131d28d76ba0a72f42f819d207c94','username');
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (2,'100', 'A', 1, 'host1', '10.0.0.0','0.0.0.0','::', '::', '1.1.1.1', 1500, 'host1', 1);
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `virtual_ipv4` WRITE;
/*!40000 ALTER TABLE `virtual_ipv4` DISABLE KEYS */;
INSERT INTO `virtual_ipv4` VALUES (1,'10.0.0.1', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(2,'10.0.0.2', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(3,'10.0.0.3', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(4,'10.0.0.4', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(5,'10.0.0.5', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(6,'10.0.0.6', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(7,'10.0.0.7', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(8,'10.0.0.8', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(9,'10.0.0.9', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(10,'10.0.0.10', FALSE, NULL, NULL ,'PUBLIC',1)
                                  ,(11,'10.0.0.11', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(12,'10.0.0.12', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(13,'10.0.0.13', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(14,'10.0.0.14', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(15,'10.0.0.15', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(16,'10.0.0.16', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(17,'10.0.0.17', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(18,'10.0.0.18', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(19,'10.0.0.19', FALSE, NULL, NULL ,'SERVICE_NET',1)
                                  ,(20,'10.0.0.20', FALSE, NULL, NULL ,'SERVICE_NET',1);
/*!40000 ALTER TABLE `virtual_ipv4` ENABLE KEYS */;
UNLOCK TABLES;
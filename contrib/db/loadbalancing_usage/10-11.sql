USE `loadbalancing_usage`;

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
  `start_time` timestamp NOT NULL,
  `num_vips` int(11) NOT NULL DEFAULT 1,
  `event_type` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lb_usage_event_account_key` (`account_id`),
  KEY `lb_usage_event_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

update `meta` set `meta_value` = '11' where `meta_key`='version';

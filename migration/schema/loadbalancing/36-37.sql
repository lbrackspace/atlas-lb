use `loadbalancing`;

INSERT INTO `limit_type` VALUES ('LOADBALANCER_META_LIMIT',25,'Max number of metadata items for a load balancer');

-- ----------------------------
--  Table structure for `meta`
-- ----------------------------
DROP TABLE IF EXISTS `lb_meta_data`;
CREATE TABLE `lb_meta_data` (
  `id` int(11) NOT NULL auto_increment,
  `key` varchar(32) default NULL,
  `value` varchar(256) default NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY  (`key`, `loadbalancer_id`),
  KEY `meta_lb_id_fk` (`loadbalancer_id`),
  CONSTRAINT `meta_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '37' where `meta_key`='version';

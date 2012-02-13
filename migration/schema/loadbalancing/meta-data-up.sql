use `loadbalancing`;

-- ----------------------------
--  Table structure for `meta`
-- ----------------------------
DROP TABLE IF EXISTS `meta`;
CREATE TABLE `meta` (
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


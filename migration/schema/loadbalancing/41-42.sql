use loadbalancing;


INSERT INTO `limit_type` VALUES ('NODE_META_LIMIT',25,'Max number of metadata items for a node');

-- ----------------------------
--  Table structure for `node meta`
-- ----------------------------
DROP TABLE IF EXISTS `node_meta_data`;
CREATE TABLE `node_meta_data` (
  `id` int(11) NOT NULL auto_increment,
  `key` varchar(32) default NULL,
  `value` varchar(256) default NULL,
  `node_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY  (`key`, `node_id`),
  KEY `meta_node_id_fk` (`node_id`),
  CONSTRAINT `meta_node_ibfk_1` FOREIGN KEY (`node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


alter table `loadbalancer` add column `content_caching` tinyint(1) NOT NULL default '0';
insert into `event_type` values("UPDATE_CONTENT_CACHING", "Update Content Caching");

update `meta` set `meta_value` = '42' where `meta_key`='version';

USE loadbalancing;

-- ----------------------------
--  Table structure for `ticket`
-- ----------------------------
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
  `id` int(11) NOT NULL auto_increment,
  `ticket_id` varchar(32) NOT NULL,
  `comment` text NOT NULL,
  `loadbalancer_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `ticket_loadbalancer_id` (`loadbalancer_id`),
  CONSTRAINT `ticket_loadbalancer_id_fk` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '18' where `meta_key`='version';

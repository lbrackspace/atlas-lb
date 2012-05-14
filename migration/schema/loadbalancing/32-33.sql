use loadbalancing;

DROP TABLE IF EXISTS `lb_ssl`;

CREATE TABLE `lb_ssl` (
  `id` int(11) NOT NULL auto_increment,
  `loadbalancer_id` int(11) default NULL,
  `pem_key` mediumtext,
  `pem_cert` mediumtext,
 `intermediate_certificate` mediumtext,
  `enabled` tinyint(1) default 0,
 `secure_port` int(11) default NULL,
`secure_traffic_only` tinyint(1) default 0,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `loadbalancer_id` (`loadbalancer_id`),
  CONSTRAINT `lb_ssl_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8;

INSERT INTO `event_type` VALUES('UPDATE_SSL_TERMINATION', 'Update SSL termination');
INSERT INTO `event_type` VALUES('DELETE_SSL_TERMINATION', 'Delete SSL termination');

update `meta` set `meta_value` = '33' where `meta_key`='version';
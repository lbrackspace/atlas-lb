use loadbalancing;

CREATE TABLE IF NOT EXISTS `lb_status_history` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `status` varchar(32) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `tyle2_fk` (`status`),
  CONSTRAINT `le2_st` FOREIGN KEY (`status`) REFERENCES `lb_status` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '40?' where `meta_key`='version';

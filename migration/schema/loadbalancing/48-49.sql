use `loadbalancing`;

ALTER TABLE `alert` CHANGE  `created` `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS `node_service_event` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `node_id` int(11) NOT NULL,
  `event_title` varchar(64) default NULL,
  `event_description` varchar(1024) default NULL,
  `relative_uri` varchar(256) default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `severity` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `category` varchar(32) NOT NULL,
  `author` varchar(32) default NULL,
  `detailed_message` mediumtext NULL,
  PRIMARY KEY  (`id`),
  KEY `svnse_fk` (`severity`),
  KEY `tynse_fk` (`type`),
  KEY `ctnse_fk` (`category`),
  CONSTRAINT `nse_sc1` FOREIGN KEY (`category`) REFERENCES `category_type` (`name`),
  CONSTRAINT `nse_st1` FOREIGN KEY (`type`) REFERENCES `event_type` (`name`),
  CONSTRAINT `nse_sv1` FOREIGN KEY (`severity`) REFERENCES `event_severity` (`name`),
  INDEX created_idx(created),
  INDEX account_id_idx(account_id),
  INDEX loadbalancer_id_idx(loadbalancer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `loadbalancer` ADD `timeout` INT(3) DEFAULT 30 AFTER `status`;

update `meta` set `meta_value` = '49' where `meta_key`='version';
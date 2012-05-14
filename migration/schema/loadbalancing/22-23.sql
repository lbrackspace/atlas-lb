USE `loadbalancing`;

CREATE TABLE `blacklist_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `blacklist_item` (
  `id` int(11) NOT NULL auto_increment,
  `cidr_block` varchar(64) NOT NULL,
  `ip_version` varchar(32) NOT NULL,
  `type` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `blacklist_type_fk` (`type`),
  CONSTRAINT `blacklist_ibfk_1` FOREIGN KEY (`type`) REFERENCES `blacklist_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into blacklist_type values('NODE', 'Blacklisted Nodes');
insert into blacklist_type values('ACCESSLIST', 'Blacklisted lists');

update `meta` set `meta_value` = '23' where `meta_key`='version';


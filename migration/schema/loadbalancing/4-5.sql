use `loadbalancing`;

CREATE TABLE `group_rate_limit` (
  `id` int (11) NOT NULL,
  `group_name` varchar(32) NOT NULL,
  `group_desc` varchar(128) NOT NULL,
  `is_default` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into group_rate_limit values(1, 'customer_group', 'customer_limit_group', 1);
CREATE TABLE `account_group` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  CONSTRAINT `grp_id_fk` FOREIGN KEY (`group_id`) REFERENCES `group_rate_limit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table group_rate_limit add UNIQUE  `name` (`group_name`); 

update loadbalancer set sessionPersistence = 'NONE' where sessionPersistence = 'SOURCE_IP';
update meta set `meta_value` = '5' where `meta_key`='version';


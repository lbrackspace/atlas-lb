USE `loadbalancing`;

DROP TABLE account_limits;
DROP TABLE limit_type;

CREATE TABLE `group_loadbalancer_limit` (
  `id` int(11) NOT NULL auto_increment,
  `group_name` varchar(32) NOT NULL,
  `group_limit` int(11) NOT NULL,
  `is_default` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `account_loadbalancer_limit` (
  `id` int(11) NOT NULL auto_increment,
  `account_id` int(11) NOT NULL,
  `loadbalancer_limit_group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '27' where `meta_key`='version';

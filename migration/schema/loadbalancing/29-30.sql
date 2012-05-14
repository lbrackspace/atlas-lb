USE `loadbalancing`;

CREATE TABLE `defaults` (
 `id` int (11) NOT NULL AUTO_INCREMENT,
 `name` varchar(32) NOT NULL,
 `value` mediumtext,
 PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '30' where `meta_key`='version';

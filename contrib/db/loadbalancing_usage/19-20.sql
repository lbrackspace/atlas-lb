USE `loadbalancing_usage`;

CREATE TABLE `host_usage` (
  `id` int(11) NOT NULL auto_increment,
  `host_id` int(11) NOT NULL,
  `bandwidth_bytes_in` bigint(20) NOT NULL default '0',
  `bandwidth_bytes_out` bigint(20) NOT NULL default '0',
  `snapshot_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`),
  KEY `host_usage_host_key` (`host_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '20' where `meta_key`='version';


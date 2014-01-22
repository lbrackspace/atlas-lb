use `loadbalancing_usage`;

CREATE TABLE `lb_usage_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `num_vips` int(11) NOT NULL DEFAULT '1',
  `event_type` varchar(32) NOT NULL,
  `last_bandwidth_bytes_in` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out` bigint(20) DEFAULT NULL,
  `last_concurrent_conns` int(11) DEFAULT NULL,
  `last_bandwidth_bytes_in_ssl` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out_ssl` bigint(20) DEFAULT NULL,
  `last_concurrent_conns_ssl` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lb_usage_event_account_key` (`account_id`),
  KEY `lb_usage_event_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `event_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `lb_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `avg_concurrent_conns` double NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_in` bigint(20) NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_out` bigint(20) NOT NULL DEFAULT '0',
  `last_bandwidth_bytes_in` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out` bigint(20) DEFAULT NULL,
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `num_polls` int(11) NOT NULL DEFAULT '0',
  `num_vips` int(11) NOT NULL DEFAULT '1',
  `tags_bitmask` int(3) NOT NULL DEFAULT '0',
  `event_type` varchar(32) DEFAULT NULL,
  `avg_concurrent_conns_ssl` double NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_in_ssl` bigint(20) NOT NULL DEFAULT '0',
  `cum_bandwidth_bytes_out_ssl` bigint(20) NOT NULL DEFAULT '0',
  `last_bandwidth_bytes_in_ssl` bigint(20) DEFAULT NULL,
  `last_bandwidth_bytes_out_ssl` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lb_usage_account_key` (`account_id`),
  KEY `lb_usage_lb_key` (`loadbalancer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

UPDATE `meta` SET `meta_value` = '<ADD VALUE HERE>' WHERE `meta_key`='version';


use `loadbalancing_usage`;

--
-- Table structure for table `usage_event_type`
--
DROP TABLE IF EXISTS `usage_event_type`;
CREATE TABLE `usage_event_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO usage_event_type values('SSL_OFF', 'Indicates SSL Termination Has Been Disabled Event');
INSERT INTO usage_event_type values('SSL_ONLY_ON', 'Indicates SSL Termination Only Has Been Enabled Event');
INSERT INTO usage_event_type values('SSL_MIXED_ON', 'Indicates SSL Termination Mixed Has Been Enabled Event');
INSERT INTO usage_event_type values('CREATE_LOADBALANCER', 'Indicates A Create Load Balancer Event ');
INSERT INTO usage_event_type values('DELETE_LOADBALANCER', 'Indicates A Delete Load Balancer Event ');
INSERT INTO usage_event_type values('CREATE_VIRTUAL_IP', 'Indicates A Create Virtual Ip Event ');
INSERT INTO usage_event_type values('DELETE_VIRTUAL_IP', 'Indicates A Delete Virtual Ip Event ');
INSERT INTO usage_event_type values('SUSPEND_LOADBALANCER', 'Indicates A Suspend Load Balancer Event ');
INSERT INTO usage_event_type values('SUSPENDED_LOADBALANCER', 'Indicates A Suspended Load Balancer Event ');
INSERT INTO usage_event_type values('UNSUSPEND_LOADBALANCER', 'Indicates A UnSuspended Load Balancer Event ');

--
-- Table structure for table `lb_host_usage`
--
DROP TABLE IF EXISTS `lb_host_usage`;
CREATE TABLE `lb_host_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `host_id` int(11) NOT NULL,
  `bandwidth_out` bigint NOT NULL DEFAULT 0,
  `bandwidth_in` bigint NOT NULL DEFAULT 0,
  `bandwidth_in_ssl` bigint NOT NULL DEFAULT 0,
  `bandwidth_out_ssl` bigint NOT NULL DEFAULT 0,
  `concurrent_connections` bigint NOT NULL DEFAULT 0,
  `concurrent_connections_ssl` bigint NOT NULL DEFAULT 0,
  `poll_time` timestamp NOT NULL,
  `tags_bitmask` int(3) NOT NULL DEFAULT 0,
  `num_vips` int(11) NOT NULL DEFAULT 1,
  `event_type` varchar(32),
  PRIMARY KEY (`id`),
  KEY `lb_host_usage_account_key` (`account_id`),
  KEY `lb_host_usage_lb_key` (`loadbalancer_id`),
  KEY `lb_host_usage_host_key` (`host_id`),
  KEY `usage_event_type_fk` (`event_type`),
  CONSTRAINT `lb_host_usage_ibfk_2` FOREIGN KEY (`event_type`) REFERENCES `usage_event_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `lb_merged_host_usage`
--
DROP TABLE IF EXISTS `lb_merged_host_usage`;
CREATE TABLE `lb_merged_host_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `loadbalancer_id` int(11) NOT NULL,
  `incoming_transfer` bigint NOT NULL DEFAULT 0,
  `outgoing_transfer` bigint NOT NULL DEFAULT 0,
  `incoming_transfer_ssl` bigint NOT NULL DEFAULT 0,
  `outgoing_transfer_ssl` bigint NOT NULL DEFAULT 0,
  `concurrent_connections` bigint NOT NULL DEFAULT 0,
  `concurrent_connections_ssl` bigint NOT NULL DEFAULT 0,
  `poll_time` timestamp NOT NULL,
  `tags_bitmask` int(3) NOT NULL DEFAULT 0,
  `num_vips` int(11) NOT NULL DEFAULT 1,
  `event_type` varchar(32),
  PRIMARY KEY (`id`),
  KEY `lb_merged_host_usage_account_key` (`account_id`),
  KEY `lb_merged_host_usage_lb_key` (`loadbalancer_id`),
  KEY `_merged_usage_event_type_fk` (`event_type`),
  CONSTRAINT `lb_merged_host_usage_ibfk_2` FOREIGN KEY (`event_type`) REFERENCES `usage_event_type` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


update `meta` set `meta_value` = '58' where `meta_key`='version';

use `loadbalancing`;

DROP TABLE IF EXISTS `certificate_mapping`;

CREATE TABLE `certificate_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `loadbalancer_id` int(11) NOT NULL,
  `host_name` varchar(128) NOT NULL,
  `pem_key` mediumtext NOT NULL,
  `pem_cert` mediumtext NOT NULL,
  `intermediate_certificate` mediumtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `loadbalancer_id` (`loadbalancer_id`),
  UNIQUE KEY `host_name_lb_id` (`host_name`,`loadbalancer_id`),
  CONSTRAINT `certificate_mapping_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `event_type` values('UPDATE_CERTIFICATE_MAPPING', 'Update Certificate Mapping');
insert into `event_type` values('DELETE_CERTIFICATE_MAPPING', 'Delete Certificate Mapping');

insert into `limit_type`(`name`, `default_value`, `description`) values('CERTIFICATE_MAPPING_LIMIT', 20, 'Max number of certificate mappings for a load balancer');

update `meta` set `meta_value` = '???' where `meta_key`='version';
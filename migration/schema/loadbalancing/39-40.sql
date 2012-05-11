use loadbalancing;

INSERT INTO `lb_protocol` VALUES('SFTP', 'TCP protocol running on port 22', 22, 1);


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


ALTER TABLE `lb_usage` ADD COLUMN `avg_concurrent_conns_ssl` double NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `bandwidth_in_ssl` bigint(20) NOT NULL default '0';
ALTER TABLE `lb_usage` ADD COLUMN `bandwidth_out_ssl` bigint(20) NOT NULL default '0';

INSERT INTO `event_type`(`name`, `description`) VALUES('SSL_ONLY_ON', 'Only the ssl virtual server is enabled in Zeus');
INSERT INTO `event_type`(`name`, `description`) VALUES('SSL_MIXED_ON', 'Both the ssl virtual server and non-ssl virtual server are enabled in Zeus');


insert into host_status(name,description)values("SOAP_API_ENDPOINT","Indicates that this Host shall handle all SOAP requests for this cluster.");

-- insert into event_type(name, description) values('BUILD_LOADBALANCER', 'Load balancer in build status');
-- insert into event_type(name, description) values('PENDING_UPDATE_LOADBALANCER', 'Load balancer in pending update status');
-- insert into event_type(name, description) values('PENDING_DELETE_LOADBALANCER', 'Load balancer in pending delete status');

update `meta` set `meta_value` = '40' where `meta_key`='version';

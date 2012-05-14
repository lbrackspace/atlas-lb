USE `loadbalancing`;

CREATE TABLE `event_type` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `lb_usage` ADD COLUMN `event_type` varchar(32);

INSERT INTO event_type values('CREATE_LOADBALANCER', 'A load balancer was created');
INSERT INTO event_type values('DELETE_LOADBALANCER', 'A load balancer was deleted');
INSERT INTO event_type values('CREATE_VIRTUAL_IP', 'A virtual ip was created');
INSERT INTO event_type values('DELETE_VIRTUAL_IP', 'A virtual ip was deleted');
INSERT INTO event_type values('SSL_ON', 'SSL was turned on');
INSERT INTO event_type values('SSL_OFF', 'SSL was turned off');

update `meta` set `meta_value` = '6' where `meta_key`='version';


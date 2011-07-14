USE `loadbalancing`;

insert ignore into event_severity (name, description) values ('CRITICAL', 'critical');
insert ignore into event_severity (name, description) values('WARNING', 'warning');
insert ignore into event_severity (name, description) values('INFO', 'information');

alter table health_monitor add UNIQUE KEY `loadbalancer_id` (`loadbalancer_id`);
update `meta` set `meta_value` = '26' where `meta_key`='version';


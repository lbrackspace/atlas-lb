use loadbalancing;

INSERT INTO `lb_protocol` VALUES('MYSQL', 'TCP protocol running on port 3306', 3306, 1);

insert into event_type(name, description) values('BUILD_LOADBALANCER', 'Load balancer in build status');
insert into event_type(name, description) values('PENDING_UPDATE_LOADBALANCER', 'Load balancer in pending update status');
insert into event_type(name, description) values('PENDING_DELETE_LOADBALANCER', 'Load balancer in pending delete status');

update `meta` set `meta_value` = '39' where `meta_key`='version';

USE `loadbalancing`;
insert into event_type(name, description) values('BUILD_LOADBALANCER', 'Load balancer in build status');
insert into event_type(name, description) values('PENDING_UPDATE_LOADBALANCER', 'Load balancer in pending update status');
insert into event_type(name, description) values('PENDING_DELETE_LOADBALANCER', 'Load balancer in pending delete status');

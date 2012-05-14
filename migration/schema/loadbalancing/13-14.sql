USE loadbalancing;

insert into event_type values('SUSPEND_LOADBALANCER', 'A load balancer was suspended');
insert into event_type values('UNSUSPEND_LOADBALANCER', 'A load balancer was unsuspended');

update `meta` set `meta_value` = '14' where `meta_key`='version';

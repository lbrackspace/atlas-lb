USE loadbalancing_usage;

delete from event_type where name = 'SUSPEND_LOADBALANCER';
delete from event_type where name = 'UNSUSPEND_LOADBALANCER';

update `loadbalancerMeta` set `meta_value` = '13' where `meta_key`='version';

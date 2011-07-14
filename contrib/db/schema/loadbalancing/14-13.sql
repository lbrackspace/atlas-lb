USE loadbalancing;

delete from event_type where name = 'SUSPEND_LOADBALANCER';
delete from event_type where name = 'UNSUSPEND_LOADBALANCER';

update `meta` set `meta_value` = '13' where `meta_key`='version';

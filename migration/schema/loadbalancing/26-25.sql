USE `loadbalancing`;

alter table health_monitor drop key `loadbalancer_id`;
update `loadbalancerMeta` set `meta_value` = '25' where `meta_key`='version';

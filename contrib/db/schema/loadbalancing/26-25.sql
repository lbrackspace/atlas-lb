USE `loadbalancing`;

alter table health_monitor drop key `loadbalancer_id`;
update `meta` set `meta_value` = '25' where `meta_key`='version';

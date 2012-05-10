USE `loadbalancing`;

insert ignore into lb_protocol values('TCP', 'The TCP protocol', 0, '1');
update `loadbalancerMeta` set `meta_value` = '29' where `meta_key`='version';


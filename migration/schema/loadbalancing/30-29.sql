USE `loadbalancing`;

drop table if exists defaults;

update `loadbalancerMeta` set `meta_value` = '29' where `meta_key`='version';

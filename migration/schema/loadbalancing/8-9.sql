USE `loadbalancing`;

ALTER TABLE `loadbalancer` ADD COLUMN `max_concurrent_connections` int(32) default NULL;

update `loadbalancerMeta` set `meta_value` = '9' where `meta_key`='version';

USE `loadbalancing`;

ALTER TABLE `loadbalancer` DROP COLUMN `max_concurrent_connections`;

update `loadbalancerMeta` set `meta_value` = '8' where `meta_key`='version';

use `loadbalancing`;

DROP TABLE `node_service_event`;

ALTER TABLE `loadbalancer` DROP COLUMN `timeout`;

update `meta` set `meta_value` = '48' where `meta_key`='version';

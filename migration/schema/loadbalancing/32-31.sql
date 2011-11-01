use loadbalancing;

ALTER TABLE `cluster` DROP COLUMN `status`;

update `meta` set `meta_value` = '31' where `meta_key`='version';
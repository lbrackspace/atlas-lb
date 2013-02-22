use `loadbalancing`;

ALTER TABLE `lb_usage` DROP COLUMN `uuid`;

update `meta` set `meta_value` = '51' where `meta_key`='version';
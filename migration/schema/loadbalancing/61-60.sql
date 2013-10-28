use `loadbalancing`;

ALTER TABLE `lb_usage` DROP COLUMN `reference_id`;

update `meta` set `meta_value` = '60' where `meta_key`='version';
use `loadbalancing`;

ALTER TABLE `lb_usage` DROP COLUMN `reference_id`;

update `meta` set `meta_value` = '???' where `meta_key`='version';
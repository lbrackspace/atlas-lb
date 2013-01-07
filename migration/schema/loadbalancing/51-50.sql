use `loadbalancing`;

ALTER TABLE `loadbalancer` DROP COLUMN `half_closed`;

update `meta` set `meta_value` = '50' where `meta_key`='version';

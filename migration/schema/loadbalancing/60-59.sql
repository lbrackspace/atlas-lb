USE `loadbalancing`;

ALTER TABLE `loadbalancer` DROP COLUMN `https_redirect`;
ALTER TABLE `loadbalancer` DROP COLUMN `provisioned`;

UPDATE `meta` SET `meta_value` = '59' WHERE `meta_key`='version';

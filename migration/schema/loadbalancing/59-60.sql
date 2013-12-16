use `loadbalancing`;

ALTER TABLE `loadbalancer` ADD COLUMN `https_redirect` tinyint(1) NOT NULL DEFAULT '0';
ALTER TABLE `loadbalancer` ADD COLUMN `provisioned` TIMESTAMP NULL DEFAULT NULL;
UPDATE `loadbalancer` SET `provisioned` = `created`;

UPDATE `meta` SET `meta_value` = '60' WHERE `meta_key`='version';

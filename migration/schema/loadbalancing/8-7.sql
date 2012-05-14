USE `loadbalancing`;

ALTER TABLE `virtual_ip` DROP FOREIGN KEY `fk_ip_version`;
ALTER TABLE `virtual_ip` DROP COLUMN `ip_version`;

DROP TABLE IF EXISTS `ip_version`;

update `meta` set `meta_value` = '7' where `meta_key`='version';

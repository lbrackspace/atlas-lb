USE `loadbalancing`;
ALTER TABLE `loadbalancing`.`host` ADD COLUMN `ipv6_servicenet` VARCHAR(39) NULL;
ALTER TABLE `loadbalancing`.`host` ADD COLUMN `ipv4_servicenet` VARCHAR(39) NULL;
ALTER TABLE `loadbalancing`.`host` ADD COLUMN `ipv4_public` VARCHAR(39) NULL;
ALTER TABLE `loadbalancing`.`host` ADD COLUMN `ipv6_public` VARCHAR(39) NULL;
update `meta` set `meta_value` = '22' where `meta_key`='version';


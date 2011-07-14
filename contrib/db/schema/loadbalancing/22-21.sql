USE `loadbalancing`;
ALTER TABLE `loadbalancing`.`host` DROP COLUMN `ipv6_servicenet`;
ALTER TABLE `loadbalancing`.`host` DROP COLUMN `ipv4_servicenet`;
ALTER TABLE `loadbalancing`.`host` DROP COLUMN `ipv4_public`;
ALTER TABLE `loadbalancing`.`host` DROP COLUMN `ipv6_public`;
update `meta` set `meta_value` = '21' where `meta_key`='version';

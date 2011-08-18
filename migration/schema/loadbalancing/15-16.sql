USE loadbalancing;

ALTER TABLE `loadbalancing`.`group_rate_limit` CHANGE COLUMN `id` `id` INT NOT NULL AUTO_INCREMENT;

update `meta` set `meta_value` = '16' where `meta_key`='version';

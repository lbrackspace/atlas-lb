use loadbalancing;

ALTER TABLE `cluster` ADD `status` VARCHAR(11) NOT NULL;

update `meta` set `meta_value` = '32' where `meta_key`='version';
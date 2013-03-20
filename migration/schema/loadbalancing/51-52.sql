use `loadbalancing`;

ALTER TABLE `lb_usage` ADD `uuid` VARCHAR(256);

update `meta` set `meta_value` = '52' where `meta_key`='version';
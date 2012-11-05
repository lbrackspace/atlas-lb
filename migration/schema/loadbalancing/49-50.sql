use `loadbalancing`;

ALTER TABLE `alert` CHANGE  `created` `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

update `meta` set `meta_value` = '50' where `meta_key`='version';

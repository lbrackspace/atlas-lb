use `loadbalancing`;

ALTER TABLE `loadbalancer` ADD `half_closed` tinyint(1) NOT NULL DEFAULT '0';

update `meta` set `meta_value` = '51' where `meta_key`='version';

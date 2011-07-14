USE `loadbalancing_usage`;

DROP TABLE `host_usage`;

update `meta` set `meta_value` = '19' where `meta_key`='version';

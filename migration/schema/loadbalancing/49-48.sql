use `loadbalancing`;

DROP TABLE `node_service_event`;

update `meta` set `meta_value` = '48' where `meta_key`='version';

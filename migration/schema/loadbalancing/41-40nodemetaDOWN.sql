use `loadbalancing`;

DROP TABLE IF EXISTS `node_meta_data`;

DELETE FROM `limit_type` WHERE `name` = 'NODE_META_LIMIT' LIMIT 1;

update `meta` set `meta_value` = '40?' where `meta_key`='version';

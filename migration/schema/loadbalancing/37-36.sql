use `loadbalancing`;

DROP TABLE IF EXISTS `lb_meta_data`;

DELETE FROM `limit_type` WHERE `name` = 'LOADBALANCER_META_LIMIT' LIMIT 1;

update `meta` set `meta_value` = '36' where `meta_key`='version';

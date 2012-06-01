use loadbalancing;

DROP TABLE IF EXISTS `node_meta_data`;
DELETE FROM `limit_type` WHERE `name` = 'NODE_META_LIMIT' LIMIT 1;


alter table `loadbalancer` drop `content_caching`;
delete from `event_type` where name = 'UPDATE_CONTENT_CACHING';

update `meta` set `meta_value` = '41' where `meta_key`='version';

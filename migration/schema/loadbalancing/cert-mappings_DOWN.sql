use `loadbalancing`;

delete from `limit_type` where `name` = 'CERTIFICATE_MAPPING_LIMIT';

delete from `event_type` where name = 'DELETE_CERTIFICATE_MAPPING';
delete from `event_type` where name = 'UPDATE_CERTIFICATE_MAPPING';

DROP TABLE IF EXISTS `certificate_mapping`;

update `meta` set `meta_value` = '???' where `meta_key`='version';
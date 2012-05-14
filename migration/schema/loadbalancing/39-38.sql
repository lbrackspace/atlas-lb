use loadbalancing;

DELETE FROM `lb_protocol` WHERE `name`  = 'MYSQL';

DELETE FROM event_type WHERE name = 'BUILD_LOADBALANCER';
DELETE FROM event_type WHERE name = 'PENDING_UPDATE_LOADBALANCER';
DELETE FROM event_type WHERE name = 'PENDING_DELETE_LOADBALANCER';

update `meta` set `meta_value` = '38' where `meta_key`='version';

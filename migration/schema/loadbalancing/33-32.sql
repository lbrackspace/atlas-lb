use loadbalancing;

DROP TABLE IF EXISTS `lb_ssl`;

DELETE FROM `event_type` WHERE `name` = 'UPDATE_SSL_TERMINATION';
DELETE FROM `event_type` WHERE `name` = 'DELETE_SSL_TERMINATION';

update `meta` set `meta_value` = '32' where `meta_key`='version';
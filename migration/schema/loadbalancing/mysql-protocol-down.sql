use loadbalancing;

DELETE FROM `lb_protocol` WHERE `name`  = 'MYSQL';

update `meta` set `meta_value` = '?' where `meta_key`='version';

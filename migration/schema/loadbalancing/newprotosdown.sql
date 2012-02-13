use loadbalancing;

DELETE FROM `lb_protocol` WHERE `name` = 'UDP_STREAM';
DELETE FROM `lb_protocol` WHERE `name` = 'UDP';
DELETE FROM `lb_protocol` WHERE `name` = 'DNS_UDP';
DELETE FROM `lb_protocol` WHERE `name` = 'DNS_TCP';

update `meta` set `meta_value` = '?' where `meta_key`='version';
use loadbalancing;

DELETE FROM `lb_protocol` WHERE `name`  = 'TCP_CLIENT_FIRST';

update `meta` set `meta_value` = '37' where `meta_key`='version';

use loadbalancing;

DELETE FROM `lb_protocol` WHERE `name`  = 'SFTP';


update `meta` set `meta_value` = '39?' where `meta_key`='version';

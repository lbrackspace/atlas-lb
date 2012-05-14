use loadbalancing;

INSERT INTO `lb_protocol` VALUES('TCP_CLIENT_FIRST', 'The TCP Client First Protocol', 0, 1);

update `meta` set `meta_value` = '38' where `meta_key`='version';

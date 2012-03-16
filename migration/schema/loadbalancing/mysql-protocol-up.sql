use loadbalancing;

INSERT INTO `lb_protocol` VALUES('MYSQL', 'TCP protocol running on port 3306', 3306, 1);

update `meta` set `meta_value` = '?' where `meta_key`='version';

use loadbalancing;

INSERT INTO `lb_protocol` VALUES('SFTP', 'TCP protocol running on port 22', 22, 1);

update `meta` set `meta_value` = '40?' where `meta_key`='version';

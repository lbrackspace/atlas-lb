USE loadbalancing;

DROP TABLE IF EXISTS `group_loadbalancer_limit`;
DROP TABLE IF EXISTS `account_loadbalancer_limit`;

update `meta` set `meta_value` = '12' where `meta_key`='version';

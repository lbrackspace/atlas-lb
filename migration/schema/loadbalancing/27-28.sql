USE `loadbalancing`;

CREATE TABLE limit_type(
   `name` varchar(32) NOT NULL,
   `default_value` int(11) NOT NULL,
   `description` varchar(128) NOT NULL,
   PRIMARY KEY (`name`)
   )ENGINE=InnoDB;

INSERT INTO limit_type values('LOADBALANCER_LIMIT', 25, 'Max number of load balancers for an account');
INSERT INTO limit_type values('NODE_LIMIT', 25, 'Max number of nodes for a load balancer');
INSERT INTO limit_type values('ACCESS_LIST_LIMIT', 100, 'Max number of items for an access list');
INSERT INTO limit_type values('IPV6_LIMIT', 25, 'Max number of IPv6 addresses for a load balancer');
INSERT INTO limit_type values('BATCH_DELETE_LIMIT', 10, 'Max number of items that can be deleted for batch delete operations');

CREATE TABLE account_limits(
   `id` int(11) NOT NULL auto_increment,
   `account_id` int(11) NOT NULL,
   `limit_amount` int(11) NOT NULL,
   `limit_type` varchar(32),
   PRIMARY KEY  (`id`),
   UNIQUE KEY `account_id_limit_type` (`account_id`,`limit_type`),
   KEY `account_limits_account_id` (`account_id`),
   FOREIGN KEY (`limit_type`) references limit_type(`name`)
   )ENGINE=InnoDB;

INSERT INTO account_limits (`account_id`, `limit_amount`, `limit_type`) SELECT a.account_id, g.group_limit, l.name FROM account_loadbalancer_limit a, group_loadbalancer_limit g, limit_type l WHERE a.loadbalancer_limit_group_id = g.id and l.name = 'LOADBALANCER_LIMIT';

DROP TABLE account_loadbalancer_limit;
DROP TABLE group_loadbalancer_limit;

update `meta` set `meta_value` = '28' where `meta_key`='version';

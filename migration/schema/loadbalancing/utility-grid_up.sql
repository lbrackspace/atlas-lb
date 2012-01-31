use loadbalancing;

ALTER TABLE `lb_usage` ADD `account_id` int(11) NOT NULL;

UPDATE `lb_usage` u,`loadbalancer` l SET `u`.`account_id`=`l`.`account_id` WHERE `u`.`loadbalancer_id` = `l`.`id`;

ALTER TABLE `lb_usage` ADD index(`account_id`);

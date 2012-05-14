USE loadbalancing_usage;

alter table lb_usage add UNIQUE KEY `account_id_lb_id_start_time` (`account_id`,`loadbalancer_id`,`start_time`);

update `meta` set `meta_value` = '16' where `meta_key`='version';

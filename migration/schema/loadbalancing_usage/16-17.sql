USE loadbalancing_usage;

alter table lb_usage drop key `account_id_lb_id_start_time`;

update `meta` set `meta_value` = '17' where `meta_key`='version';

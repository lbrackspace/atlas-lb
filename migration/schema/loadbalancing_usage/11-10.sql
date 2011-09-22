use `loadbalancing_usage`;

drop table `lb_usage_event`;

update `meta` set `meta_value` = '10' where `meta_key`='version';

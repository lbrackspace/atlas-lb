use `loadbalancing`;
alter table `host` change `target_host` `traffic_manager_name` varchar(255) not null;
update `meta` set `meta_value` = '3' where `meta_key`='version';

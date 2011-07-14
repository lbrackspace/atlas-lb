use `loadbalancing`;

alter table `host` change `traffic_manager_name` `target_host` varchar(255) not null;
update `meta` set `meta_value` = '2' where `meta_key`='version';

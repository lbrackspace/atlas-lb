use loadbalancing;

alter table `health_monitor` change host_header host_header varchar(256) NOT NULL default '';

update `meta` set `meta_value` = '44' where `meta_key`='version';
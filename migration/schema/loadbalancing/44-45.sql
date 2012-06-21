use loadbalancing;

alter table `health_monitor` change host_header host_header varchar(256) NULL DEFAULT " ";

update `meta` set `meta_value` = '45' where `meta_key`='version';

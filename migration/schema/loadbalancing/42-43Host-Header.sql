use loadbalancing;

alter table `health_monitor` add column `host_header` varchar(256) NOT NULL default '';

update `meta` set `meta_value` = '43???????' where `meta_key`='version';

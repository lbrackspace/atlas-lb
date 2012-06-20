use loadbalancing;

alter table `health_monitor` add column `host_header` varchar(256) NOT NULL default '';

alter table `lb_usage` add entry_version int(11);
alter table `lb_usage` add needs_pushed tinyint(1) NOT NULL DEFAULT 1;
alter table `account_usage` add needs_pushed tinyint(1) NOT NULL DEFAULT 1;

update `meta` set `meta_value` = '43' where `meta_key`='version';

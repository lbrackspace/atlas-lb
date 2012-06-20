use loadbalancing;

alter table `health_monitor` drop column `host_header`;

alter table `lb_usage` drop column entry_version;
alter table `lb_usage` drop column needs_pushed;
alter table `account_usage` drop column needs_pushed;

update `meta` set `meta_value` = '42' where `meta_key`='version';

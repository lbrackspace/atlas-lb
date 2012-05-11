use loadbalancing;

alter table `lb_usage` drop column entry_version;
alter table `lb_usage` drop column needs_pushed;
alter table `account_usage` drop column needs_pushed;

update `meta` set `meta_value` = '40?' where `meta_key`='version';

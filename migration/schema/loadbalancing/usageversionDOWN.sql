use loadbalancing;

alter table `lb_usage` drop column entry_version;
alter table `lb_usage` drop column is_pushed;

update `meta` set `meta_value` = '40?' where `meta_key`='version';

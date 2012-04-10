use loadbalancing;

alter table `lb_usage` drop column entry_version;

update `meta` set `meta_value` = '40?' where `meta_key`='version';

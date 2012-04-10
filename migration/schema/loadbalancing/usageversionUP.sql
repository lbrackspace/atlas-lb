use loadbalancing;

alter table `lb_usage` add entry_version int(11) default 0;

update `meta` set `meta_value` = '41?' where `meta_key`='version';

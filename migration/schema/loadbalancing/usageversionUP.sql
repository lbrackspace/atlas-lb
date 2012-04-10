use loadbalancing;

alter table `lb_usage` add entry_version int(11);

alter table `lb_usage` add is_pushed tinyint(1) NOT NULL DEFAULT 0;


update `meta` set `meta_value` = '41?' where `meta_key`='version';

use loadbalancing;

alter table `lb_usage` add entry_version int(11);

alter table `lb_usage` add needs_pushed tinyint(1) NOT NULL DEFAULT 1;

alter table `account_usage` add needs_pushed tinyint(1) NOT NULL DEFAULT 1;


update `meta` set `meta_value` = '41?' where `meta_key`='version';

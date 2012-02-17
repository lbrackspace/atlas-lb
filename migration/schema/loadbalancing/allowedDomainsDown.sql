use loadbalancing;
drop table allowed_domain;

update `meta` set `meta_value` = '?' where `meta_key`='version';
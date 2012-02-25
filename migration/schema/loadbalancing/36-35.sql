use loadbalancing;
drop table allowed_domain;

update `meta` set `meta_value` = '35' where `meta_key`='version';

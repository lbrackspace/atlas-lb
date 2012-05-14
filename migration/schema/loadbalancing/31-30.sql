use loadbalancing;

drop table user_pages;

update `meta` set `meta_value` = '30' where `meta_key`='version';

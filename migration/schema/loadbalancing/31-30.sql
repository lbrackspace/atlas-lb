use loadbalancing;

drop table user_pages;

update `loadbalancerMeta` set `meta_value` = '30' where `meta_key`='version';

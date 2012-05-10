use loadbalancing;
drop table allowed_domain;

update `loadbalancerMeta` set `meta_value` = '35' where `meta_key`='version';

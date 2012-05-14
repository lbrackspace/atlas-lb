use `loadbalancing`;
drop table account_group;
drop table group_rate_limit;
update meta set `meta_value` = '4' where `meta_key`='version';


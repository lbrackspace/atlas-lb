USE loadbalancing;

drop table `ticket`;

update `loadbalancerMeta` set `meta_value` = '17' where `meta_key`='version';

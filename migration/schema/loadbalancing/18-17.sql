USE loadbalancing;

drop table `ticket`;

update `meta` set `meta_value` = '17' where `meta_key`='version';

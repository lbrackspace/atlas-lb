use loadbalancing;

alter table `lb_usage` change entry_version entry_version int(11);

update `meta` set `meta_value` = '43' where `meta_key`='version';
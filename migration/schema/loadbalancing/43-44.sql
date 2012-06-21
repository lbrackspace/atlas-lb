use loadbalancing;

alter table `lb_usage` change entry_version entry_version int(11) NOT NULL DEFAULT 0;

update `meta` set `meta_value` = '44' where `meta_key`='version';

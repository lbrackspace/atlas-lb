use loadbalancing;

alter table `node` modify `ip_address` VARCHAR(128);

update `meta` set `meta_value` = '41' where `meta_key`='version';

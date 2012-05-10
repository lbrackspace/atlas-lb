use loadbalancing;

alter table `node` modify `ip_address` VARCHAR(39);

update `loadbalancerMeta` set `meta_value` = '40' where `meta_key`='version';

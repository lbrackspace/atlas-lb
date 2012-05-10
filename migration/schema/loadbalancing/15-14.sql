USE loadbalancing;

alter table loadbalancer_virtualip drop KEY `virtual_ip_port`;
alter table loadbalancer_virtualip drop column port;

update `loadbalancerMeta` set `meta_value` = '14' where `meta_key`='version';

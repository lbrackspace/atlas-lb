use loadbalancing;

drop table loadbalancer_virtualipv6;
drop table account;
drop table virtual_ip_ipv6;
alter table cluster drop column cluster_ipv6_cidr;

update `meta` set `meta_value` = '24' where `meta_key`='version';


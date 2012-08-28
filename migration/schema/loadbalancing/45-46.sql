use loadbalancing;

drop index virtual_ip_port on loadbalancer_virtualip;

alter table loadbalancer_virtualipv6 drop foreign key loadbalancer_virtualipv6_ibfk_1;
alter table loadbalancer_virtualipv6 drop foreign key loadbalancer_virtualipv6_ibfk_2;

drop index virtualip6_port on loadbalancer_virtualipv6;

alter table loadbalancer_virtualipv6 add CONSTRAINT `loadbalancer_virtualipv6_ibfk_1` FOREIGN KEY (`loadbalancer_id`) REFERENCES `loadbalancer` (`id`);
alter table loadbalancer_virtualipv6 add CONSTRAINT `loadbalancer_virtualipv6_ibfk_2` FOREIGN KEY (`virtualip6_id`) REFERENCES `virtual_ip_ipv6` (`id`);

update `meta` set `meta_value` = '46' where `meta_key`='version';

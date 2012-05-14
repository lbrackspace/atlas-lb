USE loadbalancing;

alter table loadbalancer_virtualip add column port int(11) not null;
update loadbalancer_virtualip j, loadbalancer l set j.port = l.port where j.loadbalancer_id = l.id;
alter table loadbalancer_virtualip add UNIQUE KEY `virtual_ip_port` (`virtualip_id`, `port`);

update `meta` set `meta_value` = '15' where `meta_key`='version';

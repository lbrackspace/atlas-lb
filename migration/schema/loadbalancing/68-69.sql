use loadbalancing;
alter table lb_ssl add column tls10_enabled tinyint(1) not null default 1;
update `meta` set `meta_value` = '69' where `meta_key`='version';

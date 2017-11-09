use loadbalancing;
alter table lb_ssl drop column tls10_enabled;
update `meta` set `meta_value` = '68' where `meta_key`='version';

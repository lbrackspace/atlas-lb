use `loadbalancing`;

alter table lb_ssl drop column tls11_enabled;

update `meta` set `meta_value` = '70' where `meta_key`='version';
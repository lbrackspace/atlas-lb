use `loadbalancing`;

alter table lb_ssl add column tls11_enabled tinyint(1) not null default 1 AFTER tls10_enabled;

update `meta` set `meta_value` = '71' where `meta_key`='version';
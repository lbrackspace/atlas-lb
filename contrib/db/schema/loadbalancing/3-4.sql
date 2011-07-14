use `loadbalancing`;
alter table `host` add column `soap_endpoint_active` tinyint(1);
update `host` set `soap_endpoint_active`='1';
alter table `host` modify column `soap_endpoint_active` tinyint(1) not null;
update `meta` set `meta_value` = '4' where `meta_key`='version';


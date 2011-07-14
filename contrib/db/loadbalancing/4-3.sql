use `loadbalancing`;
alter table `host` drop column `soap_endpoint_active`;
update meta set `meta_value` = '3' where `meta_key`='version';


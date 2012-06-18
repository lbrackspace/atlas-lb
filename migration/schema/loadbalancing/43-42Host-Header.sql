use loadbalancing;

alter table `health_monitor` drop column `host_header`;

update `meta` set `meta_value` = '42???????????????' where `meta_key`='version';

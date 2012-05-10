use loadbalancing;

alter table `loadbalancer` add column `content_caching` tinyint(1) NULL default '0';

insert into `event_type` values("UPDATE_CONTENT_CACHING", "Update Content Caching");

update `loadbalancerMeta` set `meta_value` = '42???' where `meta_key`='version';

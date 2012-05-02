use loadbalancing;

alter table `loadbalancer` add column `content_caching` tinyint(1) NOT NULL default '0';

insert into `event_type` values("UPDATE_CONTENT_CACHING", "Update Content Caching");

update `meta` set `meta_value` = '42???' where `meta_key`='version';

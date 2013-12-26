use `loadbalancing`;
alter table loadbalancer add column location_header_rewrite tinyint(1) not null default true;

update meta set `meta_value` = '???' where `meta_key`='version';
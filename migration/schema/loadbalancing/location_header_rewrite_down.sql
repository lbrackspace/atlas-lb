use `loadbalancing`;
alter table loadbalancer drop column location_header_rewrite;

update meta set `meta_value` = '???' where `meta_key`='version';
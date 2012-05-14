USE `loadbalancing`;

drop table if exists defaults;

update `meta` set `meta_value` = '29' where `meta_key`='version';

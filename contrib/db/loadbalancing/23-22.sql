USE `loadbalancing`;

drop table `blacklist_item`;
drop table `blacklist_type`;

update `meta` set `meta_value` = '22' where `meta_key`='version';


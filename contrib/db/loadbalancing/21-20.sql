USE `loadbalancing`;

delete from lb_protocol where name in ('IMAPv2', 'IMAPv3');
update `meta` set `meta_value` = '20' where `meta_key`='version';

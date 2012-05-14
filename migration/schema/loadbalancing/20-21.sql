USE `loadbalancing`;

insert into lb_protocol values('IMAPv2', 'The IMAPv2 protocol',  143, 1);
insert into lb_protocol values('IMAPv3', 'The IMAPv3 protocol',  220, 1);

update `meta` set `meta_value` = '21' where `meta_key`='version';


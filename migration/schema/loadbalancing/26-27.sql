USE `loadbalancing`;

insert ignore into category_type (name, description) values ('CREATE', 'Resource created');
insert ignore into category_type (name, description) values('DELETE', 'Resource deleted');
insert ignore into category_type (name, description) values('UPDATE', 'Resource updated');

update `meta` set `meta_value` = '27' where `meta_key`='version';

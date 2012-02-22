use loadbalancing;

ALTER TABLE `lb_usage` DROP COLUMN `account_id`;

alter table node drop foreign key fk_type2node_typename;
alter table node drop column type;
drop table node_type;
alter table loadbalancer drop index account_id;

DELETE FROM `lb_protocol` WHERE `name` = 'UDP_STREAM';
DELETE FROM `lb_protocol` WHERE `name` = 'UDP';
DELETE FROM `lb_protocol` WHERE `name` = 'DNS_UDP';
DELETE FROM `lb_protocol` WHERE `name` = 'DNS_TCP';

update `meta` set `meta_value` = '34' where `meta_key`='version';

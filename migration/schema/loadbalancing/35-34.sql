use loadbalancing;

alter table node drop foreign key fk_type2node_typename;
alter table node drop column type;
drop table node_type;
alter table loadbalancer drop index account_id;
update `meta` set `meta_value` = '34' where `meta_key`='version';

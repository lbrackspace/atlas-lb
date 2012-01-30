use loadbalancing;

alter table node drop foreign key fk_type2node_typename;
alter table node drop column type;
drop table node_type;

use loadbalancing;

create table node_type(
    name varchar(16),
    primary key(name)
);

insert into node_type(name)values("PRIMARY"),("FAILOVER");
alter table node add column type varchar(32) not null default "PRIMARY";
alter table node add constraint fk_type2node_typename foreign key  (type) references node_type(name);

use loadbalancing;

INSERT INTO `lb_protocol` VALUES('UDP_STREAM', 'The UDP STREAMING Protocol', 0, 1);
INSERT INTO `lb_protocol` VALUES('UDP', 'The UDP Protocol', 0, 1);
INSERT INTO `lb_protocol` VALUES('DNS_UDP', 'The DNS/UDP  Protocol', 53, 1);
INSERT INTO `lb_protocol` VALUES('DNS_TCP', 'The DNS/TCP Protocol', 53, 1);

create table node_type(
    name varchar(16),
    primary key(name)
)engine=InnoDB DEFAULT CHARSET=utf8;

insert into node_type(name)values("PRIMARY"),("SECONDARY");
alter table node add column type varchar(32) not null default "PRIMARY";
alter table node add constraint fk_type2node_typename foreign key  (type) references node_type(name);
alter table loadbalancer add index(account_id);

ALTER TABLE `lb_usage` ADD `account_id` int(11) NOT NULL;
UPDATE `lb_usage` u,`loadbalancer` l SET `u`.`account_id`=`l`.`account_id` WHERE `u`.`loadbalancer_id` = `l`.`id`;
ALTER TABLE `lb_usage` ADD index(`account_id`);

update `meta` set `meta_value` = '35' where `meta_key`='version';

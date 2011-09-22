drop table if exists account;
create table account(
    id int NOT NULL,
    sha1sum_ipv6 varchar(9) NOT NULL,
    primary key(id),
    unique key(sha1sum_ipv6)
)ENGINE=InnoDB;

alter table cluster add cluster_ipv6_cidr varchar(44);

drop table if exists virtual_ip_ipv6;
create table virtual_ip_ipv6(
    id int not null AUTO_INCREMENT,
    account_id int not null,
    vip_octets int not null,
    cluster_id int not null,
    primary key(id),
    unique key account_octets (account_id,vip_octets),
    foreign key(cluster_id) references cluster(id)
)ENGINE=InnoDB;

alter table virtual_ip_ipv6 add index(account_id);
alter table virtual_ip_ipv6 add index(vip_octets);
alter table virtual_ip_ipv6 AUTO_INCREMENT=9000000;

drop table if exists loadbalancer_virtualipv6;
create table loadbalancer_virtualipv6(
    loadbalancer_id int not null,
    virtualip6_id int not null,
    port int not null,
    primary key(loadbalancer_id,virtualip6_id),
    unique key virtualip6_port(virtualip6_id,port),
    foreign key(loadbalancer_id) references loadbalancer(id),
    foreign key(virtualip6_id) references virtual_ip_ipv6(id)
)ENGINE=InnoDB;

update `meta` set `meta_value` = '25' where `meta_key`='version';


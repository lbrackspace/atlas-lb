use loadbalancing;

create table allowed_domain(
    id int not null AUTO_INCREMENT,
    name varchar(255) not null unique,
    primary key(id)
)Engine=InnoDb DEFAULT CHARSET=utf8;

update `meta` set `meta_value` = '36' where `meta_key`='version';

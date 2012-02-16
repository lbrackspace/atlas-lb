use loadbalancing;

create table allowed_domains(
    id int not null AUTO_INCREMENT,
    name varchar(255) not null unique,
    primary key(id)
)Engine=InnoDb DEFAULT CHARSET=utf8;


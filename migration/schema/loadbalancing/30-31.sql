use loadbalancing;

create table user_pages(
    id int not null auto_increment,
    loadbalancer_id int not null,
    errorpage mediumtext null,
    primary key(id),
    foreign key(loadbalancer_id) references loadbalancer(id)
)Engine=InnoDb CHARSET=utf8;

#insert into user_pages(loadbalancer_id,errorpage)values(316,"<html>Test</html>");

update `meta` set `meta_value` = '31' where `meta_key`='version';


use loadbalancing;

CREATE TABLE cl_type(
    name varchar(32),
    description varchar(128) NOT NULL,
    primary key(name)
) engine=InnoDB default charset=utf8;
INSERT INTO cl_type(name,description)values('STANDARD','Normal User account');
INSERT INTO cl_type(name,description)values('INTERNAL','Internal Rackspace account');

ALTER TABLE account ADD COLUMN cluster_type varchar(32) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE cluster ADD COLUMN cluster_type varchar(32) NOT NULL DEFAULT 'STANDARD';

ALTER TABLE account ADD CONSTRAINT fk_account_cl_type foreign key(cluster_type) references cl_type(name);
ALTER TABLE cluster ADD CONSTRAINT fk_cluster_cl_type foreign key(cluster_type) references cl_type(name);

update meta set `meta_value` = '63' where `meta_key`='version';

use loadbalancing;

ALTER TABLE account DROP foreign key fk_account_cl_type;
ALTER TABLE cluster DROP foreign key fk_cluster_cl_type;

ALTER TABLE account DROP COLUMN cluster_type;
ALTER TABLE cluster DROP COLUMN cluster_type;

DROP TABLE cl_type;

update meta set `meta_value` = '62' where `meta_key`='version';

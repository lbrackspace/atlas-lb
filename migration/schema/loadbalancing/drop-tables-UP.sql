use `loadbalancing`;

RENAME TABLE host_backup TO deprecated_host_backup,
             ip_version TO deprecated_ip_version;

ALTER TABLE access_list DROP COLUMN ip_version;

DROP TABLE test;

UPDATE `meta` SET `meta_value` = '<ADD VALUE HERE>' WHERE `meta_key`='version';


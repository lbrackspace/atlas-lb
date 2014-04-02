use `loadbalancing`;

RENAME TABLE ip_version TO deprecated_ip_version;

ALTER TABLE access_list DROP COLUMN ip_version;

DROP TABLE test;

UPDATE `meta` SET `meta_value` = '64' WHERE `meta_key`='63';


use loadbalancing;

ALTER TABLE lb_ssl DROP COLUMN reencryption;
ALTER TABLE lb_ssl DROP COLUMN certificate_authority;

update meta set `meta_value` = '63' where `meta_key`='version';

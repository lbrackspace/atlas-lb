use loadbalancing;

ALTER TABLE lb_ssl DROP COLUMN reencryption;

update meta set `meta_value` = '65' where `meta_key`='version';

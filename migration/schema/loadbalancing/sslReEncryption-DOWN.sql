use loadbalancing;

ALTER TABLE lb_ssl DROP COLUMN reencryption;

update meta set `meta_value` = '??' where `meta_key`='version';

use loadbalancing;

ALTER TABLE lb_ssl ADD COLUMN `reencryption` tinyint(1) DEFAULT 0;

update meta set `meta_value` = '66' where `meta_key`='version';

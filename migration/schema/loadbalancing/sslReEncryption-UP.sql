use loadbalancing;

ALTER TABLE lb_ssl ADD COLUMN `reencryption` tinyint(1) DEFAULT 0;
ALTER TABLE lb_ssl ADD COLUMN `certificate_authority` mediumtext;

update meta set `meta_value` = '??' where `meta_key`='version';

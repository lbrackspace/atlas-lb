use `loadbalancing_usage`;

ALTER TABLE lb_host_usage MODIFY COLUMN id bigint(20) NOT NULL AUTO_INCREMENT;;
ALTER TABLE lb_merged_host_usage MODIFY COLUMN id bigint(20) NOT NULL AUTO_INCREMENT;;
UPDATE `meta` SET `meta_value` = '61' WHERE `meta_key`='version';

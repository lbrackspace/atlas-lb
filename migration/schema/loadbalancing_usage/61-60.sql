use `loadbalancing_usage`;

ALTER TABLE lb_host_usage MODIFY COLUMN id int(11) NOT NULL AUTO_INCREMENT;;
ALTER TABLE lb_merged_host_usage MODIFY COLUMN id int(11) NOT NULL AUTO_INCREMENT;;
UPDATE `meta` SET `meta_value` = '60' WHERE `meta_key`='version';


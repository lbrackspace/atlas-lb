use loadbalancing;

ALTER TABLE `cluster` DROP FOREIGN KEY `cluster_ibfk_2`;
ALTER TABLE `cluster` DROP KEY `cluster_status_fk`;
ALTER TABLE `cluster` DROP COLUMN `cluster_status`;

DROP TABLE IF EXISTS `cluster_status`;

update `meta` set `meta_value` = '31' where `meta_key`='version';
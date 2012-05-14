use loadbalancing;

DROP TABLE IF EXISTS `cluster_status`;
CREATE TABLE `cluster_status` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
   PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO cluster_status values('ACTIVE', 'Indicates that the cluster is active');
INSERT INTO cluster_status values('INACTIVE', 'Indicates that the cluster is inactive');

ALTER TABLE `cluster` ADD `cluster_status` VARCHAR(32) NOT NULL;
UPDATE `cluster` SET `cluster_status` = 'ACTIVE';

ALTER TABLE `cluster` ADD KEY `cluster_status_fk` (`cluster_status`);

ALTER TABLE `cluster` ADD CONSTRAINT `cluster_ibfk_2` FOREIGN KEY (`cluster_status`) REFERENCES `cluster_status` (`name`);

update `meta` set `meta_value` = '32' where `meta_key`='version';
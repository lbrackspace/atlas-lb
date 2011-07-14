USE `loadbalancing`;

CREATE TABLE `ip_version` (
  `name` varchar(32) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO ip_version values('IPV4', 'A IPV4 ip address');
INSERT INTO ip_version values('IPV6', 'A IPV6 ip address');

ALTER TABLE `virtual_ip` ADD COLUMN `ip_version` varchar(32) default 'IPV4';
ALTER TABLE `virtual_ip` ADD CONSTRAINT `fk_ip_version` FOREIGN KEY (`ip_version`) REFERENCES `ip_version` (`name`);

update `meta` set `meta_value` = '8' where `meta_key`='version';

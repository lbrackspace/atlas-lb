use `loadbalancing`;

DROP TABLE IF EXISTS `ssl_cipher_profile`;

CREATE TABLE `ssl_cipher_profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `ciphers` varchar(1024),
  `comments` varchar(256),
   PRIMARY KEY (`id`),
   UNIQUE KEY `name` (`name`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `lb_ssl` ADD COLUMN `cipher_list` VARCHAR(1024);
ALTER TABLE `lb_ssl` ADD COLUMN `cipher_profile` VARCHAR (128), ADD CONSTRAINT `ssl_cipher_profile_ibfk_1` FOREIGN KEY (`cipher_profile`) REFERENCES `ssl_cipher_profile` (`name`);

update `meta` set `meta_value` = '?' where `meta_key`='version';

use `loadbalancing`;

DROP TABLE IF EXISTS `ssl_cipher_profile`;

CREATE TABLE `ssl_cipher_profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `ciphers` TEXT,
  `comments` varchar(256),
   PRIMARY KEY (`id`),
   UNIQUE KEY `name` (`name`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `lb_ssl` ADD COLUMN `cipher_list` TEXT;
ALTER TABLE `lb_ssl` ADD COLUMN `cipher_profile` VARCHAR (255), ADD CONSTRAINT `lb_ssl_b32b8da0` FOREIGN KEY (`cipher_profile`) REFERENCES `ssl_cipher_profile` (`name`);

update `meta` set `meta_value` = '70' where `meta_key`='version';

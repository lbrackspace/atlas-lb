use `loadbalancing`;

ALTER TABLE lb_ssl DROP foreign key `lb_ssl_b32b8da0`;

ALTER TABLE lb_ssl DROP COLUMN cipher_profile;
ALTER TABLE lb_ssl DROP COLUMN cipher_list;
DROP TABLE IF EXISTS `ssl_cipher_profile`;

update `meta` set `meta_value` = '69' where `meta_key`='version';

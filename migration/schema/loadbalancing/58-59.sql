USE `loadbalancing`;

ALTER TABLE `host` ADD COLUMN `rest_endpoint` VARCHAR (255) NOT NULL;
ALTER TABLE `host` ADD COLUMN `rest_endpoint_active` tinyint(1) NOT NULL;
UPDATE `host` SET `rest_endpoint_active`='1';
ALTER TABLE `host` modify COLUMN `rest_endpoint_active` tinyint(1) NOT NULL;
INSERT INTO host_status(NAME,description)VALUES("REST_API_ENDPOINT","Indicates that this Host shall handle all REST requests for this cluster.");

UPDATE `meta` SET `meta_value` = '59' WHERE `meta_key`='version';

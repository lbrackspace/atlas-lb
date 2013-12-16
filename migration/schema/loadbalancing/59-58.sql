<<<<<<< HEAD
USE `loadbalancing`;

ALTER TABLE `host` DROP COLUMN `rest_endpoint`;
ALTER TABLE `host` DROP COLUMN `rest_endpoint_active`;
delete from host_status where name="REST_API_ENDPOINT";


update `meta` set `meta_value` = '58' where `meta_key`='version';
=======
use `loadbalancing`;

DELETE FROM `lb_data_center` WHERE `name` = 'HKG';

UPDATE `meta` SET `meta_value` = '58' WHERE `meta_key`='version';
>>>>>>> 1.19-candidate

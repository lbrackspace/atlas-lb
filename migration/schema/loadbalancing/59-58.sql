use `loadbalancing`;

DELETE FROM `lb_data_center` WHERE `name` = 'HKG';

UPDATE `meta` SET `meta_value` = '58' WHERE `meta_key`='version';

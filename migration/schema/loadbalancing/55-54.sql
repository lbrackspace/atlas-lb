use `loadbalancing`;

DELETE FROM `lb_data_center` WHERE `name` = 'SYD';

UPDATE `meta` SET `meta_value` = '54' WHERE `meta_key`='version';
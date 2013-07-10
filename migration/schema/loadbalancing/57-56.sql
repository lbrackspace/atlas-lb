use `loadbalancing`;

DELETE FROM `lb_data_center` WHERE `name` = 'IAD';

UPDATE `meta` SET `meta_value` = '56' WHERE `meta_key`='version';

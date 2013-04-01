use `loadbalancing`;

ALTER TABLE `lb_usage` ADD `corrected` tinyint(1) NOT NULL DEFAULT '0';

ALTER TABLE `lb_usage` ADD `num_attempts` int(11) NOT NULL DEFAULT '0';

INSERT INTO event_type VALUES('AH_USAGE_EXECUTION', 'Atom Hopper Usage Processor');

UPDATE `meta` SET `meta_value` = '53' WHERE `meta_key`='version';
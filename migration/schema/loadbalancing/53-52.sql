use `loadbalancing`;

ALTER TABLE `lb_usage` DROP COLUMN `corrected`;

ALTER TABLE `lb_usage` DROP COLUMN `num_attempts`;

DELETE FROM event_type WHERE NAME = 'AH_USAGE_EXECUTION';


update `meta` set `meta_value` = '52' where `meta_key`='version';
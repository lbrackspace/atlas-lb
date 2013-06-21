USE `loadbalancing`;

UPDATE `state` SET `inputpath` = NULL WHERE `jobname` = 'LB_USAGE_ROLLUP';
DELETE FROM `state` WHERE `jobname` = 'THE_ONE_TO_RULE_THEM_ALL';

update `meta` set `meta_value` = '57' where `meta_key`='version';

USE `loadbalancing`;

DELETE FROM `state` WHERE `jobname` = 'THE_ONE_TO_RULE_THEM_ALL';

update `meta` set `meta_value` = 'THEPREVIOUSVERSION' where `meta_key`='version';

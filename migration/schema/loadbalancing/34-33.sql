use loadbalancing;

ALTER TABLE `lb_ssl` DROP INDEX `uk_lb_id`;

update `meta` set `meta_value` = '33' where `meta_key`='version';
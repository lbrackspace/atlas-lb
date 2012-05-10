use loadbalancing;

ALTER TABLE `lb_ssl` DROP INDEX `uk_lb_id`;

update `loadbalancerMeta` set `meta_value` = '33' where `meta_key`='version';
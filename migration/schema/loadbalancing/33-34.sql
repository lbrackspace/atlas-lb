use loadbalancing;

ALTER TABLE `lb_ssl` ADD UNIQUE INDEX `uk_lb_id` (loadbalancer_id);

update `meta` set `meta_value` = '34' where `meta_key`='version';
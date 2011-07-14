USE loadbalancing;

ALTER TABLE `lb_suspension` ADD COLUMN	`ticket_id` int(11) NOT NULL;
ALTER TABLE `lb_rate_limit` ADD COLUMN	`ticket_id` int(11) NOT NULL;

update `meta` set `meta_value` = '18' where `meta_key`='version';

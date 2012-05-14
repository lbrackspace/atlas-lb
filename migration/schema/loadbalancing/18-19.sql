USE loadbalancing;

insert into ticket(ticket_id, loadbalancer_id) select ticket_id, loadbalancer_id from lb_rate_limit;
insert into ticket(ticket_id, loadbalancer_id, comment) select ticket_id, loadbalancer_id, reason from lb_suspension;

ALTER TABLE `lb_suspension` DROP COLUMN `ticket_id`;
ALTER TABLE `lb_rate_limit` DROP COLUMN `ticket_id`;

update `meta` set `meta_value` = '19' where `meta_key`='version';

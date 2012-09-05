

use `loadbalancing`;
alter table loadbalancer_service_event drop column detailed_msg;

alter table loadbalancer_service_event drop index created_idx;
alter table loadbalancer_service_event drop index account_id_idx;
alter table loadbalancer_service_event drop index loadbalancer_id_idx;

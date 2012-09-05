use `loadbalancing`;
alter table loadbalancer_service_event add column detailed_msg mediumtext null;
alter table loadbalancer_service_event add index created_idx(created);
alter table loadbalancer_service_event add index account_id_idx(account_id);
alter table loadbalancer_service_event add index loadbalancer_id_idx(loadbalancer_id);


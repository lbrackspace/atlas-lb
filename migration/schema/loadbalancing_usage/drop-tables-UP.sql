use `loadbalancing_usage`;

DROP TABLE lb_usage_event;
DROP TABLE event_type;
DROP TABLE lb_usage;

UPDATE `meta` SET `meta_value` = '<ADD VALUE HERE>' WHERE `meta_key`='version';


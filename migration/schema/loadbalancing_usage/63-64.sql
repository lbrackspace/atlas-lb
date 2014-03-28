use `loadbalancing_usage`;

DROP TABLE lb_usage_event;
DROP TABLE event_type;
DROP TABLE lb_usage;

UPDATE `meta` SET `meta_value` = '64' WHERE `meta_key`='version';


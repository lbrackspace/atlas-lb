use `loadbalancing`;

ALTER TABLE loadbalancer_service_event CHANGE event_title event_title VARCHAR(64);

UPDATE `meta` SET `meta_value` = '54' WHERE `meta_key`='version';
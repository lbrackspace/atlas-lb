use `loadbalancing`;

INSERT INTO lb_protocol (name, description, port, enabled) VALUES("GENERIC_STREAMING", "The Generic Streaming protocol", 0, 1);

UPDATE `meta` SET `meta_value` = '65' WHERE `meta_key`='version';
use `loadbalancing`;

ALTER TABLE node_service_event DROP COLUMN callback_host;

UPDATE `meta` SET `meta_value` = '??' WHERE `meta_key`='version';
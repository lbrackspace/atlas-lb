use `loadbalancing`;

ALTER TABLE node_service_event ADD callback_host VARCHAR(255) NOT NULL;

UPDATE `meta` SET `meta_value` = '??' WHERE `meta_key`='version';

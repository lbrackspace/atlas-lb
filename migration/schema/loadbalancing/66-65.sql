use `loadbalancing`;

DELETE from lb_session_persistence where `name`='SSL_ID';
DELETE from cl_type where `name`='SMOKE';
ALTER TABLE node_service_event DROP COLUMN callback_host;

UPDATE `meta` SET `meta_value` = '65' WHERE `meta_key`='version';
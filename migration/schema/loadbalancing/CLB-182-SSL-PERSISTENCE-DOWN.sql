use `loadbalancing`;

DELETE from lb_session_persistence where `name`='SSL_ID'

UPDATE `meta` SET `meta_value` = '64' WHERE `meta_key`='version';
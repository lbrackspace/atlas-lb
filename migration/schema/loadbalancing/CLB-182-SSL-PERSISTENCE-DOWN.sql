use `loadbalancing`;

DELETE from lb_session_persistence where `name`='SSL_ID'

UPDATE `meta` SET `meta_value` = '?' WHERE `meta_key`='version';
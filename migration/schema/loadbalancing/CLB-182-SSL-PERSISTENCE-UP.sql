use `loadbalancing`;

INSERT INTO lb_session_persistence values('SSL_ID', 'Indicates that the load balancer uses SSL_ID session persistence',true);

UPDATE `meta` SET `meta_value` = '65' WHERE `meta_key`='version';
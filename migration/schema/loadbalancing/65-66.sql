use `loadbalancing`;

INSERT INTO lb_session_persistence values('SSL_ID', 'Indicates that the load balancer uses SSL_ID session persistence',true);
INSERT INTO cl_type(name,description)values('SMOKE','Deployment testing');
ALTER TABLE node_service_event ADD callback_host VARCHAR(255) NOT NULL;

UPDATE `meta` SET `meta_value` = '66' WHERE `meta_key`='version';
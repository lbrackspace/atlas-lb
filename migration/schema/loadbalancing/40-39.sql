use loadbalancing;

DELETE FROM `lb_protocol` WHERE `name`  = 'SFTP';

delete from host_status where name="SOAP_API_ENDPOINT";

DELETE FROM `event_type` WHERE `name` = 'SSL_MIXED_ON';
DELETE FROM `event_type` WHERE `name` = 'SSL_ONLY_ON';

ALTER TABLE `lb_usage` DROP COLUMN `avg_concurrent_conns_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `bandwidth_in_ssl`;
ALTER TABLE `lb_usage` DROP COLUMN `bandwidth_out_ssl`;

DROP TABLE `lb_status_history`;

delete from event_type where `name` = 'BUILD_LOADBALANCER';
delete from event_type where `name` = 'PENDING_UPDATE_LOADBALANCER';
delete from event_type where `name` = 'PENDING_DELETE_LOADBALANCER';

update `meta` set `meta_value` = '39' where `meta_key`='version';

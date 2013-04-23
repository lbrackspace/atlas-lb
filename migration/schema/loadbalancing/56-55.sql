use `loadbalancing`;

-- DROP INDEXES~!!!
ALTER TABLE `loadbalancer` ADD UNIQUE INDEX id(id);
ALTER TABLE `node` ADD UNIQUE INDEX id(id);
ALTER TABLE `host` ADD UNIQUE INDEX id(id);
ALTER TABLE `cluster` ADD UNIQUE INDEX id(id);
ALTER TABLE `virtual_ip_ipv4` ADD UNIQUE INDEX id(id);
ALTER TABLE `health_monitor` ADD UNIQUE INDEX id(id);
ALTER TABLE `alert` ADD UNIQUE INDEX id(id);

ALTER TABLE `lb_usage` ADD UNIQUE INDEX id(id);
ALTER TABLE `account_usage` ADD UNIQUE INDEX id(id);


UPDATE `meta` SET `meta_value` = '55' WHERE `meta_key`='version';
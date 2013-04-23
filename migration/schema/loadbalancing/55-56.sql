use `loadbalancing`;

-- DROP INDEXES~!!!
DROP INDEX `id` ON `loadbalancer`;
DROP INDEX `id` ON `node`;
DROP INDEX `id` ON `host`;
DROP INDEX `id` ON `cluster`;
DROP INDEX `id` ON `virtual_ip_ipv4`;
DROP INDEX `id` ON `health_monitor`;
DROP INDEX `id` ON `alert`;


DROP INDEX `id` ON `lb_usage`;
DROP INDEX `id` ON `account_usage`;

UPDATE `meta` SET `meta_value` = '56' WHERE `meta_key`='version';
use `loadbalancing_usage`;

DROP TABLE `lb_merged_host_usage`;
DROP TABLE `lb_host_usage`;

update `meta` set `meta_value` = '56' where `meta_key`='version';

use `loadbalancing_usage`;

DROP TABLE `lb_merged_host_usage`;
DROP TABLE `lb_host_usage`;
DROP TABLE `usage_event_type`;

update `meta` set `meta_value` = '56' where `meta_key`='version';

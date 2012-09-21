use loadbalancing_usage;

alter table lb_usage modify last_bandwidth_bytes_in bigint(20);
alter table lb_usage modify last_bandwidth_bytes_in_ssl bigint(20);
alter table lb_usage modify last_bandwidth_bytes_out bigint(20);
alter table lb_usage modify last_bandwidth_bytes_out_ssl bigint(20);

update `meta` set `meta_value` = '48' where `meta_key`='version';

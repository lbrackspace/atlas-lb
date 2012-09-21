use loadbalancing_usage;

alter table lb_usage modify last_bandwidth_bytes_in bigint(20) NOT NULL DEFAULT '0';
alter table lb_usage modify last_bandwidth_bytes_in_ssl bigint(20) NOT NULL DEFAULT '0';
alter table lb_usage modify last_bandwidth_bytes_out bigint(20) NOT NULL DEFAULT '0';
alter table lb_usage modify last_bandwidth_bytes_out_ssl bigint(20) NOT NULL DEFAULT '0';

update `meta` set `meta_value` = '47' where `meta_key`='version';

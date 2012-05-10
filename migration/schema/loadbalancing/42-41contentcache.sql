use loadbalancing;

alter table `loadbalancer` drop `content_caching`;

delete from `event_type` where name = 'UPDATE_CONTENT_CACHING';

update `loadbalancerMeta` set `meta_value` = '41????' where `meta_key`='version';

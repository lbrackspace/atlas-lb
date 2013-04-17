use `loadbalancing_usage`;

update `meta` set `meta_value` = 'THEPREVIOUSVERSION' where `meta_key`='version';

use 'loadbalancing';

DELETE from cl_type where `name`='SMOKE';

update meta set `meta_value` = '??' where `meta_key`='version';
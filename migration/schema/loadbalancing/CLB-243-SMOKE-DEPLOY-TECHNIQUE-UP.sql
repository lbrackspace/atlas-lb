use 'loadbalancing';

INSERT INTO cl_type(name,description)values('SMOKE','Deployment testing');

update meta set `meta_value` = '??' where `meta_key`='version';
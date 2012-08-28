CREATE UNIQUE INDEX `virtual_ip_port` ON (`virtualip_id`,`port`);
CREATE UNIQUE INDEX `virtualip6_port` ON (`virtualip6_id`,`port`);

update `meta` set `meta_value` = '?' where `meta_key`='version';

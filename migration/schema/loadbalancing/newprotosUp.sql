use loadbalancing;

INSERT INTO `lb_protocol` VALUES('UDP_STREAM', 'The UDP STREAMING Protocol', 0, 1);
INSERT INTO `lb_protocol` VALUES('UDP', 'The UDP Protocol', 0, 1);
INSERT INTO `lb_protocol` VALUES('DNS_UDP', 'The DNS/UDP  Protocol', 53, 1);
INSERT INTO `lb_protocol` VALUES('DNS_TCP', 'The DNS/TCP Protocol', 53, 1);

update `meta` set `meta_value` = '?' where `meta_key`='version';
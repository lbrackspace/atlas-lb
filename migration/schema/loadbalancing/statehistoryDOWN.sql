use loadbalancing;


DROP TABLE `lb_status_history`;

update `meta` set `meta_value` = '40?' where `meta_key`='version';

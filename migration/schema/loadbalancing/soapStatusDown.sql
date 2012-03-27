use loadbalancing;
delete from host_status where name="SOAP_API_ENDPOINT";
update `meta` set `meta_value` = '??' where `meta_key`='version';

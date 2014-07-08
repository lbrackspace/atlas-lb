use `loadbalancing`;

DELETE FROM lb_protocol WHERE name = "TCP_STREAM";

UPDATE `meta` SET `meta_value` = '64' WHERE `meta_key`='version';
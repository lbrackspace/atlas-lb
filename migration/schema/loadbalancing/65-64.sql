use `loadbalancing`;

DELETE FROM lb_protocol WHERE name = "GENERIC_STREAMING";

UPDATE `meta` SET `meta_value` = '64' WHERE `meta_key`='version';
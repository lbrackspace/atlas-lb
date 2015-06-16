use loadbalancing;

CREATE TABLE `hdfs_lzo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hour_key` int(11) NOT NULL,
  `finished` tinyint(1) NOT NULL,
  `reupload_needed` tinyint(1) NOT NULL,
  `file_size` bigint(20) NOT NULL,
  `md5_needed` tinyint(1) NOT NULL,
  `cf_needed` tinyint(1) NOT NULL,
  `hdfs_needed` tinyint(1) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hour_key_idx` (`hour_key`),
  KEY `finished_idx` (`finished`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `cloud_files_lzo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hour_key` int(11) NOT NULL,
  `frag` int(1) NOT NULL,
  `finished` tinyint(1) NOT NULL,
  `md5` varchar(48) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hour_key_frag_idx` (`hour_key`,`frag`),
  KEY `hour_key_idx` (`hour_key`),
  KEY `finished_idx` (`finished`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
update `meta` set `meta_value` = '68' where `meta_key`='version';

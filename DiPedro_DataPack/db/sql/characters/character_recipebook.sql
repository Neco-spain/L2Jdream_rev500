CREATE TABLE IF NOT EXISTS `character_recipebook` (
  `char_id` decimal(11) NOT NULL default 0,
  `id` decimal(11) NOT NULL default 0,
  `type` INT NOT NULL default 0,
  PRIMARY KEY  (`id`,`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
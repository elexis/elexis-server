CREATE TABLE IF NOT EXISTS `ch_medelexis_labortarif2009` (
  `ID` varchar(25) NOT NULL,
  `lastupdate` bigint(20) DEFAULT NULL,
  `deleted` char(1) DEFAULT '0',
  `chapter` varchar(255) DEFAULT NULL,
  `code` varchar(12) DEFAULT NULL,
  `tp` varchar(10) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `limitatio` longtext,
  `fachbereich` varchar(10) DEFAULT NULL,
  `GueltigVon` char(8) DEFAULT NULL,
  `GueltigBis` char(8) DEFAULT NULL,
  `praxistyp` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);
INSERT INTO `CH_MEDELEXIS_LABORTARIF2009` VALUES ('a6e58fc71c723bd54016760',1456222647737,'0','1,2','1442.00','120','Immunglobulin IgD','','CI','20150101',NULL,'-1');
INSERT INTO `CH_MEDELEXIS_LABORTARIF2009` VALUES ('ub49a50af4d3e51e40906',1456222647736,'0','1.2','1442.00','120','Immunglobulin IgD','','CI','20130101','20150101','-1');
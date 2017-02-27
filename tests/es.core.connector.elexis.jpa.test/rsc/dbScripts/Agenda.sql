CREATE TABLE `AGNTERMINE`
(
   `ID`                varchar(127),
   lastupdate          bigint(20),
   `PatID`             varchar(80),
   `Bereich`           varchar(25),
   `Tag`               char(8),
   `Beginn`            char(4),
   `Dauer`             char(4),
   `Grund`             longtext,
   `TerminTyp`         varchar(50),
   `TerminStatus`      varchar(50),
   `ErstelltVon`       varchar(25),
   angelegt            varchar(10),
   lastedit            varchar(10),
   `PalmID`            int(11) DEFAULT 0,
   flags               varchar(10),
   deleted             char(2) DEFAULT '0',
   `Extension`         longtext,
   linkgroup           varchar(50),
   `StatusHistory`     longtext,
   priority            char(1),
   `caseType`          char(1),
   `insuranceType`     char(1),
   `treatmentReason`   char(1)
);

CREATE INDEX it on AGNTERMINE (Tag,Beginn,Bereich);
CREATE INDEX pattern on AGNTERMINE (PatID);
CREATE INDEX agnbereich on AGNTERMINE (Bereich);
INSERT INTO AGNTERMINE (ID, PatId) VALUES (1, '');
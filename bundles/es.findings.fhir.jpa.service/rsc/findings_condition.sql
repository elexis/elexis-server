CREATE TABLE IF NOT EXISTS CH_ELEXIS_CORE_FINDINGS_CONDITION (
  ID varchar(25) DEFAULT NULL,
  lastupdate bigint(20) DEFAULT NULL,
  deleted char(1) DEFAULT '0',
  patientid varchar(80) DEFAULT NULL,
  encounterid varchar(80) DEFAULT NULL,
  content longtext
);

CREATE INDEX CH_ELEXIS_CORE_FINDINGS_CONDITION_IDX1 ON CH_ELEXIS_CORE_FINDINGS_CONDITION (patientid);
CREATE INDEX CH_ELEXIS_CORE_FINDINGS_CONDITION_IDX2 ON CH_ELEXIS_CORE_FINDINGS_CONDITION (consultationid);

INSERT INTO  CH_ELEXIS_CORE_FINDINGS_CONDITION (ID, patientid) VALUES ('VERSION','1.0.0');
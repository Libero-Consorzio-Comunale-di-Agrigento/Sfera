--liquibase formatted sql
--changeset rdestasio:2.4.5.0_20200221_01

ALTER TABLE DETERMINE ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/

UPDATE DETERMINE SET STATO_MARCATURA = 'DA_NON_MARCARE' where MARCATURA_TEMPORALE = 'N'
/

UPDATE DETERMINE SET STATO_MARCATURA = 'MARCATO' where MARCATURA_TEMPORALE = 'Y'
/

ALTER TABLE DETERMINE DROP COLUMN MARCATURA_TEMPORALE
/

ALTER TABLE DELIBERE ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/

UPDATE DELIBERE SET STATO_MARCATURA = 'DA_NON_MARCARE' where MARCATURA_TEMPORALE = 'N'
/

UPDATE DELIBERE SET STATO_MARCATURA = 'MARCATO' where MARCATURA_TEMPORALE = 'Y'
/

ALTER TABLE DELIBERE DROP COLUMN MARCATURA_TEMPORALE
/

ALTER TABLE VISTI_PARERI ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/

UPDATE VISTI_PARERI SET STATO_MARCATURA = 'DA_NON_MARCARE' where MARCATURA_TEMPORALE = 'N'
/

UPDATE VISTI_PARERI SET STATO_MARCATURA = 'MARCATO' where MARCATURA_TEMPORALE = 'Y'
/

ALTER TABLE VISTI_PARERI DROP COLUMN MARCATURA_TEMPORALE
/

ALTER TABLE CERTIFICATI ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/

UPDATE CERTIFICATI SET STATO_MARCATURA = 'DA_NON_MARCARE' where MARCATURA_TEMPORALE = 'N'
/

UPDATE CERTIFICATI SET STATO_MARCATURA = 'MARCATO' where MARCATURA_TEMPORALE = 'Y'
/

ALTER TABLE CERTIFICATI DROP COLUMN MARCATURA_TEMPORALE
/


ALTER TABLE FILE_ALLEGATI ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/

UPDATE FILE_ALLEGATI SET STATO_MARCATURA = 'DA_NON_MARCARE' where MARCATURA_TEMPORALE = 'N'
/

UPDATE FILE_ALLEGATI SET STATO_MARCATURA = 'MARCATO' where MARCATURA_TEMPORALE = 'Y'
/

ALTER TABLE FILE_ALLEGATI DROP COLUMN MARCATURA_TEMPORALE
/

ALTER TABLE GDO_DOCUMENTI ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/

ALTER TABLE PROPOSTE_DELIBERA ADD (STATO_MARCATURA VARCHAR2(255 BYTE) default 'DA_NON_MARCARE')
/
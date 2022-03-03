--liquibase formatted sql
--changeset rdestasio:2.5.2.2_20200221_01

-- eseguibilità immediata per la determina
ALTER TABLE TIPI_DETERMINA ADD (eseguibilita_immediata CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

ALTER TABLE DETERMINE ADD (eseguibilita_immediata CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

alter table DETERMINE add motivazioni_eseguibilita varchar2(255 byte) NULL
/

-- Gestione dei certificati di immediata eseguibilità.
ALTER TABLE TIPI_DETERMINA ADD (ID_TIPO_CERT_IMM_ESEG NUMBER(19))
/

ALTER TABLE PROPOSTE_DELIBERA ADD (CONTROLLA_DESTINATARI CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

ALTER TABLE DETERMINE ADD (CONTROLLA_DESTINATARI CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/
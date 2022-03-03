--liquibase formatted sql
--changeset rdestasio:2.5.0.0_20200221_01

alter table tipi_determina add ruolo_riservato varchar2(255 byte) NULL
/

alter table tipi_delibera add ruolo_riservato varchar2(255 byte) NULL
/

CREATE TABLE NOTIFICHE_ERRORI
(
  ID_NOTIFICA_ERRORE        NUMBER(19)          NOT NULL,
  OPERAZIONE                VARCHAR2(255 BYTE)  NOT NULL,
  ID_RIFERIMENTO            VARCHAR2(255 BYTE)  NOT NULL,
  ID_NOTIFICA               NUMBER(19),
  ID_STEP_CORRENTE          NUMBER(19),
  DATA_INS                  DATE                NOT NULL,
  ENTE                      VARCHAR2(255 BYTE)  NOT NULL,
  DATA_UPD                  DATE                NOT NULL,
  UTENTE_INS                VARCHAR2(255 BYTE)  NOT NULL,
  UTENTE_UPD                VARCHAR2(255 BYTE)  NOT NULL,
  VALIDO                    CHAR(1 BYTE)        NOT NULL,
  VERSION                   NUMBER(19)          NOT NULL
)
/

ALTER TABLE NOTIFICHE_ERRORI ADD (PRIMARY KEY (ID_NOTIFICA_ERRORE) ENABLE VALIDATE)
/

ALTER TABLE TIPI_DETERMINA ADD (DESCRIZIONE_NOTIFICA VARCHAR2(1000 BYTE))
/

ALTER TABLE TIPI_VISTO_PARERE ADD (DESCRIZIONE_NOTIFICA VARCHAR2(1000 BYTE))
/

ALTER TABLE TIPI_CERTIFICATO ADD (DESCRIZIONE_NOTIFICA VARCHAR2(1000 BYTE))
/

ALTER TABLE TIPI_DELIBERA ADD (DESCRIZIONE_NOTIFICA VARCHAR2(1000 BYTE), DESCRIZIONE_NOTIFICA_DELIBERA VARCHAR2(1000 BYTE))
/
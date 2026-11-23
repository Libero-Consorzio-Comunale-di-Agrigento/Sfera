--liquibase formatted sql
--changeset amarinelli:2.5_20240911_74.create_table_impatti_generazionali runOnChange:true stripComments:false failOnError:true

CREATE TABLE IMPATTI_GENERAZIONALI
(
  ID_IMPATTO_GENERAZIONALE  NUMBER(19)          NOT NULL,
  VERSION                   NUMBER(19)          NOT NULL,
  TITOLO                    VARCHAR2(255 BYTE)  NOT NULL,
  DESCRIZIONE               VARCHAR2(4000 BYTE),
  FRASE                     VARCHAR2(4000 BYTE),
  DELIBERA                  CHAR(1 BYTE)        NOT NULL,
  DETERMINA                 CHAR(1 BYTE)        NOT NULL,
  ENTE                      VARCHAR2(255 BYTE)  NOT NULL,
  DATA_INS                  DATE                NOT NULL,
  DATA_UPD                  DATE                NOT NULL,
  UTENTE_INS                VARCHAR2(255 BYTE)  NOT NULL,
  UTENTE_UPD                VARCHAR2(255 BYTE)  NOT NULL,
  VALIDO                    CHAR(1 BYTE)        NOT NULL,
  VALIDO_AL                 DATE,
  VALIDO_DAL                DATE                NOT NULL
)
/

alter table impatti_generazionali
    add (primary key (id_impatto_generazionale) enable validate)
/

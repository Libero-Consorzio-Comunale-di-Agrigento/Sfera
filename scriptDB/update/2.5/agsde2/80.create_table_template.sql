--liquibase formatted sql
--changeset amarinelli:2.5_20241113_80.create_table_template runOnChange:true stripComments:false failOnError:true

CREATE TABLE PROTOCOLLO_PITRE_TEMPLATE
(
  TEMPLATE_ID  NUMBER                           NOT NULL,
  NOME         VARCHAR2(255 BYTE),
  CODICE       CLOB,
  MODELLO      CLOB,
  VALIDO       CHAR(1 BYTE)
)
/

CREATE TABLE PROTOCOLLO_PITRE_FIELD
(
  FIELD_ID     NUMBER,
  TEMPLATE_ID  NUMBER                           NOT NULL,
  NOME         VARCHAR2(255 BYTE),
  VALORI       VARCHAR2(4000 BYTE),
  TIPO         VARCHAR2(255 BYTE),
  CODICE       VARCHAR2(255 BYTE),
  RICHIESTO    CHAR(1 BYTE),
  VALIDO       CHAR(1 BYTE)
)
/


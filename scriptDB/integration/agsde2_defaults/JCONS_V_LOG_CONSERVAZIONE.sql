--liquibase formatted sql
--changeset rdestasio:install_20200221_defaults_05

/* Formatted on 29/08/2017 12:19:32 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW JCONS_V_LOG_CONSERVAZIONE
(
   ID_DOCUMENTO,
   DATA_FINE,
   DATA_INIZIO,
   DESCRIZIONE,
   ESITO_CONSERVAZIONE,
   ID_DOCUMENTO_RIF,
   ID_SISTEMA_CONSERVAZIONE,
   ID_TRANSAZIONE,
   LOG,
   NOME,
   STATO_CONSERVAZIONE
)
AS
   SELECT CAST (0 AS NUMBER (19)) ID_DOCUMENTO,
          sysdate DATA_FINE,
          sysdate DATA_INIZIO,
          CAST ('' AS VARCHAR (255)) DESCRIZIONE,
          CAST ('' AS VARCHAR (255)) ESITO_CONSERVAZIONE,
          CAST (0 AS NUMBER (19)) ID_DOCUMENTO_RIF,
          CAST ('' AS VARCHAR (255)) ID_SISTEMA_CONSERVAZIONE,
          CAST (0 AS NUMBER (19)) ID_TRANSAZIONE,
          CAST ('' AS VARCHAR (255)) LOG,
          CAST ('' AS VARCHAR (255)) NOME,
          CAST ('' AS VARCHAR (255)) STATO_CONSERVAZIONE
     FROM dual
/

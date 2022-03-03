--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_07 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_STATI
(
   STATO_TERRITORIO,
   DENOMINAZIONE,
   DENOMINAZIONE_AL1,
   DENOMINAZIONE_AL2,
   SIGLA,
   DESC_CITTADINANZA,
   DESC_CITTADINANZA_AL1,
   DESC_CITTADINANZA_AL2,
   RAGGRUPPAMENTO,
   STATO_APPARTENENZA,
   UTENTE_AGGIORNAMENTO,
   DATA_AGGIORNAMENTO
)
AS
   SELECT STATO_TERRITORIO,
          DENOMINAZIONE,
          DENOMINAZIONE_AL1,
          DENOMINAZIONE_AL2,
          SIGLA,
          DESC_CITTADINANZA,
          DESC_CITTADINANZA_AL1,
          DESC_CITTADINANZA_AL2,
          RAGGRUPPAMENTO,
          STATO_APPARTENENZA,
          UTENTE_AGGIORNAMENTO,
          DATA_AGGIORNAMENTO
     FROM AD4_STATI_TERRITORI
/



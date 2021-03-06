--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_05 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_REGIONI
(
   REGIONE,
   DENOMINAZIONE,
   DENOMINAZIONE_AL1,
   DENOMINAZIONE_AL2,
   ID_REGIONE,
   UTENTE_AGGIORNAMENTO,
   DATA_AGGIORNAMENTO
)
AS
   SELECT REGIONE,
          DENOMINAZIONE,
          DENOMINAZIONE_AL1,
          DENOMINAZIONE_AL2,
          ID_REGIONE,
          UTENTE_AGGIORNAMENTO,
          DATA_AGGIORNAMENTO
     FROM AD4_REGIONI
/




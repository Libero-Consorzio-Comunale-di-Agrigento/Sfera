--liquibase formatted sql
--changeset rdestasio:install_20200221_defaults_07

-- questa vista serve quando so4 non è aggiornato all'ultima versione
CREATE OR REPLACE FORCE VIEW SO4_V_DELEGHE
(
   ID_DELEGA,
   DELEGANTE_SOGGETTO,
   DELEGANTE_UTENTE,
   DELEGATO_SOGGETTO,
   DELEGATO_UTENTE,
   OTTICA,
   PROGR_UNITA_ORGANIZZATIVA,
   RUOLO,
   ID_APPLICATIVO,
   ISTANZA_APPLICATIVO,
   MODULO_APPLICATIVO,
   ID_COMPETENZA_DELEGA,
   CODICE_COMPETENZA_DELEGA,
   DAL,
   AL,
   UTENTE_AGGIORNAMENTO,
   DATA_AGGIORNAMENTO
)
AS
   SELECT 0 ID_DELEGA,
          0 DELEGANTE_SOGGETTO,
          cast('' as varchar2(255)) DELEGANTE_UTENTE,
          0 DELEGATO_SOGGETTO,
          cast('' as varchar2(255)) DELEGATO_UTENTE,
          cast('' as varchar2(255)) ,
          0,
          cast('' as varchar2(255)) ,
          0,
          cast('' as varchar2(255))  istanza_applicativo,
          cast('' as varchar2(255))  modulo_applicativo,
          0,
          cast('' as varchar2(255)) ,
          sysdate,
          sysdate,
          cast('' as varchar2(255)),
          sysdate
     FROM dual
/

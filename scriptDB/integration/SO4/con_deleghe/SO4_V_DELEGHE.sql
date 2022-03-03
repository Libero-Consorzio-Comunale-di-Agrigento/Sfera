--liquibase formatted sql
--changeset rdestasio:install_20200221_deleghe_03

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
   SELECT ID_DELEGA,
          DELEGANTE DELEGANTE_SOGGETTO,
          US.UTENTE DELEGANTE_UTENTE,
          DELEGATO DELEGATO_SOGGETTO,
          US2.UTENTE DELEGATO_UTENTE,
          deleghe.OTTICA,
          deleghe.PROGR_UNITA_ORGANIZZATIVA,
          deleghe.RUOLO,
          a.id_applicativo,
          a.istanza istanza_applicativo,
          a.modulo modulo_applicativo,
          cd.id_COMPETENZA_delega,
          cd.codice codice_competenza_delega,
          deleghe.DAL,
          deleghe.AL,
          deleghe.UTENTE_AGGIORNAMENTO,
          deleghe.DATA_AGGIORNAMENTO
     FROM SO4_DELEGHE deleghe,
          SO4_COMPETENZE_DELEGA CD,
          SO4_APPLICATIVI A,
          ad4_utenti_soggetti us,
          ad4_utenti_soggetti us2
    WHERE     US.soggetto = DELEGANTE
          AND US2.soggetto = DELEGATO
          AND cd.id_competenza_delega(+) = deleghe.id_competenza_delega
          AND a.id_applicativo(+) = cd.id_applicativo
/

--liquibase formatted sql
--changeset rdestasio:install_20200221_15

CREATE OR REPLACE FORCE VIEW SO4_V_UNITA_ORGANIZZATIVE
(
   OTTICA,
   REVISIONE,
   REVISIONE_CESSAZIONE,
   SEQUENZA,
   PROGR,
   DAL,
   AL,
   PROGR_PADRE,
   CODICE,
   DESCRIZIONE,
   ID_SUDDIVISIONE,
   TIPOLOGIA,
   SE_GIURIDICO,
   ASSEGNAZIONE_COMPONENTI,
   AMMINISTRAZIONE,
   CODICE_AOO,
   CENTRO_COSTO,
   CENTRO_RESPONSABILITA,
   UTENTE_AD4,
   TIPO_UNITA,
   ETICHETTA,
   TAG_MAIL
)
AS
   SELECT ottica,
          revisione,
          revisione_cessazione,
          sequenza,
          progr_unita_organizzativa progr,
          dal,
          al,
          progr_unita_padre progr_padre,
          codice_uo codice,
          descrizione,
          id_suddivisione,
          tipologia_unita tipologia,
          DECODE (se_giuridico,  'SI', 1,  'NO', 0,  NULL) se_giuridico,
          DECODE (assegnazione_componenti,  'SI', 1,  'NO', 0,  NULL)
             assegnazione_componenti,
          amministrazione,
          aoo codice_aoo,
          centro centro_costo,
          DECODE (centro_responsabilita,  'SI', 1,  'NO', 0,  NULL)
             centro_responsabilita,
          utente_ad4,
          tipo_unita,
          etichetta,
          tag_mail
     FROM SO4_UNITA_ORGANIZZATIVE
/

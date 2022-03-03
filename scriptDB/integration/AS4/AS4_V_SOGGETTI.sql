--liquibase formatted sql
--changeset rdestasio:install_20200221_as4_01 runOnChange:true

CREATE OR REPLACE FORCE VIEW AS4_V_SOGGETTI
(
   NI,
   DAL,
   COGNOME,
   NOME,
   SESSO,
   DATA_NAS,
   PROVINCIA_NAS,
   COMUNE_NAS,
   STATO_NAS,
   LUOGO_NAS,
   CODICE_FISCALE,
   CODICE_FISCALE_ESTERO,
   PARTITA_IVA,
   CITTADINANZA,
   GRUPPO_LING,
   INDIRIZZO_RES,
   PROVINCIA_RES,
   COMUNE_RES,
   STATO_RES,
   CAP_RES,
   TEL_RES,
   FAX_RES,
   PRESSO,
   INDIRIZZO_DOM,
   PROVINCIA_DOM,
   COMUNE_DOM,
   STATO_DOM,
   CAP_DOM,
   TEL_DOM,
   FAX_DOM,
   UTENTE_AGG,
   DATA_AGG,
   COMPETENZA,
   COMPETENZA_ESCLUSIVA,
   TIPO_SOGGETTO,
   FLAG_TRG,
   STATO_CEE,
   PARTITA_IVA_CEE,
   FINE_VALIDITA,
   AL,
   DENOMINAZIONE,
   INDIRIZZO_WEB,
   NOTE,
   UTENTE
)
AS
   SELECT s.NI,
          s.DAL,
          s.COGNOME,
          s.NOME,
          s.SESSO,
          s.DATA_NAS,
          CASE WHEN s.provincia_nas < 200 THEN s.provincia_nas ELSE NULL END
             PROVINCIA_NAS,
          TO_NUMBER (
             DECODE (s.comune_nas,
                     NULL, NULL,
                     s.provincia_nas || LPAD (s.comune_nas, 4, 0)))
             COMUNE_NAS,
          CASE
             WHEN s.provincia_nas < 200 THEN 100
             ELSE statiNas.stato_territorio
          END
             STATO_NAS,
          s.LUOGO_NAS,
          s.CODICE_FISCALE,
          s.CODICE_FISCALE_ESTERO,
          s.PARTITA_IVA,
          s.CITTADINANZA,
          s.GRUPPO_LING,
          s.INDIRIZZO_RES,
          CASE WHEN s.provincia_res < 200 THEN s.provincia_res ELSE NULL END
             PROVINCIA_RES,
          TO_NUMBER (
             DECODE (s.comune_res,
                     NULL, NULL,
                     s.provincia_res || LPAD (s.comune_res, 4, 0)))
             COMUNE_RES,
          CASE
             WHEN s.provincia_res < 200 THEN 100
             ELSE statiRes.stato_territorio
          END
             STATO_RES,
          s.CAP_RES,
          s.TEL_RES,
          s.FAX_RES,
          s.PRESSO,
          s.INDIRIZZO_DOM,
          CASE WHEN s.provincia_dom < 200 THEN s.provincia_dom ELSE NULL END
             PROVINCIA_DOM,
          TO_NUMBER (
             DECODE (s.comune_dom,
                     NULL, NULL,
                     s.provincia_dom || LPAD (s.comune_dom, 4, 0)))
             COMUNE_DOM,
          CASE
             WHEN s.provincia_dom < 200 THEN 100
             ELSE statiDom.stato_territorio
          END
             STATO_DOM,
          s.CAP_DOM,
          s.TEL_DOM,
          s.FAX_DOM,
          s.UTENTE UTENTE_AGG,
          s.DATA_AGG,
          s.COMPETENZA,
          s.COMPETENZA_ESCLUSIVA,
          s.TIPO_SOGGETTO,
          s.FLAG_TRG,
          s.STATO_CEE,
          s.PARTITA_IVA_CEE,
          s.FINE_VALIDITA,
          s.AL,
          s.DENOMINAZIONE,
          s.INDIRIZZO_WEB,
          s.NOTE,
          us.utente UTENTE
     FROM AS4_ANAGRAFE_SOGGETTI s,
          ad4_utenti_soggetti us,
          ad4_stati_territori statiNas,
          ad4_stati_territori statiRes,
          ad4_stati_territori statiDom
    WHERE     s.ni = us.soggetto(+)
          AND statiNas.stato_territorio(+) = s.PROVINCIA_NAS
          AND statiRes.stato_territorio(+) = s.PROVINCIA_RES
          AND statiDom.stato_territorio(+) = s.PROVINCIA_DOM
/




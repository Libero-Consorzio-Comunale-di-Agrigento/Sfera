--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_04

/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_COMUNI
(
   ID,
   COMUNE,
   STATO,
   PROVINCIA,
   DENOMINAZIONE,
   CAP,
   SIGLA_CFIS,
   DATA_SOPPRESSIONE
)
AS
   SELECT TO_NUMBER (c.provincia_stato || LPAD (c.comune, 4, 0)) id,
          c.comune,
          COALESCE (s.stato_territorio, 100) stato,
          CASE WHEN provincia_stato < 200 THEN provincia_stato ELSE NULL END
             provincia,
          c.denominazione,
          c.cap,
          c.sigla_cfis,
          c.data_soppressione
     FROM AD4_COMUNI c, ad4_stati_territori s
    WHERE s.stato_territorio(+) = c.provincia_stato
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_MODULI
(
   MODULO,
   DESCRIZIONE,
   PROGETTO,
   NOTE
)
AS
   SELECT modulo,
          descrizione,
          progetto,
          note
     FROM AD4_MODULI
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_PROGETTI
(
   PROGETTO,
   DESCRIZIONE,
   PRIORITA,
   NOTE
)
AS
   SELECT p.progetto,
          p.descrizione,
          p.priorita,
          p.note
     FROM AD4_PROGETTI p
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_PROVINCE
(
   PROVINCIA,
   DENOMINAZIONE,
   DENOMINAZIONE_AL1,
   DENOMINAZIONE_AL2,
   REGIONE,
   SIGLA,
   UTENTE_AGGIORNAMENTO,
   DATA_AGGIORNAMENTO
)
AS
   SELECT PROVINCIA,
          DENOMINAZIONE,
          DENOMINAZIONE_AL1,
          DENOMINAZIONE_AL2,
          REGIONE,
          SIGLA,
          UTENTE_AGGIORNAMENTO,
          DATA_AGGIORNAMENTO
     FROM AD4_PROVINCE
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
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


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_RUOLI
(
   RUOLO,
   DESCRIZIONE,
   MODULO,
   PROGETTO,
   RUOLO_APPLICATIVO
)
AS
   SELECT ruolo,
          descrizione,
          modulo,
          progetto,
          CAST (
             DECODE (gruppo_lavoro,
                     'S', DECODE (gruppo_so, 'S', 'Y', 'N'),
                     'N') AS CHAR (1))
             ruolo_applicativo
     FROM AD4_RUOLI
   UNION
   SELECT da.modulo || '_' || r.ruolo ruolo,
          r.descrizione,
          r.modulo,
          r.progetto,
          'N' ruolo_applicativo
     FROM ad4_diritti_accesso da, AD4_RUOLI r
    WHERE da.ruolo = r.ruolo
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
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


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_UTENTI
(
   UTENTE,
   NOMINATIVO,
   PASSWORD,
   ENABLED,
   ACCOUNT_EXPIRED,
   ACCOUNT_LOCKED,
   PASSWORD_EXPIRED,
   TIPO_UTENTE,
   NOMINATIVO_SOGGETTO,
   ESISTE_SOGGETTO
)
AS
   SELECT u.utente,
          u.nominativo,
          u.password,
          CAST (DECODE (u.stato, 'U', 'Y', 'N') AS CHAR (1)) enabled,
          CAST (DECODE (u.stato, 'U', 'N', 'Y') AS CHAR (1)) account_expired,
          CAST (DECODE (u.stato, 'U', 'N', 'Y') AS CHAR (1)) account_locked,
          CAST (DECODE (u.pwd_da_modificare, 'NO', 'N', 'Y') AS CHAR (1))
             password_expired,
          u.tipo_utente,
          AD4_SOGGETTO.GET_DENOMINAZIONE (
             AD4_UTENTE.GET_SOGGETTO (u.utente, 'N', 0))
             nominativo_soggetto,
          CAST (
             DECODE (AD4_UTENTE.GET_SOGGETTO (u.utente, 'N', 0),
                     NULL, 'N',
                     'Y') AS CHAR (1))
             esiste_soggetto
     FROM AD4_UTENTI u
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AD4_V_UTENTI_RUOLI
(
   UTENTE,
   RUOLO,
   ISTANZA
)
AS
   SELECT utente, modulo || '_' || ruolo ruolo, istanza
     FROM AD4_DIRITTI_ACCESSO
    WHERE istanza = 'AGSDE2'
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
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


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AS4_V_SOGGETTI_CORRENTI
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
          AND TRUNC (SYSDATE) BETWEEN s.dal
                                  AND NVL (s.al, TO_DATE ('3333333', 'j'))
          AND statiNas.stato_territorio(+) = s.PROVINCIA_NAS
          AND statiRes.stato_territorio(+) = s.PROVINCIA_RES
          AND statiDom.stato_territorio(+) = s.PROVINCIA_DOM
/


/* Formatted on 29/12/2014 10:59:57 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW AS4_V_TIPI_SOGGETTO
(
   TIPO_SOGGETTO,
   DESCRIZIONE,
   FLAG_TRG
)
AS
   SELECT TIPO_SOGGETTO, DESCRIZIONE, FLAG_TRG FROM AS4_TIPI_SOGGETTO
/

/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_AMMINISTRAZIONI
(
   CODICE,
   ENTE,
   DATA_ISTITUZIONE,
   DATA_SOPPRESSIONE,
   ID_SOGGETTO
)
AS
   SELECT codice_amministrazione AS codice,
          DECODE (ente, 'SI', 1, 0) AS ente,
          data_istituzione,
          data_soppressione,
          ni AS id_soggetto
     FROM SO4_AMMINISTRAZIONI
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_AOO
(
   PROGR_AOO,
   AMMINISTRAZIONE,
   CODICE,
   DESCRIZIONE,
   ABBREVIAZIONE,
   INDIRIZZO,
   CAP,
   PROVINCIA,
   COMUNE,
   TELEFONO,
   FAX,
   UTENTE_AGGIORNAMENTO,
   DATA_AGGIORNAMENTO,
   DAL,
   AL
)
AS
   SELECT progr_aoo,
          codice_amministrazione amministrazione,
          codice_aoo codice,
          descrizione,
          des_abb abbreviazione,
          indirizzo,
          cap,
          provincia,
          comune,
          telefono,
          fax,
          utente_aggiornamento,
          data_aggiornamento,
          dal,
          al
     FROM SO4_AOO
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_ATTR_COMPONENTE
(
   ID_ATTR_COMPONENTE,
   ID_COMPONENTE,
   DAL,
   AL,
   CODICE_INCARICO,
   DESCRIZIONE_INCARICO,
   SE_RESPONSABILE,
   ORDINAMENTO,
   TELEFONO,
   E_MAIL,
   FAX,
   PERCENTUALE_IMPIEGO,
   GRADAZIONE,
   TIPO_ASSEGNAZIONE,
   ASSEGNAZIONE_PREVALENTE
)
AS
   SELECT id_attr_componente,
          id_componente,
          dal,
          al,
          incarico codice_incarico,
          des_incarico descrizione_incarico,
          DECODE (responsabile,  'SI', 1,  'NO', 0,  NULL) se_Responsabile,
          ordinamento,
          telefono,
          e_mail,
          fax,
          percentuale_impiego,
          gradazione,
          tipo_assegnazione,
          assegnazione_prevalente_char assegnazione_prevalente
     FROM SO4_ATTRIBUTI_COMPONENTE
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_ATTR_COMPONENTE_PUBB
(
   ID_ATTR_COMPONENTE,
   ID_COMPONENTE,
   DAL,
   AL,
   CODICE_INCARICO,
   DESCRIZIONE_INCARICO,
   SE_RESPONSABILE,
   ORDINAMENTO,
   TELEFONO,
   E_MAIL,
   FAX,
   PERCENTUALE_IMPIEGO,
   GRADAZIONE,
   TIPO_ASSEGNAZIONE,
   ASSEGNAZIONE_PREVALENTE
)
AS
   SELECT id_attr_componente,
          id_componente,
          dal,
          al,
          incarico codice_incarico,
          des_incarico descrizione_incarico,
          DECODE (responsabile,  'SI', 1,  'NO', 0,  NULL) se_Responsabile,
          ordinamento,
          telefono,
          e_mail,
          fax,
          percentuale_impiego,
          gradazione,
          tipo_assegnazione,
          assegnazione_prevalente_char assegnazione_prevalente
     FROM SO4_ATTRIBUTI_COMPONENTE_PUBB
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_COMPONENTI
(
   ID_COMPONENTE,
   PROGR_UNITA,
   DAL,
   AL,
   ID_SOGGETTO,
   NOMINATIVO_SOGGETTO,
   CI_SOGGETTO_GP4,
   OTTICA,
   STATO
)
AS
   SELECT id_componente,
          progr_unita_organizzativa progr_unita,
          dal,
          al,
          ni id_soggetto,
          nominativo nominativo_soggetto,
          ci ci_soggetto_Gp4,
          ottica,
          stato
     FROM SO4_COMPONENTI
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_COMPONENTI_PUBB
(
   ID_COMPONENTE,
   ID_SOGGETTO,
   NOMINATIVO_SOGGETTO,
   CI_SOGGETTO_GP4,
   PROGR_UNITA,
   DAL,
   AL,
   OTTICA,
   STATO
)
AS
   SELECT id_componente,
          ni id_soggetto,
          nominativo nominativo_soggetto,
          ci ci_soggetto_Gp4,
          progr_unita_organizzativa progr_unita,
          dal,
          al,
          ottica,
          stato
     FROM SO4_COMPONENTI_PUBB
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_OTTICHE
(
   CODICE,
   AMMINISTRAZIONE,
   DESCRIZIONE,
   NOTE,
   ISTITUZIONALE,
   GESTIONE_REVISIONI
)
AS
   SELECT ottica codice,
          amministrazione,
          descrizione,
          nota note,
          DECODE (ottica_istituzionale, 'SI', 1, 0) istituzionale,
          DECODE (gestione_revisioni, 'SI', 1, 0) gestione_revisioni
     FROM SO4_OTTICHE
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_RUOLI_COMPONENTE
(
   ID_RUOLO_COMPONENTE,
   ID_COMPONENTE,
   RUOLO,
   DAL,
   AL
)
AS
   SELECT id_ruolo_componente,
          id_componente,
          ruolo,
          dal,
          al
     FROM SO4_RUOLI_COMPONENTE
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_RUOLI_COMPONENTE_PUBB
(
   ID_RUOLO_COMPONENTE,
   ID_COMPONENTE,
   RUOLO,
   DAL,
   AL
)
AS
   SELECT id_ruolo_componente,
          id_componente,
          ruolo,
          dal,
          al
     FROM SO4_RUOLI_COMPONENTE_PUBB
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_SUDDIVISIONI_STRUTTURA
(
   ID_SUDDIVISIONE,
   OTTICA,
   CODICE,
   DESCRIZIONE,
   ABBREVIAZIONE,
   ORDINAMENTO
)
AS
   SELECT id_suddivisione,
          ottica,
          suddivisione AS codice,
          descrizione,
          des_abb AS abbreviazione,
          ordinamento
     FROM SO4_SUDDIVISIONI_STRUTTURA
/


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
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


/* Formatted on 29/12/2014 18:09:28 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_UNITA_ORGANIZZATIVE_PUBB
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
     FROM SO4_UNITA_ORGANIZZATIVE_PUBB
/
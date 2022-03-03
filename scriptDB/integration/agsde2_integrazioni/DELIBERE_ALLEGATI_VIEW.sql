--liquibase formatted sql
--changeset rdestasio:install_20200221_integrazioni_06

-- QUESTA VISTA VIENE USATA DA AGSPR

CREATE OR REPLACE FORCE VIEW DELIBERE_ALLEGATI_VIEW
(
   POSIZIONE,
   ID_DOCUMENTO,
   ID_DOCUMENTO_ESTERNO,
   TIPO_DOCUMENTO,
   ID_FILE_ESTERNO,
   ID_FILE_ALLEGATO,
   ANNO_DELIBERA,
   NUMERO_DELIBERA,
   DATA_NUMERO_DELIBERA,
   OGGETTO,
   CODICE_REGISTRO,
   REGISTRO,
   NOME,
   CODICE_ESITO,
   ESITO,
   ENTE,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE
)
AS
   SELECT 1 posizione,
          d.id_delibera id_documento,
          d.id_documento_esterno id_documento_etserno,
          CAST ('DELIBERA' AS VARCHAR2 (255)) tipo_documento,
          id_file_esterno,
          id_file_allegato,
          anno_delibera,
          numero_delibera,
          d.data_numero_delibera data_delibera,
          d.oggetto,
          registro_delibera codice_registro,
          r.descrizione registro,
          nome,
          s.id_esito codice_esito,
          es.titolo esito,
          d.ente AS ENTE,
          c.utente AS COMP_UTENTE,
          c.unita_progr AS COMP_UNITA_PROGR,
          c.unita_dal AS COMP_UNITA_DAL,
          c.unita_ottica AS COMP_UNITA_OTTICA,
          c.ruolo AS COMP_RUOLO,
          c.lettura AS COMP_LETTURA,
          c.modifica AS COMP_MODIFICA,
          c.cancellazione AS COMP_CANCELLAZIONE
     FROM delibere d,
          delibere_competenze c,
          tipi_registro r,
          file_allegati fa,
          proposte_delibera pd,
          odg_oggetti_seduta s,
          odg_esiti es
    WHERE     d.id_delibera = c.id_delibera
          AND d.id_proposta_delibera = PD.ID_PROPOSTA_DELIBERA
          AND PD.ID_OGGETTO_SEDUTA = s.id_oggetto_seduta
          AND s.id_esito = es.id_esito
          AND fa.id_file_allegato = d.id_file_allegato_testo
          AND r.tipo_registro(+) = d.registro_delibera
   UNION ALL
   SELECT 2 posizione,
          a.id_allegato id_documento,
          a.id_documento_esterno id_documento_etserno,
          CAST ('ALLEGATO' AS VARCHAR2 (255)) tipo_documento,
          id_file_esterno,
          id_file_allegato,
          anno_delibera,
          d.numero_delibera,
          d.data_numero_delibera data_delibera,
          d.oggetto,
          registro_delibera codice_registro,
          r.descrizione registro,
          nome,
          s.id_esito codice_esito,
          es.titolo esito,
          d.ente AS ENTE,
          c.utente AS COMP_UTENTE,
          c.unita_progr AS COMP_UNITA_PROGR,
          c.unita_dal AS COMP_UNITA_DAL,
          c.unita_ottica AS COMP_UNITA_OTTICA,
          c.ruolo AS COMP_RUOLO,
          c.lettura AS COMP_LETTURA,
          c.modifica AS COMP_MODIFICA,
          c.cancellazione AS COMP_CANCELLAZIONE
     FROM delibere d,
          delibere_competenze c,
          tipi_registro r,
          allegati a,
          allegati_file af,
          file_allegati fa,
          proposte_delibera pd,
          odg_oggetti_seduta s,
          odg_esiti es
    WHERE     a.id_delibera = d.id_delibera
          AND d.id_delibera = c.id_delibera
          AND af.id_allegato = a.id_allegato
          AND fa.id_file_allegato = af.id_file
          AND d.id_proposta_delibera = PD.ID_PROPOSTA_DELIBERA
          AND PD.ID_OGGETTO_SEDUTA = s.id_oggetto_seduta
          AND s.id_esito = es.id_esito
          AND r.tipo_registro(+) = d.registro_delibera
   ORDER BY id_documento, posizione
/
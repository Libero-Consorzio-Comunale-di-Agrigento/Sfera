--liquibase formatted sql
--changeset rdestasio:install_20200221_integrazioni_07

-- QUESTA VISTA VIENE USATA DA AGSPR

CREATE OR REPLACE FORCE VIEW DETERMINE_ALLEGATI_VIEW
(
   POSIZIONE,
   ID_DOCUMENTO,
   ID_DOCUMENTO_ESTERNO,
   TIPO_DOCUMENTO,
   ID_FILE_ESTERNO,
   ID_FILE_ALLEGATO,
   ANNO_DETERMINA,
   NUMERO_DETERMINA,
   DATA_NUMERO_DETERMINA,
   OGGETTO,
   CODICE_REGISTRO,
   REGISTRO,
   NOME,
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
          d.id_determina id_documento,
          d.id_documento_esterno id_documento_etserno,
          CAST ('DETERMINA' AS VARCHAR2 (255)) tipo_documento,
          id_file_esterno,
          id_file_allegato,
          anno_determina,
          numero_determina,
          d.data_numero_determina data_determina,
          oggetto,
          registro_determina codice_registro,
          r.descrizione registro,
          nome,
          d.ente AS ENTE,
          c.utente AS COMP_UTENTE,
          c.unita_progr AS COMP_UNITA_PROGR,
          c.unita_dal AS COMP_UNITA_DAL,
          c.unita_ottica AS COMP_UNITA_OTTICA,
          c.ruolo AS COMP_RUOLO,
          c.lettura AS COMP_LETTURA,
          c.modifica AS COMP_MODIFICA,
          c.cancellazione AS COMP_CANCELLAZIONE
     FROM determine d,
          determine_competenze c,
          tipi_registro r,
          file_allegati fa
    WHERE     d.id_determina = c.id_determina
          AND fa.id_file_allegato = d.id_file_allegato_testo
          AND r.tipo_registro(+) = d.registro_determina
   UNION ALL
   SELECT 2 posizione,
          a.id_allegato id_documento,
          a.id_documento_esterno id_documento_etserno,
          CAST ('ALLEGATO' AS VARCHAR2 (255)) tipo_documento,
          id_file_esterno,
          id_file_allegato,
          anno_determina,
          d.numero_determina,
          d.data_numero_determina data_detrmina,
          oggetto,
          registro_determina codice_registro,
          r.descrizione registro,
          nome,
          d.ente AS ENTE,
          c.utente AS COMP_UTENTE,
          c.unita_progr AS COMP_UNITA_PROGR,
          c.unita_dal AS COMP_UNITA_DAL,
          c.unita_ottica AS COMP_UNITA_OTTICA,
          c.ruolo AS COMP_RUOLO,
          c.lettura AS COMP_LETTURA,
          c.modifica AS COMP_MODIFICA,
          c.cancellazione AS COMP_CANCELLAZIONE
     FROM determine d,
          determine_competenze c,
          tipi_registro r,
          allegati a,
          allegati_file af,
          file_allegati fa
    WHERE     a.id_determina = d.id_determina
          AND d.id_determina = c.id_determina
          AND af.id_allegato = a.id_allegato
          AND fa.id_file_allegato = af.id_file
          AND r.tipo_registro(+) = d.registro_determina
   ORDER BY id_documento, posizione
/
--liquibase formatted sql
--changeset rdestasio:install_20210221_integrazioni_03 runOnChange:true

CREATE OR REPLACE FORCE VIEW CONS_DETERMINE
(
   ID_DETERMINA,
   ID_DOCUMENTO_ESTERNO,
   FILENAME,
   ID_FILE_GDM,
   ANNO_DETERMINA,
   DATA_NUMERO_DETERMINA,
   NUMERO_DETERMINA,
   REGISTRO_DETERMINA,
   DESCRIZIONE_REGISTRO_DETERMINA,
   ANNO_PROPOSTA,
   DATA_NUMERO_PROPOSTA,
   NUMERO_PROPOSTA,
   REGISTRO_PROPOSTA,
   ANNO_PROTOCOLLO,
   DATA_NUMERO_PROTOCOLLO,
   NUMERO_PROTOCOLLO,
   REGISTRO_PROTOCOLLO,
   DATA_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   DATA_ESECUTIVITA,
   OGGETTO,
   RISERVATO,
   NUM_ALLEGATI_RISERVATI,
   TIPO_DETERMINA,
   UNITA_PROPONENTE,
   REDATTORE,
   FUNZIONARIO,
   UNITA_DIRIGENTE,
   DESCRIZIONE_AREA,
   DESCRIZIONE_SERVIZIO,
   CLASSIFICA_CODICE,
   CLASSIFICA_DAL,
   CLASSIFICA_DESCRIZIONE,
   FASCICOLO_NUMERO,
   FASCICOLO_ANNO,
   FASCICOLO_OGGETTO,
   STATO,
   FIRMATARIO,
   DATA_FIRMA,
   IMPEGNO_SPESA,
   FIRMATARIO_VISTO_CONTABILE,
   DATA_FIRMA_VISTO_CONTABILE,
   UNITA_VISTO_CONTABILE,
   ESITO_VISTO_CONTABILE,
   NUMERO_ALBO,
   ANNO_ALBO,
   SOGGETTI_NOTIFICA,
   ENTE,
   CIG,
   CODICE_AOO,
   DESCRIZIONE_AOO,
   DENOMINAZIONE_AMM,
   NUMERO_ALLEGATI,
   INDICE_ALLEGATI,
   COGNOME_FIRMATARIO,
   NOME_FIRMATARIO,
   COGNOME_FUNZIONARIO,
   NOME_FUNZIONARIO
)
AS
SELECT d.id_determina,
       d.id_documento_esterno,
       fa.nome filename,
       fa.id_file_esterno,
       d.anno_determina,
       d.data_numero_determina,
       d.numero_determina,
       d.registro_determina,
       tr.descrizione,
       d.anno_proposta,
       d.data_numero_proposta,
       d.numero_proposta,
       d.registro_proposta,
       d.anno_protocollo,
       d.data_numero_protocollo,
       d.numero_protocollo,
       DECODE (d.numero_protocollo,
               NULL, NULL,
               NVL (d.registro_protocollo, 'PROT')),
       d.data_pubblicazione,
       d.data_fine_pubblicazione,
       d.data_esecutivita,
       d.oggetto,
       d.riservato,
       (SELECT COUNT (1)
        FROM allegati alle
        WHERE     alle.id_determina = d.id_determina
          AND alle.valido = 'Y'
          AND alle.riservato = 'Y')
               num_allegati_riservati,
       td.titolo tipo_determina,
       utility_pkg.get_uo_descrizione (uo_prop.unita_progr,
                                       uo_prop.unita_dal)
           unita_proponente,
       as4_soggetti_redattore.cognome
           || ' '
           || as4_soggetti_redattore.nome
           redattore,
       DECODE (
               d.controllo_funzionario,
               'Y',    as4_soggetti_funzionario.cognome
                   || ' '
                   || as4_soggetti_funzionario.nome,
               '')
           funzionario,
       utility_pkg.get_uo_descrizione (dirigente.unita_progr,
                                       dirigente.unita_dal)
           unita_dirigente,
       utility_pkg.get_suddivisione_descrizione (uo_prop.unita_progr,
                                                 uo_prop.unita_dal,
                                                 'SO4_SUDDIVISIONE_AREA',
                                                 d.ente)
           descrizione_area,
       utility_pkg.get_suddivisione_descrizione (
               uo_prop.unita_progr,
               uo_prop.unita_dal,
               'SO4_SUDDIVISIONE_SERVIZIO',
               d.ente)
           descrizione_servizio,
       d.classifica_codice,
       d.classifica_dal,
       d.classifica_descrizione,
       d.fascicolo_numero,
       d.fascicolo_anno,
       d.fascicolo_oggetto,
       d.stato,
       utility_pkg.get_primo_firmatario_determina (d.id_determina)
           firmatario,
       utility_pkg.get_prima_data_firma_determina (d.id_determina)
           data_firma,
       (SELECT DECODE (COUNT (1), 0, 'N', 'Y')
        FROM visti_pareri vp, tipi_visto_parere tvp
        WHERE     vp.id_determina = d.id_determina
          AND vp.id_tipologia = tvp.id_tipo_visto_parere
          AND vp.valido = 'Y'
          AND tvp.contabile = 'Y')
           impegno_spesa,
       utility_pkg.get_firmatario_visto_contabile (d.id_determina)
           firmatario_visto_contabile,
       utility_pkg.get_data_firma_visto_contabile (d.id_determina)
           data_firma_visto_contabile,
       utility_pkg.get_unita_visto_contabile (d.id_determina)
           unita_visto_contabile,
       utility_pkg.get_esito_visto_contabile (d.id_determina)
           esito_visto_contabile,
       numero_albo,
       anno_albo,
       utility_pkg.get_sogg_notifica_determina (d.id_determina)
           soggetti_notifica,
       d.ente,
       d.cig,
       utility_pkg.cons_get_codice_aoo (d.ente),
       utility_pkg.cons_get_descrizione_aoo (d.ente),
       utility_pkg.cons_get_denominazione_amm (d.ente),
       (SELECT COUNT (1)
        FROM allegati alle
        WHERE alle.id_determina = d.id_determina AND alle.valido = 'Y')
           num_allegati,
       utility_pkg.cons_indice_allegati_determina (d.id_determina),
       utility_pkg.get_firmatario_visto_contab_c (d.id_determina)
           cognome_firmatario,
       utility_pkg.get_firmatario_visto_contab_n (d.id_determina)
           nome_firmatario,
       DECODE (d.controllo_funzionario,
               'Y', as4_soggetti_funzionario.cognome,
               '')
           cognome_funzionario,
       DECODE (d.controllo_funzionario,
               'Y', as4_soggetti_funzionario.nome,
               '')
           nome_funzionario
FROM determine_soggetti uo_prop,
     determine_soggetti redattore,
     determine_soggetti funzionario,
     determine_soggetti dirigente,
     as4_v_soggetti_correnti as4_soggetti_redattore,
     as4_v_soggetti_correnti as4_soggetti_funzionario,
     file_allegati fa,
     determine d,
     tipi_determina td,
     tipi_registro tr
WHERE     d.id_tipo_determina = td.id_tipo_determina
  AND d.id_file_allegato_testo = fa.id_file_allegato
  AND uo_prop.id_determina = d.id_determina
  AND uo_prop.tipo_soggetto = 'UO_PROPONENTE'
  AND redattore.id_determina(+) = d.id_determina
  AND redattore.tipo_soggetto(+) = 'REDATTORE'
  AND redattore.utente = as4_soggetti_redattore.utente(+)
  AND funzionario.id_determina(+) = d.id_determina
  AND funzionario.tipo_soggetto(+) = 'FUNZIONARIO'
  AND funzionario.utente = as4_soggetti_funzionario.utente(+)
  AND dirigente.id_determina(+) = d.id_determina
  AND dirigente.tipo_soggetto(+) = 'DIRIGENTE'
  AND d.valido = 'Y'
  AND d.numero_determina IS NOT NULL
  AND d.registro_determina = tr.tipo_registro
/
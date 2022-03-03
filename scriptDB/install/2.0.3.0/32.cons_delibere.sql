--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_32

CREATE OR REPLACE FORCE VIEW CONS_DELIBERE
(
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ID_DOCUMENTO_ESTERNO,
   FILENAME,
   ID_FILE_GDM,
   ANNO_DELIBERA,
   DATA_NUMERO_DELIBERA,
   NUMERO_DELIBERA,
   REGISTRO_DELIBERA,
   DESCRIZIONE_REGISTRO_DELIBERA,
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
   TIPOLOGIA,
   OGGETTO,
   RISERVATO,
   NUM_ALLEGATI_RISERVATI,
   TIPO_DELIBERA,
   UNITA_PROPONENTE,
   REDATTORE,
   FUNZIONARIO,
   DIRIGENTE,
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
   PRESIDENTE,
   DATA_FIRMA_PRESIDENTE,
   SEGRETARIO,
   DATA_FIRMA_SEGRETARIO,
   IMPEGNO_SPESA,
   FIRMATARIO_PARERE_CONTABILE,
   DATA_FIRMA_PARERE_CONTABILE,
   UNITA_PARERE_CONTABILE,
   ESITO_PARERE_CONTABILE,
   NUMERO_ALBO,
   ANNO_ALBO,
   SOGGETTI_NOTIFICA,
   ENTE
)
AS
   SELECT d.id_delibera,
          pd.id_proposta_delibera,
          d.id_documento_esterno,
          fa.nome filename,
          fa.id_file_esterno,
          d.anno_delibera,
          d.data_numero_delibera,
          d.numero_delibera,
          d.registro_delibera,
          tr.descrizione,
          pd.anno_proposta,
          pd.data_numero_proposta,
          pd.numero_proposta,
          pd.registro_proposta,
          d.anno_protocollo,
          d.data_numero_protocollo,
          d.numero_protocollo,
          DECODE (d.numero_protocollo,
                  NULL, NULL,
                  NVL (d.registro_protocollo, 'PROT')),
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          d.data_esecutivita,
          td.titolo,
          d.oggetto,
          d.riservato,
          (SELECT COUNT (1)
             FROM allegati alle
            WHERE     (   alle.id_delibera = d.id_delibera
                       OR alle.id_proposta_delibera = d.id_proposta_delibera)
                  AND alle.valido = 'Y'
                  AND alle.riservato = 'Y')
             num_allegati_riservati,
          td.titolo tipo_delibera,
          utility_pkg.get_uo_descrizione (uo_prop.unita_progr,
                                          uo_prop.unita_dal)
             unita_proponente,
          utility_pkg.get_cognome_nome (
             utility_pkg.get_ni_soggetto (redattore.utente))
             redattore,
          DECODE (
             pd.controllo_funzionario,
             'Y', utility_pkg.get_cognome_nome (
                     utility_pkg.get_ni_soggetto (funzionario.utente)),
             '')
             funzionario,
          utility_pkg.get_cognome_nome (
             utility_pkg.get_ni_soggetto (dirigente.utente))
             dirigente,
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
          pd.classifica_codice,
          pd.classifica_dal,
          pd.classifica_descrizione,
          pd.fascicolo_numero,
          pd.fascicolo_anno,
          pd.fascicolo_oggetto,
          d.stato,
          utility_pkg.get_firmatario_delibera (d.id_delibera, 'PRESIDENTE'),
          utility_pkg.get_data_firma_delibera (d.id_delibera, 'PRESIDENTE'),
          utility_pkg.get_firmatario_delibera (d.id_delibera, 'SEGRETARIO'),
          utility_pkg.get_data_firma_delibera (d.id_delibera, 'SEGRETARIO'),
          (SELECT DECODE (COUNT (1), 0, 'N', 'Y')
             FROM visti_pareri vp, tipi_visto_parere tvp
            WHERE     (   vp.id_delibera = d.id_delibera
                       OR vp.id_proposta_delibera = d.id_proposta_delibera)
                  AND vp.id_tipologia = tvp.id_tipo_visto_parere
                  AND vp.valido = 'Y'
                  AND tvp.contabile = 'Y')
             impegno_spesa,
          utility_pkg.get_firmatario_par_contabile (d.id_delibera)
             firmatario_parere_contabile,
          utility_pkg.get_data_firma_par_contabile (d.id_delibera)
             data_firma_parere_contabile,
          utility_pkg.get_unita_parere_contabile (d.id_delibera)
             unita_parere_contabile,
          utility_pkg.get_esito_parere_contabile (d.id_delibera)
             esito_parere_contabile,
          numero_albo,
          anno_albo,
          utility_pkg.get_sogg_notifica_delibera (d.id_delibera)
             soggetti_notifica,
          d.ente
     FROM proposte_delibera_soggetti uo_prop,
          proposte_delibera_soggetti redattore,
          proposte_delibera_soggetti funzionario,
          proposte_delibera_soggetti dirigente,
          file_allegati fa,
          proposte_delibera pd,
          delibere d,
          tipi_delibera td,
          tipi_registro tr
    WHERE     pd.id_tipo_delibera = td.id_tipo_delibera
          AND d.id_file_allegato_testo = fa.id_file_allegato
          AND d.id_proposta_delibera = pd.id_proposta_delibera
          AND uo_prop.id_proposta_delibera = d.id_proposta_delibera
          AND uo_prop.tipo_soggetto = 'UO_PROPONENTE'
          AND redattore.id_proposta_delibera(+) = d.id_proposta_delibera
          AND redattore.tipo_soggetto(+) = 'REDATTORE'
          AND funzionario.id_proposta_delibera(+) = d.id_proposta_delibera
          AND funzionario.tipo_soggetto(+) = 'FUNZIONARIO'
          AND dirigente.id_proposta_delibera(+) = d.id_proposta_delibera
          AND dirigente.tipo_soggetto(+) = 'DIRIGENTE'
          AND d.valido = 'Y'
          AND d.registro_delibera = tr.tipo_registro
/

GRANT SELECT ON CONS_DELIBERE TO ${global.db.gdm.username}
/
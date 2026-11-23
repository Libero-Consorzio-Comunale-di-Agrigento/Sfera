--liquibase formatted sql
--changeset mfrancesconi:RICERCA_DETERMINA runOnChange:true

create or replace force view RICERCA_DETERMINA
(
   ID_DOCUMENTO,
   TIPO_DOCUMENTO,
   ID_DOCUMENTO_PRINCIPALE,
   TIPO_DOCUMENTO_PRINCIPALE,
   ENTE,
   OGGETTO,
   RISERVATO,
   CONTROLLO_FUNZIONARIO,
   STATO,
   ID_CATEGORIA,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   UO_PROPONENTE_CODICE,
   UO_SERVIZIO_PROGR,
   UO_SERVIZIO_DAL,
   UO_SERVIZIO_OTTICA,
   UO_SERVIZIO_DESCRIZIONE,
   UO_SERVIZIO_CODICE,
   UTENTE_SOGGETTO,
   TIPO_SOGGETTO,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   NUMERO_PASSAGGI,
   ULTIMO_ATTORE_FLUSSO,
   NUMERO_ATTO,
   ANNO_ATTO,
   REGISTRO_ATTO,
   DATA_ATTO,
   DATA_ADOZIONE,
   DATA_ESECUTIVITA,
   DATA_SCADENZA,
   NUMERO_ATTO_2,
   ANNO_ATTO_2,
   REGISTRO_ATTO_2,
   DATA_NUMERO_ATTO_2,
   NUMERO_PROPOSTA,
   ANNO_PROPOSTA,
   REGISTRO_PROPOSTA,
   DATA_PROPOSTA,
   NUMERO_PROTOCOLLO,
   ANNO_PROTOCOLLO,
   REGISTRO_PROTOCOLLO,
   CLASSIFICA_CODICE,
   CLASSIFICA_DAL,
   CLASSIFICA_DESCRIZIONE,
   FASCICOLO_ANNO,
   FASCICOLO_NUMERO,
   FASCICOLO_OGGETTO,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   CODICE_ESTERNO_TIPOLOGIA,
   TIPO_PUBBLICAZIONE_ALBO,
   CON_IMPEGNO_SPESA,
   NUMERO_ALBO,
   ANNO_ALBO,
   DATA_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   LOG_CONSERVAZIONE,
   DATA_CONSERVAZIONE,
   STATO_CONSERVAZIONE,
   DA_INVIARE_CORTE_CONTI,
   DATA_INVIO_CORTE_CONTI,
   ID_TIPO_ALLEGATO_DETERMINA,
   ID_TIPO_ALLEGATO_VISTO,
   ID_TESTO,
   ID_FILE_ALLEGATO,
   ID_OGGETTO_RICORRENTE,
   STATO_MARCATURA,
   STATO_FIRMA,
   DATA_ORDINAMENTO,
   ATTO_CONCLUSO,
   CUP,
   CIG,
   DATA_CONTROLLO_GESTIONE,
   ID_IMPATTO_GENERAZIONALE,
   CONTO_ECONOMICO,
   CODICE_PROGETTO,
   ID_TIPO_BUDGET,
   IMPORTO,
   IMPORTO_LIQUIDAZIONE,
   ELENCO_FIRMATARI,
   IN_EVIDENZA,
   DATA_IN_EVIDENZA_DA,
   DATA_IN_EVIDENZA_A,
   NOTE,
   IMPORTO_TOTALE
)
as
select d.id_determina as ID_DOCUMENTO,
       cast('DETERMINA' as varchar(255)) as TIPO_DOCUMENTO,
       d.id_determina as ID_DOCUMENTO_PRINCIPALE,
       cast('DETERMINA' as varchar(255)) as TIPO_DOCUMENTO_PRINCIPALE,
       d.ente as ENTE,
       d.oggetto as OGGETTO,
       d.riservato as RISERVATO,
       d.controllo_funzionario as CONTROLLO_FUNZIONARIO,
       upper(decode(d.stato,
                    'NON_ESECUTIVO',
                    'NON_ESECUTIVO',
                    'ANNULLATO',
                    d.stato,
                    nvl(sc.titolo,
                        d.stato))) as STATO,
       d.id_categoria as ID_CATEGORIA,
       pds.unita_progr as UO_PROPONENTE_PROGR,
       pds.unita_dal as UO_PROPONENTE_DAL,
       pds.unita_ottica as UO_PROPONENTE_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = uo_prop.unita_progr
           and ottica = uo_prop.unita_ottica
           and dal = uo_prop.unita_dal) as UO_PROPONENTE_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = uo_prop.unita_progr
           and ottica = uo_prop.unita_ottica
           and dal = uo_prop.unita_dal) as UO_PROPONENTE_CODICE,
       uo_serv.unita_progr as UO_SERVIZIO_PROGR,
       uo_serv.unita_dal as UO_SERVIZIO_DAL,
       uo_serv.unita_ottica as UO_SERVIZIO_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = uo_serv.unita_progr
           and ottica = uo_serv.unita_ottica
           and dal = uo_serv.unita_dal) as UO_SERVIZIO_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = uo_serv.unita_progr
           and ottica = uo_serv.unita_ottica
           and dal = uo_serv.unita_dal) as UO_SERVIZIO_CODICE,
       pds.utente as UTENTE_SOGGETTO,
       pds.tipo_soggetto as TIPO_SOGGETTO,
       c.id_determina_competenza as ID_COMPETENZA,
       c.utente as COMP_UTENTE,
       c.unita_progr as COMP_UNITA_PROGR,
       c.unita_dal as COMP_UNITA_DAL,
       c.unita_ottica as COMP_UNITA_OTTICA,
       c.ruolo as COMP_RUOLO,
       c.lettura as COMP_LETTURA,
       c.modifica as COMP_MODIFICA,
       c.cancellazione as COMP_CANCELLAZIONE,
       s.id_engine_step as ID_STEP,
       sa.utente as STEP_UTENTE,
       sa.unita_progr as STEP_UNITA_PROGR,
       sa.unita_dal as STEP_UNITA_DAL,
       sa.unita_ottica as STEP_UNITA_OTTICA,
       sa.ruolo as STEP_RUOLO,
       sc.titolo as STEP_NOME,
       sc.descrizione as STEP_DESCRIZIONE,
       case when s.id_cfg_step is not null then
         (select count(*) from wkf_engine_step s2
           where s2.id_engine_iter = i.id_engine_iter
             and s2.id_cfg_step = s.id_cfg_step
             and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', d.ente) = 'Y')
       end as NUMERO_PASSAGGI,
       decode(impostazioni_pkg.get_impostazione('EXCEL_ULTIMO_ATTORE_FLUSSO', d.ente), 'Y', utility_pkg.get_nominativo_utente_ad4(i.utente_upd, i.ente), null) as ULTIMO_ATTORE_FLUSSO,
       d.numero_determina as NUMERO_ATTO,
       d.anno_determina as ANNO_ATTO,
       d.registro_determina as REGISTRO_ATTO,
       trunc(d.data_numero_determina) as DATA_ATTO,
       trunc(d.data_numero_determina) as DATA_ADOZIONE,
       trunc(d.data_esecutivita) as DATA_ESECUTIVITA,
       trunc(d.data_scadenza) as DATA_SCADENZA,
       d.numero_determina_2 as NUMERO_ATTO_2,
       d.anno_determina_2 as ANNO_ATTO_2,
       d.registro_determina_2 as REGISTRO_ATTO_2,
       d.data_numero_determina_2 as DATA_NUMERO_ATTO_2,
       d.numero_proposta as NUMERO_PROPOSTA,
       d.anno_proposta as ANNO_PROPOSTA,
       d.registro_proposta as REGISTRO_PROPOSTA,
       trunc(d.data_proposta) as DATA_PROPOSTA,
       d.numero_protocollo as NUMERO_PROTOCOLLO,
       d.anno_protocollo as ANNO_PROTOCOLLO,
       d.registro_protocollo as REGISTRO_PROTOCOLLO,
       d.classifica_codice as CLASSIFICA_CODICE,
       d.classifica_dal as CLASSIFICA_DAL,
       d.classifica_descrizione as CLASSIFICA_DESCRIZIONE,
       d.fascicolo_anno as FASCICOLO_ANNO,
       d.fascicolo_numero as FASCICOLO_NUMERO,
       d.fascicolo_oggetto as FASCICOLO_OGGETTO,
       d.id_tipo_determina as ID_TIPOLOGIA,
       td.titolo as TITOLO_TIPOLOGIA,
       td.descrizione as DESCRIZIONE_TIPOLOGIA,
       td.codice_esterno as CODICE_ESTERNO_TIPOLOGIA,
       td.tipo_pubblicazione_albo as TIPO_PUBBLICAZIONE_ALBO,
       tvp.contabile as CON_IMPEGNO_SPESA,
       d.numero_albo as NUMERO_ALBO,
       d.anno_albo as ANNO_ALBO,
       trunc(d.data_pubblicazione) as DATA_PUBBLICAZIONE,
       trunc(d.data_fine_pubblicazione) as DATA_FINE_PUBBLICAZIONE,
       (select log
          from JCONS_V_LOG_CONSERVAZIONE j
         where j.id_documento_rif = d.id_documento_esterno
           and not exists
         (select 1
                  from JCONS_V_LOG_CONSERVAZIONE jj
                 where jj.id_documento_rif = j.id_documento_rif
                   and jj.id_documento > j.id_documento)) as LOG_CONSERVAZIONE,
       jcons_pkg.get_data_conservazione(d.id_documento_esterno) as DATA_CONSERVAZIONE,
       d.stato_conservazione as STATO_CONSERVAZIONE,
       d.da_inviare_corte_conti as DA_INVIARE_CORTE_CONTI,
       trunc(d.data_invio_corte_conti) as DATA_INVIO_CORTE_CONTI,
       alle.id_tipo_allegato as ID_TIPO_ALLEGATO_DETERMINA,
       alle_vp.id_tipo_allegato as ID_TIPO_ALLEGATO_VISTO,
       d.id_file_allegato_testo as ID_TESTO,
       af.id_file as ID_FILE_ALLEGATO,
       d.id_oggetto_ricorrente ID_OGGETTO_RICORRENTE,
       d.stato_marcatura STATO_MARCATURA,
       d.stato_firma STATO_FIRMA,
       d.data_ordinamento as DATA_ORDINAMENTO,
       cast(utility_pkg.get_determina_conclusa(d.id_determina) as char(1)) ATTO_CONCLUSO,
       (select valore
          from dati_aggiuntivi da
         where da.codice = 'CUP'
           and da.id_determina = d.id_determina) CUP,
       d.cig CIG,
       (select to_date(valore,'dd/mm/yyyy')
        from dati_aggiuntivi da
        where da.codice = 'DATA_CONTROLLO_GESTIONE'
          and da.id_determina = d.id_determina) DATA_CONTROLLO_GESTIONE,
       d.id_impatto_Generazionale ID_IMPATTO_GENERAZIONALE,
       b.conto_economico CONTO_ECONOMICO,
       b.codice_progetto CODICE_PROGETTO,
       b.id_tipo_budget ID_TIPO_BUDGET,
       b.importo IMPORTO,
       (select importo
          from distinte_atti
         where id_proposta = d.id_determina
           and rownum = 1
           and impostazioni_pkg.get_impostazione('CONTABILITA_CFA_EXCEL_IMPORTO',
                                                 d.ente) = 'Y') importo_liquidazione,
       utility_pkg.get_elenco_firmatari_documento(d.id_determina,
                                                  d.ente,
                                                  'DETERMINA'),
       d.in_evidenza IN_EVIDENZA,
       d.data_in_evidenza_da DATA_IN_EVIDENZA_DA,
       d.data_in_evidenza_a DATA_IN_EVIDENZA_A,
       d.NOTE NOTE,
       d.importo_totale IMPORTO_TOTALE
  from determine_competenze   c,
       determine_soggetti     pds,
       determine_soggetti     uo_prop,
       determine_soggetti     uo_serv,
       wkf_cfg_step           sc,
       wkf_engine_step_attori sa,
       wkf_engine_step        s,
       wkf_engine_iter        i,
       tipi_visto_parere      tvp,
       allegati               alle_vp,
       visti_pareri           vp,
       allegati               alle,
       tipi_determina         td,
       determine              d,
       categorie              cat,
       allegati_file          af,
       budget                 b
 where tvp.id_tipo_visto_parere(+) = vp.id_tipologia
   and c.id_determina = d.id_determina
   and cat.id_categoria(+) = d.id_categoria
   and i.id_engine_iter(+) = d.id_engine_iter
   and s.id_engine_step(+) = i.id_step_corrente
   and sa.id_engine_step(+) = s.id_engine_step
   and sc.id_cfg_step(+) = s.id_cfg_step
   and alle_vp.id_visto_parere(+) = vp.id_visto_parere
   and alle.id_determina(+) = d.id_determina
   and vp.id_determina(+) = d.id_determina
   and vp.valido(+) = 'Y'
   and uo_prop.id_determina = d.id_determina
   and uo_prop.tipo_soggetto in ('UO_PROPONENTE')
   and uo_serv.id_determina(+) = d.id_determina
   and uo_serv.tipo_soggetto(+) in ('UO_SERVIZIO')
   and pds.id_determina = d.id_determina
   and td.id_tipo_determina = d.id_tipo_determina
   and af.id_allegato(+) = alle.id_allegato
   and d.valido = 'Y'
   and b.id_determina(+) = d.id_determina
/
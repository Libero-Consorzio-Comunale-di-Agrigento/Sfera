--liquibase formatted sql
--changeset mfrancesconi:RICERCA_DELIBERA runOnChange:true

create or replace force view RICERCA_DELIBERA
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
   ID_COMMISSIONE,
   ID_OGGETTO_SEDUTA,
   ID_TIPO_ALLEGATO_DELIBERA,
   ID_TIPO_ALLEGATO_PARERE,
   ID_TESTO,
   ID_FILE_ALLEGATO,
   ID_OGGETTO_RICORRENTE,
   STATO_MARCATURA,
   STATO_FIRMA,
   DATA_ORDINAMENTO,
   NI_RELATORE,
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
select prop.id_proposta_delibera as ID_DOCUMENTO,
       decode(deli.id_delibera,
              null,
              'PROPOSTA_DELIBERA',
              'DELIBERA') as TIPO_DOCUMENTO,
       deli.id_delibera as ID_DOCUMENTO_PRINCIPALE,
       cast('DELIBERA' as varchar2(255)) as TIPO_DOCUMENTO_PRINCIPALE,
       prop.ente as ENTE,
       nvl(deli.oggetto,
           prop.oggetto) as OGGETTO,
       cast(nvl(deli.riservato,
                prop.riservato) as char(1)) as RISERVATO,
       prop.controllo_funzionario as CONTROLLO_FUNZIONARIO,
       upper(decode(nvl(deli.stato,
                        prop.stato),
                    'ANNULLATO',
                    nvl(deli.stato,
                        prop.stato),
                    nvl(nvl(de_cfg_step.titolo,
                            pr_cfg_step.titolo),
                        nvl(deli.stato,
                            prop.stato)))) as STATO,
       pr_cat.id_categoria as ID_CATEGORIA,
       pr_sog.unita_progr as UO_PROPONENTE_PROGR,
       pr_sog.unita_dal as UO_PROPONENTE_DAL,
       pr_sog.unita_ottica as UO_PROPONENTE_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_prop.unita_progr
           and ottica = pr_uo_prop.unita_ottica
           and dal = pr_uo_prop.unita_dal) as UO_PROPONENTE_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_prop.unita_progr
           and ottica = pr_uo_prop.unita_ottica
           and dal = pr_uo_prop.unita_dal) as UO_PROPONENTE_CODICE,
       pr_uo_serv.unita_progr as UO_SERVIZIO_PROGR,
       pr_uo_serv.unita_dal as UO_SERVIZIO_DAL,
       pr_uo_serv.unita_ottica as UO_SERVIZIO_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_serv.unita_progr
           and ottica = pr_uo_serv.unita_ottica
           and dal = pr_uo_serv.unita_dal) as UO_SERVIZIO_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_serv.unita_progr
           and ottica = pr_uo_serv.unita_ottica
           and dal = pr_uo_serv.unita_dal) as UO_SERVIZIO_CODICE,
       pr_sog.utente as UTENTE_SOGGETTO,
       pr_sog.tipo_soggetto as TIPO_SOGGETTO,
       de_comp.id_delibera_competenza as ID_COMPETENZA,
       de_comp.utente as COMP_UTENTE,
       de_comp.unita_progr as COMP_UNITA_PROGR,
       de_comp.unita_dal as COMP_UNITA_DAL,
       de_comp.unita_ottica as COMP_UNITA_OTTICA,
       de_comp.ruolo as COMP_RUOLO,
       de_comp.lettura as COMP_LETTURA,
       de_comp.modifica as COMP_MODIFICA,
       de_comp.cancellazione as COMP_CANCELLAZIONE,
       nvl(de_step.id_engine_step,
           pr_step.id_engine_step) as ID_STEP,
       nvl(de_attori.utente,
           pr_attori.utente) as STEP_UTENTE,
       nvl(de_attori.unita_progr,
           pr_attori.unita_progr) as STEP_UNITA_PROGR,
       nvl(de_attori.unita_dal,
           pr_attori.unita_dal) as STEP_UNITA_DAL,
       nvl(de_attori.unita_ottica,
           pr_attori.unita_ottica) as STEP_UNITA_OTTICA,
       nvl(de_attori.ruolo,
           pr_attori.ruolo) as STEP_RUOLO,
       nvl(de_cfg_step.titolo,
           pr_cfg_step.titolo) as STEP_NOME,
       nvl(de_cfg_step.descrizione,
           pr_cfg_step.descrizione) as STEP_DESCRIZIONE,
       nvl(
         case when de_step.id_cfg_step is not null then
           (select count(*) from wkf_engine_step s2
             where s2.id_engine_iter = de_iter.id_engine_iter
               and s2.id_cfg_step = de_step.id_cfg_step
               and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', deli.ente) = 'Y')
         end,
         case when pr_step.id_cfg_step is not null then
           (select count(*) from wkf_engine_step s2
             where s2.id_engine_iter = pr_iter.id_engine_iter
               and s2.id_cfg_step = pr_step.id_cfg_step
               and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', prop.ente) = 'Y')
         end
       ) as NUMERO_PASSAGGI,
       decode(impostazioni_pkg.get_impostazione('EXCEL_ULTIMO_ATTORE_FLUSSO', prop.ente), 'Y',
              nvl(utility_pkg.get_nominativo_utente_ad4(de_iter.utente_upd, de_iter.ente),
                  utility_pkg.get_nominativo_utente_ad4(pr_iter.utente_upd, pr_iter.ente)),
              null) as ULTIMO_ATTORE_FLUSSO,
       deli.numero_delibera as NUMERO_ATTO,
       deli.anno_delibera as ANNO_ATTO,
       deli.registro_delibera as REGISTRO_ATTO,
       trunc(deli.data_numero_delibera) as DATA_ATTO,
       trunc(deli.data_adozione) as DATA_ADOZIONE,
       trunc(deli.data_esecutivita) as DATA_ESECUTIVITA,
       cast('' as date) as DATA_SCADENZA,
       cast(null as number) as NUMERO_ATTO_2,
       cast(null as number) as ANNO_ATTO_2,
       cast('' as varchar2(255)) as REGISTRO_ATTO_2,
       cast(null as date) as DATA_NUMERO_ATTO_2,
       prop.numero_proposta as NUMERO_PROPOSTA,
       prop.anno_proposta as ANNO_PROPOSTA,
       prop.registro_proposta as REGISTRO_PROPOSTA,
       trunc(prop.data_proposta) as DATA_PROPOSTA,
       deli.numero_protocollo as NUMERO_PROTOCOLLO,
       deli.anno_protocollo as ANNO_PROTOCOLLO,
       deli.registro_protocollo as REGISTRO_PROTOCOLLO,
       deli.classifica_codice as CLASSIFICA_CODICE,
       deli.classifica_dal as CLASSIFICA_DAL,
       deli.classifica_descrizione as CLASSIFICA_DESCRIZIONE,
       deli.fascicolo_anno as FASCICOLO_ANNO,
       deli.fascicolo_numero as FASCICOLO_NUMERO,
       deli.fascicolo_oggetto as FASCICOLO_OGGETTO,
       prop.id_tipo_delibera as ID_TIPOLOGIA,
       de_tipi.titolo as TITOLO_TIPOLOGIA,
       de_tipi.descrizione as DESCRIZIONE_TIPOLOGIA,
       de_tipi.codice_esterno as CODICE_ESTERNO_TIPOLOGIA,
       de_tipi.tipo_pubblicazione_albo as TIPO_PUBBLICAZIONE_ALBO,
       pr_tipo_par.contabile as CON_IMPEGNO_SPESA,
       deli.numero_albo as NUMERO_ALBO,
       deli.anno_albo as ANNO_ALBO,
       trunc(deli.data_pubblicazione) as DATA_PUBBLICAZIONE,
       trunc(deli.data_fine_pubblicazione) as DATA_FINE_PUBBLICAZIONE,
       (select log
          from JCONS_V_LOG_CONSERVAZIONE j
         where j.id_documento_rif = deli.id_documento_esterno
           and not exists
         (select 1
                  from JCONS_V_LOG_CONSERVAZIONE jj
                 where jj.id_documento_rif = j.id_documento_rif
                   and jj.id_documento > j.id_documento)) as LOG_CONSERVAZIONE,
       jcons_pkg.get_data_conservazione(deli.id_documento_esterno) as DATA_CONSERVAZIONE,
       deli.stato_conservazione as STATO_CONSERVAZIONE,
       deli.da_inviare_corte_conti as DA_INVIARE_CORTE_CONTI,
       trunc(deli.data_invio_corte_conti) as DATA_INVIO_CORTE_CONTI,
       prop.id_commissione as ID_COMMISSIONE,
       deli.id_oggetto_seduta as ID_OGGETTO_SEDUTA,
       de_alle.id_tipo_allegato as ID_TIPO_ALLEGATO_DELIBERA,
       de_alle_par.id_tipo_allegato as ID_TIPO_ALLEGATO_PARERE,
       nvl(deli.id_file_allegato_testo,
           prop.id_file_allegato_testo) as ID_TESTO,
       af.id_file as ID_FILE_ALLEGATO,
       prop.id_oggetto_ricorrente ID_OGGETTO_RICORRENTE,
       deli.stato_marcatura STATO_MARCATURA,
       deli.stato_firma STATO_FIRMA,
       prop.data_ordinamento DATA_ORDINAMENTO,
       utility_pkg.get_elenco_relatori_prop_deli(prop.id_proposta_delibera,
                                                     prop.id_delega,
                                                     deli.id_oggetto_seduta) NI_RELATORE,
       cast(utility_pkg.get_delibera_conclusa(deli.id_delibera) as char(1)) as ATTO_CONCLUSO,
       null CUP,
       null CIG,
       cast (null as date) as  DATA_CONTROLLO_GESTIONE,
       deli.ID_IMPATTO_GENERAZIONALE,
       b.conto_economico CONTO_ECONOMICO,
       b.codice_progetto CODICE_PROGETTO,
       b.id_tipo_budget ID_TIPO_BUDGET,
       b.importo IMPORTO,
       cast(null as number) as IMPORTO_LIQUIDAZIONE,
       utility_pkg.get_elenco_firmatari_documento(deli.id_delibera,
                                                  deli.ente,
                                                  'DELIBERA') as ELENCO_FIRMATARI,
       prop.in_evidenza IN_EVIDENZA,
       prop.data_in_evidenza_da DATA_IN_EVIDENZA_DA,
       prop.data_in_evidenza_a DATA_IN_EVIDENZA_A,
       prop.note || deli.note NOTE,
       prop.importo_totale IMPORTO_TOTALE
  from proposte_delibera          prop,
       delibere                   deli,
       delibere_competenze        de_comp,
       tipi_delibera              de_tipi,
       proposte_delibera_soggetti pr_sog,
       proposte_delibera_soggetti pr_uo_prop,
       proposte_delibera_soggetti pr_uo_serv,
       wkf_engine_iter            de_iter,
       wkf_engine_step            de_step,
       wkf_engine_step_attori     de_attori,
       wkf_cfg_step               de_cfg_step,
       wkf_engine_iter            pr_iter,
       wkf_engine_step            pr_step,
       wkf_engine_step_attori     pr_attori,
       wkf_cfg_step               pr_cfg_step,
       allegati                   de_alle,
       allegati                   de_alle_par,
       categorie                  pr_cat,
       visti_pareri               pr_par,
       visti_pareri               de_par,
       tipi_visto_parere          pr_tipo_par,
       allegati_file              af,
       budget                     b
 where pr_sog.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_uo_prop.tipo_soggetto(+) = 'UO_PROPONENTE'
   and pr_uo_prop.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_uo_serv.tipo_soggetto(+) = 'UO_SERVIZIO'
   and pr_uo_serv.id_proposta_delibera(+) = prop.id_proposta_delibera
   and de_iter.id_engine_iter(+) = deli.id_engine_iter
   and de_step.id_engine_step(+) = de_iter.id_step_corrente
   and de_attori.id_engine_step(+) = de_step.id_engine_step
   and de_cfg_step.id_cfg_step(+) = de_step.id_cfg_step
   and pr_iter.id_engine_iter(+) = prop.id_engine_iter
   and pr_step.id_engine_step(+) = pr_iter.id_step_corrente
   and pr_attori.id_engine_step(+) = pr_step.id_engine_step
   and pr_cfg_step.id_cfg_step(+) = pr_step.id_cfg_step
   and de_alle_par.id_visto_parere(+) = de_par.id_visto_parere
   and de_alle.id_delibera(+) = deli.id_delibera
   and pr_cat.id_categoria(+) = prop.id_categoria
   and pr_tipo_par.id_tipo_visto_parere(+) = pr_par.id_tipologia
   and pr_par.valido(+) = 'Y'
   and pr_par.id_proposta_delibera(+) = prop.id_proposta_delibera
   and de_par.valido(+) = 'Y'
   and de_par.id_delibera(+) = deli.id_delibera
   and de_comp.id_delibera = deli.id_delibera
   and de_tipi.id_tipo_delibera = prop.id_tipo_delibera
   and prop.valido = 'Y'
   and deli.id_proposta_delibera = prop.id_proposta_delibera
   and af.id_allegato(+) = de_alle.id_allegato
   and deli.valido = 'Y'
   and b.id_proposta_delibera(+) = prop.id_proposta_delibera
union all
select prop.id_proposta_delibera as ID_DOCUMENTO,
       decode(deli.id_delibera,
              null,
              'PROPOSTA_DELIBERA',
              'DELIBERA') as TIPO_DOCUMENTO,
       deli.id_delibera as ID_DOCUMENTO_PRINCIPALE,
       cast('DELIBERA' as varchar2(255)) as TIPO_DOCUMENTO_PRINCIPALE,
       prop.ente as ENTE,
       nvl(deli.oggetto,
           prop.oggetto) as OGGETTO,
       cast(nvl(deli.riservato,
                prop.riservato) as char(1)) as RISERVATO,
       prop.controllo_funzionario as CONTROLLO_FUNZIONARIO,
       upper(decode(nvl(deli.stato,
                        prop.stato),
                    'ANNULLATO',
                    nvl(deli.stato,
                        prop.stato),
                    nvl(nvl(de_cfg_step.titolo,
                            pr_cfg_step.titolo),
                        nvl(deli.stato,
                            prop.stato)))) as STATO,
       pr_cat.id_categoria as ID_CATEGORIA,
       pr_sog.unita_progr as UO_PROPONENTE_PROGR,
       pr_sog.unita_dal as UO_PROPONENTE_DAL,
       pr_sog.unita_ottica as UO_PROPONENTE_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_prop.unita_progr
           and ottica = pr_uo_prop.unita_ottica
           and dal = pr_uo_prop.unita_dal) as UO_PROPONENTE_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_prop.unita_progr
           and ottica = pr_uo_prop.unita_ottica
           and dal = pr_uo_prop.unita_dal) as UO_PROPONENTE_CODICE,
       pr_uo_serv.unita_progr as UO_SERVIZIO_PROGR,
       pr_uo_serv.unita_dal as UO_SERVIZIO_DAL,
       pr_uo_serv.unita_ottica as UO_SERVIZIO_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_serv.unita_progr
           and ottica = pr_uo_serv.unita_ottica
           and dal = pr_uo_serv.unita_dal) as UO_SERVIZIO_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_serv.unita_progr
           and ottica = pr_uo_serv.unita_ottica
           and dal = pr_uo_serv.unita_dal) as UO_SERVIZIO_CODICE,
       decode(deli.id_delibera,
              null,
              pr_sog.utente,
              ds.utente) as UTENTE_SOGGETTO,
       decode(deli.id_delibera,
              null,
              pr_sog.tipo_soggetto,
              ds.tipo_soggetto) as TIPO_SOGGETTO,
       de_comp.id_delibera_competenza as ID_COMPETENZA,
       de_comp.utente as COMP_UTENTE,
       de_comp.unita_progr as COMP_UNITA_PROGR,
       de_comp.unita_dal as COMP_UNITA_DAL,
       de_comp.unita_ottica as COMP_UNITA_OTTICA,
       de_comp.ruolo as COMP_RUOLO,
       de_comp.lettura as COMP_LETTURA,
       de_comp.modifica as COMP_MODIFICA,
       de_comp.cancellazione as COMP_CANCELLAZIONE,
       nvl(de_step.id_engine_step,
           pr_step.id_engine_step) as ID_STEP,
       nvl(de_attori.utente,
           pr_attori.utente) as STEP_UTENTE,
       nvl(de_attori.unita_progr,
           pr_attori.unita_progr) as STEP_UNITA_PROGR,
       nvl(de_attori.unita_dal,
           pr_attori.unita_dal) as STEP_UNITA_DAL,
       nvl(de_attori.unita_ottica,
           pr_attori.unita_ottica) as STEP_UNITA_OTTICA,
       nvl(de_attori.ruolo,
           pr_attori.ruolo) as STEP_RUOLO,
       nvl(de_cfg_step.titolo,
           pr_cfg_step.titolo) as STEP_NOME,
       nvl(de_cfg_step.descrizione,
           pr_cfg_step.descrizione) as STEP_DESCRIZIONE,
       nvl(
         case when de_step.id_cfg_step is not null then
           (select count(*) from wkf_engine_step s2
             where s2.id_engine_iter = de_iter.id_engine_iter
               and s2.id_cfg_step = de_step.id_cfg_step
               and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', prop.ente) = 'Y')
         end,
         case when pr_step.id_cfg_step is not null then
           (select count(*) from wkf_engine_step s2
             where s2.id_engine_iter = pr_iter.id_engine_iter
               and s2.id_cfg_step = pr_step.id_cfg_step
               and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', prop.ente) = 'Y')
         end
       ) as NUMERO_PASSAGGI,
       decode(impostazioni_pkg.get_impostazione('EXCEL_ULTIMO_ATTORE_FLUSSO', prop.ente), 'Y',
              nvl(utility_pkg.get_nominativo_utente_ad4(de_iter.utente_upd, de_iter.ente),
                  utility_pkg.get_nominativo_utente_ad4(pr_iter.utente_upd, pr_iter.ente)),
              null) as ULTIMO_ATTORE_FLUSSO,
       deli.numero_delibera as NUMERO_ATTO,
       deli.anno_delibera as ANNO_ATTO,
       deli.registro_delibera as REGISTRO_ATTO,
       trunc(deli.data_numero_delibera) as DATA_ATTO,
       trunc(deli.data_adozione) as DATA_ADOZIONE,
       trunc(deli.data_esecutivita) as DATA_ESECUTIVITA,
       cast('' as date) as DATA_SCADENZA,
       cast(null as number) as NUMERO_ATTO_2,
       cast(null as number) as ANNO_ATTO_2,
       cast('' as varchar2(255)) as REGISTRO_ATTO_2,
       cast(null as date) as DATA_NUMERO_ATTO_2,
       prop.numero_proposta as NUMERO_PROPOSTA,
       prop.anno_proposta as ANNO_PROPOSTA,
       prop.registro_proposta as REGISTRO_PROPOSTA,
       trunc(prop.data_proposta) as DATA_PROPOSTA,
       deli.numero_protocollo as NUMERO_PROTOCOLLO,
       deli.anno_protocollo as ANNO_PROTOCOLLO,
       deli.registro_protocollo as REGISTRO_PROTOCOLLO,
       deli.classifica_codice as CLASSIFICA_CODICE,
       deli.classifica_dal as CLASSIFICA_DAL,
       deli.classifica_descrizione as CLASSIFICA_DESCRIZIONE,
       deli.fascicolo_anno as FASCICOLO_ANNO,
       deli.fascicolo_numero as FASCICOLO_NUMERO,
       deli.fascicolo_oggetto as FASCICOLO_OGGETTO,
       prop.id_tipo_delibera as ID_TIPOLOGIA,
       de_tipi.titolo as TITOLO_TIPOLOGIA,
       de_tipi.descrizione as DESCRIZIONE_TIPOLOGIA,
       de_tipi.codice_esterno as CODICE_ESTERNO_TIPOLOGIA,
       de_tipi.tipo_pubblicazione_albo as TIPO_PUBBLICAZIONE_ALBO,
       pr_tipo_par.contabile as CON_IMPEGNO_SPESA,
       deli.numero_albo as NUMERO_ALBO,
       deli.anno_albo as ANNO_ALBO,
       trunc(deli.data_pubblicazione) as DATA_PUBBLICAZIONE,
       trunc(deli.data_fine_pubblicazione) as DATA_FINE_PUBBLICAZIONE,
       (select log
          from JCONS_V_LOG_CONSERVAZIONE j
         where j.id_documento_rif = deli.id_documento_esterno
           and not exists
         (select 1
                  from JCONS_V_LOG_CONSERVAZIONE jj
                 where jj.id_documento_rif = j.id_documento_rif
                   and jj.id_documento > j.id_documento)) as LOG_CONSERVAZIONE,
       jcons_pkg.get_data_conservazione(deli.id_documento_esterno) as DATA_CONSERVAZIONE,
       deli.stato_conservazione as STATO_CONSERVAZIONE,
       deli.da_inviare_corte_conti as DA_INVIARE_CORTE_CONTI,
       trunc(deli.data_invio_corte_conti) as DATA_INVIO_CORTE_CONTI,
       prop.id_commissione as ID_COMMISSIONE,
       deli.id_oggetto_seduta as ID_OGGETTO_SEDUTA,
       de_alle.id_tipo_allegato as ID_TIPO_ALLEGATO_DELIBERA,
       de_alle_par.id_tipo_allegato as ID_TIPO_ALLEGATO_PARERE,
       nvl(deli.id_file_allegato_testo,
           prop.id_file_allegato_testo) as ID_TESTO,
       af.id_file as ID_FILE_ALLEGATO,
       prop.id_oggetto_ricorrente ID_OGGETTO_RICORRENTE,
       deli.stato_marcatura STATO_MARCATURA,
       deli.stato_firma STATO_FIRMA,
       prop.data_ordinamento DATA_ORDINAMENTO,
       utility_pkg.get_elenco_relatori_prop_deli(prop.id_proposta_delibera,
                                                     prop.id_delega,
                                                     deli.id_oggetto_seduta) NI_RELATORE,
       cast(utility_pkg.get_delibera_conclusa(deli.id_delibera) as char(1)) as ATTO_CONCLUSO,
       null CUP,
       null CIG,
       cast (null as date) as DATA_CONTROLLO_GESTIONE,
       deli.ID_IMPATTO_GENERAZIONALE,
       b.conto_economico CONTO_ECONOMICO,
       b.codice_progetto CODICE_PROGETTO,
       b.id_tipo_budget ID_TIPO_BUDGET,
       b.importo IMPORTO,
       cast(null as number) as IMPORTO_LIQUIDAZIONE,
       utility_pkg.get_elenco_firmatari_documento(deli.id_delibera,
                                                  deli.ente,
                                                  'DELIBERA') as ELENCO_FIRMATARI,
       prop.in_evidenza IN_EVIDENZA,
       prop.data_in_evidenza_da DATA_IN_EVIDENZA_DA,
       prop.data_in_evidenza_a DATA_IN_EVIDENZA_A,
       prop.note || deli.note NOTE,
       prop.importo_totale IMPORTO_TOTALE
  from proposte_delibera          prop,
       delibere                   deli,
       delibere_competenze        de_comp,
       delibere_soggetti          ds,
       tipi_delibera              de_tipi,
       proposte_delibera_soggetti pr_sog,
       proposte_delibera_soggetti pr_uo_prop,
       proposte_delibera_soggetti pr_uo_serv,
       wkf_engine_iter            de_iter,
       wkf_engine_step            de_step,
       wkf_engine_step_attori     de_attori,
       wkf_cfg_step               de_cfg_step,
       wkf_engine_iter            pr_iter,
       wkf_engine_step            pr_step,
       wkf_engine_step_attori     pr_attori,
       wkf_cfg_step               pr_cfg_step,
       allegati                   de_alle,
       allegati                   de_alle_par,
       categorie                  pr_cat,
       visti_pareri               pr_par,
       visti_pareri               de_par,
       tipi_visto_parere          pr_tipo_par,
       allegati_file              af,
       budget                     b
 where ds.id_delibera(+) = deli.id_delibera
   and pr_sog.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_uo_prop.tipo_soggetto(+) = 'UO_PROPONENTE'
   and pr_uo_prop.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_uo_serv.tipo_soggetto(+) = 'UO_SERVIZIO'
   and pr_uo_serv.id_proposta_delibera(+) = prop.id_proposta_delibera
   and de_iter.id_engine_iter(+) = deli.id_engine_iter
   and de_step.id_engine_step(+) = de_iter.id_step_corrente
   and de_attori.id_engine_step(+) = de_step.id_engine_step
   and de_cfg_step.id_cfg_step(+) = de_step.id_cfg_step
   and pr_iter.id_engine_iter(+) = prop.id_engine_iter
   and pr_step.id_engine_step(+) = pr_iter.id_step_corrente
   and pr_attori.id_engine_step(+) = pr_step.id_engine_step
   and pr_cfg_step.id_cfg_step(+) = pr_step.id_cfg_step
   and de_alle_par.id_visto_parere(+) = de_par.id_visto_parere
   and de_alle.id_delibera(+) = deli.id_delibera
   and pr_cat.id_categoria(+) = prop.id_categoria
   and pr_tipo_par.id_tipo_visto_parere(+) = pr_par.id_tipologia
   and pr_par.valido(+) = 'Y'
   and pr_par.id_proposta_delibera(+) = prop.id_proposta_delibera
   and de_par.valido(+) = 'Y'
   and de_par.id_delibera(+) = deli.id_delibera
   and de_comp.id_delibera = deli.id_delibera
   and de_tipi.id_tipo_delibera = prop.id_tipo_delibera
   and prop.valido = 'Y'
   and deli.id_proposta_delibera = prop.id_proposta_delibera
   and af.id_allegato(+) = de_alle.id_allegato
   and deli.valido = 'Y'
   and b.id_proposta_delibera(+) = prop.id_proposta_delibera
union all
select prop.id_proposta_delibera as ID_DOCUMENTO,
       decode(deli.id_delibera,
              null,
              'PROPOSTA_DELIBERA',
              'DELIBERA') as TIPO_DOCUMENTO,
       deli.id_delibera as ID_DOCUMENTO_PRINCIPALE,
       cast('DELIBERA' as varchar2(255)) as TIPO_DOCUMENTO_PRINCIPALE,
       prop.ente as ENTE,
       nvl(deli.oggetto,
           prop.oggetto) as OGGETTO,
       cast(nvl(deli.riservato,
                prop.riservato) as char(1)) as RISERVATO,
       prop.controllo_funzionario as CONTROLLO_FUNZIONARIO,
       upper(decode(nvl(deli.stato,
                        prop.stato),
                    'ANNULLATO',
                    nvl(deli.stato,
                        prop.stato),
                    nvl(nvl(de_cfg_step.titolo,
                            pr_cfg_step.titolo),
                        nvl(deli.stato,
                            prop.stato)))) as STATO,
       pr_cat.id_categoria as ID_CATEGORIA,
       pr_sog.unita_progr as UO_PROPONENTE_PROGR,
       pr_sog.unita_dal as UO_PROPONENTE_DAL,
       pr_sog.unita_ottica as UO_PROPONENTE_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_prop.unita_progr
           and ottica = pr_uo_prop.unita_ottica
           and dal = pr_uo_prop.unita_dal) as UO_PROPONENTE_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_prop.unita_progr
           and ottica = pr_uo_prop.unita_ottica
           and dal = pr_uo_prop.unita_dal) as UO_PROPONENTE_CODICE,
       pr_uo_serv.unita_progr as UO_SERVIZIO_PROGR,
       pr_uo_serv.unita_dal as UO_SERVIZIO_DAL,
       pr_uo_serv.unita_ottica as UO_SERVIZIO_OTTICA,
       (select descrizione
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_serv.unita_progr
           and ottica = pr_uo_serv.unita_ottica
           and dal = pr_uo_serv.unita_dal) as UO_SERVIZIO_DESCRIZIONE,
       (select codice
          from so4_v_unita_organizzative_pubb
         where progr = pr_uo_serv.unita_progr
           and ottica = pr_uo_serv.unita_ottica
           and dal = pr_uo_serv.unita_dal) as UO_SERVIZIO_CODICE,
       decode(deli.id_delibera,
              null,
              pr_sog.utente,
              ds.utente) as UTENTE_SOGGETTO,
       decode(deli.id_delibera,
              null,
              pr_sog.tipo_soggetto,
              ds.tipo_soggetto) as TIPO_SOGGETTO,
       pr_comp.id_proposta_delibera_comp as ID_COMPETENZA,
       pr_comp.utente as COMP_UTENTE,
       pr_comp.unita_progr as COMP_UNITA_PROGR,
       pr_comp.unita_dal as COMP_UNITA_DAL,
       pr_comp.unita_ottica as COMP_UNITA_OTTICA,
       pr_comp.ruolo as COMP_RUOLO,
       pr_comp.lettura as COMP_LETTURA,
       pr_comp.modifica as COMP_MODIFICA,
       pr_comp.cancellazione as COMP_CANCELLAZIONE,
       nvl(de_step.id_engine_step,
           pr_step.id_engine_step) as ID_STEP,
       nvl(de_attori.utente,
           pr_attori.utente) as STEP_UTENTE,
       nvl(de_attori.unita_progr,
           pr_attori.unita_progr) as STEP_UNITA_PROGR,
       nvl(de_attori.unita_dal,
           pr_attori.unita_dal) as STEP_UNITA_DAL,
       nvl(de_attori.unita_ottica,
           pr_attori.unita_ottica) as STEP_UNITA_OTTICA,
       nvl(de_attori.ruolo,
           pr_attori.ruolo) as STEP_RUOLO,
       nvl(de_cfg_step.titolo,
           pr_cfg_step.titolo) as STEP_NOME,
       nvl(de_cfg_step.descrizione,
           pr_cfg_step.descrizione) as STEP_DESCRIZIONE,
       nvl(
         case when de_step.id_cfg_step is not null then
           (select count(*) from wkf_engine_step s2
             where s2.id_engine_iter = de_iter.id_engine_iter
               and s2.id_cfg_step = de_step.id_cfg_step
               and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', prop.ente) = 'Y')
         end,
         case when pr_step.id_cfg_step is not null then
           (select count(*) from wkf_engine_step s2
             where s2.id_engine_iter = pr_iter.id_engine_iter
               and s2.id_cfg_step = pr_step.id_cfg_step
               and impostazioni_pkg.get_impostazione('EXCEL_NUMERO_PASSAGGI', prop.ente) = 'Y')
         end
       ) as NUMERO_PASSAGGI,
       decode(impostazioni_pkg.get_impostazione('EXCEL_ULTIMO_ATTORE_FLUSSO', prop.ente), 'Y',
              nvl(utility_pkg.get_nominativo_utente_ad4(de_iter.utente_upd, de_iter.ente),
                  utility_pkg.get_nominativo_utente_ad4(pr_iter.utente_upd, pr_iter.ente)),
              null) as ULTIMO_ATTORE_FLUSSO,
       deli.numero_delibera as NUMERO_ATTO,
       deli.anno_delibera as ANNO_ATTO,
       DELI.REGISTRO_DELIBERA as REGISTRO_ATTO,
       trunc(deli.data_numero_delibera) as DATA_ATTO,
       trunc(deli.data_adozione) as DATA_ADOZIONE,
       trunc(deli.data_esecutivita) as DATA_ESECUTIVITA,
       cast('' as date) as DATA_SCADENZA,
       cast(null as number) as NUMERO_ATTO_2,
       cast(null as number) as ANNO_ATTO_2,
       cast('' as varchar2(255)) as REGISTRO_ATTO_2,
       cast(null as date) as DATA_NUMERO_ATTO_2,
       prop.numero_proposta as NUMERO_PROPOSTA,
       prop.anno_proposta as ANNO_PROPOSTA,
       prop.registro_proposta as REGISTRO_PROPOSTA,
       trunc(prop.data_proposta) as DATA_PROPOSTA,
       deli.numero_protocollo as NUMERO_PROTOCOLLO,
       deli.anno_protocollo as ANNO_PROTOCOLLO,
       deli.registro_protocollo as REGISTRO_PROTOCOLLO,
       deli.classifica_codice as CLASSIFICA_CODICE,
       deli.classifica_dal as CLASSIFICA_DAL,
       deli.classifica_descrizione as CLASSIFICA_DESCRIZIONE,
       deli.fascicolo_anno as FASCICOLO_ANNO,
       deli.fascicolo_numero as FASCICOLO_NUMERO,
       deli.fascicolo_oggetto as FASCICOLO_OGGETTO,
       prop.id_tipo_delibera as ID_TIPOLOGIA,
       pr_tipo.titolo as TITOLO_TIPOLOGIA,
       pr_tipo.descrizione as DESCRIZIONE_TIPOLOGIA,
       pr_tipo.codice_esterno as CODICE_ESTERNO_TIPOLOGIA,
       pr_tipo.tipo_pubblicazione_albo as TIPO_PUBBLICAZIONE_ALBO,
       pr_tipo_par.contabile as CON_IMPEGNO_SPESA,
       deli.numero_albo as NUMERO_ALBO,
       deli.anno_albo as ANNO_ALBO,
       trunc(deli.data_pubblicazione) as DATA_PUBBLICAZIONE,
       trunc(deli.data_fine_pubblicazione) as DATA_FINE_PUBBLICAZIONE,
       (select log
          from JCONS_V_LOG_CONSERVAZIONE j
         where j.id_documento_rif = deli.id_documento_esterno
           and not exists
         (select 1
                  from JCONS_V_LOG_CONSERVAZIONE jj
                 where jj.id_documento_rif = j.id_documento_rif
                   and jj.id_documento > j.id_documento)) as LOG_CONSERVAZIONE,
       jcons_pkg.get_data_conservazione(deli.id_documento_esterno) as DATA_CONSERVAZIONE,
       deli.stato_conservazione as STATO_CONSERVAZIONE,
       cast(nvl(deli.da_inviare_corte_conti,
                prop.da_inviare_corte_conti) as char(1)) as DA_INVIARE_CORTE_CONTI,
       trunc(deli.data_invio_corte_conti) as DATA_INVIO_CORTE_CONTI,
       prop.id_commissione as ID_COMMISSIONE,
       deli.id_oggetto_seduta as ID_OGGETTO_SEDUTA,
       pr_alle.id_tipo_allegato as ID_TIPO_ALLEGATO_DELIBERA,
       pr_alle_par.id_tipo_allegato as ID_TIPO_ALLEGATO_PARERE,
       nvl(deli.id_file_allegato_testo,
           prop.id_file_allegato_testo) as ID_TESTO,
       af.id_file as ID_FILE_ALLEGATO,
       prop.id_oggetto_ricorrente ID_OGGETTO_RICORRENTE,
       deli.stato_marcatura STATO_MARCATURA,
       deli.stato_firma STATO_FIRMA,
       prop.data_ordinamento as DATA_ORDINAMENTO,
       utility_pkg.get_elenco_relatori_prop_deli(prop.id_proposta_delibera,
                                                     prop.id_delega,
                                                     deli.id_oggetto_seduta) NI_RELATORE,
       cast(utility_pkg.get_delibera_conclusa(deli.id_delibera) as char(1)) as ATTO_CONCLUSO,
       null as CUP,
       null as CIG,
       cast (null as date) as DATA_CONTROLLO_GESTIONE,
       decode(deli.id_delibera,
              null,
              prop.ID_IMPATTO_GENERAZIONALE,
              deli.ID_IMPATTO_GENERAZIONALE) as ID_IMPATTO_GENERAZIONALE,
       b.conto_economico CONTO_ECONOMICO,
       b.codice_progetto CODICE_PROGETTO,
       b.id_tipo_budget ID_TIPO_BUDGET,
       b.importo IMPORTO,
       cast(null as number) as IMPORTO_LIQUIDAZIONE,
       decode(deli.id_delibera,
              null,
              utility_pkg.get_elenco_firmatari_documento(prop.id_proposta_delibera,
                                                         prop.ente,
                                                         'PROPOSTA_DELIBERA'),
              utility_pkg.get_elenco_firmatari_documento(deli.id_delibera,
                                                         deli.ente,
                                                         'DELIBERA')),
       prop.in_evidenza IN_EVIDENZA,
       prop.data_in_evidenza_da DATA_IN_EVIDENZA_DA,
       prop.data_in_evidenza_a DATA_IN_EVIDENZA_A,
       prop.note || deli.note NOTE,
       prop.importo_totale IMPORTO_TOTALE
  from proposte_delibera            prop,
       delibere                     deli,
       delibere_soggetti            ds,
       proposte_delibera_competenze pr_comp,
       tipi_delibera                pr_tipo,
       proposte_delibera_soggetti   pr_sog,
       proposte_delibera_soggetti   pr_uo_prop,
       proposte_delibera_soggetti   pr_uo_serv,
       wkf_engine_iter              de_iter,
       wkf_engine_step              de_step,
       wkf_engine_step_attori       de_attori,
       wkf_cfg_step                 de_cfg_step,
       wkf_engine_iter              pr_iter,
       wkf_engine_step              pr_step,
       wkf_engine_step_attori       pr_attori,
       wkf_cfg_step                 pr_cfg_step,
       allegati                     pr_alle,
       allegati                     pr_alle_par,
       categorie                    pr_cat,
       visti_pareri                 pr_par,
       tipi_visto_parere            pr_tipo_par,
       allegati_file                af,
       budget                       b
 where ds.id_delibera(+) = deli.id_delibera
   and deli.id_proposta_delibera(+) = prop.id_proposta_delibera
   and deli.valido(+) = 'Y'
   and pr_uo_prop.tipo_soggetto(+) = 'UO_PROPONENTE'
   and pr_uo_prop.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_uo_serv.tipo_soggetto(+) = 'UO_SERVIZIO'
   and pr_uo_serv.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_sog.id_proposta_delibera(+) = prop.id_proposta_delibera
   and de_iter.id_engine_iter(+) = deli.id_engine_iter
   and de_step.id_engine_step(+) = de_iter.id_step_corrente
   and de_attori.id_engine_step(+) = de_step.id_engine_step
   and de_cfg_step.id_cfg_step(+) = de_step.id_cfg_step
   and pr_iter.id_engine_iter(+) = prop.id_engine_iter
   and pr_step.id_engine_step(+) = pr_iter.id_step_corrente
   and pr_attori.id_engine_step(+) = pr_step.id_engine_step
   and pr_cfg_step.id_cfg_step(+) = pr_step.id_cfg_step
   and pr_alle.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_alle_par.id_visto_parere(+) = pr_par.id_visto_parere
   and pr_cat.id_categoria(+) = prop.id_categoria
   and pr_par.id_proposta_delibera(+) = prop.id_proposta_delibera
   and pr_par.valido(+) = 'Y'
   and pr_tipo_par.id_tipo_visto_parere(+) = pr_par.id_tipologia
   and pr_comp.id_proposta_delibera = prop.id_proposta_delibera
   and pr_tipo.id_tipo_delibera = prop.id_tipo_delibera
   and af.id_allegato(+) = pr_alle.id_allegato
   and prop.valido = 'Y'
   and b.id_proposta_delibera(+) = prop.id_proposta_delibera
/
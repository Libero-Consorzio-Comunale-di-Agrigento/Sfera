--liquibase formatted sql
--changeset mfrancesconi:STAT_C_G337_DETE_TEMPI_CONT runOnChange:true

--COMUNE DI PARMA: query per i tempi delle determine nei nodi contabili
create or replace force view stat_c_g337_dete_tempi_cont
(
    id_determina,
    registro,
    anno,
    numero,
    data_esecutivita,
    data_inizio,
    data_ora_presa_incarico,
    utente_presa_in_carico,
    data_ora_invio_contabilita,
    utente_invio_contabilita,
    step_successivo,
    data_ora_ingresso_ragioneria,
    data_ora_uscita_ragioneria,
    esito_ragioneria
)
AS
select distinct d.id_determina,
       d.registro_determina sigla,
       d.anno_determina anno,
       d.numero_determina numero,
       trunc (d.data_esecutivita) data_esecutivita,
       to_char (d.data_ins, 'dd/mm/yyyy hh24:mi') ts,
       to_char (wes_da_prendere_incarico.data_fine, 'dd/mm/yyyy hh24:mi') data_ora_presa_incarico,
       (select avsc.cognome || ' ' || avsc.nome
          from as4_v_soggetti_correnti avsc
         where avsc.utente = wes_da_prendere_incarico.utente_upd) des_user_presa_in_carico,
       integrazione_stats_pkg.get_data_ingresso_step (ds.id_engine_iter,
                                                      wes_da_prendere_incarico.id_engine_step,
                                                      'CONTROLLO CONTABILE - IN CARICO') trasmissione,
       integrazione_stats_pkg.get_utente_ingresso_step (ds.id_engine_iter,
                                                        wes_da_prendere_incarico.id_engine_step,
                                                        'CONTROLLO CONTABILE - IN CARICO') des_user_trasmissione,
       decode (
           (select to_char (wes_visto.data_inizio, 'dd/mm/yyyy hh24:mi')
              from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
             where wes_visto.id_engine_iter = ds.id_engine_iter
               and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
               and wcs_visto.titolo = 'HA VISTO CONTABILE ?'
               and wes_visto.data_ins > wes_da_prendere_incarico.data_upd
               and not exists
                       (select 1
                          from wkf_engine_step wes_da_prendere_incarico_succ,
                               wkf_cfg_step wcs_da_prendere_incarico_succ
                         where wes_da_prendere_incarico_succ.data_inizio > wes_da_prendere_incarico.data_inizio
                           and wcs_da_prendere_incarico_succ.titolo = 'CONTROLLO CONTABILE - PRENDI IN CARICO'
                           and wes_da_prendere_incarico_succ.id_cfg_step = wcs_da_prendere_incarico_succ.id_cfg_step
                           and wes_da_prendere_incarico_succ.id_engine_iter = ds.id_engine_iter)), null, 'REVISIONE PROPOSTA', 'VISTO CONTABILE') des_fase_att,
       to_char (wes_da_prendere_incarico.data_inizio, 'dd/mm/yyyy hh24:mi') ts_inizio,
       (select min ( to_char (wes_presa_incarico.data_fine, 'dd/mm/yyyy hh24:mi'))
          from wkf_engine_step wes_presa_incarico,
               wkf_cfg_step wcs_presa_incarico
         where wcs_presa_incarico.titolo = 'CONTROLLO CONTABILE - IN CARICO'
           and wes_presa_incarico.id_cfg_step =
               wcs_presa_incarico.id_cfg_step
           and wes_presa_incarico.id_engine_iter = ds.id_engine_iter
           and wes_presa_incarico.data_inizio > wes_da_prendere_incarico.data_inizio) ts_fine,
       decode (
           (select to_char (wes_visto.data_inizio, 'dd/mm/yyyy hh24:mi')
              from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
             where wes_visto.id_engine_iter = ds.id_engine_iter
               and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
               and wcs_visto.titolo = 'HA VISTO CONTABILE ?'
               and wes_visto.data_ins > wes_da_prendere_incarico.data_upd
               --non esiste un record di controllo contabile successivo a quello che sto considerando
               and not exists
                       (select 1
                          from wkf_engine_step wes_da_prendere_incarico_succ,
                               wkf_cfg_step wcs_da_prendere_incarico_succ
                         where wes_da_prendere_incarico_succ.data_inizio > wes_da_prendere_incarico.data_inizio
                           and wcs_da_prendere_incarico_succ.titolo = 'CONTROLLO CONTABILE - PRENDI IN CARICO'
                           and wes_da_prendere_incarico_succ.id_cfg_step = wcs_da_prendere_incarico_succ.id_cfg_step
                           and wes_da_prendere_incarico_succ.id_engine_iter = ds.id_engine_iter)), null, 'REVISIONE PROPOSTA', 'DETERMINA ESECUTIVA') des_esito
  from determine d,
       wkf_engine_step wes_da_prendere_incarico,
       wkf_cfg_step wcs_da_prendere_incarico,
       determine_storico ds
 where d.valido = 'Y'
   and ds.id_determina = d.id_determina
   and d.registro_determina = 'DD'
   and wes_da_prendere_incarico.id_engine_iter = ds.id_engine_iter
   and wes_da_prendere_incarico.id_cfg_step =
       wcs_da_prendere_incarico.id_cfg_step
   and wcs_da_prendere_incarico.titolo = 'CONTROLLO CONTABILE - PRENDI IN CARICO'
/
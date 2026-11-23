--liquibase formatted sql
--changeset mfrancesconi:STAT_C_G337_DELI_TEMPI_CONT runOnChange:true

create or replace force view stat_c_g337_deli_tempi_cont
(
    id_delibera,
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
as
select d.id_delibera,
       d.registro_delibera,
       d.anno_delibera,
       d.numero_delibera,
       d.data_esecutivita,
       to_char (pd.data_ins, 'dd/mm/yyyy hh24:mi'),
       to_char (wes_da_prendere_incarico.data_fine, 'dd/mm/yyyy hh24:mi'),
       utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (wes_da_prendere_incarico.utente_upd)),
       integrazione_stats_pkg.get_data_uscita_step (vp.id_engine_iter,
                                                    wes_da_prendere_incarico.id_engine_step,
                                                    'IN CARICO') trasmissione,
       integrazione_stats_pkg.get_utente_uscita_step (vp.id_engine_iter,
                                                      wes_da_prendere_incarico.id_engine_step,
                                                      'IN CARICO') des_user_trasmissione,
       decode (vp.esito, 'FAVOREVOLE', 'CONTROLLO SEGRETERIA', 'REVISIONE PROPOSTA') DES_FASE_ATT,
       to_char (wes_da_prendere_incarico.data_inizio, 'dd/mm/yyyy hh24:mi') TS_INIZIO,
       (select to_char (wes_visto.data_fine, 'dd/mm/yyyy hh24:mi')
        from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
        where wes_visto.id_engine_iter = vp.id_engine_iter
          and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
          and wcs_visto.titolo = 'IN CARICO'
          and wes_visto.data_inizio = wes_da_prendere_incarico.data_fine) TS_FINE,
       decode (vp.esito, 'FAVOREVOLE', 'CONTROLLO SEGRETERIA', 'REVISIONE PROPOSTA') DES_ESITO
from delibere d,
     proposte_delibera pd,
     visti_pareri vp,
     tipi_visto_parere tvp,
     wkf_engine_step wes_da_prendere_incarico,
     wkf_cfg_step wcs_da_prendere_incarico
where pd.valido = 'Y'
  and d.id_proposta_delibera = pd.id_proposta_delibera
  and d.valido = 'Y'
  and pd.id_proposta_delibera = vp.id_proposta_delibera
  and vp.id_tipologia = tvp.id_tipo_visto_parere
  and tvp.contabile = 'Y'
  and d.registro_delibera in ('GC', 'CC')
  --nodo prendi in carico del flusso del parere contabile
  and wes_da_prendere_incarico.id_engine_iter = vp.id_engine_iter
  and wes_da_prendere_incarico.id_cfg_step = wcs_da_prendere_incarico.id_cfg_step
  and wcs_da_prendere_incarico.titolo = 'PRENDI IN CARICO'
/
--liquibase formatted sql
--changeset mfrancesconi:2.5_20220928_18.view_statistiche runOnChange:true

create or replace force view STATISTICHE_DELIBERE_STEP
(
   ID_ATTO,
   REGISTRO,
   DESCRIZIONE_REGISTRO,
   TIPOLOGIA,
   ID_TIPOLOGIA,
   NOME_FLUSSO,
   ID_FLUSSO,
   DATA_INIZIO,
   DATA_FINE,
   DURATA_FLUSSO_GIORNI,
   DURATA_FLUSSO_ORE,
   DURATA_FLUSSO_MINUTI,
   DURATA_FLUSSO_SECONDI,
   DURATA_STEP_GIORNI,
   DURATA_STEP_ORE,
   DURATA_STEP_MINUTI,
   DURATA_STEP_SECONDI,
   PERCENTUALE,
   MESE,
   DESCRIZIONE_MESE,
   ANNO,
   PASSAGGIO_DATA,
   PASSAGGIO_DATA_FINE,
   PASSAGGIO_TITOLO,
   PASSAGGIO_ATTORE,
   PASSAGGIO_SEQUENZA
)
as
select deli.id_delibera,
       registro_delibera REGISTRO,
       tp.descrizione DESCRIZIONE_REGISTRO,
       tipologia.titolo tipologia,
       prop.id_tipo_delibera id_tipologia,
       cfg_iter.nome nome_flusso,
       ITER.ID_CFG_ITER id_flusso,
       prop.data_ins DATA_inizio,
       deli.data_pubblicazione data_fine,
       trunc (deli.data_pubblicazione - prop.data_ins) durata_flusso_giorni,
       trunc ( (deli.data_pubblicazione) - prop.data_ins) * 24 durata_flusso_ore,
       trunc ( ( (deli.data_pubblicazione) - prop.data_ins) * 24 * 60) durata_flusso_minuti,
       trunc ( ( (deli.data_pubblicazione) - prop.data_ins) * 24 * 60 * 60) durata_flusso_secondi,
       trunc (step.data_fine - step.data_inizio) durata_step_giorni,
       trunc ( (step.data_fine - step.data_inizio) * 24) durata_step_ore,
       trunc ( (step.data_fine - step.data_inizio) * 24 * 60) durata_step_minuti,
       trunc ( (step.data_fine - step.data_inizio) * 24 * 60 * 60) durata_step_secondi,
       trunc (
                       ( (step.data_fine - step.data_inizio) * 24 * 60 * 60)
                       * 100
                   / ( ( (deli.data_pubblicazione) - prop.data_ins) * 24 * 60 * 60))
           percentuale,
       extract (month from deli.data_pubblicazione) mese,
       to_char (deli.data_pubblicazione, 'MONTH') descrizione_mese,
       extract (year from deli.data_pubblicazione) anno,
       step.data_inizio PASSAGGIO_DATA,
       step.data_fine PASSAGGIO_DATA_fine,
       upper (cfg_step.titolo) PASSAGGIO_TITOLO,
       nvl (utility_pkg.get_uo_descrizione (a_step.unita_progr, a_step.unita_dal),
            utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (a_step.utente), prop.ente))
           PASSAGGIO_ATTORE,
       cfg_step.sequenza passaggio_sequenza
from delibere deli,
     proposte_delibera prop,
     wkf_engine_step step,
     wkf_engine_step_attori a_step,
     wkf_engine_iter iter,
     wkf_cfg_step cfg_step,
     tipi_registro tp,
     wkf_cfg_iter cfg_iter,
     tipi_delibera tipologia
where iter.id_engine_iter in (select distinct ds.id_engine_iter
                              from delibere_storico ds
                              where ds.id_engine_iter is not null and ds.id_delibera = deli.id_delibera
                              union all
                              select distinct ds.id_engine_iter
                              from proposte_delibera_storico ds
                              where ds.id_engine_iter is not null and ds.id_proposta_delibera = deli.id_proposta_delibera)
  and prop.id_proposta_delibera = deli.id_proposta_delibera
  and step.id_engine_iter = iter.id_engine_iter
  and cfg_step.id_cfg_step = step.id_cfg_step
  and step.id_engine_step = a_step.id_engine_step(+)
  and trunc ( (step.data_fine - step.data_inizio) * 24 * 60 * 60) > 0
  and tp.tipo_registro(+) = deli.registro_delibera
  and ITER.ID_CFG_ITER = cfg_iter.id_cfg_iter
  and prop.id_tipo_delibera = tipologia.id_tipo_delibera
  and deli.data_pubblicazione is not null
  and step.data_fine < deli.data_pubblicazione
  and deli.valido = 'Y'
order by deli.id_Delibera desc, step.id_engine_step desc
/
create or replace force view STATISTICHE_DETERMINE_STEP
(
   ID_ATTO,
   REGISTRO,
   DESCRIZIONE_REGISTRO,
   TIPOLOGIA,
   ID_TIPOLOGIA,
   NOME_FLUSSO,
   ID_FLUSSO,
   DATA_INIZIO,
   DATA_FINE,
   DURATA_FLUSSO_GIORNI,
   DURATA_FLUSSO_ORE,
   DURATA_FLUSSO_MINUTI,
   DURATA_FLUSSO_SECONDI,
   DURATA_STEP_GIORNI,
   DURATA_STEP_ORE,
   DURATA_STEP_MINUTI,
   DURATA_STEP_SECONDI,
   PERCENTUALE,
   MESE,
   DESCRIZIONE_MESE,
   ANNO,
   PASSAGGIO_DATA,
   PASSAGGIO_DATA_FINE,
   PASSAGGIO_TITOLO,
   PASSAGGIO_ATTORE,
   PASSAGGIO_SEQUENZA
)
as
select dete.id_determina,
       registro_determina REGISTRO,
       tp.descrizione DESCRIZIONE_REGISTRO,
       tipologia.titolo tipologia,
       dete.id_tipo_determina id_tipologia,
       cfg_iter.nome nome_flusso,
       ITER.ID_CFG_ITER id_flusso,
       Dete.data_ins DATA_inizio,
       dete.data_pubblicazione data_fine,
       trunc (dete.data_pubblicazione - dete.data_ins) durata_flusso_giorni,
       trunc ( (dete.data_pubblicazione) - dete.data_ins) * 24 durata_flusso_ore,
       trunc ( ( (dete.data_pubblicazione) - dete.data_ins) * 24 * 60) durata_flusso_minuti,
       trunc ( ( (dete.data_pubblicazione) - dete.data_ins) * 24 * 60 * 60) durata_flusso_secondi,
       trunc (step.data_fine - step.data_inizio) durata_step_giorni,
       trunc ( (step.data_fine - step.data_inizio) * 24) durata_step_ore,
       trunc ( (step.data_fine - step.data_inizio) * 24 * 60) durata_step_minuti,
       trunc ( (step.data_fine - step.data_inizio) * 24 * 60 * 60) durata_step_secondi,
       trunc (
                       ( (step.data_fine - step.data_inizio) * 24 * 60 * 60)
                       * 100
                   / ( ( (dete.data_pubblicazione) - dete.data_ins) * 24 * 60 * 60))
           percentuale,
       extract (month from dete.data_pubblicazione) mese,
       to_char (dete.data_pubblicazione, 'MONTH') descrizione_mese,
       extract (year from dete.data_pubblicazione) anno,
       step.data_inizio PASSAGGIO_DATA,
       step.data_fine PASSAGGIO_DATA_fine,
       upper (cfg_step.titolo) PASSAGGIO_TITOLO,
       nvl (utility_pkg.get_uo_descrizione (a_step.unita_progr, a_step.unita_dal),
            utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (a_step.utente), dete.ente))
           PASSAGGIO_ATTORE,
       cfg_step.sequenza passaggio_sequenza
from determine dete,
     wkf_engine_step step,
     wkf_engine_step_attori a_step,
     wkf_engine_iter iter,
     wkf_cfg_step cfg_step,
     tipi_registro tp,
     wkf_cfg_iter cfg_iter,
     tipi_determina tipologia
where iter.id_engine_iter in (select distinct ds.id_engine_iter
                              from determine_storico ds
                              where ds.id_engine_iter is not null and ds.id_determina = dete.id_determina)
  and step.id_engine_iter = iter.id_engine_iter
  and cfg_step.id_cfg_step = step.id_cfg_step
  and step.id_engine_step = a_step.id_engine_step(+)
  and trunc ( (step.data_fine - step.data_inizio) * 24 * 60 * 60) > 0
  and tp.tipo_registro(+) = dete.registro_determina
  and ITER.ID_CFG_ITER = cfg_iter.id_cfg_iter
  and dete.id_tipo_determina = tipologia.id_tipo_determina
  and Dete.data_pubblicazione is not null
  and step.data_fine < dete.data_pubblicazione
  and dete.valido = 'Y'
order by dete.id_determina desc, step.id_engine_step desc
/
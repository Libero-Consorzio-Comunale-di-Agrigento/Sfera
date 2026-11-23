--liquibase formatted sql
--changeset mfrancesconi:STATISTICHE_DETERMINE_STEP runOnChange:true

create or replace force view statistiche_determine_step (
   id_atto,
   numero_atto,
   anno_atto,
   registro,
   descrizione_registro,
   numero_proposta,
   anno_proposta,
   oggetto,
   tipologia,
   data_adozione,
   data_esecutivita,
   unita_proponente,
   nome_flusso,
   data_inizio,
   data_fine,
   durata_flusso_giorni,
   durata_step_giorni,
   passaggio_data,
   passaggio_data_fine,
   passaggio_titolo,
   passaggio_attore)
as
   select dete.id_determina,
          dete.numero_determina,
          dete.anno_determina,
          registro_determina,
          tp.descrizione,
          dete.numero_proposta,
          dete.anno_proposta,
          dete.oggetto,
          tipologia.titolo tipologia,
          dete.data_numero_determina,
          dete.data_esecutivita,
          utility_pkg.get_unita_prop_determina (dete.id_determina) unita_proponente,
          cfg_iter.nome nome_flusso,
          dete.data_ins DATA_inizio,
          nvl (dete.data_pubblicazione, iter.data_fine) data_fine,
          round ( (trunc(nvl (dete.data_pubblicazione, iter.data_fine)) - trunc(dete.data_ins)) + 1) durata_flusso_giorni,
          round ( trunc(step.data_fine - step.data_inizio) + 1) durata_step_giorni,
          step.data_inizio passaggio_data,
          step.data_fine passaggio_data_fine,
          upper (cfg_step.titolo) passaggio_titolo,
          nvl (utility_pkg.get_uo_descrizione (a_step.unita_progr, a_step.unita_dal),
               utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (a_step.utente), dete.ente))
             passaggio_attore
     from wkf_cfg_iter cfg_iter,
          wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          determine dete,
          tipi_determina tipologia,
          tipi_registro tp
    where iter.id_engine_iter in (select distinct ds.id_engine_iter
                                    from determine_storico ds
                                   where ds.id_engine_iter is not null and ds.id_determina = dete.id_determina)
      and step.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step(+)
      and tp.tipo_registro(+) = dete.registro_determina
      and iter.id_cfg_iter = cfg_iter.id_cfg_iter
      and dete.id_tipo_determina = tipologia.id_tipo_determina
      and step.data_fine < nvl (dete.data_pubblicazione, iter.data_fine)
      and dete.valido = 'Y'
/
--liquibase formatted sql
--changeset mfrancesconi:STATISTICHE_DELIBERE_STEP runOnChange:true

create or replace force view statistiche_delibere_step (
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
   select deli.id_delibera,
          deli.numero_delibera,
          deli.anno_delibera ANNO_ATTO,
          registro_delibera REGISTRO,
          tp.descrizione DESCRIZIONE_REGISTRO,
          prop.numero_proposta,
          prop.anno_proposta,
          deli.oggetto,
          tipologia.titolo tipologia,
          deli.data_adozione,
          deli.data_esecutivita,
          utility_pkg.get_unita_prop_delibera (deli.id_delibera) UNITA_PROPONENTE,
          cfg_iter.nome nome_flusso,
          prop.data_ins DATA_inizio,
          nvl (deli.data_pubblicazione, iter.data_fine) data_fine,
          round ( ( trunc((nvl (deli.data_pubblicazione, iter.data_fine)) - trunc(prop.data_ins))) + 1) durata_giorni,
          (trunc (step.data_fine - step.data_inizio) + 1) durata_step_giorni,
          step.data_inizio passaggio_data,
          step.data_fine passaggio_data_fine,
          upper (cfg_step.titolo) passaggio_titolo,
          nvl (utility_pkg.get_uo_descrizione (a_step.unita_progr, a_step.unita_dal),
               utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (a_step.utente), deli.ente))
             passaggio_attore
     from wkf_cfg_iter cfg_iter,
          wkf_engine_iter iter,
          wkf_engine_iter iter_delibera,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          proposte_delibera prop,
          delibere deli,
          tipi_delibera tipologia,
          tipi_registro tp
    where iter.id_engine_iter in (select distinct ds.id_engine_iter
                                    from delibere_storico ds
                                   where ds.id_engine_iter is not null and ds.id_delibera = deli.id_delibera)
      and prop.id_proposta_delibera = deli.id_proposta_delibera
      and step.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step(+)
      and tp.tipo_registro(+) = deli.registro_delibera
      and iter.id_cfg_iter = cfg_iter.id_cfg_iter
      and prop.id_tipo_delibera = tipologia.id_tipo_delibera
      and step.data_fine < nvl (deli.data_pubblicazione, iter.data_fine)
      and deli.valido = 'Y'
      and deli.id_engine_iter = iter_delibera.id_engine_iter
      and (deli.data_pubblicazione is not null or iter_delibera.data_fine is not null)
   union
   ----passi dell'iter per le proposte di delibera sempre delle delibere concluse (o con data pubblicazione valorizzato o con iter concluso)
   select deli.id_delibera,
          deli.numero_delibera,
          deli.anno_delibera anno_atto,
          registro_delibera REGISTRO,
          tp.descrizione DESCRIZIONE_REGISTRO,
          prop.numero_proposta,
          prop.anno_proposta,
          deli.oggetto,
          tipologia.titolo tipologia,
          deli.data_adozione,
          deli.data_esecutivita,
          utility_pkg.get_unita_prop_delibera (deli.id_delibera) UNITA_PROPONENTE,
          cfg_iter.nome nome_flusso,
          prop.data_ins DATA_inizio,
          nvl (deli.data_pubblicazione, iter.data_fine) data_fine,
          round ( ( trunc((nvl (deli.data_pubblicazione, iter.data_fine)) - trunc(prop.data_ins))) + 1) durata_giorni,
          (trunc (step.data_fine - step.data_inizio) + 1) durata_step_giorni,
          step.data_inizio passaggio_data,
          step.data_fine passaggio_data_fine,
          upper (cfg_step.titolo) passaggio_titolo,
          nvl (utility_pkg.get_uo_descrizione (a_step.unita_progr, a_step.unita_dal),
               utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (a_step.utente), deli.ente))
             passaggio_attore
     from wkf_cfg_iter cfg_iter,
          wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          proposte_delibera prop,
          delibere deli,
          tipi_delibera tipologia,
          tipi_registro tp
    where iter.id_engine_iter = prop.id_engine_iter
      and prop.id_proposta_delibera = deli.id_proposta_delibera
      and step.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step(+)
      and tp.tipo_registro(+) = deli.registro_delibera
      and iter.id_cfg_iter = cfg_iter.id_cfg_iter
      and prop.id_tipo_delibera = tipologia.id_tipo_delibera
      and iter.data_fine is not null
      and deli.valido = 'Y'
      --verifico che la delibera abbia terminato l'istruttoria
      and (deli.data_pubblicazione is not null
        or exists
              (select 1
                 from wkf_engine_iter
                where id_engine_iter = deli.id_engine_iter and data_fine is not null))
   union
   --select delle proposte concluse (iter chiuso) che non hanno mai generato una delibera
   select prop.id_proposta_delibera,
          null,
          null,
          null REGISTRO,
          null DESCRIZIONE_REGISTRO,
          prop.numero_proposta,
          prop.anno_proposta,
          prop.oggetto,
          tipologia.titolo tipologia,
          null,
          null,
          utility_pkg.get_unita_prop_delibera (prop.id_proposta_delibera) UNITA_PROPONENTE,
          cfg_iter.nome nome_flusso,
          prop.data_ins DATA_inizio,
          iter.data_fine data_fine,
          round ( ( trunc(iter.data_fine - prop.data_ins)) + 1) durata_giorni,
          (trunc (step.data_fine - step.data_inizio) + 1) durata_step_giorni,
          step.data_inizio passaggio_data,
          step.data_fine passaggio_data_fine,
          upper (cfg_step.titolo) passaggio_titolo,
          nvl (utility_pkg.get_uo_descrizione (a_step.unita_progr, a_step.unita_dal),
               utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (a_step.utente), prop.ente))
             passaggio_attore
     from wkf_cfg_iter cfg_iter,
          wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          proposte_delibera prop,
          tipi_delibera tipologia
    where iter.id_engine_iter = prop.id_engine_iter
      and step.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step(+)
      and iter.id_cfg_iter = cfg_iter.id_cfg_iter
      and prop.id_tipo_delibera = tipologia.id_tipo_delibera
      and iter.data_fine is not null
      and prop.valido = 'Y'
      and not exists
             (select 1
                from delibere
               where id_proposta_delibera = prop.id_proposta_delibera)
/
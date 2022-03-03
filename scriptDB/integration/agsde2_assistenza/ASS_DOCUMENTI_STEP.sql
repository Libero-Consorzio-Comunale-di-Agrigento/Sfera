--liquibase formatted sql
--changeset rdestasio:install_20200221_assistenza_07 runOnChange:true

/* Formatted on 09/02/2016 10:03:36 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_DOCUMENTI_STEP
(
   TIPO_OGGETTO,
   ID_DOCUMENTO,
   ID_ENGINE_ITER,
   ID_ENGINE_STEP,
   ID_ENGINE_STEP_CORRENTE,
   DATA_INIZIO,
   STEP_TITOLO,
   ID_PADRE,
   ID_TIPOLOGIA,
   CODICE_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   STATO,
   STATO_FIRMA,
   STATO_CONSERVAZIONE,
   STATO_ODG,
   UNITA_PROPONENTE,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ENTE,
   RISERVATO,
   DATA_FINE_ITER
)
AS
   SELECT 'DELIBERA',
          deli.id_delibera,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          NULL,
          tipo.id_tipo_delibera,
          tipo.titolo,
          tipo.descrizione,
          pr_deli.anno_proposta,
          pr_deli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          deli.oggetto,
          deli.stato,
          deli.stato_firma,
          deli.stato_conservazione,
          pr_deli.stato_odg,
          so4_unita.descrizione,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          pr_deli.ente,
          pr_deli.riservato,
          iter.data_fine
     FROM proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo
    WHERE     deli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_iter = iter.id_engine_iter
          AND tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step(+)
          AND deli.id_proposta_delibera = pr_deli.id_proposta_delibera
          --AND iter.data_fine IS NULL
          -- AND step.data_fine IS NULL
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND pr_deli.valido = 'Y'
          AND deli.valido = 'Y'
   UNION ALL
   SELECT 'PROPOSTA_DELIBERA',
          pr_deli.id_proposta_delibera,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          NULL,
          tipo.id_tipo_delibera,
          tipo.titolo,
          tipo.descrizione,
          pr_deli.anno_proposta,
          pr_deli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          pr_deli.oggetto,
          pr_deli.stato,
          pr_deli.stato_firma,
          deli.stato_conservazione,
          pr_deli.stato_odg,
          so4_unita.descrizione,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          pr_deli.ente,
          pr_deli.riservato,
          iter.data_fine
     FROM proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo
    WHERE     pr_deli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_iter = iter.id_engine_iter
          AND tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step(+)
          AND deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
          --  AND iter.data_fine IS NULL
          --  AND step.data_fine IS NULL
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND pr_deli.valido = 'Y'
          AND deli.valido(+) = 'Y'
   UNION ALL
   SELECT 'DETERMINA',
          dete.id_determina,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          NULL,
          tipo.id_tipo_determina,
          tipo.titolo,
          tipo.descrizione,
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          dete.stato,
          dete.stato_firma,
          dete.stato_conservazione,
          dete.stato_odg,
          so4_unita.descrizione,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          dete.ente,
          dete.riservato,
          iter.data_fine
     FROM determine_soggetti ds_uoprop,
          determine dete,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_determina tipo
    WHERE     dete.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_iter = iter.id_engine_iter
          AND tipo.id_tipo_determina = dete.id_tipo_determina
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step(+)
          -- AND iter.data_fine IS NULL
          -- AND step.data_fine IS NULL
          AND ds_uoprop.id_determina = dete.id_determina
          AND ds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND ds_uoprop.unita_dal = so4_unita.dal
          AND ds_uoprop.unita_ottica = so4_unita.ottica
          AND ds_uoprop.unita_progr = so4_unita.progr
          AND dete.valido = 'Y'
   UNION ALL
   SELECT 'VISTO',
          vp.id_visto_parere,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          dete.id_determina,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          vp.stato,
          vp.stato_firma,
          dete.stato_conservazione,
          dete.stato_odg,
          NULL,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          dete.ente,
          dete.riservato,
          iter.data_fine
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          determine dete,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo
    WHERE     vp.id_determina = dete.id_determina
          AND step.id_engine_iter = iter.id_engine_iter
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step(+)
          --AND iter.data_fine IS NULL
          --AND step.data_fine IS NULL
          AND vp.valido = 'Y'
   UNION ALL
   SELECT 'PARERE',
          vp.id_visto_parere,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          dete.id_proposta_delibera,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          dete.anno_proposta,
          dete.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          dete.oggetto,
          vp.stato,
          vp.stato_firma,
          deli.stato_conservazione,
          dete.stato_odg,
          NULL,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          dete.ente,
          dete.riservato,
          iter.data_fine
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera dete,
          delibere deli,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo
    WHERE     vp.id_proposta_delibera = dete.id_proposta_delibera
          AND step.id_engine_iter = iter.id_engine_iter
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step(+)
          AND deli.id_proposta_delibera(+) = dete.id_proposta_delibera
          -- AND iter.data_fine IS NULL
          -- AND step.data_fine IS NULL
          AND vp.valido = 'Y'
   UNION ALL
   SELECT 'CERTIFICATO',
          cert.id_certificato,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          dete.id_determina,
          NULL,
             'CERTIFICATO DI '
          || DECODE (cert.tipo,
                     'AVVENUTA_PUBBLICAZIONE', 'AVVENUTA PUBBLICAZIONE',
                     cert.tipo),
          '',
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          cert.stato,
          cert.stato_firma,
          dete.stato_conservazione,
          dete.stato_odg,
          NULL,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cert.ente,
          dete.riservato,
          iter.data_fine
     FROM certificati cert,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          determine dete,
          wkf_cfg_step cfg_step
    WHERE     cert.id_determina = dete.id_determina
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step(+)
          -- AND iter.data_fine IS NULL
          -- AND step.data_fine IS NULL
          AND cert.valido = 'Y'
   UNION ALL
   SELECT 'CERTIFICATO',
          cert.id_certificato,
          step.id_engine_iter,
          step.id_engine_step,
          iter.id_step_corrente,
          step.data_inizio,
          cfg_step.titolo,
          dete.id_delibera,
          NULL,
             'CERTIFICATO DI '
          || DECODE (cert.tipo,
                     'AVVENUTA_PUBBLICAZIONE', 'AVVENUTA PUBBLICAZIONE',
                     cert.tipo),
          '',
          p_dete.anno_proposta,
          p_dete.numero_proposta,
          dete.anno_delibera,
          dete.numero_delibera,
          dete.oggetto,
          cert.stato,
          cert.stato_firma,
          dete.stato_conservazione,
          p_dete.stato_odg,
          NULL,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cert.ente,
          p_dete.riservato,
          iter.data_fine
     FROM certificati cert,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          delibere dete,
          proposte_delibera p_dete,
          wkf_cfg_step cfg_step
    WHERE     cert.id_delibera = dete.id_delibera
          AND p_dete.id_proposta_delibera = dete.id_proposta_delibera
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step(+)
          -- AND iter.data_fine IS NULL
          --AND step.data_fine IS NULL
          AND cert.valido = 'Y'
   ORDER BY 2 DESC, 5 DESC
/

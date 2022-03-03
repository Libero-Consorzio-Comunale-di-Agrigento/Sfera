--liquibase formatted sql
--changeset rdestasio:2.5.0.0_20200221_02

CREATE OR REPLACE FORCE VIEW STATISTICHE_DELIBERE
(
   ID_ATTO,
   NUMERO_ATTO,
   ANNO_ATTO,
   REGISTRO,
   DESCRIZIONE_REGISTRO,
   TIPOLOGIA,
   DATA_ADOZIONE,
   DATA_INIZIO,
   DATA_FINE,
   DURATA,
   MESE,
   DESCRIZIONE_MESE,
   ANNO,
   UNITA_PROPONENTE,
   AREA_PROPONENTE,
   SERVIZIO_PROPONENTE,
   SUDDIVISIONE,
   FIRMATARIO,
   STATO
)
AS
   SELECT D.id_delibera,
          numero_delibera,
          anno_delibera,
          registro_delibera,
          tipo.titolo tipologia,
          tp.descrizione DESCRIZIONE_REGISTRO,
          D.DATA_ESECUTIVITA,
          D.data_ins DATA_inizio,
          NVL (d.data_pubblicazione, ITER.data_fine) data_fine,
          TRUNC (NVL (d.data_pubblicazione, ITER.data_fine) - d.data_ins)
             durata,
          EXTRACT (MONTH FROM NVL (d.data_pubblicazione, ITER.data_fine))
             mese,
          TO_CHAR (NVL (d.data_pubblicazione, ITER.data_fine), 'MONTH')
             descrizione_mese,
          EXTRACT (YEAR FROM NVL (d.data_pubblicazione, ITER.data_fine)) anno,
          utility_pkg.get_unita_prop_delibera (d.id_delibera)
             UNITA_PROPONENTE,
          utility_pkg.get_suddivisione_prop_delibera (
             d.id_delibera,
             'SO4_SUDDIVISIONE_AREA',
             d.ente)
             AREA_PROPONENTE,
          utility_pkg.get_suddivisione_prop_delibera (
             d.id_delibera,
             'SO4_SUDDIVISIONE_SERVIZIO',
             d.ente)
             SETTORE,
          utility_pkg.get_suddivisione_uo_prop_deli (d.id_delibera)
             SUDDIVISIONE,
          utility_pkg.get_primo_firmatario_prop_deli (d.id_delibera)
             FIRMATARIO,
          d.stato
     FROM delibere d,
          proposte_delibera prop,
          tipi_registro tp,
          WKF_ENGINE_ITER iter,
          tipi_delibera tipo
    WHERE     D.VALIDO = 'Y'
          AND prop.valido = 'Y'
          AND prop.id_proposta_delibera = d.id_proposta_delibera
          AND tp.tipo_registro(+) = d.registro_delibera
          AND prop.id_tipo_delibera = tipo.id_tipo_delibera
          AND iter.id_engine_iter = D.ID_ENGINE_ITER
          AND (iter.data_fine IS NOT NULL OR D.data_pubblicazione IS NOT NULL)
          AND (D.data_pubblicazione IS NOT NULL OR d.stato = 'ANNULLATO')
/

CREATE OR REPLACE FORCE VIEW AGSDE2.STATISTICHE_DELIBERE_STEP
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
AS
     SELECT deli.id_delibera,
            registro_delibera REGISTRO,
            tp.descrizione DESCRIZIONE_REGISTRO,
            tipologia.titolo tipologia,
            prop.id_tipo_delibera id_tipologia,
            cfg_iter.nome nome_flusso,
            ITER.ID_CFG_ITER id_flusso,
            prop.data_ins DATA_inizio,
            deli.data_pubblicazione data_fine,
            TRUNC (deli.data_pubblicazione - prop.data_ins)
               durata_flusso_giorni,
            TRUNC ( (deli.data_pubblicazione) - prop.data_ins) * 24
               durata_flusso_ore,
            TRUNC ( ( (deli.data_pubblicazione) - prop.data_ins) * 24 * 60)
               durata_flusso_minuti,
            TRUNC (
               ( (deli.data_pubblicazione) - prop.data_ins) * 24 * 60 * 60)
               durata_flusso_secondi,
            TRUNC (step.data_fine - step.data_inizio) durata_step_giorni,
            TRUNC ( (step.data_fine - step.data_inizio) * 24) durata_step_ore,
            TRUNC ( (step.data_fine - step.data_inizio) * 24 * 60)
               durata_step_minuti,
            TRUNC ( (step.data_fine - step.data_inizio) * 24 * 60 * 60)
               durata_step_secondi,
            TRUNC (
                 ( (step.data_fine - step.data_inizio) * 24 * 60 * 60)
               * 100
               / ( ( (deli.data_pubblicazione) - prop.data_ins) * 24 * 60 * 60))
               percentuale,
            EXTRACT (MONTH FROM deli.data_pubblicazione) mese,
            TO_CHAR (deli.data_pubblicazione, 'MONTH') descrizione_mese,
            EXTRACT (YEAR FROM deli.data_pubblicazione) anno,
            step.data_inizio PASSAGGIO_DATA,
            step.data_fine PASSAGGIO_DATA_fine,
            UPPER (cfg_step.titolo) PASSAGGIO_TITOLO,
            NVL (
               utility_pkg.get_uo_descrizione (a_step.unita_progr,
                                               a_step.unita_dal),
               utility_pkg.get_cognome_nome (
                  utility_pkg.get_ni_soggetto (a_step.utente)))
               PASSAGGIO_ATTORE,
            cfg_step.sequenza passaggio_sequenza
       FROM delibere deli,
            proposte_delibera prop,
            wkf_engine_step step,
            wkf_engine_step_attori a_step,
            wkf_engine_iter iter,
            wkf_cfg_step cfg_step,
            tipi_registro tp,
            wkf_cfg_iter cfg_iter,
            tipi_delibera tipologia
      WHERE     iter.id_engine_iter IN
                   (SELECT DISTINCT ds.id_engine_iter
                      FROM delibere_storico ds
                     WHERE     ds.id_engine_iter IS NOT NULL
                           AND ds.id_delibera = deli.id_delibera
                    UNION ALL
                    SELECT DISTINCT ds.id_engine_iter
                      FROM proposte_delibera_storico ds
                     WHERE     ds.id_engine_iter IS NOT NULL
                           AND ds.id_proposta_delibera =
                                  deli.id_proposta_delibera)
            AND prop.id_proposta_delibera = deli.id_proposta_delibera
            AND step.id_engine_iter = iter.id_engine_iter
            AND cfg_step.id_cfg_step = step.id_cfg_step
            AND step.id_engine_step = a_step.id_engine_step(+)
            AND TRUNC ( (step.data_fine - step.data_inizio) * 24 * 60 * 60) > 0
            AND tp.tipo_registro(+) = deli.registro_delibera
            AND ITER.ID_CFG_ITER = cfg_iter.id_cfg_iter
            AND prop.id_tipo_delibera = tipologia.id_tipo_delibera
            AND deli.data_pubblicazione IS NOT NULL
            AND step.data_fine < deli.data_pubblicazione
            AND deli.valido = 'Y'
   ORDER BY deli.id_Delibera DESC, step.id_engine_step DESC
/

CREATE OR REPLACE FORCE VIEW AGSDE2.STATISTICHE_DETERMINE
(
   ID_ATTO,
   NUMERO_ATTO,
   ANNO_ATTO,
   REGISTRO,
   DESCRIZIONE_REGISTRO,
   TIPOLOGIA,
   DATA_ADOZIONE,
   DATA_INIZIO,
   DATA_FINE,
   DURATA,
   MESE,
   DESCRIZIONE_MESE,
   ANNO,
   UNITA_PROPONENTE,
   AREA_PROPONENTE,
   SERVIZIO_PROPONENTE,
   SUDDIVISIONE,
   FIRMATARIO,
   STATO
)
AS
   SELECT D.id_determina,
          numero_determina,
          anno_determina,
          registro_determina,
          tipo.titolo tipologia,
          tp.descrizione DESCRIZIONE_REGISTRO,
          D.DATA_ESECUTIVITA,
          D.data_ins DATA_inizio,
          NVL (d.data_pubblicazione, ITER.data_fine) data_fine,
          TRUNC (NVL (d.data_pubblicazione, ITER.data_fine) - d.data_ins)
             durata,
          EXTRACT (MONTH FROM NVL (d.data_pubblicazione, ITER.data_fine))
             mese,
          TO_CHAR (NVL (d.data_pubblicazione, ITER.data_fine), 'MONTH')
             descrizione_mese,
          EXTRACT (YEAR FROM NVL (d.data_pubblicazione, ITER.data_fine)) anno,
          utility_pkg.get_unita_prop_determina (d.id_determina)
             UNITA_PROPONENTE,
          utility_pkg.get_suddivisione_determina (d.id_determina,
                                                  'SO4_SUDDIVISIONE_AREA',
                                                  d.ente)
             AREA_PROPONENTE,
          utility_pkg.get_suddivisione_determina (
             d.id_determina,
             'SO4_SUDDIVISIONE_SERVIZIO',
             d.ente)
             SETTORE,
          utility_pkg.get_suddivisione_uo_determina (d.id_determina)
             SUDDIVISIONE,
          utility_pkg.get_primo_firmatario_determina (d.id_determina)
             FIRMATARIO,
          d.stato
     FROM determine d,
          tipi_registro tp,
          WKF_ENGINE_ITER iter,
          tipi_determina tipo
    WHERE     D.VALIDO = 'Y'
          AND tp.tipo_registro(+) = d.registro_determina
          AND d.id_tipo_determina = tipo.id_tipo_determina
          AND iter.id_engine_iter = D.ID_ENGINE_ITER
          AND (iter.data_fine IS NOT NULL OR D.data_pubblicazione IS NOT NULL)
          AND (   D.data_pubblicazione IS NOT NULL
               OR d.stato IN ('ANNULLATO', 'NON_ESECUTIVO'))
/

CREATE OR REPLACE FORCE VIEW AGSDE2.STATISTICHE_DETERMINE_STEP
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
AS
     SELECT dete.id_determina,
            registro_determina REGISTRO,
            tp.descrizione DESCRIZIONE_REGISTRO,
            tipologia.titolo tipologia,
            dete.id_tipo_determina id_tipologia,
            cfg_iter.nome nome_flusso,
            ITER.ID_CFG_ITER id_flusso,
            Dete.data_ins DATA_inizio,
            dete.data_pubblicazione data_fine,
            TRUNC (dete.data_pubblicazione - dete.data_ins)
               durata_flusso_giorni,
            TRUNC ( (dete.data_pubblicazione) - dete.data_ins) * 24
               durata_flusso_ore,
            TRUNC ( ( (dete.data_pubblicazione) - dete.data_ins) * 24 * 60)
               durata_flusso_minuti,
            TRUNC (
               ( (dete.data_pubblicazione) - dete.data_ins) * 24 * 60 * 60)
               durata_flusso_secondi,
            TRUNC (step.data_fine - step.data_inizio) durata_step_giorni,
            TRUNC ( (step.data_fine - step.data_inizio) * 24) durata_step_ore,
            TRUNC ( (step.data_fine - step.data_inizio) * 24 * 60)
               durata_step_minuti,
            TRUNC ( (step.data_fine - step.data_inizio) * 24 * 60 * 60)
               durata_step_secondi,
            TRUNC (
                 ( (step.data_fine - step.data_inizio) * 24 * 60 * 60)
               * 100
               / ( ( (dete.data_pubblicazione) - dete.data_ins) * 24 * 60 * 60))
               percentuale,
            EXTRACT (MONTH FROM dete.data_pubblicazione) mese,
            TO_CHAR (dete.data_pubblicazione, 'MONTH') descrizione_mese,
            EXTRACT (YEAR FROM dete.data_pubblicazione) anno,
            step.data_inizio PASSAGGIO_DATA,
            step.data_fine PASSAGGIO_DATA_fine,
            UPPER (cfg_step.titolo) PASSAGGIO_TITOLO,
            NVL (
               utility_pkg.get_uo_descrizione (a_step.unita_progr,
                                               a_step.unita_dal),
               utility_pkg.get_cognome_nome (
                  utility_pkg.get_ni_soggetto (a_step.utente)))
               PASSAGGIO_ATTORE,
            cfg_step.sequenza passaggio_sequenza
       FROM determine dete,
            wkf_engine_step step,
            wkf_engine_step_attori a_step,
            wkf_engine_iter iter,
            wkf_cfg_step cfg_step,
            tipi_registro tp,
            wkf_cfg_iter cfg_iter,
            tipi_determina tipologia
      WHERE     iter.id_engine_iter IN
                   (SELECT DISTINCT ds.id_engine_iter
                      FROM determine_storico ds
                     WHERE     ds.id_engine_iter IS NOT NULL
                           AND ds.id_determina = dete.id_determina)
            AND step.id_engine_iter = iter.id_engine_iter
            AND cfg_step.id_cfg_step = step.id_cfg_step
            AND step.id_engine_step = a_step.id_engine_step(+)
            AND TRUNC ( (step.data_fine - step.data_inizio) * 24 * 60 * 60) > 0
            AND tp.tipo_registro(+) = dete.registro_determina
            AND ITER.ID_CFG_ITER = cfg_iter.id_cfg_iter
            AND dete.id_tipo_determina = tipologia.id_tipo_determina
            AND Dete.data_pubblicazione IS NOT NULL
            AND step.data_fine < dete.data_pubblicazione
            AND dete.valido = 'Y'
   ORDER BY dete.id_determina DESC, step.id_engine_step DESC
/
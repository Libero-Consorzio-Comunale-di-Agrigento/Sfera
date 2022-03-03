--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_20

/* Formatted on 09/02/2016 10:03:36 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_DETERMINE_IN_CORSO
(
   ID_DETERMINA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO_DETERMINA,
   NUMERO_DETERMINA,
   REGISTRO_DETERMINA,
   TIPOLOGIA,
   OGGETTO,
   DATA_ESECUTIVITA,
   STATO,
   STATO_FIRMA,
   POSIZIONE,
   IN_CARICO_A,
   REDATTORE,
   REDATTORE_LOGIN,
   REDATTORE_AD4,
   DIRIGENTE,
   DIRIGENTE_LOGIN,
   DIRIGENTE_AD4,
   FIRMATARIO,
   FIRMATARIO_LOGIN,
   FIRMATARIO_AD4,
   UNITA_PROPONENTE,
   CODICE_UNITA_PROPONENTE,
   ID_TESTO,
   ID_TESTO_GDM,
   ID_TESTO_ODT,
   ID_STAMPA_UNICA,
   ID_STAMPA_UNICA_GDM,
   ID_ALBO,
   ID_ENGINE_ITER,
   ID_ENGINE_STEP,
   ID_CFG_STEP
)
AS
   SELECT d.id_determina,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          d.registro_determina,
          td.titolo,
          d.oggetto,
          d.data_esecutivita,
          d.stato,
          d.stato_firma,
          wcs.nome posizione,
             DECODE (wesa.ruolo,
                     NULL, NULL,
                     'RUOLO SO4: ' || wesa.ruolo || ' ')
          || DECODE (wesa.utente,
                     NULL, NULL,
                     'UTENTE AD4: ' || wesa.utente || ' ')
          || DECODE (so4_unita_wesa.codice,
                     NULL, NULL,
                     'UNITA SO4: ' || so4_unita_wesa.codice || ' ')
             in_carico_a,
          as4_soggetti_red.denominazione redattore,
          ad4_utenti_red.nominativo redattore_login,
          ds_red.utente redattore_ad4,
          as4_soggetti_dir.denominazione dirigente,
          ad4_utenti_dir.nominativo dirigente_login,
          ds_dir.utente dirigente_ad4,
          as4_soggetti_firm.denominazione firmatario,
          ad4_utenti_firm.nominativo firmatario_login,
          f.utente_firmatario firmatario_ad4,
          so4_unita.descrizione unita_proponente,
          so4_unita.codice codice_unita_proponente,
          d.id_file_allegato_testo,
          fa_testo.id_file_esterno,
          d.id_file_allegato_testo_odt,
          d.id_file_allegato_stampa_unica,
          fa_testo_stampa_unica.id_file_esterno,
          d.id_documento_albo,
          d.id_engine_iter,
          wes.id_engine_step,
          wcs.id_cfg_step
     FROM so4_v_unita_organizzative_pubb so4_unita,
          so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_dir,
          ad4_v_utenti ad4_utenti_dir,
          as4_v_soggetti_correnti as4_soggetti_red,
          ad4_v_utenti ad4_utenti_red,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          file_allegati fa_testo,
          file_allegati fa_testo_stampa_unica,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          determine_soggetti ds_uoprop,
          determine_soggetti ds_dir,
          determine_soggetti ds_red,
          determine d,
          tipi_determina td
    WHERE     d.valido = 'Y'
          AND td.id_tipo_determina = d.id_tipo_determina
          AND d.id_file_allegato_testo = fa_testo.id_file_allegato(+)
          AND d.id_file_allegato_stampa_unica =
                 fa_testo_stampa_unica.id_file_allegato(+)
          AND ds_uoprop.id_determina(+) = d.id_determina
          AND ds_uoprop.tipo_soggetto(+) = 'UO_PROPONENTE'
          AND ds_uoprop.unita_dal = so4_unita.dal(+)
          AND ds_uoprop.unita_ottica = so4_unita.ottica(+)
          AND ds_uoprop.unita_progr = so4_unita.progr(+)
          AND ds_dir.id_determina(+) = d.id_determina
          AND ds_dir.tipo_soggetto(+) = 'DIRIGENTE'
          AND ds_dir.utente = as4_soggetti_dir.utente(+)
          AND as4_soggetti_dir.utente = ad4_utenti_dir.utente(+)
          AND ds_red.id_determina(+) = d.id_determina
          AND ds_red.tipo_soggetto(+) = 'REDATTORE'
          AND ds_red.utente = as4_soggetti_red.utente(+)
          AND as4_soggetti_red.utente = ad4_utenti_red.utente(+)
          AND f.id_determina(+) = d.id_determina
          AND f.utente_firmatario = as4_soggetti_firm.utente(+)
          AND as4_soggetti_firm.utente = ad4_utenti_firm.utente(+)
          AND d.id_engine_iter = wes.id_engine_iter
          AND wes.data_fine IS NULL
          AND wes.id_cfg_step = wcs.id_cfg_step
          AND wesa.id_engine_step(+) = wes.id_engine_step
          AND wesa.unita_dal = so4_unita_wesa.dal(+)
          AND wesa.unita_ottica = so4_unita_wesa.ottica(+)
          AND wesa.unita_progr = so4_unita_wesa.progr(+)
/

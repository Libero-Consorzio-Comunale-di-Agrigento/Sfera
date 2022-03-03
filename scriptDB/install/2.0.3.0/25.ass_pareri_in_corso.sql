--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_25

/* Formatted on 09/02/2016 10:03:37 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_PARERI_IN_CORSO
(
   ID_PARERE,
   ID_PROPOSTA_DELIBERA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   VISTO,
   OGGETTO,
   STATO_FIRMA,
   POSIZIONE,
   IN_CARICO_A,
   FIRMATARIO,
   FIRMATARIO_LOGIN,
   FIRMATARIO_AD4,
   DIRIGENTE,
   DIRIGENTE_LOGIN,
   DIRIGENTE_AD4,
   UNITA_PROPONENTE,
   CODICE_UNITA_PROPONENTE,
   ID_TESTO,
   ID_TESTO_GDM,
   ID_TESTO_ODT,
   ID_ENGINE_ITER,
   ID_ENGINE_STEP,
   ID_CFG_STEP
)
AS
   SELECT vp.id_visto_parere,
          pd.id_proposta_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          tvp.titolo,
          pd.oggetto,
          vp.stato_firma,
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
          as4_soggetti_firm.denominazione firmatario,
          ad4_utenti_firm.nominativo firmatario_login,
          f.utente_firmatario firmatario_ad4,
          as4_soggetti_dir.denominazione dirigente,
          ad4_utenti_dir.nominativo dirigente_login,
          vp.utente_firmatario dirigente_ad4,
          so4_unita.descrizione unita_proponente,
          so4_unita.codice codice_unita_proponente,
          vp.id_file_allegato_testo,
          fa_testo.id_file_esterno,
          vp.id_file_allegato_testo_odt,
          vp.id_engine_iter,
          wes.id_engine_step,
          wcs.id_cfg_step
     FROM so4_v_unita_organizzative_pubb so4_unita,
          so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_dir,
          ad4_v_utenti ad4_utenti_dir,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          file_allegati fa_testo,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          visti_pareri vp,
          proposte_delibera pd,
          tipi_visto_parere tvp
    WHERE     vp.valido = 'Y'
          AND vp.id_proposta_delibera = pd.id_proposta_delibera
          AND tvp.id_tipo_visto_parere = vp.id_tipologia
          AND vp.id_file_allegato_testo = fa_testo.id_file_allegato(+)
          AND vp.unita_dal = so4_unita.dal(+)
          AND vp.unita_ottica = so4_unita.ottica(+)
          AND vp.unita_progr = so4_unita.progr(+)
          AND vp.utente_firmatario = as4_soggetti_dir.utente(+)
          AND vp.utente_firmatario = ad4_utenti_dir.utente(+)
          AND f.id_visto_parere(+) = vp.id_visto_parere
          AND f.utente_firmatario = as4_soggetti_firm.utente(+)
          AND as4_soggetti_firm.utente = ad4_utenti_firm.utente(+)
          AND vp.id_engine_iter = wes.id_engine_iter
          AND wes.data_fine IS NULL
          AND wes.id_cfg_step = wcs.id_cfg_step
          AND wesa.id_engine_step(+) = wes.id_engine_step
          AND wesa.unita_dal = so4_unita_wesa.dal(+)
          AND wesa.unita_ottica = so4_unita_wesa.ottica(+)
          AND wesa.unita_progr = so4_unita_wesa.progr(+)
/

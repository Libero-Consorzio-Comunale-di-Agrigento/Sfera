--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_16

/* Formatted on 09/02/2016 10:03:35 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_CERTIFICATI_IN_CORSO
(
   ID_CERTIFICATO,
   CERTIFICATO,
   STATO_FIRMA,
   POSIZIONE,
   IN_CARICO_A,
   FIRMATARIO,
   FIRMATARIO_LOGIN,
   FIRMATARIO_AD4,
   ID_TESTO,
   ID_TESTO_GDM,
   ID_TESTO_ODT,
   ID_ENGINE_ITER,
   ID_ENGINE_STEP,
   ID_CFG_STEP
)
AS
   SELECT c.id_certificato,
          tc.titolo,
          c.stato_firma,
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
          c.id_file_allegato_testo,
          fa_testo.id_file_esterno,
          c.id_file_allegato_testo_odt,
          c.id_engine_iter,
          wes.id_engine_step,
          wcs.id_cfg_step
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          file_allegati fa_testo,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          certificati c,
          tipi_certificato tc
    WHERE     c.valido = 'Y'
          AND c.id_tipologia = tc.id_tipo_certificato
          AND c.id_file_allegato_testo = fa_testo.id_file_allegato(+)
          AND f.id_certificato(+) = c.id_certificato
          AND f.utente_firmatario = as4_soggetti_firm.utente(+)
          AND as4_soggetti_firm.utente = ad4_utenti_firm.utente(+)
          AND c.id_engine_iter = wes.id_engine_iter
          AND wes.data_fine IS NULL
          AND wes.id_cfg_step = wcs.id_cfg_step
          AND wesa.id_engine_step(+) = wes.id_engine_step
          AND wesa.unita_dal = so4_unita_wesa.dal(+)
          AND wesa.unita_ottica = so4_unita_wesa.ottica(+)
          AND wesa.unita_progr = so4_unita_wesa.progr(+)
/

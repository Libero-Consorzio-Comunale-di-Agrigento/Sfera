--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_26

/* Formatted on 09/02/2016 10:03:37 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_PROPOSTE_DELIBERA_IN_CORSO
(
   ID_PROPOSTA_DELIBERA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   TIPOLOGIA,
   OGGETTO,
   STATO,
   STATO_FIRMA,
   COMMISSIONE,
   DATA_SEDUTA,
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
   ID_ENGINE_ITER,
   ID_ENGINE_STEP,
   ID_CFG_STEP
)
AS
   SELECT pd.id_proposta_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          td.titolo,
          pd.oggetto,
          pd.stato,
          pd.stato_firma,
          oc.titolo,
          os.data_seduta,
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
          pd.id_file_allegato_testo,
          fa_testo.id_file_esterno,
          pd.id_file_allegato_testo_odt,
          pd.id_engine_iter,
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
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          proposte_delibera_soggetti ds_uoprop,
          proposte_delibera_soggetti ds_dir,
          proposte_delibera_soggetti ds_red,
          odg_oggetti_seduta oos,
          proposte_delibera pd,
          odg_sedute os,
          odg_commissioni oc,
          tipi_delibera td
    WHERE     pd.valido = 'Y'
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND pd.id_oggetto_seduta = oos.id_oggetto_seduta(+)
          AND oos.id_seduta = os.id_seduta(+)
          AND os.id_commissione = oc.id_commissione(+)
          AND pd.id_file_allegato_testo = fa_testo.id_file_allegato(+)
          AND ds_uoprop.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND ds_uoprop.tipo_soggetto(+) = 'UO_PROPONENTE'
          AND ds_uoprop.unita_dal = so4_unita.dal(+)
          AND ds_uoprop.unita_ottica = so4_unita.ottica(+)
          AND ds_uoprop.unita_progr = so4_unita.progr(+)
          AND ds_dir.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND ds_dir.tipo_soggetto(+) = 'DIRIGENTE'
          AND ds_dir.utente = as4_soggetti_dir.utente(+)
          AND as4_soggetti_dir.utente = ad4_utenti_dir.utente(+)
          AND ds_red.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND ds_red.tipo_soggetto(+) = 'REDATTORE'
          AND ds_red.utente = as4_soggetti_red.utente(+)
          AND as4_soggetti_red.utente = ad4_utenti_red.utente(+)
          AND f.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND f.utente_firmatario = as4_soggetti_firm.utente(+)
          AND as4_soggetti_firm.utente = ad4_utenti_firm.utente(+)
          AND pd.id_engine_iter = wes.id_engine_iter
          AND wes.data_fine IS NULL
          AND wes.id_cfg_step = wcs.id_cfg_step
          AND wesa.id_engine_step(+) = wes.id_engine_step
          AND wesa.unita_dal = so4_unita_wesa.dal(+)
          AND wesa.unita_ottica = so4_unita_wesa.ottica(+)
          AND wesa.unita_progr = so4_unita_wesa.progr(+)
/

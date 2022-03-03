--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_09

/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
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


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_DELIBERE_FINITE
(
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO_DELIBERA,
   NUMERO_DELIBERA,
   REGISTRO_DELIBERA,
   TIPOLOGIA,
   OGGETTO,
   DATA_ESECUTIVITA,
   STATO,
   COMMISSIONE,
   DATA_SEDUTA,
   REDATTORE,
   REDATTORE_LOGIN,
   REDATTORE_AD4,
   DIRIGENTE,
   DIRIGENTE_LOGIN,
   DIRIGENTE_AD4,
   PRESIDENTE,
   PRESIDENTE_LOGIN,
   PRESIDENTE_AD4,
   SEGRETARIO,
   SEGRETARIO_LOGIN,
   SEGRETARIO_AD4,
   UNITA_PROPONENTE,
   CODICE_UNITA_PROPONENTE,
   ID_TESTO,
   ID_TESTO_GDM,
   ID_TESTO_ODT,
   ID_STAMPA_UNICA,
   ID_STAMPA_UNICA_GDM,
   ID_ALBO,
   ID_ENGINE_ITER
)
AS
   SELECT d.id_delibera,
          pd.id_proposta_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          d.registro_delibera,
          td.titolo,
          d.oggetto,
          d.data_esecutivita,
          d.stato,
          oc.titolo,
          os.data_seduta,
          as4_soggetti_red.denominazione redattore,
          ad4_utenti_red.nominativo redattore_login,
          ds_red.utente redattore_ad4,
          as4_soggetti_dir.denominazione dirigente,
          ad4_utenti_dir.nominativo dirigente_login,
          ds_dir.utente dirigente_ad4,
          as4_soggetti_pre.denominazione presidente,
          ad4_utenti_pre.nominativo presidente_login,
          ds_pre.utente presidente_ad4,
          as4_soggetti_seg.denominazione segretario,
          ad4_utenti_seg.nominativo segretario_login,
          ds_seg.utente segretario_ad4,
          so4_unita.descrizione unita_proponente,
          so4_unita.codice codice_unita_proponente,
          d.id_file_allegato_testo,
          fa_testo.id_file_esterno,
          d.id_file_allegato_testo_odt,
          d.id_file_allegato_stampa_unica,
          fa_testo_stampa_unica.id_file_esterno,
          d.id_documento_albo,
          d.id_engine_iter
     FROM so4_v_unita_organizzative_pubb so4_unita,
          as4_v_soggetti_correnti as4_soggetti_dir,
          ad4_v_utenti ad4_utenti_dir,
          as4_v_soggetti_correnti as4_soggetti_red,
          ad4_v_utenti ad4_utenti_red,
          as4_v_soggetti_correnti as4_soggetti_seg,
          ad4_v_utenti ad4_utenti_seg,
          as4_v_soggetti_correnti as4_soggetti_pre,
          ad4_v_utenti ad4_utenti_pre,
          file_allegati fa_testo,
          file_allegati fa_testo_stampa_unica,
          wkf_engine_iter wei,
          proposte_delibera_soggetti ds_uoprop,
          proposte_delibera_soggetti ds_dir,
          proposte_delibera_soggetti ds_red,
          delibere_soggetti ds_pre,
          delibere_soggetti ds_seg,
          odg_oggetti_seduta oos,
          proposte_delibera pd,
          delibere d,
          odg_sedute os,
          odg_commissioni oc,
          tipi_delibera td
    WHERE     d.valido = 'Y'
          AND d.id_proposta_delibera = pd.id_proposta_delibera
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND d.id_oggetto_seduta = oos.id_oggetto_seduta
          AND oos.id_seduta = os.id_seduta
          AND os.id_commissione = oc.id_commissione
          AND d.id_file_allegato_testo = fa_testo.id_file_allegato(+)
          AND d.id_file_allegato_stampa_unica =
                 fa_testo_stampa_unica.id_file_allegato(+)
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
          AND ds_pre.id_delibera(+) = d.id_delibera
          AND ds_pre.tipo_soggetto(+) = 'PRESIDENTE'
          AND ds_pre.utente = as4_soggetti_pre.utente(+)
          AND as4_soggetti_pre.utente = ad4_utenti_pre.utente(+)
          AND ds_seg.id_delibera(+) = d.id_delibera
          AND ds_seg.tipo_soggetto(+) = 'SEGRETARIO'
          AND ds_seg.utente = as4_soggetti_seg.utente(+)
          AND as4_soggetti_seg.utente = ad4_utenti_seg.utente(+)
          AND d.id_engine_iter = wei.id_engine_iter
          AND wei.data_fine IS NOT NULL
/


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_DELIBERE_IN_CORSO
(
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO_DELIBERA,
   NUMERO_DELIBERA,
   REGISTRO_DELIBERA,
   TIPOLOGIA,
   OGGETTO,
   DATA_ESECUTIVITA,
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
   PRESIDENTE,
   PRESIDENTE_LOGIN,
   PRESIDENTE_AD4,
   SEGRETARIO,
   SEGRETARIO_LOGIN,
   SEGRETARIO_AD4,
   FIRMATARIO,
   SEQUENZA_FIRMA,
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
   SELECT d.id_delibera,
          pd.id_proposta_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          d.registro_delibera,
          td.titolo,
          d.oggetto,
          d.data_esecutivita,
          d.stato,
          d.stato_firma,
          oc.titolo,
          os.data_seduta,
          DECODE (d.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome)
             posizione,
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
          as4_soggetti_pre.denominazione presidente,
          ad4_utenti_pre.nominativo presidente_login,
          ds_pre.utente presidente_ad4,
          as4_soggetti_seg.denominazione segretario,
          ad4_utenti_seg.nominativo segretario_login,
          ds_seg.utente segretario_ad4,
          as4_soggetti_firm.denominazione firmatario,
          f.sequenza,
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
          as4_v_soggetti_correnti as4_soggetti_seg,
          ad4_v_utenti ad4_utenti_seg,
          as4_v_soggetti_correnti as4_soggetti_pre,
          ad4_v_utenti ad4_utenti_pre,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          file_allegati fa_testo,
          file_allegati fa_testo_stampa_unica,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          proposte_delibera_soggetti ds_uoprop,
          proposte_delibera_soggetti ds_dir,
          proposte_delibera_soggetti ds_red,
          delibere_soggetti ds_pre,
          delibere_soggetti ds_seg,
          odg_oggetti_seduta oos,
          proposte_delibera pd,
          delibere d,
          odg_sedute os,
          odg_commissioni oc,
          tipi_delibera td
    WHERE     d.valido = 'Y'
          AND d.id_proposta_delibera = pd.id_proposta_delibera
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND d.id_oggetto_seduta = oos.id_oggetto_seduta
          AND oos.id_seduta = os.id_seduta
          AND os.id_commissione = oc.id_commissione
          AND d.id_file_allegato_testo = fa_testo.id_file_allegato(+)
          AND d.id_file_allegato_stampa_unica =
                 fa_testo_stampa_unica.id_file_allegato(+)
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
          AND ds_pre.id_delibera(+) = d.id_delibera
          AND ds_pre.tipo_soggetto(+) = 'PRESIDENTE'
          AND ds_pre.utente = as4_soggetti_pre.utente(+)
          AND as4_soggetti_pre.utente = ad4_utenti_pre.utente(+)
          AND ds_seg.id_delibera(+) = d.id_delibera
          AND ds_seg.tipo_soggetto(+) = 'SEGRETARIO'
          AND ds_seg.utente = as4_soggetti_seg.utente(+)
          AND as4_soggetti_seg.utente = ad4_utenti_seg.utente(+)
          AND f.id_delibera(+) = d.id_delibera
          AND f.utente_firmatario = as4_soggetti_firm.utente(+)
          AND as4_soggetti_firm.utente = ad4_utenti_firm.utente(+)
          AND d.id_engine_iter = wes.id_engine_iter(+)
          AND wes.data_fine IS NULL
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wesa.id_engine_step(+) = wes.id_engine_step
          AND wesa.unita_dal = so4_unita_wesa.dal(+)
          AND wesa.unita_ottica = so4_unita_wesa.ottica(+)
          AND wesa.unita_progr = so4_unita_wesa.progr(+)
/


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_DETERMINE_FINITE
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
   ID_ENGINE_ITER
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
          d.id_engine_iter
     FROM so4_v_unita_organizzative_pubb so4_unita,
          as4_v_soggetti_correnti as4_soggetti_dir,
          ad4_v_utenti ad4_utenti_dir,
          as4_v_soggetti_correnti as4_soggetti_red,
          ad4_v_utenti ad4_utenti_red,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          file_allegati fa_testo,
          file_allegati fa_testo_stampa_unica,
          wkf_engine_iter wei,
          firmatari f,
          determine_soggetti ds_uoprop,
          determine_soggetti ds_dir,
          determine_soggetti ds_red,
          determine d,
          tipi_determina td
    WHERE     d.numero_determina IS NOT NULL
          AND d.valido = 'Y'
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
          AND d.id_engine_iter = wei.id_engine_iter
          AND wei.data_fine IS NOT NULL
/


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
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


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_DOCUMENTI
(
   TIPO_OGGETTO,
   ID,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   TIPOLOGIA,
   OGGETTO,
   STATO,
   ENTE
)
AS
   SELECT 'DETERMINA',
          d.id_determina,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          td.titolo,
          d.oggetto,
          DECODE (d.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          determine d,
          wkf_cfg_step wcs,
          tipi_determina td
    WHERE     d.valido = 'Y'
          AND td.id_tipo_determina = d.id_tipo_determina
          AND d.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'PROPOSTA_DELIBERA',
          pd.id_proposta_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          td.titolo,
          pd.oggetto,
          DECODE (pd.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          pd.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          proposte_delibera pd,
          delibere d,
          wkf_cfg_step wcs,
          tipi_delibera td
    WHERE     pd.valido = 'Y'
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND pd.id_proposta_delibera = d.id_proposta_delibera(+)
          AND d.valido(+) = 'Y'
          AND pd.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'DELIBERA',
          d.id_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          td.titolo,
          d.oggetto,
          DECODE (d.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          proposte_delibera pd,
          delibere d,
          wkf_cfg_step wcs,
          tipi_delibera td
    WHERE     pd.valido = 'Y'
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND pd.id_proposta_delibera = d.id_proposta_delibera
          AND d.valido = 'Y'
          AND d.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'VISTO',
          v.id_visto_parere,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          tvp.titolo,
          d.oggetto,
          DECODE (v.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          visti_pareri v,
          determine d,
          wkf_cfg_step wcs,
          tipi_visto_parere tvp
    WHERE     v.valido = 'Y'
          AND v.id_determina = d.id_determina
          AND d.valido = 'Y'
          AND tvp.id_tipo_visto_parere = v.id_tipologia
          AND v.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'PARERE',
          v.id_visto_parere,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          tvp.titolo,
          pd.oggetto,
          DECODE (v.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          visti_pareri v,
          proposte_delibera pd,
          delibere d,
          wkf_cfg_step wcs,
          tipi_visto_parere tvp
    WHERE     v.valido = 'Y'
          AND v.id_proposta_delibera = pd.id_proposta_delibera
          AND pd.valido = 'Y'
          AND d.valido(+) = 'Y'
          AND tvp.id_tipo_visto_parere = v.id_tipologia
          AND pd.id_proposta_delibera = d.id_proposta_delibera(+)
          AND v.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'PARERE',
          v.id_visto_parere,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          tvp.titolo,
          pd.oggetto,
          DECODE (v.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          visti_pareri v,
          proposte_delibera pd,
          delibere d,
          wkf_cfg_step wcs,
          tipi_visto_parere tvp
    WHERE     v.valido = 'Y'
          AND v.id_proposta_delibera IS NULL
          AND v.id_delibera = d.id_delibera
          AND pd.valido = 'Y'
          AND d.valido = 'Y'
          AND tvp.id_tipo_visto_parere = v.id_tipologia
          AND pd.id_proposta_delibera = d.id_proposta_delibera
          AND v.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'CERTIFICATO',
          c.id_certificato,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          tc.titolo,
          d.oggetto,
          DECODE (c.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          certificati c,
          determine d,
          wkf_cfg_step wcs,
          tipi_certificato tc
    WHERE     c.valido = 'Y'
          AND c.id_determina = d.id_determina
          AND d.valido = 'Y'
          AND tc.id_tipo_certificato = c.id_tipologia
          AND c.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
   UNION ALL
   SELECT 'CERTIFICATO',
          c.id_certificato,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          tc.titolo,
          d.oggetto,
          DECODE (c.id_engine_iter,
                  NULL, 'ITER NON ANCORA ATTIVATO',
                  wcs.nome),
          d.ente
     FROM wkf_engine_step wes,
          wkf_engine_iter wei,
          certificati c,
          proposte_delibera pd,
          delibere d,
          wkf_cfg_step wcs,
          tipi_certificato tc
    WHERE     c.valido = 'Y'
          AND c.id_delibera = d.id_delibera
          AND d.id_proposta_delibera = pd.id_proposta_delibera
          AND d.valido = 'Y'
          AND pd.valido = 'Y'
          AND tc.id_tipo_certificato = c.id_tipologia
          AND c.id_engine_iter = wei.id_engine_iter(+)
          AND wei.id_step_corrente = wes.id_engine_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
          AND wes.id_cfg_step = wcs.id_cfg_step(+)
/


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
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


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_ERRORE_FIRMA
(
   TIPO_OGGETTO,
   ID_DOCUMENTO,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO_DETERMINA,
   NUMERO_DETERMINA,
   REGISTRO_DETERMINA,
   TIPOLOGIA,
   OGGETTO,
   STATO_FIRMA,
   POSIZIONE,
   IN_CARICO_A,
   FIRMATARIO,
   FIRMATARIO_LOGIN,
   FIRMATARIO_AD4
)
AS
   SELECT 'DETERMINA',
          d.id_determina,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          d.registro_determina,
          td.titolo,
          d.oggetto,
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
          as4_soggetti_firm.denominazione firmatario,
          ad4_utenti_firm.nominativo firmatario_login,
          f.utente_firmatario firmatario_ad4
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          determine d,
          tipi_determina td
    WHERE     d.valido = 'Y'
          AND d.stato_firma IN ('IN_FIRMA', 'FIRMATO_DA_SBLOCCARE')
          AND td.id_tipo_determina = d.id_tipo_determina
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
   UNION
   SELECT 'PROPOSTA_DELIBERA',
          pd.id_proposta_delibera,
          pd.anno_proposta,
          pd.numero_proposta,
          NULL,
          NULL,
          NULL,
          td.titolo,
          pd.oggetto,
          pd.stato_firma,
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
          f.utente_firmatario firmatario_ad4
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          proposte_delibera pd,
          tipi_delibera td
    WHERE     pd.valido = 'Y'
          AND pd.stato_firma IN ('IN_FIRMA', 'FIRMATO_DA_SBLOCCARE')
          AND td.id_tipo_delibera = pd.id_tipo_delibera
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
   UNION
   SELECT 'VISTO',
          vp.id_visto_parere,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          d.registro_determina,
          tvp.titolo,
          d.oggetto,
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
          f.utente_firmatario firmatario_ad4
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          visti_pareri vp,
          determine d,
          tipi_visto_parere tvp
    WHERE     vp.valido = 'Y'
          AND vp.stato_firma IN ('IN_FIRMA', 'FIRMATO_DA_SBLOCCARE')
          AND tvp.id_tipo_visto_parere = vp.id_tipologia
          AND vp.id_determina = d.id_determina
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
   UNION
   SELECT 'PARERE',
          vp.id_visto_parere,
          pd.anno_proposta,
          pd.numero_proposta,
          NULL,
          NULL,
          NULL,
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
          f.utente_firmatario firmatario_ad4
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          visti_pareri vp,
          proposte_delibera pd,
          tipi_visto_parere tvp
    WHERE     vp.valido = 'Y'
          AND vp.stato_firma IN ('IN_FIRMA', 'FIRMATO_DA_SBLOCCARE')
          AND tvp.id_tipo_visto_parere = vp.id_tipologia
          AND vp.id_proposta_delibera = pd.id_proposta_delibera
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
   UNION
   SELECT 'CERTIFICATO_DETERMINA',
          c.id_certificato,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          d.registro_determina,
          tc.titolo,
          d.oggetto,
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
          f.utente_firmatario firmatario_ad4
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          certificati c,
          determine d,
          tipi_certificato tc
    WHERE     c.valido = 'Y'
          AND c.stato_firma IN ('IN_FIRMA', 'FIRMATO_DA_SBLOCCARE')
          AND tc.id_tipo_certificato = c.id_tipologia
          AND c.id_determina = d.id_determina
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
   UNION
   SELECT 'CERTIFICATO_DELIBERA',
          c.id_certificato,
          pd.anno_proposta,
          pd.numero_proposta,
          d.anno_delibera,
          d.numero_delibera,
          d.registro_delibera,
          tc.titolo,
          d.oggetto,
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
          f.utente_firmatario firmatario_ad4
     FROM so4_v_unita_organizzative_pubb so4_unita_wesa,
          as4_v_soggetti_correnti as4_soggetti_firm,
          ad4_v_utenti ad4_utenti_firm,
          wkf_engine_step_attori wesa,
          wkf_engine_step wes,
          wkf_cfg_step wcs,
          firmatari f,
          certificati c,
          proposte_delibera pd,
          delibere d,
          tipi_certificato tc
    WHERE     c.valido = 'Y'
          AND c.stato_firma IN ('IN_FIRMA', 'FIRMATO_DA_SBLOCCARE')
          AND tc.id_tipo_certificato = c.id_tipologia
          AND c.id_delibera = d.id_delibera
          AND d.id_proposta_delibera = pd.id_proposta_delibera
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


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_FILE_ALLEGATI_VUOTI
(
   ID_ALLEGATO,
   DESCRIZIONE,
   NOME,
   ID_PROPOSTA_DELIBERA,
   ID_DELIBERA,
   ID_DETERMINA
)
AS
   SELECT a.id_allegato,
          a.descrizione,
          F.NOME,
          a.id_proposta_delibera,
          a.id_delibera,
          a.id_determina
     FROM allegati a, allegati_file af, file_allegati f
    WHERE     A.ID_ALLEGATO = af.id_allegato
          AND f.id_file_allegato = af.id_file
          AND (   NOT EXISTS
                     (SELECT '1'
                        FROM gdm_oggetti_file o
                       WHERE O.ID_OGGETTO_FILE = F.ID_FILE_ESTERNO)
               OR EXISTS
                     (SELECT '1'
                        FROM gdm_oggetti_file o
                       WHERE     O.ID_OGGETTO_FILE = F.ID_FILE_ESTERNO
                             AND NVL (DBMS_LOB.getlength (testoocr), 0) < 1))
/


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
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


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
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


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_SO4_SOGG_UTENTI_RUOLI_UO
(
   AMMINISTRAZIONE,
   OTTICA,
   PROGR_UO,
   CODICE_UO,
   DESC_UO,
   RUOLO,
   UTENTE,
   UTENTE_LOGIN,
   SOGGETTO,
   UO_DAL,
   UO_AL,
   COMP_DAL,
   COMP_AL,
   RUOLO_DAL,
   RUOLO_AL
)
AS
   SELECT uo.amministrazione,
          uo.ottica,
          uo.progr progr_uo,
          uo.codice codice_uo,
          uo.descrizione desc_uo,
          rc.ruolo ruolo,
          s.utente utente,
          au.nominativo utente_login,
          s.nome || ' ' || s.cognome soggetto,
          uo.dal uo_dal,
          uo.al uo_al,
          c.dal comp_dal,
          c.al comp_al,
          rc.dal ruolo_dal,
          rc.al ruolo_al
     FROM so4_v_componenti_pubb c,
          so4_v_unita_organizzative_pubb uo,
          as4_v_soggetti_correnti s,
          so4_v_ruoli_componente_pubb rc,
          ad4_utenti au
    WHERE     uo.progr = c.progr_unita
          AND s.ni = c.id_soggetto
          AND rc.id_componente = c.id_componente
          AND s.utente = au.utente(+)
/


/* Formatted on 09/07/2015 12:47:40 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_VISTI_IN_CORSO
(
   ID_VISTO,
   ID_DETERMINA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO_DETERMINA,
   NUMERO_DETERMINA,
   REGISTRO_DETERMINA,
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
          d.id_determina,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          d.registro_determina,
          tvp.titolo,
          d.oggetto,
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
          determine d,
          tipi_visto_parere tvp
    WHERE     vp.valido = 'Y'
          AND vp.id_determina = d.id_determina
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

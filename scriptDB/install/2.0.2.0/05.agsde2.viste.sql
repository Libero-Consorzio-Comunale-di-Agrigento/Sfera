--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_05

/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW DOCUMENTI_COMPETENZE
(
   TIPO_OGGETTO,
   ID_DOCUMENTO,
   ID_PADRE,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   ID_FILE_ALLEGATO_TESTO,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   STEP_TITOLO,
   STATO,
   ENTE,
   RISERVATO,
   ID_DETERMINA,
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ID_VISTO,
   ID_PARERE,
   ID_CERTIFICATO
)
AS
   SELECT 'DETERMINA',
          dete.id_determina,
          NULL,
          tipo.id_tipo_determina,
          tipo.titolo,
          tipo.descrizione,
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          dete.id_file_allegato_testo,
          comp.id_determina_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (dete.stato,
                     'ANNULLATO', dete.stato,
                     NVL (cfg_step.titolo, dete.stato)))
             stato,
          dete.ente,
          dete.riservato,
          dete.id_determina id_determina,
          NULL id_delibera,
          NULL id_proposta_delibera,
          NULL id_visto,
          NULL id_parere,
          NULL id_certificato
     FROM determine_competenze comp,
          determine dete,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          wkf_cfg_step cfg_step,
          tipi_determina tipo
    WHERE     dete.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND comp.id_determina = dete.id_determina
          AND tipo.id_tipo_determina = dete.id_tipo_determina
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND dete.valido = 'Y'
   UNION ALL
   SELECT 'VISTO',
          vp.id_visto_parere,
          dete.id_determina,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          vp.id_file_allegato_testo,
          comp.id_visto_parere_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (dete.stato,
                     'ANNULLATO', vp.stato,
                     NVL (cfg_step.titolo, vp.stato)))
             stato,
          vp.ente,
          dete.riservato,
          dete.id_determina id_determina,
          NULL id_delibera,
          NULL id_proposta_delibera,
          vp.id_visto_parere id_visto,
          NULL id_parere,
          NULL id_certificato
     FROM visti_pareri_competenze comp,
          visti_pareri vp,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          determine dete,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo
    WHERE     vp.id_determina = dete.id_determina
          AND comp.id_visto_parere = vp.id_visto_parere
          AND step.id_engine_step = iter.id_step_corrente
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step
          AND vp.valido = 'Y'
   UNION ALL
   SELECT 'PARERE',                       -- pareri della proposta di delibera
          vp.id_visto_parere,
          dete.id_delibera,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          p_deli.anno_proposta,
          p_deli.numero_proposta,
          dete.anno_delibera,
          dete.numero_delibera,
          p_deli.oggetto,
          p_deli.id_file_allegato_testo,
          comp.id_visto_parere_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (p_deli.stato,
                     'ANNULLATO', vp.stato,
                     NVL (cfg_step.titolo, vp.stato)))
             stato,
          vp.ente,
          p_deli.riservato,
          NULL id_determina,
          dete.id_delibera id_delibera,
          p_deli.id_proposta_delibera id_proposta_delibera,
          NULL id_visto,
          vp.id_visto_parere id_parere,
          NULL id_certificato
     FROM visti_pareri_competenze comp,
          visti_pareri vp,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          delibere dete,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo,
          proposte_delibera p_deli
    WHERE     vp.id_proposta_delibera = p_deli.id_proposta_delibera
          AND comp.id_visto_parere = vp.id_visto_parere
          AND p_deli.id_proposta_delibera = dete.id_proposta_delibera(+)
          AND step.id_engine_step = iter.id_step_corrente
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step
          AND vp.valido = 'Y'
   UNION ALL
   SELECT 'PARERE',                                   -- pareri della delibera
          vp.id_visto_parere,
          dete.id_delibera,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          p_deli.anno_proposta,
          p_deli.numero_proposta,
          dete.anno_delibera,
          dete.numero_delibera,
          p_deli.oggetto,
          p_deli.id_file_allegato_testo,
          comp.id_visto_parere_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (dete.stato,
                     'ANNULLATO', vp.stato,
                     NVL (cfg_step.titolo, vp.stato)))
             stato,
          vp.ente,
          p_deli.riservato,
          NULL id_determina,
          dete.id_delibera id_delibera,
          p_deli.id_proposta_delibera id_proposta_delibera,
          NULL id_visto,
          vp.id_visto_parere id_parere,
          NULL id_certificato
     FROM visti_pareri_competenze comp,
          visti_pareri vp,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          delibere dete,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo,
          proposte_delibera p_deli
    WHERE     vp.id_delibera = dete.id_delibera
          AND comp.id_visto_parere = vp.id_visto_parere
          AND p_deli.id_proposta_delibera = dete.id_proposta_delibera(+)
          AND step.id_engine_step = iter.id_step_corrente
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step
          AND vp.valido = 'Y'
   UNION ALL
   SELECT 'CERTIFICATO',
          cert.id_certificato,
          dete.id_determina,
          NULL,
          '',
          '',
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          cert.id_file_allegato_testo,
          comp.id_certificato_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (dete.stato,
                     'ANNULLATO', cert.stato,
                     NVL (cfg_step.titolo, cert.stato)))
             stato,
          cert.ente,
          dete.riservato,
          dete.id_determina id_determina,
          NULL id_delibera,
          NULL id_proposta_delibera,
          NULL id_visto,
          NULL id_parere,
          cert.id_certificato id_certificato
     FROM certificati_competenze comp,
          certificati cert,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          determine dete,
          wkf_cfg_step cfg_step
    WHERE     comp.id_certificato = cert.id_certificato
          AND cert.id_determina = dete.id_determina
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND cert.valido = 'Y'
          AND dete.valido = 'Y'
   UNION ALL
   SELECT 'CERTIFICATO',
          cert.id_certificato,
          dete.id_delibera,
          NULL,
          '',
          '',
          p_deli.anno_proposta,
          p_deli.numero_proposta,
          dete.anno_delibera,
          dete.numero_delibera,
          dete.oggetto,
          cert.id_file_allegato_testo,
          comp.id_certificato_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (dete.stato,
                     'ANNULLATO', cert.stato,
                     NVL (cfg_step.titolo, cert.stato)))
             stato,
          cert.ente,
          p_deli.riservato,
          NULL id_determina,
          dete.id_delibera id_delibera,
          p_deli.id_proposta_delibera id_proposta_delibera,
          NULL id_visto,
          NULL id_parere,
          cert.id_certificato id_certificato
     FROM certificati_competenze comp,
          certificati cert,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          delibere dete,
          wkf_cfg_step cfg_step,
          proposte_delibera p_deli
    WHERE     comp.id_certificato = cert.id_certificato
          AND cert.id_delibera = dete.id_delibera
          AND p_deli.id_proposta_delibera = dete.id_proposta_delibera
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND cert.valido = 'Y'
          AND dete.valido = 'Y'
   UNION ALL
   SELECT 'DELIBERA',
          deli.id_delibera,
          NULL,
          tipo.id_tipo_delibera,
          tipo.titolo,
          tipo.descrizione,
          propdeli.anno_proposta,
          propdeli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          deli.oggetto,
          deli.id_file_allegato_testo,
          comp.id_delibera_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (deli.stato,
                     'ANNULLATO', deli.stato,
                     NVL (cfg_step.titolo, deli.stato)))
             stato,
          deli.ente,
          propdeli.riservato,
          NULL id_determina,
          deli.id_delibera id_delibera,
          propdeli.id_proposta_delibera id_proposta_delibera,
          NULL id_visto,
          NULL id_parere,
          NULL id_certificato
     FROM delibere_competenze comp,
          delibere deli,
          proposte_delibera propdeli,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo
    WHERE     deli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND comp.id_delibera = deli.id_delibera
          AND tipo.id_tipo_delibera = propdeli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND deli.id_proposta_delibera = propdeli.id_proposta_delibera
          AND step.id_engine_step = a_step.id_engine_step
          AND deli.valido = 'Y'
          AND propdeli.valido = 'Y'
   UNION ALL
   SELECT 'PROPOSTA_DELIBERA',
          propdeli.id_proposta_delibera,
          NULL,
          tipo.id_tipo_delibera,
          tipo.titolo,
          tipo.descrizione,
          propdeli.anno_proposta,
          propdeli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          propdeli.oggetto,
          propdeli.id_file_allegato_testo,
          comp.id_proposta_delibera_comp,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          UPPER (
             DECODE (propdeli.stato,
                     'ANNULLATO', propdeli.stato,
                     NVL (cfg_step.titolo, propdeli.stato)))
             stato,
          propdeli.ente,
          propdeli.riservato,
          NULL id_determina,
          deli.id_delibera id_delibera,
          propdeli.id_proposta_delibera id_proposta_delibera,
          NULL id_visto,
          NULL id_parere,
          NULL id_certificato
     FROM proposte_delibera_competenze comp,
          proposte_delibera propdeli,
          delibere deli,
          wkf_engine_step_attori a_step,
          wkf_engine_step step,
          wkf_engine_iter iter,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo
    WHERE     propdeli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND comp.id_proposta_delibera = propdeli.id_proposta_delibera
          AND tipo.id_tipo_delibera = propdeli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step(+)
          AND deli.id_proposta_delibera(+) = propdeli.id_proposta_delibera
          AND deli.id_delibera IS NULL
          AND deli.valido(+) = 'Y'
          AND propdeli.valido = 'Y'
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW DOCUMENTI_COMPETENZE_TESTO
(
   TIPO_OGGETTO,
   ID_DOCUMENTO,
   ID_PADRE,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   ID_FILE_ALLEGATO_TESTO,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   STEP_TITOLO,
   STATO,
   ENTE,
   RISERVATO,
   ID_DETERMINA,
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ID_VISTO,
   ID_PARERE,
   ID_CERTIFICATO,
   TESTO
)
AS
   SELECT comp.TIPO_OGGETTO,
          comp.ID_DOCUMENTO,
          comp.ID_PADRE,
          comp.ID_TIPOLOGIA,
          comp.TITOLO_TIPOLOGIA,
          comp.DESCRIZIONE_TIPOLOGIA,
          comp.ANNO_PROPOSTA,
          comp.NUMERO_PROPOSTA,
          comp.ANNO,
          comp.NUMERO,
          comp.OGGETTO,
          comp.ID_FILE_ALLEGATO_TESTO,
          comp.ID_COMPETENZA,
          comp.COMP_UTENTE,
          comp.COMP_UNITA_PROGR,
          comp.COMP_UNITA_DAL,
          comp.COMP_UNITA_OTTICA,
          comp.COMP_RUOLO,
          comp.COMP_LETTURA,
          comp.COMP_MODIFICA,
          comp.COMP_CANCELLAZIONE,
          comp.ID_STEP,
          comp.STEP_UTENTE,
          comp.STEP_UNITA_PROGR,
          comp.STEP_UNITA_DAL,
          comp.STEP_UNITA_OTTICA,
          comp.STEP_RUOLO,
          comp.STEP_NOME,
          comp.STEP_DESCRIZIONE,
          comp.STEP_TITOLO,
          comp.STATO,
          comp.ENTE,
          comp.RISERVATO,
          comp.ID_DETERMINA,
          comp.ID_DELIBERA,
          comp.ID_PROPOSTA_DELIBERA,
          comp.ID_VISTO,
          comp.ID_PARERE,
          comp.ID_CERTIFICATO,
          fileAllegati.testo
     FROM DOCUMENTI_COMPETENZE comp, file_allegati fileAllegati
    WHERE fileAllegati.id_file_allegato(+) = comp.id_file_allegato_testo
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW DOCUMENTI_STEP
(
   TIPO_OGGETTO,
   ID_DOCUMENTO,
   ID_DETERMINA,
   ID_PROPOSTA_DELIBERA,
   ID_DELIBERA,
   ID_VISTO_PARERE,
   ID_CERTIFICATO,
   ID_PADRE,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
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
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   STEP_TITOLO,
   ENTE,
   RISERVATO,
   TIPO_REGISTRO
)
AS
   SELECT 'DELIBERA',
          deli.id_delibera,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          deli.id_delibera,                                    -- ID_DELIBERA,
          NULL,                                            -- ID_VISTO_PARERE,
          NULL,                                             -- ID_CERTIFICATO,
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
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          pr_deli.ente,
          pr_deli.riservato,
          deli.registro_delibera
     FROM proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo,
          tipi_registro registro
    WHERE     deli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND deli.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND deli.registro_delibera = registro.tipo_registro
          AND pr_deli.valido = 'Y'
          AND deli.valido = 'Y'
   UNION ALL
   SELECT 'PROPOSTA_DELIBERA',
          pr_deli.id_proposta_delibera,
          NULL,                                               -- ID_DETERMINA,
          pr_deli.id_proposta_delibera,               -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          NULL,                                            -- ID_VISTO_PARERE,
          NULL,                                             -- ID_CERTIFICATO,
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
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          pr_deli.ente,
          pr_deli.riservato,
          DECODE (
             deli.id_delibera,
             NULL, DECODE (tipo.id_tipo_registro_delibera,
                           NULL, comm.id_tipo_registro,
                           tipo.id_tipo_registro_delibera),
             deli.registro_delibera)
     FROM proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          odg_commissioni comm,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo
    WHERE     pr_deli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND pr_deli.id_commissione = comm.id_commissione
          AND pr_deli.valido = 'Y'
          AND deli.valido(+) = 'Y'
   UNION ALL
   SELECT 'DETERMINA',
          dete.id_determina,
          dete.id_determina,                                  -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          NULL,                                            -- ID_VISTO_PARERE,
          NULL,                                             -- ID_CERTIFICATO,
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
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          dete.ente,
          dete.riservato,
          DECODE (dete.numero_determina,
                  NULL, tipo.id_tipo_registro,
                  dete.registro_determina)
     FROM determine_soggetti ds_uoprop,
          determine dete,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_determina tipo,
          tipi_registro registro
    WHERE     dete.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND tipo.id_tipo_determina = dete.id_tipo_determina
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND ds_uoprop.id_determina = dete.id_determina
          AND ds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND ds_uoprop.unita_dal = so4_unita.dal
          AND ds_uoprop.unita_ottica = so4_unita.ottica
          AND ds_uoprop.unita_progr = so4_unita.progr
          AND dete.valido = 'Y'
          AND dete.registro_determina = registro.tipo_registro(+)
   UNION ALL
   SELECT 'VISTO',
          vp.id_visto_parere,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          vp.id_visto_parere,                              -- ID_VISTO_PARERE,
          NULL,                                             -- ID_CERTIFICATO,
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
          so4_unita.descrizione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          dete.ente,
          dete.riservato,
          DECODE (dete.numero_determina,
                  NULL, tipo_dete.id_tipo_registro,
                  dete.registro_determina)
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          determine_soggetti ds_uoprop,
          determine dete,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo,
          tipi_determina tipo_dete
    WHERE     vp.id_determina = dete.id_determina
          AND step.id_engine_step = iter.id_step_corrente
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND dete.id_tipo_determina = tipo_dete.id_tipo_determina
          AND ds_uoprop.id_determina = dete.id_determina
          AND ds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND ds_uoprop.unita_dal = so4_unita.dal
          AND ds_uoprop.unita_ottica = so4_unita.ottica
          AND ds_uoprop.unita_progr = so4_unita.progr
          AND vp.valido = 'Y'
   UNION ALL
   SELECT 'PARERE',                       -- pareri della PROPOSTA DI DELIBERA
          vp.id_visto_parere,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          vp.id_visto_parere,                              -- ID_VISTO_PARERE,
          NULL,                                             -- ID_CERTIFICATO,
          pr_deli.id_proposta_delibera,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          pr_deli.anno_proposta,
          pr_deli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          pr_deli.oggetto,
          vp.stato,
          vp.stato_firma,
          deli.stato_conservazione,
          pr_deli.stato_odg,
          so4_unita.descrizione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          pr_deli.ente,
          pr_deli.riservato,
          DECODE (
             deli.id_delibera,
             NULL, DECODE (tipo_deli.id_tipo_registro_delibera,
                           NULL, comm.id_tipo_registro,
                           tipo_deli.id_tipo_registro_delibera),
             deli.registro_delibera)
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo,
          odg_commissioni comm,
          tipi_delibera tipo_deli
    WHERE     vp.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND step.id_engine_step = iter.id_step_corrente
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step
          AND deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND vp.valido = 'Y'
          AND tipo_deli.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND pr_deli.id_commissione = comm.id_commissione
   UNION ALL
   SELECT 'PARERE',                                   -- PARERI DELLA DELIBERA
          vp.id_visto_parere,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          vp.id_visto_parere,                              -- ID_VISTO_PARERE,
          NULL,                                             -- ID_CERTIFICATO,
          pr_deli.id_proposta_delibera,
          tipo.id_tipo_visto_parere,
          tipo.titolo,
          tipo.descrizione,
          pr_deli.anno_proposta,
          pr_deli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          pr_deli.oggetto,
          vp.stato,
          vp.stato_firma,
          deli.stato_conservazione,
          pr_deli.stato_odg,
          so4_unita.descrizione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          pr_deli.ente,
          pr_deli.riservato,
          DECODE (
             deli.id_delibera,
             NULL, DECODE (tipo_deli.id_tipo_registro_delibera,
                           NULL, comm.id_tipo_registro,
                           tipo_deli.id_tipo_registro_delibera),
             deli.registro_delibera)
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step,
          tipi_visto_parere tipo,
          odg_commissioni comm,
          tipi_delibera tipo_deli
    WHERE     vp.id_delibera = deli.id_delibera
          AND step.id_engine_step = iter.id_step_corrente
          AND vp.id_engine_iter = iter.id_engine_iter
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND tipo.id_tipo_visto_parere = vp.id_tipologia
          AND step.id_engine_step = a_step.id_engine_step
          AND deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND vp.valido = 'Y'
          AND tipo_deli.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND pr_deli.id_commissione = comm.id_commissione
   UNION ALL
   SELECT 'CERTIFICATO',
          cert.id_certificato,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          NULL,                                            -- ID_VISTO_PARERE,
          cert.id_certificato,                              -- ID_CERTIFICATO,
          deli.id_delibera,
          NULL,
             'CERTIFICATO DI '
          || DECODE (cert.tipo,
                     'AVVENUTA_PUBBLICAZIONE', 'AVVENUTA PUBBLICAZIONE',
                     cert.tipo),
          '',
          pr_deli.anno_proposta,
          pr_deli.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          deli.oggetto,
          cert.stato,
          cert.stato_firma,
          deli.stato_conservazione,
          pr_deli.stato_odg,
          so4_unita.descrizione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          cert.ente,
          pr_deli.riservato,
          deli.registro_delibera
     FROM certificati cert,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera_soggetti pds_uoprop,
          proposte_delibera pr_deli,
          delibere deli,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step
    WHERE     cert.id_delibera = deli.id_delibera
          AND pr_deli.id_proposta_delibera = deli.id_proposta_delibera
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND pds_uoprop.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND pds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds_uoprop.unita_dal = so4_unita.dal
          AND pds_uoprop.unita_ottica = so4_unita.ottica
          AND pds_uoprop.unita_progr = so4_unita.progr
          AND cert.valido = 'Y'
   UNION ALL
   SELECT 'CERTIFICATO',
          cert.id_certificato,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          NULL,                                            -- ID_VISTO_PARERE,
          cert.id_certificato,                              -- ID_CERTIFICATO,
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
          so4_unita.descrizione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          cert.ente,
          dete.riservato,
          dete.registro_determina
     FROM certificati cert,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          determine_soggetti ds_uoprop,
          determine dete,
          so4_v_unita_organizzative_pubb so4_unita,
          wkf_cfg_step cfg_step
    WHERE     cert.id_determina = dete.id_determina
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND ds_uoprop.id_determina = dete.id_determina
          AND ds_uoprop.tipo_soggetto = 'UO_PROPONENTE'
          AND ds_uoprop.unita_dal = so4_unita.dal
          AND ds_uoprop.unita_ottica = so4_unita.ottica
          AND ds_uoprop.unita_progr = so4_unita.progr
          AND cert.valido = 'Y'
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_CERTIFICATO
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   TITOLO_TIPOLOGIA,
   ID_PADRE,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_ADOZIONE_ATTO,
   DATA_PROPOSTA,
   FIRMATARIO_DOCUMENTO_PADRE,
   TIPO_SOGGETTO_DOC_PADRE,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   REGISTRO,
   STATO_ATTO,
   TIPOLOGIA_CERTIFICATO,
   FIRMATARIO_CERTIFICATO,
   DATA_APPOSIZIONE_CERTIFICATO,
   ID_FILE_ALLEGATO_TESTO,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ENTE
)
AS
   SELECT vp.id_certificato,
          CAST ('CERTIFICATO' AS VARCHAR (255)) AS tipo_atto,
          tipo.titolo,
          dete.id_determina,
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          dete.data_numero_determina,
          dete.data_proposta,
          NULL,                                  -- firmatario documento padre
          NULL,                               -- tipo soggetto documento padre
          pds.unita_progr,
          pds.unita_dal,
          pds.unita_ottica,
          so4_util.anuo_get_descrizione (pds.unita_progr, pds.unita_dal)
             descrizione_uo,
          dete.registro_determina,
          dete.stato,
          vp.tipo,
          vp.FIRMATARIO,
          CAST (NULL AS DATE) data_firma,
          vp.id_file_allegato_testo,
          comp.id_certificato_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          vp.ente
     FROM wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          determine_soggetti pds,
          determine dete,
          certificati vp,
          certificati_competenze comp,
          tipi_certificato tipo
    WHERE     cfg_step.id_cfg_step(+) = step.id_cfg_step
          AND a_step.id_engine_step(+) = step.id_engine_step
          AND step.id_engine_step(+) = iter.id_step_corrente
          AND iter.id_engine_iter(+) = vp.id_engine_iter
          AND comp.id_certificato = vp.id_certificato
          AND vp.id_tipologia = tipo.id_tipo_certificato
          AND pds.id_determina = dete.id_determina
          AND pds.tipo_soggetto = 'UO_PROPONENTE'
          AND dete.valido = 'Y'
          AND vp.id_determina = dete.id_determina
          AND vp.valido = 'Y'
   UNION ALL
   SELECT vp.id_certificato,
          CAST ('CERTIFICATO' AS VARCHAR (255)) AS tipo_atto,
          tipo.titolo,
          deli.id_delibera,
          dete.anno_proposta,
          dete.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          dete.oggetto,
          deli.data_adozione,
          dete.data_proposta,
          NULL,                                  -- firmatario documento padre
          NULL,                               -- tipo soggetto documento padre
          pds.unita_progr,
          pds.unita_dal,
          pds.unita_ottica,
          so4_util.anuo_get_descrizione (pds.unita_progr, pds.unita_dal)
             descrizione_uo,
          deli.registro_delibera,
          dete.stato,
          vp.tipo,
          vp.FIRMATARIO,
          CAST (NULL AS DATE) data_firma,
          vp.id_file_allegato_testo,
          comp.id_certificato_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          vp.ente
     FROM wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          proposte_delibera_soggetti pds,
          proposte_delibera dete,
          delibere deli,
          certificati vp,
          certificati_competenze comp,
          tipi_certificato tipo
    WHERE     cfg_step.id_cfg_step(+) = step.id_cfg_step
          AND a_step.id_engine_step(+) = step.id_engine_step
          AND step.id_engine_step(+) = iter.id_step_corrente
          AND iter.id_engine_iter(+) = vp.id_engine_iter
          AND comp.id_certificato = vp.id_certificato
          AND vp.id_tipologia = tipo.id_tipo_certificato
          AND pds.id_proposta_delibera = dete.id_proposta_delibera
          AND pds.tipo_soggetto = 'UO_PROPONENTE'
          AND deli.valido = 'Y'
          AND dete.id_proposta_delibera = deli.id_proposta_delibera
          AND vp.id_delibera = deli.id_delibera
          AND vp.valido = 'Y'
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_CERTIFICATO_TESTO
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   TITOLO_TIPOLOGIA,
   ID_PADRE,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_ADOZIONE_ATTO,
   DATA_PROPOSTA,
   FIRMATARIO_DOCUMENTO_PADRE,
   TIPO_SOGGETTO_DOC_PADRE,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   REGISTRO,
   STATO_ATTO,
   TIPOLOGIA_CERTIFICATO,
   FIRMATARIO_CERTIFICATO,
   DATA_APPOSIZIONE_CERTIFICATO,
   ID_FILE_ALLEGATO_TESTO,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ENTE,
   TESTO
)
AS
   SELECT riccert.id_documento,
          riccert.tipo_atto,
          riccert.titolo_tipologia,
          riccert.id_padre,
          riccert.anno_proposta,
          riccert.numero_proposta,
          riccert.anno,
          riccert.numero,
          riccert.oggetto,
          riccert.data_adozione_atto,
          riccert.data_proposta,
          riccert.firmatario_documento_padre,
          riccert.tipo_soggetto_doc_padre,
          riccert.uo_proponente_progr,
          riccert.uo_proponente_dal,
          riccert.uo_proponente_ottica,
          riccert.uo_proponente_descrizione,
          riccert.registro,
          riccert.stato_atto,
          riccert.tipologia_certificato,
          riccert.firmatario_certificato,
          riccert.data_apposizione_certificato,
          riccert.id_file_allegato_testo,
          riccert.id_competenza,
          riccert.comp_utente,
          riccert.comp_unita_progr,
          riccert.comp_unita_dal,
          riccert.comp_unita_ottica,
          riccert.comp_ruolo,
          riccert.comp_lettura,
          riccert.comp_modifica,
          riccert.comp_cancellazione,
          riccert.id_step,
          riccert.step_utente,
          riccert.step_unita_progr,
          riccert.step_unita_dal,
          riccert.step_unita_ottica,
          riccert.step_ruolo,
          riccert.step_nome,
          riccert.step_descrizione,
          riccert.ente,
          fileallegato.testo
     FROM    ricerca_certificato riccert
          LEFT JOIN
             file_allegati fileallegato
          ON riccert.id_file_allegato_testo = fileallegato.id_file_allegato
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_DELIBERA
(
   ID_DOCUMENTO,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_NUMERO_DELIBERA,
   DATA_ADOZIONE,
   DATA_ESECUTIVITA,
   DATA_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   DATA_PROPOSTA,
   REGISTRO,
   UTENTE_SOGGETTO,
   TIPO_SOGGETTO,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ID_COMMISSIONE,
   STATO_ATTO,
   ID_FILE_ALLEGATO_TESTO,
   TIPO_ATTO,
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ENTE,
   STATO_CONSERVAZIONE,
   RISERVATO,
   ID_CATEGORIA,
   TIPOLOGIA_CONTABILE_VISTO,
   ID_OGGETTO_SEDUTA,
   STATO,
   LOG_CONSERVAZIONE
)
AS
   SELECT d.id_delibera AS id_documento,
          pd.id_tipo_delibera AS id_tipologia,
          td.titolo AS titolo_tipologia,
          td.descrizione AS descrizione_tipologia,
          pd.anno_proposta AS anno_proposta,
          pd.numero_proposta AS numero_proposta,
          d.anno_delibera AS anno,
          d.numero_delibera AS numero,
          NVL (d.oggetto, pd.oggetto) AS oggetto,
          d.data_numero_delibera AS data_numero_delibera,
          d.data_adozione AS data_adozione,
          d.data_esecutivita AS data_esecutivita,
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          pd.data_proposta,
          NVL (d.registro_delibera, pd.registro_proposta) AS registro,
          pds.utente AS utente_soggetto,
          pds.tipo_soggetto AS tipo_soggetto,
          pds.unita_progr AS uo_proponente_progr,
          pds.unita_dal AS uo_proponente_dal,
          pds.unita_ottica AS uo_proponente_ottica,
          SO4_UTIL.ANUO_GET_DESCRIZIONE (pds.unita_progr, pds.unita_dal)
             AS uo_proponente_descrizione,
          c.id_delibera_competenza AS id_competenza,
          c.utente AS comp_utente,
          c.unita_progr AS comp_unita_progr,
          c.unita_dal AS comp_unita_dal,
          c.unita_ottica AS comp_unita_ottica,
          c.ruolo AS comp_ruolo,
          c.lettura AS comp_lettura,
          c.modifica AS comp_modifica,
          c.cancellazione AS comp_cancellazione,
          NVL (s.id_engine_step, pd_s.id_engine_step) AS id_step,
          NVL (sa.utente, pd_sa.utente) AS step_utente,
          NVL (sa.unita_progr, pd_sa.unita_progr) AS step_unita_progr,
          NVL (sa.unita_dal, pd_sa.unita_dal) AS step_unita_dal,
          NVL (sa.unita_ottica, pd_sa.unita_ottica) AS step_unita_ottica,
          NVL (sa.ruolo, pd_sa.ruolo) AS step_ruolo,
          NVL (sc.nome, pd_sc.nome) AS step_nome,
          NVL (sc.descrizione, pd_sc.descrizione) AS step_descrizione,
          pd.id_commissione AS id_commissione,
          NVL (d.stato, pd.stato) AS stato_atto,
          NVL (d.id_file_allegato_testo, pd.id_file_allegato_testo)
             AS id_file_allegato_testo,
          CAST ('DELIBERA' AS VARCHAR2 (255)) AS tipo_atto,
          d.id_delibera AS id_delibera,
          pd.id_proposta_delibera AS id_proposta_delibera,
          pd.ente AS ente,
          d.stato_conservazione AS stato_conservazione,
          CAST (NVL (d.riservato, pd.riservato) AS CHAR (1)) AS riservato,
          cat.id_categoria AS id_categoria,
          tvp.contabile AS tipologia_contabile_visto,
          d.ID_OGGETTO_SEDUTA,
          UPPER (
             DECODE (
                NVL (d.stato, pd.stato),
                'ANNULLATO', NVL (d.stato, pd.stato),
                NVL (NVL (sc.titolo, pd_sc.titolo), NVL (d.stato, pd.stato))))
             stato,
          jcons_pkg.get_log_conservazione (d.id_documento_esterno)
             AS LOG_CONSERVAZIONE
     FROM proposte_delibera pd,
          delibere d,
          delibere_competenze c,
          tipi_delibera td,
          proposte_delibera_soggetti pds,
          proposte_delibera_soggetti pds_uoproponente,
          wkf_engine_iter i,
          wkf_engine_step s,
          wkf_engine_step_attori sa,
          wkf_cfg_step sc,
          wkf_engine_iter pd_i,
          wkf_engine_step pd_s,
          wkf_engine_step_attori pd_sa,
          wkf_cfg_step pd_sc,
          categorie cat,
          visti_pareri vp,
          tipi_visto_parere tvp
    WHERE     d.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND c.id_delibera = d.id_delibera
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND pds.id_proposta_delibera = pd.id_proposta_delibera
          AND pd.valido = 'Y'
          AND d.valido(+) = 'Y'
          AND pds.tipo_soggetto(+) = 'UO_PROPONENTE'
          AND pds.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND pds_uoproponente.tipo_soggetto(+) = 'UO_PROPONENTE'
          AND pds_uoproponente.id_proposta_delibera(+) =
                 pd.id_proposta_delibera
          AND i.id_engine_iter(+) = d.id_engine_iter
          AND s.id_engine_step(+) = i.id_step_corrente
          AND sa.id_engine_step(+) = s.id_engine_step
          AND sc.id_cfg_step(+) = s.id_cfg_step
          AND pd_i.id_engine_iter(+) = pd.id_engine_iter
          AND pd_s.id_engine_step(+) = pd_i.id_step_corrente
          AND pd_sa.id_engine_step(+) = pd_s.id_engine_step
          AND pd_sc.id_cfg_step(+) = pd_s.id_cfg_step
          AND cat.id_categoria(+) = pd.id_categoria
          AND cat.tipo_oggetto(+) = 'PROPOSTA_DELIBERA'
          AND vp.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND vp.valido(+) = 'Y'
          AND tvp.id_tipo_visto_parere(+) = vp.id_tipologia
   UNION ALL
   SELECT NVL (d.id_delibera, pd.id_proposta_delibera) AS id_documento,
          pd.id_tipo_delibera AS id_tipologia,
          td.titolo AS titolo_tipologia,
          td.descrizione AS descrizione_tipologia,
          pd.anno_proposta AS anno_proposta,
          pd.numero_proposta AS numero_proposta,
          d.anno_delibera AS anno,
          d.numero_delibera AS numero,
          NVL (d.oggetto, pd.oggetto) AS oggetto,
          d.data_numero_delibera AS data_numero_delibera,
          d.data_adozione AS data_adozione,
          d.data_esecutivita AS data_esecutivita,
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          pd.data_proposta,
          NVL (d.registro_delibera, pd.registro_proposta) AS registro,
          pds.utente AS utente_soggetto,
          pds.tipo_soggetto AS tipo_soggetto,
          pds.unita_progr AS uo_proponente_progr,
          pds.unita_dal AS uo_proponente_dal,
          pds.unita_ottica AS uo_proponente_ottica,
          SO4_UTIL.ANUO_GET_DESCRIZIONE (pds.unita_progr, pds.unita_dal)
             AS uo_proponente_descrizione,
          c.id_proposta_delibera_comp AS id_competenza,
          c.utente AS comp_utente,
          c.unita_progr AS comp_unita_progr,
          c.unita_dal AS comp_unita_dal,
          c.unita_ottica AS comp_unita_ottica,
          c.ruolo AS comp_ruolo,
          c.lettura AS comp_lettura,
          c.modifica AS comp_modifica,
          c.cancellazione AS comp_cancellazione,
          NVL (s.id_engine_step, pd_s.id_engine_step) AS id_step,
          NVL (sa.utente, pd_sa.utente) AS step_utente,
          NVL (sa.unita_progr, pd_sa.unita_progr) AS step_unita_progr,
          NVL (sa.unita_dal, pd_sa.unita_dal) AS step_unita_dal,
          NVL (sa.unita_ottica, pd_sa.unita_ottica) AS step_unita_ottica,
          NVL (sa.ruolo, pd_sa.ruolo) AS step_ruolo,
          NVL (sc.nome, pd_sc.nome) AS step_nome,
          NVL (sc.descrizione, pd_sc.descrizione) AS step_descrizione,
          pd.id_commissione AS id_commissione,
          NVL (d.stato, pd.stato) AS stato_atto,
          NVL (d.id_file_allegato_testo, pd.id_file_allegato_testo)
             AS id_file_allegato_testo,
          CAST (
             DECODE (d.id_delibera, NULL, 'PROPOSTA_DELIBERA', 'DELIBERA') AS VARCHAR2 (255))
             AS tipo_atto,
          d.id_delibera AS id_delibera,
          pd.id_proposta_delibera AS id_proposta_delibera,
          pd.ente AS ente,
          d.stato_conservazione AS stato_conservazione,
          CAST (NVL (d.riservato, pd.riservato) AS CHAR (1)) AS riservato,
          cat.id_categoria AS id_categoria,
          tvp.contabile AS contabile,
          d.ID_OGGETTO_SEDUTA,
          UPPER (
             DECODE (
                NVL (d.stato, pd.stato),
                'ANNULLATO', NVL (d.stato, pd.stato),
                NVL (NVL (sc.titolo, pd_sc.titolo), NVL (d.stato, pd.stato))))
             stato,
          jcons_pkg.get_log_conservazione (d.id_documento_esterno) AS LOG_CONSERVAZIONE
     FROM proposte_delibera pd,
          delibere d,
          proposte_delibera_competenze c,
          tipi_delibera td,
          proposte_delibera_soggetti pds,
          wkf_engine_iter i,
          wkf_engine_step s,
          wkf_engine_step_attori sa,
          wkf_cfg_step sc,
          wkf_engine_iter pd_i,
          wkf_engine_step pd_s,
          wkf_engine_step_attori pd_sa,
          wkf_cfg_step pd_sc,
          categorie cat,
          visti_pareri vp,
          tipi_visto_parere tvp
    WHERE     d.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND c.id_proposta_delibera = pd.id_proposta_delibera
          AND td.id_tipo_delibera = pd.id_tipo_delibera
          AND pds.id_proposta_delibera = pd.id_proposta_delibera
          AND pd.valido = 'Y'
          AND d.valido(+) = 'Y'
          AND pds.tipo_soggetto(+) = 'UO_PROPONENTE'
          AND pds.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND i.id_engine_iter(+) = d.id_engine_iter
          AND s.id_engine_step(+) = i.id_step_corrente
          AND sa.id_engine_step(+) = s.id_engine_step
          AND sc.id_cfg_step(+) = s.id_cfg_step
          AND pd_i.id_engine_iter(+) = pd.id_engine_iter
          AND pd_s.id_engine_step(+) = pd_i.id_step_corrente
          AND pd_sa.id_engine_step(+) = pd_s.id_engine_step
          AND pd_sc.id_cfg_step(+) = pd_s.id_cfg_step
          AND cat.id_categoria(+) = pd.id_categoria
          AND cat.tipo_oggetto(+) = 'PROPOSTA_DELIBERA'
          AND vp.id_proposta_delibera(+) = pd.id_proposta_delibera
          AND vp.valido(+) = 'Y'
          AND tvp.id_tipo_visto_parere(+) = vp.id_tipologia
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_DELIBERA_TESTO
(
   ID_DOCUMENTO,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_NUMERO_DELIBERA,
   DATA_ADOZIONE,
   DATA_ESECUTIVITA,
   DATA_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   DATA_PROPOSTA,
   REGISTRO,
   UTENTE_SOGGETTO,
   TIPO_SOGGETTO,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ID_COMMISSIONE,
   STATO_ATTO,
   ID_FILE_ALLEGATO_TESTO,
   TIPO_ATTO,
   ID_DELIBERA,
   ID_PROPOSTA_DELIBERA,
   ENTE,
   STATO_CONSERVAZIONE,
   RISERVATO,
   ID_CATEGORIA,
   TIPOLOGIA_CONTABILE_VISTO,
   ID_OGGETTO_SEDUTA,
   STATO,
   LOG_CONSERVAZIONE,
   TESTO
)
AS
   SELECT ricdeli.id_documento,
          ricdeli.id_tipologia,
          ricdeli.titolo_tipologia,
          ricdeli.descrizione_tipologia,
          ricdeli.anno_proposta,
          ricdeli.numero_proposta,
          ricdeli.anno,
          ricdeli.numero,
          ricdeli.oggetto,
          ricdeli.data_numero_delibera,
          ricdeli.data_adozione,
          ricdeli.data_esecutivita,
          ricdeli.data_pubblicazione,
          ricdeli.data_fine_pubblicazione,
          ricdeli.data_proposta,
          ricdeli.registro,
          ricdeli.utente_soggetto,
          ricdeli.tipo_soggetto,
          ricdeli.uo_proponente_progr,
          ricdeli.uo_proponente_dal,
          ricdeli.uo_proponente_ottica,
          ricdeli.uo_proponente_descrizione,
          ricdeli.id_competenza,
          ricdeli.comp_utente,
          ricdeli.comp_unita_progr,
          ricdeli.comp_unita_dal,
          ricdeli.comp_unita_ottica,
          ricdeli.comp_ruolo,
          ricdeli.comp_lettura,
          ricdeli.comp_modifica,
          ricdeli.comp_cancellazione,
          ricdeli.id_step,
          ricdeli.step_utente,
          ricdeli.step_unita_progr,
          ricdeli.step_unita_dal,
          ricdeli.step_unita_ottica,
          ricdeli.step_ruolo,
          ricdeli.step_nome,
          ricdeli.step_descrizione,
          ricdeli.id_commissione,
          ricdeli.stato_atto,
          ricdeli.id_file_allegato_testo,
          ricdeli.tipo_atto,
          ricdeli.id_delibera,
          ricdeli.id_proposta_delibera,
          ricdeli.ente,
          ricdeli.stato_conservazione,
          ricdeli.riservato,
          ricdeli.id_categoria,
          ricdeli.tipologia_contabile_visto,
          ricdeli.id_oggetto_seduta,
          ricdeli.stato,
          ricdeli.log_conservazione,
          fileallegati.testo
     FROM    ricerca_delibera ricdeli
          LEFT JOIN
             file_allegati fileallegati
          ON ricdeli.id_file_allegato_testo = fileallegati.id_file_allegato
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_DETERMINA
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_NUMERO_DETERMINA,
   DATA_ESECUTIVITA,
   DATA_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   DATA_PROPOSTA,
   REGISTRO,
   UTENTE_SOGGETTO_DETERMINA,
   TIPO_SOGGETTO,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   STATO_ATTO,
   ID_FILE_ALLEGATO_TESTO,
   ENTE,
   STATO_CONSERVAZIONE,
   ID_CATEGORIA,
   TIPOLOGIA_CONTABILE_VISTO,
   RISERVATO,
   STATO,
   LOG_CONSERVAZIONE
)
AS
   SELECT d.id_determina,
          CAST ('DETERMINA' AS VARCHAR (255)) AS tipo_atto,
          td.id_tipo_determina,
          td.titolo,
          td.descrizione,
          d.anno_proposta,
          d.numero_proposta,
          d.anno_determina,
          d.numero_determina,
          d.oggetto,
          d.data_numero_determina,
          d.data_esecutivita,
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          d.data_proposta,
          d.registro_determina,
          pds.utente,
          pds.tipo_soggetto,
          pds.unita_progr,
          pds.unita_dal,
          pds.unita_ottica,
          SO4_UTIL.ANUO_GET_DESCRIZIONE (uo_prop.unita_progr,
                                         uo_prop.unita_dal),
          c.id_determina_competenza,
          c.utente,
          c.unita_progr,
          c.unita_dal,
          c.unita_ottica,
          c.ruolo,
          c.lettura,
          c.modifica,
          c.cancellazione,
          s.id_engine_step,
          sa.utente,
          sa.unita_progr,
          sa.unita_dal,
          sa.unita_ottica,
          sa.ruolo,
          sc.nome,
          sc.descrizione,
          d.stato,
          d.id_file_allegato_testo,
          d.ente,
          d.stato_conservazione,
          cat.id_categoria,
          tvp.contabile,
          d.riservato,
          UPPER (
             DECODE (d.stato, 'ANNULLATO', d.stato, NVL (sc.titolo, d.stato)))
             stato,
          jcons_pkg.get_log_conservazione (d.id_documento_esterno)
             AS log_conservazione
     FROM determine_competenze c,
          determine_soggetti pds,
          determine_soggetti uo_prop,
          tipi_determina td,
          wkf_cfg_step sc,
          wkf_engine_step_attori sa,
          wkf_engine_step s,
          wkf_engine_iter i,
          tipi_visto_parere tvp,
          visti_pareri vp,
          determine d,
          categorie cat
    WHERE     tvp.id_tipo_visto_parere(+) = vp.id_tipologia
          AND vp.id_determina(+) = d.id_determina
          AND vp.valido(+) = 'Y'
          AND c.id_determina = d.id_determina
          AND cat.id_categoria(+) = d.id_categoria
          AND pds.id_determina = d.id_determina
          AND uo_prop.id_determina = d.id_determina
          AND uo_prop.tipo_soggetto = 'UO_PROPONENTE'
          AND d.valido = 'Y'
          AND td.id_tipo_determina = d.id_tipo_determina
          AND i.id_engine_iter(+) = d.id_engine_iter
          AND s.id_engine_step(+) = i.id_step_corrente
          AND sa.id_engine_step(+) = s.id_engine_step
          AND sc.id_cfg_step(+) = s.id_cfg_step
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_DETERMINA_TESTO
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   ID_TIPOLOGIA,
   TITOLO_TIPOLOGIA,
   DESCRIZIONE_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_NUMERO_DETERMINA,
   DATA_ESECUTIVITA,
   DATA_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   DATA_PROPOSTA,
   REGISTRO,
   UTENTE_SOGGETTO_DETERMINA,
   TIPO_SOGGETTO,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   STATO_ATTO,
   ID_FILE_ALLEGATO_TESTO,
   ENTE,
   STATO_CONSERVAZIONE,
   ID_CATEGORIA,
   TIPOLOGIA_CONTABILE_VISTO,
   RISERVATO,
   STATO,
   LOG_CONSERVAZIONE,
   TESTO
)
AS
   SELECT ricdet.id_documento,
          ricdet.tipo_atto,
          ricdet.id_tipologia,
          ricdet.titolo_tipologia,
          ricdet.descrizione_tipologia,
          ricdet.anno_proposta,
          ricdet.numero_proposta,
          ricdet.anno,
          ricdet.numero,
          ricdet.oggetto,
          ricdet.data_numero_determina,
          ricdet.data_esecutivita,
          ricdet.data_pubblicazione,
          ricdet.data_fine_pubblicazione,
          ricdet.data_proposta,
          ricdet.registro,
          ricdet.utente_soggetto_determina,
          ricdet.tipo_soggetto,
          ricdet.uo_proponente_progr,
          ricdet.uo_proponente_dal,
          ricdet.uo_proponente_ottica,
          ricdet.uo_proponente_descrizione,
          ricdet.id_competenza,
          ricdet.comp_utente,
          ricdet.comp_unita_progr,
          ricdet.comp_unita_dal,
          ricdet.comp_unita_ottica,
          ricdet.comp_ruolo,
          ricdet.comp_lettura,
          ricdet.comp_modifica,
          ricdet.comp_cancellazione,
          ricdet.id_step,
          ricdet.step_utente,
          ricdet.step_unita_progr,
          ricdet.step_unita_dal,
          ricdet.step_unita_ottica,
          ricdet.step_ruolo,
          ricdet.step_nome,
          ricdet.step_descrizione,
          ricdet.stato_atto,
          ricdet.id_file_allegato_testo,
          ricdet.ente,
          ricdet.stato_conservazione,
          ricdet.id_categoria,
          ricdet.tipologia_contabile_visto,
          ricdet.riservato,
          ricdet.stato,
          ricdet.log_conservazione,
          fileallegati.testo
     FROM    ricerca_determina ricdet
          LEFT JOIN
             file_allegati fileallegati
          ON fileallegati.id_file_allegato = ricdet.id_file_allegato_testo
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_PARERE
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   TITOLO_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_ADOZIONE_ATTO,
   DATA_PROPOSTA,
   FIRMATARIO_DOCUMENTO_PADRE,
   TIPO_SOGGETTO_DOC_PADRE,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   REGISTRO,
   STATO_ATTO,
   POSIZIONE_FLUSSO,
   VALUTAZIONE,
   UNITA_PROGR,
   UNITA_DAL,
   UNITA_OTTICA,
   FIRMATARIO,
   ID_TIPO_VISTO_PARERE,
   DATA_ADOZIONE_VP,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ID_FILE_ALLEGATO_TESTO,
   ENTE
)
AS
   SELECT vp.id_visto_parere,
          CAST ('PARERE' AS VARCHAR (255)) AS tipo_atto,
          tipo.titolo,
          dete.anno_proposta,
          dete.numero_proposta,
          deli.anno_delibera,
          deli.numero_delibera,
          NVL (deli.oggetto, dete.oggetto),
          deli.data_adozione,
          dete.data_proposta,
          pds.utente,
          pds.tipo_soggetto,
          pds.unita_progr,
          pds.unita_dal,
          pds.unita_ottica,
          SO4_UTIL.ANUO_GET_DESCRIZIONE (pds.unita_progr, pds.unita_dal)
             descrizione_uo,
          dete.registro_proposta,
          dete.stato,
          vp.stato,
          vp.esito,
          vp.unita_progr,
          vp.unita_dal,
          vp.unita_ottica,
          vp.utente_firmatario,
          vp.id_tipologia,
          vp.data_adozione,
          comp.id_visto_parere_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          vp.id_file_allegato_testo,
          vp.ente
     FROM wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          delibere deli,
          proposte_delibera_soggetti pds,
          proposte_delibera dete,
          visti_pareri vp,
          visti_pareri_competenze comp,
          tipi_visto_parere tipo
    WHERE     cfg_step.id_cfg_step(+) = step.id_cfg_step
          AND a_step.id_engine_step(+) = step.id_engine_step
          AND step.id_engine_step(+) = iter.id_step_corrente
          AND iter.id_engine_iter(+) = vp.id_engine_iter
          AND comp.id_visto_parere = vp.id_visto_parere
          AND vp.id_tipologia = tipo.id_tipo_visto_parere
          AND pds.id_proposta_delibera = dete.id_proposta_delibera
          AND pds.tipo_soggetto = 'UO_PROPONENTE'
          AND dete.valido = 'Y'
          AND deli.id_proposta_delibera(+) = dete.id_proposta_delibera
          AND vp.id_proposta_delibera = dete.id_proposta_delibera
          AND vp.valido = 'Y'
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_PARERE_TESTO
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   TITOLO_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_ADOZIONE_ATTO,
   DATA_PROPOSTA,
   FIRMATARIO_DOCUMENTO_PADRE,
   TIPO_SOGGETTO_DOC_PADRE,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   REGISTRO,
   STATO_ATTO,
   POSIZIONE_FLUSSO,
   VALUTAZIONE,
   UNITA_PROGR,
   UNITA_DAL,
   UNITA_OTTICA,
   FIRMATARIO,
   ID_TIPO_VISTO_PARERE,
   DATA_ADOZIONE_VP,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ID_FILE_ALLEGATO_TESTO,
   ENTE,
   TESTO
)
AS
   SELECT ricpar.id_documento,
          ricpar.tipo_atto,
          ricpar.titolo_tipologia,
          ricpar.anno_proposta,
          ricpar.numero_proposta,
          ricpar.anno,
          ricpar.numero,
          ricpar.oggetto,
          ricpar.data_adozione_atto,
          ricpar.data_proposta,
          ricpar.firmatario_documento_padre,
          ricpar.tipo_soggetto_doc_padre,
          ricpar.uo_proponente_progr,
          ricpar.uo_proponente_dal,
          ricpar.uo_proponente_ottica,
          ricpar.uo_proponente_descrizione,
          ricpar.registro,
          ricpar.stato_atto,
          ricpar.posizione_flusso,
          ricpar.valutazione,
          ricpar.unita_progr,
          ricpar.unita_dal,
          ricpar.unita_ottica,
          ricpar.firmatario,
          ricpar.id_tipo_visto_parere,
          ricpar.data_adozione_vp,
          ricpar.id_competenza,
          ricpar.comp_utente,
          ricpar.comp_unita_progr,
          ricpar.comp_unita_dal,
          ricpar.comp_unita_ottica,
          ricpar.comp_ruolo,
          ricpar.comp_lettura,
          ricpar.comp_modifica,
          ricpar.comp_cancellazione,
          ricpar.id_step,
          ricpar.step_utente,
          ricpar.step_unita_progr,
          ricpar.step_unita_dal,
          ricpar.step_unita_ottica,
          ricpar.step_ruolo,
          ricpar.step_nome,
          ricpar.step_descrizione,
          ricpar.id_file_allegato_testo,
          ricpar.ente,
          fileallegati.testo
     FROM    ricerca_parere ricpar
          LEFT JOIN
             file_allegati fileallegati
          ON ricpar.id_file_allegato_testo = fileallegati.id_file_allegato
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_UNITA_DOCUMENTI_ATTIVI
(
   TIPO_OGGETTO,
   ID_OGGETTO,
   ID,
   ID_PADRE,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   UNITA_PROGR,
   UNITA_DAL,
   UNITA_OTTICA,
   ANNO,
   NUMERO,
   OGGETTO,
   ENTE
)
AS
   SELECT 'DETERMINA',
          dete.id_determina,
          soggetto.id_determina_soggetto,
          NULL,
          dete.anno_proposta,
          dete.numero_proposta,
          soggetto.unita_progr,
          soggetto.unita_dal,
          soggetto.unita_ottica,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          dete.ente
     FROM determine dete,
          wkf_engine_iter iter,
          determine_soggetti soggetto,
          so4_v_unita_organizzative_pubb unita
    WHERE     dete.id_engine_iter = iter.id_engine_iter
          AND iter.data_fine IS NULL
          AND dete.valido = 'Y'
          AND soggetto.id_determina = dete.id_determina
          AND soggetto.utente IS NULL
          AND unita.progr = soggetto.unita_progr
          AND unita.dal = soggetto.unita_dal
          AND unita.ottica = soggetto.unita_ottica
          AND unita.al IS NOT NULL
   UNION ALL
   SELECT 'VISTO',
          vp.id_visto_parere,
          vp.id_visto_parere,
          dete.id_determina,
          dete.anno_proposta,
          dete.numero_proposta,
          vp.unita_progr,
          vp.unita_dal,
          vp.unita_ottica,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          vp.ente
     FROM visti_pareri vp,
          wkf_engine_iter iter,
          determine dete,
          so4_v_unita_organizzative_pubb unita
    WHERE     vp.id_determina = dete.id_determina
          AND dete.valido = 'Y'
          AND vp.id_engine_iter = iter.id_engine_iter
          AND iter.data_fine IS NULL
          AND vp.valido = 'Y'
          AND unita.progr = vp.unita_progr
          AND unita.dal = vp.unita_dal
          AND unita.ottica = vp.unita_ottica
          AND unita.al IS NOT NULL
   UNION ALL
   SELECT 'PARERE',
          vp.id_visto_parere,
          vp.id_visto_parere,
          propDeli.id_proposta_delibera,
          propDeli.anno_proposta,
          propDeli.numero_proposta,
          vp.unita_progr,
          vp.unita_dal,
          vp.unita_ottica,
          deli.anno_delibera,
          deli.numero_delibera,
          deli.oggetto,
          vp.ente
     FROM visti_pareri vp,
          wkf_engine_iter iter,
          delibere deli,
          proposte_delibera propDeli,
          so4_v_unita_organizzative_pubb unita
    WHERE     vp.id_proposta_delibera = propDeli.id_proposta_delibera
          AND propDeli.valido = 'Y'
          AND propDeli.id_proposta_delibera = deli.id_proposta_delibera(+)
          AND deli.valido(+) = 'Y'
          AND vp.id_engine_iter = iter.id_engine_iter
          AND iter.data_fine IS NULL
          AND vp.valido = 'Y'
          AND unita.progr = vp.unita_progr
          AND unita.dal = vp.unita_dal
          AND unita.ottica = vp.unita_ottica
          AND unita.al IS NOT NULL
   UNION ALL
   SELECT 'DELIBERA',
          deli.id_delibera,
          soggetto.id_delibera_soggetto,
          NULL,
          propDeli.anno_proposta,
          propDeli.numero_proposta,
          soggetto.unita_progr,
          soggetto.unita_dal,
          soggetto.unita_ottica,
          deli.anno_delibera,
          deli.numero_delibera,
          deli.oggetto,
          deli.ente
     FROM delibere deli,
          proposte_delibera propDeli,
          wkf_engine_iter iter,
          delibere_soggetti soggetto,
          so4_v_unita_organizzative_pubb unita
    WHERE     deli.id_engine_iter = iter.id_engine_iter
          AND iter.data_fine IS NULL
          AND deli.valido = 'Y'
          AND deli.id_proposta_delibera = propDeli.id_proposta_delibera
          AND propDeli.valido = 'Y'
          AND soggetto.id_delibera = deli.id_delibera
          AND soggetto.utente IS NULL
          AND unita.progr = soggetto.unita_progr
          AND unita.dal = soggetto.unita_dal
          AND unita.ottica = soggetto.unita_ottica
          AND unita.al IS NOT NULL
   UNION ALL
   SELECT 'PROPOSTA DELIBERA',
          propDeli.id_proposta_delibera,
          soggetto.id_proposta_delibera_soggetto,
          NULL,
          propDeli.anno_proposta,
          propDeli.numero_proposta,
          soggetto.unita_progr,
          soggetto.unita_dal,
          soggetto.unita_ottica,
          deli.anno_delibera,
          deli.numero_delibera,
          propDeli.oggetto,
          propDeli.ente
     FROM proposte_delibera propDeli,
          delibere deli,
          wkf_engine_iter iter,
          proposte_delibera_soggetti soggetto,
          so4_v_unita_organizzative_pubb unita
    WHERE     propDeli.id_engine_iter = iter.id_engine_iter
          AND iter.data_fine IS NULL
          AND deli.valido(+) = 'Y'
          AND deli.id_proposta_delibera(+) = propDeli.id_proposta_delibera
          AND deli.id_delibera IS NULL
          AND propDeli.valido = 'Y'
          AND soggetto.id_proposta_delibera = propDeli.id_proposta_delibera
          AND soggetto.utente IS NULL
          AND unita.progr = soggetto.unita_progr
          AND unita.dal = soggetto.unita_dal
          AND unita.ottica = soggetto.unita_ottica
          AND unita.al IS NOT NULL
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_VISTO
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   TITOLO_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_ADOZIONE_ATTO,
   DATA_PROPOSTA,
   FIRMATARIO_DOCUMENTO_PADRE,
   TIPO_SOGGETTO_DOC_PADRE,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   REGISTRO,
   STATO_ATTO,
   POSIZIONE_FLUSSO,
   VALUTAZIONE,
   UNITA_PROGR,
   UNITA_DAL,
   UNITA_OTTICA,
   FIRMATARIO,
   ID_TIPO_VISTO_PARERE,
   DATA_ADOZIONE_VP,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ID_FILE_ALLEGATO_TESTO,
   ENTE
)
AS
   SELECT vp.id_visto_parere,
          CAST ('VISTO' AS VARCHAR (255)) AS tipo_atto,
          tipo.titolo,
          dete.anno_proposta,
          dete.numero_proposta,
          dete.anno_determina,
          dete.numero_determina,
          dete.oggetto,
          dete.data_numero_determina,
          dete.data_proposta,
          pds.utente,
          pds.tipo_soggetto,
          pds.unita_progr,
          pds.unita_dal,
          pds.unita_ottica,
          SO4_UTIL.ANUO_GET_DESCRIZIONE (pds.unita_progr, pds.unita_dal)
             descrizione_uo,
          dete.registro_determina,
          dete.stato,
          vp.stato,
          vp.esito,
          vp.unita_progr,
          vp.unita_dal,
          vp.unita_ottica,
          vp.utente_firmatario,
          vp.id_tipologia,
          vp.data_adozione,
          comp.id_visto_parere_competenza,
          comp.utente,
          comp.unita_progr,
          comp.unita_dal,
          comp.unita_ottica,
          comp.ruolo,
          comp.lettura,
          comp.modifica,
          comp.cancellazione,
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          vp.id_file_allegato_testo,
          vp.ente
     FROM wkf_engine_iter iter,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_cfg_step cfg_step,
          determine_soggetti pds,
          determine dete,
          visti_pareri vp,
          visti_pareri_competenze comp,
          tipi_visto_parere tipo
    WHERE     cfg_step.id_cfg_step(+) = step.id_cfg_step
          AND a_step.id_engine_step(+) = step.id_engine_step
          AND step.id_engine_step(+) = iter.id_step_corrente
          AND iter.id_engine_iter(+) = vp.id_engine_iter
          AND comp.id_visto_parere = vp.id_visto_parere
          AND vp.id_tipologia = tipo.id_tipo_visto_parere
          AND pds.id_determina = dete.id_determina
          AND pds.tipo_soggetto = 'UO_PROPONENTE'
          AND dete.valido = 'Y'
          AND vp.id_determina = dete.id_determina
          AND vp.valido = 'Y'
/


/* Formatted on 09/07/2015 12:46:29 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW RICERCA_VISTO_TESTO
(
   ID_DOCUMENTO,
   TIPO_ATTO,
   TITOLO_TIPOLOGIA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   ANNO,
   NUMERO,
   OGGETTO,
   DATA_ADOZIONE_ATTO,
   DATA_PROPOSTA,
   FIRMATARIO_DOCUMENTO_PADRE,
   TIPO_SOGGETTO_DOC_PADRE,
   UO_PROPONENTE_PROGR,
   UO_PROPONENTE_DAL,
   UO_PROPONENTE_OTTICA,
   UO_PROPONENTE_DESCRIZIONE,
   REGISTRO,
   STATO_ATTO,
   POSIZIONE_FLUSSO,
   VALUTAZIONE,
   UNITA_PROGR,
   UNITA_DAL,
   UNITA_OTTICA,
   FIRMATARIO,
   ID_TIPO_VISTO_PARERE,
   DATA_ADOZIONE_VP,
   ID_COMPETENZA,
   COMP_UTENTE,
   COMP_UNITA_PROGR,
   COMP_UNITA_DAL,
   COMP_UNITA_OTTICA,
   COMP_RUOLO,
   COMP_LETTURA,
   COMP_MODIFICA,
   COMP_CANCELLAZIONE,
   ID_STEP,
   STEP_UTENTE,
   STEP_UNITA_PROGR,
   STEP_UNITA_DAL,
   STEP_UNITA_OTTICA,
   STEP_RUOLO,
   STEP_NOME,
   STEP_DESCRIZIONE,
   ID_FILE_ALLEGATO_TESTO,
   ENTE,
   TESTO
)
AS
   SELECT ricvis.id_documento,
          ricvis.tipo_atto,
          ricvis.titolo_tipologia,
          ricvis.anno_proposta,
          ricvis.numero_proposta,
          ricvis.anno,
          ricvis.numero,
          ricvis.oggetto,
          ricvis.data_adozione_atto,
          ricvis.data_proposta,
          ricvis.firmatario_documento_padre,
          ricvis.tipo_soggetto_doc_padre,
          ricvis.uo_proponente_progr,
          ricvis.uo_proponente_dal,
          ricvis.uo_proponente_ottica,
          ricvis.uo_proponente_descrizione,
          ricvis.registro,
          ricvis.stato_atto,
          ricvis.posizione_flusso,
          ricvis.valutazione,
          ricvis.unita_progr,
          ricvis.unita_dal,
          ricvis.unita_ottica,
          ricvis.firmatario,
          ricvis.id_tipo_visto_parere,
          ricvis.data_adozione_vp,
          ricvis.id_competenza,
          ricvis.comp_utente,
          ricvis.comp_unita_progr,
          ricvis.comp_unita_dal,
          ricvis.comp_unita_ottica,
          ricvis.comp_ruolo,
          ricvis.comp_lettura,
          ricvis.comp_modifica,
          ricvis.comp_cancellazione,
          ricvis.id_step,
          ricvis.step_utente,
          ricvis.step_unita_progr,
          ricvis.step_unita_dal,
          ricvis.step_unita_ottica,
          ricvis.step_ruolo,
          ricvis.step_nome,
          ricvis.step_descrizione,
          ricvis.id_file_allegato_testo,
          ricvis.ente,
          fileallegati.testo
     FROM    ricerca_visto ricvis
          LEFT JOIN
             file_allegati fileallegati
          ON ricvis.id_file_allegato_testo = fileallegati.id_file_allegato
/

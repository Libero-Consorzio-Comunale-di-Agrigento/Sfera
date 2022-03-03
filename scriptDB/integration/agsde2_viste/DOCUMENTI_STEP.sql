--liquibase formatted sql
--changeset rdestasio:install_20210221_viste_01 runOnChange:true

-- QUESTA VISTA VIENE USATA DA AGSPR

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
   TIPO_REGISTRO,
   DATA_ADOZIONE,
   STATO_VISTI_PARERI,
   DATA_SCADENZA,
   PRIORITA,
   data_ordinamento
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
          utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera),
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
          deli.registro_delibera,
          deli.data_adozione,
          utility_pkg.get_stato_visti (deli.id_delibera),
          NULL,
          0,
          NULL
     FROM proposte_delibera pr_deli,
          delibere deli,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          wkf_cfg_step cfg_step,
          tipi_delibera tipo,
          tipi_registro registro
    WHERE     deli.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND deli.id_proposta_delibera = pr_deli.id_proposta_delibera
          AND deli.registro_delibera = registro.tipo_registro(+)
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
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
          utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera),
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
             deli.registro_delibera),
          NULL,
          utility_pkg.get_stato_visti (pr_deli.id_proposta_delibera),
          NULL,
          NVL (pr_deli.priorita, 0),
          pr_deli.data_ordinamento
     FROM proposte_delibera pr_deli,
          delibere deli,
          odg_commissioni comm,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
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
          utility_pkg.get_unita_prop_determina (dete.id_determina),
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
                  dete.registro_determina),
          dete.data_numero_determina,
          utility_pkg.get_stato_visti (dete.id_determina),
          dete.data_scadenza,
          NVL (dete.priorita, 0),
          dete.data_ordinamento
     FROM determine dete,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
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
          utility_pkg.get_unita_prop_determina (dete.id_determina),
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
                  dete.registro_determina),
          dete.data_numero_determina,
          NULL,
          NULL,
          0,
          vp.data_ordinamento
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          determine dete,
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
          utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera),
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
             deli.registro_delibera),
          deli.data_adozione,
          NULL,
          NULL,
          0,
          vp.data_ordinamento
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera pr_deli,
          delibere deli,
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
          utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera),
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
             deli.registro_delibera),
          deli.data_adozione,
          NULL,
          NULL,
          0,
          vp.data_ordinamento
     FROM visti_pareri vp,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera pr_deli,
          delibere deli,
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
                     'IMMEDIATA_ESEGUIBILITA', 'IMMEDIATA ESEGUIBILITA',
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
          utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera),
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
          deli.registro_delibera,
          deli.data_adozione,
          NULL,
          NULL,
          0,
          NULL
     FROM certificati cert,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          proposte_delibera pr_deli,
          delibere deli,
          wkf_cfg_step cfg_step
    WHERE     cert.id_delibera = deli.id_delibera
          AND pr_deli.id_proposta_delibera = deli.id_proposta_delibera
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
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
          utility_pkg.get_unita_prop_determina (dete.id_determina),
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
          dete.registro_determina,
          dete.data_numero_determina,
          NULL,
          NULL,
          0,
          NULL
     FROM certificati cert,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          determine dete,
          wkf_cfg_step cfg_step
    WHERE     cert.id_determina = dete.id_determina
          AND cert.id_engine_iter = iter.id_engine_iter
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND cert.valido = 'Y'
   UNION ALL
   SELECT 'SEDUTA_STAMPA',
          ss.id_seduta_stampa,
          NULL,                                               -- ID_DETERMINA,
          NULL,                                       -- ID_PROPOSTA_DELIBERA,
          NULL,                                                -- ID_DELIBERA,
          NULL,                                            -- ID_VISTO_PARERE,
          ss.id_seduta_stampa,                                  -- ID_CERTIFICATO,
          s.id_seduta,
          NULL,
          C.TITOLO,
          '',
          NULL,                                               -- anno_proposta
          NULL,                     --dete.numero_proposta, -- numero_proposta
          NULL,                       --dete.anno_determina, -- anno_determina
          NULL,                   --dete.numero_determina, -- numero_determina
          'Seduta n.'||s.numero||' del '||to_char(s.data_seduta, 'dd/mm/yyyy')||' - '||C.TITOLO,                                  --dete.oggetto, --oggetto
          d.stato,
          d.stato_firma,
          d.stato_conservazione,
          '',                                                     -- stato_odg
          '',                                              -- unita proponente
          step.id_engine_step,
          a_step.utente,
          a_step.unita_progr,
          a_step.unita_dal,
          a_step.unita_ottica,
          a_step.ruolo,
          cfg_step.nome,
          cfg_step.descrizione,
          cfg_step.titolo,
          d.ente,
          d.riservato,
          '',                                       --dete.registro_determina,
          NULL,                                  --dete.data_numero_determina,
          NULL,
          NULL,
          0,
          NULL
     FROM gdo_documenti d,
          odg_sedute_stampe ss,
          odg_sedute s,
          odg_commissioni_stampe c,
          wkf_engine_step step,
          wkf_engine_step_attori a_step,
          wkf_engine_iter iter,
          wkf_cfg_step cfg_step
    WHERE     ss.id_seduta_stampa = d.id_documento
          AND ss.id_seduta = s.id_seduta
          AND d.id_engine_iter = iter.id_engine_iter
          AND ss.id_commissione_stampa = c.id_commissione_stampa
          AND step.id_engine_step = iter.id_step_corrente
          AND cfg_step.id_cfg_step = step.id_cfg_step
          AND step.id_engine_step = a_step.id_engine_step
          AND iter.data_fine IS NULL
          AND step.data_fine IS NULL
          AND d.valido = 'Y'
/

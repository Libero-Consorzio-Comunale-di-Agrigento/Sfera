--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_22

/* Formatted on 09/02/2016 10:03:36 (QP5 v5.215.12089.38647) */
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

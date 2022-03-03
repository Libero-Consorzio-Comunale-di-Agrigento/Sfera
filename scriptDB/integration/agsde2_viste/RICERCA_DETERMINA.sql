--liquibase formatted sql
--changeset rdestasio:install_20210221_viste_04 runOnChange:true

CREATE OR REPLACE FORCE VIEW RICERCA_DETERMINA
(
    -- dati identificativi del documento
    ID_DOCUMENTO,
    TIPO_DOCUMENTO,
    ID_DOCUMENTO_PRINCIPALE,
    TIPO_DOCUMENTO_PRINCIPALE,
    ENTE,

    -- dati del documento
    OGGETTO,
    RISERVATO,
    STATO,
    ID_CATEGORIA,

    -- soggetti
    UO_PROPONENTE_PROGR,
    UO_PROPONENTE_DAL,
    UO_PROPONENTE_OTTICA,
    UO_PROPONENTE_DESCRIZIONE,
    UTENTE_SOGGETTO,
    TIPO_SOGGETTO,

    -- competenze
    ID_COMPETENZA,
    COMP_UTENTE,
    COMP_UNITA_PROGR,
    COMP_UNITA_DAL,
    COMP_UNITA_OTTICA,
    COMP_RUOLO,
    COMP_LETTURA,
    COMP_MODIFICA,
    COMP_CANCELLAZIONE,

    -- step
    ID_STEP,
    STEP_UTENTE,
    STEP_UNITA_PROGR,
    STEP_UNITA_DAL,
    STEP_UNITA_OTTICA,
    STEP_RUOLO,
    STEP_NOME,
    STEP_DESCRIZIONE,

    -- dati dell'atto
    NUMERO_ATTO,
    ANNO_ATTO,
    REGISTRO_ATTO,
    DATA_ATTO,
    DATA_ADOZIONE,
    DATA_ESECUTIVITA,
    DATA_SCADENZA,

    -- dati della seconda numerazione
    NUMERO_ATTO_2,
    ANNO_ATTO_2,
    REGISTRO_ATTO_2,
    DATA_NUMERO_ATTO_2,

    -- dati della proposta
    NUMERO_PROPOSTA,
    ANNO_PROPOSTA,
    REGISTRO_PROPOSTA,
    DATA_PROPOSTA,

    -- dati di protocollo
    NUMERO_PROTOCOLLO,
    ANNO_PROTOCOLLO,
    REGISTRO_PROTOCOLLO,

    -- tipologia
    ID_TIPOLOGIA,
    TITOLO_TIPOLOGIA,
    DESCRIZIONE_TIPOLOGIA,
    CON_IMPEGNO_SPESA,

    -- dati di pubblicazione
    NUMERO_ALBO,
    ANNO_ALBO,
    DATA_PUBBLICAZIONE,
    DATA_FINE_PUBBLICAZIONE,

    -- dati di conservazione
    LOG_CONSERVAZIONE,
    DATA_CONSERVAZIONE,
    STATO_CONSERVAZIONE,

    -- corte dei conti
    DA_INVIARE_CORTE_CONTI,
    DATA_INVIO_CORTE_CONTI,

    -- dati specifici della ricerca determina
       ID_TIPO_ALLEGATO_DETERMINA,
    ID_TIPO_ALLEGATO_VISTO,
    ID_TESTO,
	ID_FILE_ALLEGATO,
	ID_OGGETTO_RICORRENTE,
	STATO_MARCATURA,
	STATO_FIRMA,
	DATA_ORDINAMENTO,
    ATTO_CONCLUSO,
    CUP,
    CONTO_ECONOMICO,
    CODICE_PROGETTO,
    ID_TIPO_BUDGET,
    IMPORTO
)
AS
   SELECT -- dati identificativi del documento
          d.id_determina                                AS ID_DOCUMENTO,
          CAST ('DETERMINA' AS VARCHAR (255))            AS TIPO_DOCUMENTO,
          d.id_determina                                  AS ID_DOCUMENTO_PRINCIPALE,
          CAST ('DETERMINA' AS VARCHAR (255))              AS TIPO_DOCUMENTO_PRINCIPALE,
          d.ente                                         AS ENTE,

          -- dati del documento
          d.oggetto                                     AS OGGETTO,
          d.riservato                                    AS RISERVATO,
          UPPER (DECODE (d.stato, 'NON_ESECUTIVO', 'ANNULLATO', 'ANNULLATO', d.stato, NVL (sc.titolo, d.stato)))    AS STATO,
          d.id_categoria                                   AS ID_CATEGORIA,

          -- soggetti
          pds.unita_progr    AS UO_PROPONENTE_PROGR,
          pds.unita_dal      AS UO_PROPONENTE_DAL,
          pds.unita_ottica   AS UO_PROPONENTE_OTTICA,
           (SELECT descrizione
             FROM so4_v_unita_organizzative_pubb
            WHERE     progr = uo_prop.unita_progr
                  AND ottica = uo_prop.unita_ottica
                  AND dal = uo_prop.unita_dal)
             AS UO_PROPONENTE_DESCRIZIONE,
          --so4_util.anuo_get_descrizione (uo_prop.unita_progr, uo_prop.unita_dal) AS UO_PROPONENTE_DESCRIZIONE,
          pds.utente         AS UTENTE_SOGGETTO,
          pds.tipo_soggetto  AS TIPO_SOGGETTO,

          -- competenze
          c.id_determina_competenza AS ID_COMPETENZA,
          c.utente                 AS COMP_UTENTE,
          c.unita_progr            AS COMP_UNITA_PROGR,
          c.unita_dal              AS COMP_UNITA_DAL,
          c.unita_ottica           AS COMP_UNITA_OTTICA,
          c.ruolo                  AS COMP_RUOLO,
          c.lettura                AS COMP_LETTURA,
          c.modifica               AS COMP_MODIFICA,
          c.cancellazione          AS COMP_CANCELLAZIONE,

          -- step
          s.id_engine_step            AS ID_STEP,
          sa.utente                 AS STEP_UTENTE,
          sa.unita_progr             AS STEP_UNITA_PROGR,
          sa.unita_dal                 AS STEP_UNITA_DAL,
          sa.unita_ottica             AS STEP_UNITA_OTTICA,
          sa.ruolo                     AS STEP_RUOLO,
          sc.titolo                    AS STEP_NOME,
          sc.descrizione             AS STEP_DESCRIZIONE,

          -- dati dell'atto
          d.numero_determina          AS NUMERO_ATTO,
          d.anno_determina            AS ANNO_ATTO,
          d.registro_determina        AS REGISTRO_ATTO,
          trunc(d.data_numero_determina)     AS DATA_ATTO,
          trunc(d.data_numero_determina)   AS DATA_ADOZIONE,
          trunc(d.data_esecutivita)         AS DATA_ESECUTIVITA,
          trunc(d.data_scadenza)             AS DATA_SCADENZA,

          -- dati della seconda numerazione
          d.numero_determina_2        AS NUMERO_ATTO_2,
          d.anno_determina_2           AS ANNO_ATTO_2,
          d.registro_determina_2       AS REGISTRO_ATTO_2,
          d.data_numero_determina_2     AS DATA_NUMERO_ATTO_2,

          -- dati della proposta
          d.numero_proposta          AS NUMERO_PROPOSTA,
          d.anno_proposta            AS ANNO_PROPOSTA,
          d.registro_proposta        AS REGISTRO_PROPOSTA,
          trunc(d.data_proposta)            AS DATA_PROPOSTA,

          -- dati di protocollo
          d.numero_protocollo        AS NUMERO_PROTOCOLLO,
          d.anno_protocollo         AS ANNO_PROTOCOLLO,
          d.registro_protocollo      AS REGISTRO_PROTOCOLLO,

          -- tipologia
          d.id_tipo_determina       AS ID_TIPOLOGIA,
          td.titolo                    AS TITOLO_TIPOLOGIA,
          td.descrizione               AS DESCRIZIONE_TIPOLOGIA,
          tvp.contabile                AS CON_IMPEGNO_SPESA,

          -- dati di pubblicazione
          d.numero_albo              AS NUMERO_ALBO,
          d.anno_albo                AS ANNO_ALBO,
          trunc(d.data_pubblicazione)      AS DATA_PUBBLICAZIONE,
          trunc(d.data_fine_pubblicazione)    AS DATA_FINE_PUBBLICAZIONE,

          -- dati di conservazione
          --jcons_pkg.get_log_conservazione (d.id_documento_esterno) AS LOG_CONSERVAZIONE,
          (SELECT LOG
           FROM JCONS_V_LOG_CONSERVAZIONE j
          WHERE j.id_documento_rif = d.id_documento_esterno
                AND NOT EXISTS
                           (SELECT 1
                              FROM JCONS_V_LOG_CONSERVAZIONE jj
                             WHERE     jj.id_documento_rif = j.id_documento_rif
                                   AND jj.id_documento > j.id_documento)
                        ) AS LOG_CONSERVAZIONE,
          jcons_pkg.get_data_conservazione (d.id_documento_esterno) AS DATA_CONSERVAZIONE,
          d.stato_conservazione     AS STATO_CONSERVAZIONE,

          -- corte dei conti
          d.da_inviare_corte_conti     AS DA_INVIARE_CORTE_CONTI,
          trunc(d.data_invio_corte_conti)     AS DATA_INVIO_CORTE_CONTI,

          -- dati specifici della ricerca delibera
          alle.id_tipo_allegato     AS ID_TIPO_ALLEGATO_DETERMINA,
          alle_vp.id_tipo_allegato  AS ID_TIPO_ALLEGATO_VISTO,

          -- questo campo serve per poi poter fare la ricerca sul testo.
          d.id_file_allegato_testo    AS ID_TESTO,
          af.id_file AS ID_FILE_ALLEGATO,
          d.id_oggetto_ricorrente ID_OGGETTO_RICORRENTE,
          d.stato_marcatura STATO_MARCATURA,
          d.stato_firma STATO_FIRMA,
          d.data_ordinamento AS DATA_ORDINAMENTO,
          CAST (utility_pkg.get_determina_conclusa(d.id_determina) AS CHAR (1)) ATTO_CONCLUSO,
          (select valore from dati_aggiuntivi da where da.codice = 'CUP' and da.id_determina = d.id_determina) CUP,
          b.conto_economico CONTO_ECONOMICO,
          b.codice_progetto CODICE_PROGETTO,
          b.id_tipo_budget ID_TIPO_BUDGET,
          b.importo IMPORTO
     FROM determine_competenze c,
          determine_soggetti pds,
          determine_soggetti uo_prop,
          wkf_cfg_step sc,
          wkf_engine_step_attori sa,
          wkf_engine_step s,
          wkf_engine_iter i,
          tipi_visto_parere tvp,
          allegati alle_vp,
          visti_pareri vp,
          allegati alle,
          tipi_determina td,
          determine d,
          categorie cat,
          allegati_file af,
          budget b
    WHERE     tvp.id_tipo_visto_parere(+) = vp.id_tipologia
          AND c.id_determina = d.id_determina
          AND cat.id_categoria(+) = d.id_categoria
          AND i.id_engine_iter(+) = d.id_engine_iter
          AND s.id_engine_step(+) = i.id_step_corrente
          AND sa.id_engine_step(+) = s.id_engine_step
          AND sc.id_cfg_step(+) = s.id_cfg_step
          AND alle_vp.id_visto_parere(+) = vp.id_visto_parere
          AND alle.id_determina(+) = d.id_determina
          AND vp.id_determina(+) = d.id_determina
          AND vp.valido(+) = 'Y'
          AND uo_prop.id_determina = d.id_determina
          AND uo_prop.tipo_soggetto = 'UO_PROPONENTE'
          AND pds.id_determina = d.id_determina
          AND td.id_tipo_determina = d.id_tipo_determina
          AND af.id_allegato(+) = alle.id_allegato
          AND d.valido = 'Y'
          AND b.id_determina(+) = d.id_determina
/

--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_13

CREATE OR REPLACE FORCE VIEW RICERCA_VISTO
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
	STATO_CONSERVAZIONE,

	-- corte dei conti
	DA_INVIARE_CORTE_CONTI,
	DATA_INVIO_CORTE_CONTI,

	-- id del testo per fare la ricerca full-text
	ID_TESTO,
	
	-- dati specifici della ricerca visto
    ID_TIPO_ALLEGATO_VISTO,
    ESITO,
    UO_REDAZIONE_PROGR,
    UO_REDAZIONE_DAL,
    UO_REDAZIONE_OTTICA,
    UTENTE_FIRMATARIO
)
AS
   SELECT -- dati identificativi del documento
          vp.id_visto_parere             		AS ID_DOCUMENTO,
          CAST ('VISTO' AS VARCHAR (255))       AS TIPO_DOCUMENTO,
          vp.id_determina                       AS ID_DOCUMENTO_PRINCIPALE,
          CAST ('DETERMINA' AS VARCHAR (255))   AS TIPO_DOCUMENTO_PRINCIPALE,
          vp.ente                               AS ENTE,

          -- dati del documento
          dete.oggetto                          AS OGGETTO,
          dete.riservato                        AS RISERVATO,
          UPPER (DECODE (vp.stato, 'ANNULLATO', vp.stato, NVL (sc.titolo, vp.stato)))    AS STATO,
          dete.id_categoria                     AS ID_CATEGORIA,

          -- soggetti
          pds.unita_progr    AS UO_PROPONENTE_PROGR,
          pds.unita_dal      AS UO_PROPONENTE_DAL,
          pds.unita_ottica   AS UO_PROPONENTE_OTTICA,
          so4_util.anuo_get_descrizione (pds.unita_progr, pds.unita_dal) AS UO_PROPONENTE_DESCRIZIONE,
          pds.utente         AS UTENTE_SOGGETTO,
          pds.tipo_soggetto  AS TIPO_SOGGETTO,

          -- competenze
          comp.id_visto_parere_competenza     	AS ID_COMPETENZA,
          comp.utente                         	AS COMP_UTENTE,
          comp.unita_progr                   	AS COMP_UNITA_PROGR,
          comp.unita_dal                      	AS COMP_UNITA_DAL,
          comp.unita_ottica                   	AS COMP_UNITA_OTTICA,
          comp.ruolo                          	AS COMP_RUOLO,
          comp.lettura                        	AS COMP_LETTURA,
          comp.modifica                       	AS COMP_MODIFICA,
          comp.cancellazione                  	AS COMP_CANCELLAZIONE,

          -- step
          s.id_engine_step	AS ID_STEP,
          sa.utente         AS STEP_UTENTE,
          sa.unita_progr    AS STEP_UNITA_PROGR,
          sa.unita_dal      AS STEP_UNITA_DAL,
          sa.unita_ottica   AS STEP_UNITA_OTTICA,
          sa.ruolo          AS STEP_RUOLO,
          sc.nome           AS STEP_NOME,
          sc.descrizione    AS STEP_DESCRIZIONE,

          -- dati dell'atto
          dete.numero_determina         AS NUMERO_ATTO,
          dete.anno_determina           AS ANNO_ATTO,
          dete.registro_determina       AS REGISTRO_ATTO,
          trunc(dete.data_numero_determina)    AS DATA_ATTO,
          trunc(dete.data_numero_determina)	AS DATA_ADOZIONE,
          trunc(dete.data_esecutivita)         AS DATA_ESECUTIVITA,

          -- dati della proposta
          dete.numero_proposta          AS NUMERO_PROPOSTA,
          dete.anno_proposta            AS ANNO_PROPOSTA,
          dete.registro_proposta        AS REGISTRO_PROPOSTA,
          trunc(dete.data_proposta)            AS DATA_PROPOSTA,

          -- dati di protocollo
          dete.numero_protocollo        AS NUMERO_PROTOCOLLO,
          dete.anno_protocollo             AS ANNO_PROTOCOLLO,
          dete.registro_protocollo      AS REGISTRO_PROTOCOLLO,

          -- tipologia
          vp.id_tipologia               AS ID_TIPOLOGIA,
          tipo.titolo                   AS TITOLO_TIPOLOGIA,
          tipo.descrizione              AS DESCRIZIONE_TIPOLOGIA,
          tipo.contabile                AS CON_IMPEGNO_SPESA,

          -- dati di pubblicazione
          dete.numero_albo              AS NUMERO_ALBO,
          dete.anno_albo                AS ANNO_ALBO,
          trunc(dete.data_pubblicazione)       AS DATA_PUBBLICAZIONE,
          trunc(dete.data_fine_pubblicazione)  AS DATA_FINE_PUBBLICAZIONE,

          -- dati di conservazione
          jcons_pkg.get_log_conservazione (dete.id_documento_esterno) AS LOG_CONSERVAZIONE,
          dete.stato_conservazione     	AS STATO_CONSERVAZIONE,

          -- corte dei conti
          dete.da_inviare_corte_conti   AS DA_INVIARE_CORTE_CONTI,
          trunc(dete.data_invio_corte_conti)   AS DATA_INVIO_CORTE_CONTI,

          -- questo campo serve per poi poter fare la ricerca sul testo.
          vp.id_file_allegato_testo     AS ID_TESTO,
          
          -- dati specifici della ricerca visto
          alle.id_tipo_allegato         AS ID_TIPO_ALLEGATO_VISTO,
          
          -- dati specifici del visto
          vp.esito                      AS ESITO,
          vp.unita_progr                AS UO_REDAZIONE_PROGR,
          vp.unita_dal                  AS UO_REDAZIONE_DAL,
          vp.unita_ottica               AS UO_REDAZIONE_OTTICA,
          vp.utente_firmatario          AS UTENTE_FIRMATARIO
     
     FROM wkf_engine_iter iter,
          wkf_engine_step s,
          wkf_engine_step_attori sa,
          wkf_cfg_step sc,
          allegati alle,
          determine_soggetti pds,
          determine dete,
          visti_pareri vp,
          visti_pareri_competenze comp,
          tipi_visto_parere tipo
    WHERE     sc.id_cfg_step(+) = s.id_cfg_step
          AND sa.id_engine_step(+) = s.id_engine_step
          AND s.id_engine_step(+) = iter.id_step_corrente
          AND iter.id_engine_iter(+) = vp.id_engine_iter
          AND alle.id_visto_parere(+) = vp.id_visto_parere
          AND comp.id_visto_parere = vp.id_visto_parere
          AND vp.id_tipologia = tipo.id_tipo_visto_parere
          AND pds.id_determina = dete.id_determina
          AND pds.tipo_soggetto = 'UO_PROPONENTE'
          AND dete.valido = 'Y'
          AND vp.id_determina = dete.id_determina
          AND vp.valido = 'Y'
/

--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_10

CREATE OR REPLACE FORCE VIEW RICERCA_DELIBERA
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

	-- dati specifici della ricerca delibera
   	ID_COMMISSIONE,
	ID_OGGETTO_SEDUTA,
   	ID_TIPO_ALLEGATO_DELIBERA,
	ID_TIPO_ALLEGATO_PARERE,
	ID_TESTO
)
AS
   SELECT -- dati identificativi del documento
          prop.id_proposta_delibera                     AS ID_DOCUMENTO,
          decode (deli.id_delibera, null, 'PROPOSTA_DELIBERA', 'DELIBERA')  AS TIPO_DOCUMENTO,
          deli.id_delibera                              AS ID_DOCUMENTO_PRINCIPALE,
          CAST ('DELIBERA'          AS VARCHAR2 (255))  AS TIPO_DOCUMENTO_PRINCIPALE,
          prop.ente                                     AS ENTE,

          -- dati del documento
          NVL (deli.oggetto, prop.oggetto) AS OGGETTO,
          CAST (NVL (deli.riservato, prop.riservato) AS CHAR (1)) AS RISERVATO,
          UPPER (
             DECODE (
                NVL (deli.stato, prop.stato),
                'ANNULLATO', NVL (deli.stato, prop.stato),
                NVL (NVL (de_cfg_step.titolo, pr_cfg_step.titolo),
                     NVL (deli.stato, prop.stato))))    AS STATO,
          pr_cat.id_categoria                           AS ID_CATEGORIA,

          -- soggetti
          pr_sog.unita_progr    AS UO_PROPONENTE_PROGR,
          pr_sog.unita_dal      AS UO_PROPONENTE_DAL,
          pr_sog.unita_ottica   AS UO_PROPONENTE_OTTICA,
          so4_util.anuo_get_descrizione (pr_uo_prop.unita_progr, pr_uo_prop.unita_dal) AS UO_PROPONENTE_DESCRIZIONE,
          pr_sog.utente         AS UTENTE_SOGGETTO,
          pr_sog.tipo_soggetto  AS TIPO_SOGGETTO,

          -- competenze
          de_comp.id_delibera_competenza AS ID_COMPETENZA,
          de_comp.utente                 AS COMP_UTENTE,
          de_comp.unita_progr            AS COMP_UNITA_PROGR,
          de_comp.unita_dal              AS COMP_UNITA_DAL,
          de_comp.unita_ottica           AS COMP_UNITA_OTTICA,
          de_comp.ruolo                  AS COMP_RUOLO,
          de_comp.lettura                AS COMP_LETTURA,
          de_comp.modifica               AS COMP_MODIFICA,
          de_comp.cancellazione          AS COMP_CANCELLAZIONE,

          -- step
          NVL (de_step.id_engine_step, pr_step.id_engine_step)   AS ID_STEP,
          NVL (de_attori.utente, pr_attori.utente)               AS STEP_UTENTE,
          NVL (de_attori.unita_progr, pr_attori.unita_progr)     AS STEP_UNITA_PROGR,
          NVL (de_attori.unita_dal, pr_attori.unita_dal)         AS STEP_UNITA_DAL,
          NVL (de_attori.unita_ottica, pr_attori.unita_ottica)   AS STEP_UNITA_OTTICA,
          NVL (de_attori.ruolo, pr_attori.ruolo)                 AS STEP_RUOLO,
          NVL (de_cfg_step.nome, pr_cfg_step.nome)               AS STEP_NOME,
          NVL (de_cfg_step.descrizione, pr_cfg_step.descrizione) AS STEP_DESCRIZIONE,

          -- dati dell'atto
          deli.numero_delibera      AS NUMERO_ATTO,
          deli.anno_delibera        AS ANNO_ATTO,
          deli.registro_delibera    AS REGISTRO_ATTO,
          trunc(deli.data_numero_delibera) AS DATA_ATTO,
          trunc(deli.data_adozione)        AS DATA_ADOZIONE,
          trunc(deli.data_esecutivita)     AS DATA_ESECUTIVITA,

          -- dati della proposta
          prop.numero_proposta      AS NUMERO_PROPOSTA,
          prop.anno_proposta        AS ANNO_PROPOSTA,
          prop.registro_proposta    AS REGISTRO_PROPOSTA,
          trunc(prop.data_proposta)        AS DATA_PROPOSTA,

          -- dati di protocollo
          deli.numero_protocollo    AS NUMERO_PROTOCOLLO,
          deli.anno_protocollo      AS ANNO_PROTOCOLLO,
          deli.registro_protocollo  AS REGISTRO_PROTOCOLLO,

          -- tipologia
          prop.id_tipo_delibera     AS ID_TIPOLOGIA,
          de_tipi.titolo            AS TITOLO_TIPOLOGIA,
          de_tipi.descrizione       AS DESCRIZIONE_TIPOLOGIA,
          pr_tipo_par.contabile		AS CON_IMPEGNO_SPESA,

          -- dati di pubblicazione
          deli.numero_albo          AS NUMERO_ALBO,
          deli.anno_albo            AS ANNO_ALBO,
          trunc(deli.data_pubblicazione)       AS DATA_PUBBLICAZIONE,
          trunc(deli.data_fine_pubblicazione)  AS DATA_FINE_PUBBLICAZIONE,

          -- dati di conservazione
          jcons_pkg.get_log_conservazione (deli.id_documento_esterno) AS LOG_CONSERVAZIONE,
          deli.stato_conservazione AS STATO_CONSERVAZIONE,

          -- corte dei conti
          deli.da_inviare_corte_conti AS DA_INVIARE_CORTE_CONTI,
          trunc(deli.data_invio_corte_conti) AS DATA_INVIO_CORTE_CONTI,

          -- dati specifici della ricerca delibera
   		  prop.id_commissione 			AS ID_COMMISSIONE,
          deli.id_oggetto_seduta		AS ID_OGGETTO_SEDUTA,
          de_alle.id_tipo_allegato      AS ID_TIPO_ALLEGATO_DELIBERA,
          de_alle_par.id_tipo_allegato  AS ID_TIPO_ALLEGATO_PARERE,

          -- questo campo serve per poi poter fare la ricerca sul testo.
          NVL (deli.id_file_allegato_testo, prop.id_file_allegato_testo) AS ID_TESTO

     FROM proposte_delibera prop,
          delibere deli,
          delibere_competenze de_comp,
          tipi_delibera de_tipi,
          proposte_delibera_soggetti pr_sog,
          proposte_delibera_soggetti pr_uo_prop,
          wkf_engine_iter de_iter,
          wkf_engine_step de_step,
          wkf_engine_step_attori de_attori,
          wkf_cfg_step de_cfg_step,
          wkf_engine_iter pr_iter,
          wkf_engine_step pr_step,
          wkf_engine_step_attori pr_attori,
          wkf_cfg_step pr_cfg_step,
          allegati de_alle,
          allegati de_alle_par,
          categorie pr_cat,
          visti_pareri pr_par,
          visti_pareri de_par,
          tipi_visto_parere pr_tipo_par
    WHERE     pr_sog.id_proposta_delibera(+) 		= prop.id_proposta_delibera
          AND pr_uo_prop.tipo_soggetto(+) 			= 'UO_PROPONENTE'
          AND pr_uo_prop.id_proposta_delibera(+) 	= prop.id_proposta_delibera
          AND de_iter.id_engine_iter(+) 		= deli.id_engine_iter
          AND de_step.id_engine_step(+) 		= de_iter.id_step_corrente
          AND de_attori.id_engine_step(+) 		= de_step.id_engine_step
          AND de_cfg_step.id_cfg_step(+) 		= de_step.id_cfg_step
          AND pr_iter.id_engine_iter(+) 		= prop.id_engine_iter
          AND pr_step.id_engine_step(+) 		= pr_iter.id_step_corrente
          AND pr_attori.id_engine_step(+) 		= pr_step.id_engine_step
          AND pr_cfg_step.id_cfg_step(+) 		= pr_step.id_cfg_step
          AND de_alle_par.id_visto_parere(+) 	= de_par.id_visto_parere
          AND de_alle.id_delibera(+) = deli.id_delibera
          AND pr_cat.id_categoria(+) = prop.id_categoria
          AND pr_tipo_par.id_tipo_visto_parere(+) = pr_par.id_tipologia
          AND pr_par.valido(+) = 'Y'
          AND pr_par.id_proposta_delibera(+) = prop.id_proposta_delibera
          AND de_par.valido(+) = 'Y'
          AND de_par.id_delibera(+) = deli.id_delibera
          AND de_comp.id_delibera = deli.id_delibera
          AND de_tipi.id_tipo_delibera = prop.id_tipo_delibera
          AND pr_sog.id_proposta_delibera = prop.id_proposta_delibera
          AND prop.valido = 'Y'
          AND deli.id_proposta_delibera = prop.id_proposta_delibera
          AND deli.valido = 'Y'
   UNION ALL
   SELECT -- dati identificativi del documento
          prop.id_proposta_delibera                     AS ID_DOCUMENTO,
          decode (deli.id_delibera, null, 'PROPOSTA_DELIBERA', 'DELIBERA')  AS TIPO_DOCUMENTO,
          deli.id_delibera                              AS ID_DOCUMENTO_PRINCIPALE,
          CAST ('DELIBERA'          AS VARCHAR2 (255))  AS TIPO_DOCUMENTO_PRINCIPALE,
          prop.ente                                     AS ENTE,

          -- dati del documento
          NVL (deli.oggetto, prop.oggetto) AS OGGETTO,
          CAST (NVL (deli.riservato, prop.riservato) AS CHAR (1)) AS RISERVATO,
          UPPER (
             DECODE (
                NVL (deli.stato, prop.stato),
                'ANNULLATO', NVL (deli.stato, prop.stato),
                NVL (NVL (de_cfg_step.titolo, pr_cfg_step.titolo),
                     NVL (deli.stato, prop.stato))))    AS STATO,
          pr_cat.id_categoria                           AS ID_CATEGORIA,

          -- soggetti
          pr_sog.unita_progr    AS UO_PROPONENTE_PROGR,
          pr_sog.unita_dal      AS UO_PROPONENTE_DAL,
          pr_sog.unita_ottica   AS UO_PROPONENTE_OTTICA,
          so4_util.anuo_get_descrizione (pr_uo_prop.unita_progr, pr_uo_prop.unita_dal) AS UO_PROPONENTE_DESCRIZIONE,
          pr_sog.utente         AS UTENTE_SOGGETTO,
          pr_sog.tipo_soggetto  AS TIPO_SOGGETTO,

          -- competenze
          pr_comp.id_proposta_delibera_comp AS ID_COMPETENZA,
          pr_comp.utente                 AS COMP_UTENTE,
          pr_comp.unita_progr            AS COMP_UNITA_PROGR,
          pr_comp.unita_dal              AS COMP_UNITA_DAL,
          pr_comp.unita_ottica           AS COMP_UNITA_OTTICA,
          pr_comp.ruolo                  AS COMP_RUOLO,
          pr_comp.lettura                AS COMP_LETTURA,
          pr_comp.modifica               AS COMP_MODIFICA,
          pr_comp.cancellazione          AS COMP_CANCELLAZIONE,

          -- step
          NVL (de_step.id_engine_step, pr_step.id_engine_step)   AS ID_STEP,
          NVL (de_attori.utente, pr_attori.utente)               AS STEP_UTENTE,
          NVL (de_attori.unita_progr, pr_attori.unita_progr)     AS STEP_UNITA_PROGR,
          NVL (de_attori.unita_dal, pr_attori.unita_dal)         AS STEP_UNITA_DAL,
          NVL (de_attori.unita_ottica, pr_attori.unita_ottica)   AS STEP_UNITA_OTTICA,
          NVL (de_attori.ruolo, pr_attori.ruolo)                 AS STEP_RUOLO,
          NVL (de_cfg_step.nome, pr_cfg_step.nome)               AS STEP_NOME,
          NVL (de_cfg_step.descrizione, pr_cfg_step.descrizione) AS STEP_DESCRIZIONE,

          -- dati dell'atto
          deli.numero_delibera      AS NUMERO_ATTO,
          deli.anno_delibera        AS ANNO_ATTO,
          DELI.REGISTRO_DELIBERA    AS REGISTRO_ATTO,
          trunc(deli.data_numero_delibera) AS DATA_ATTO,
          trunc(deli.data_adozione)        AS DATA_ADOZIONE,
          trunc(deli.data_esecutivita)     AS DATA_ESECUTIVITA,

          -- dati della proposta
          prop.numero_proposta      AS NUMERO_PROPOSTA,
          prop.anno_proposta        AS ANNO_PROPOSTA,
          prop.registro_proposta    AS REGISTRO_PROPOSTA,
          trunc(prop.data_proposta)        AS DATA_PROPOSTA,

          -- dati di protocollo
          deli.numero_protocollo    AS NUMERO_PROTOCOLLO,
          deli.anno_protocollo      AS ANNO_PROTOCOLLO,
          deli.registro_protocollo  AS REGISTRO_PROTOCOLLO,

          -- tipologia
          prop.id_tipo_delibera     AS ID_TIPOLOGIA,
          pr_tipo.titolo            AS TITOLO_TIPOLOGIA,
          pr_tipo.descrizione       AS DESCRIZIONE_TIPOLOGIA,
          pr_tipo_par.contabile		AS CON_IMPEGNO_SPESA,

          -- dati di pubblicazione
          deli.numero_albo          	AS NUMERO_ALBO,
          deli.anno_albo            	AS ANNO_ALBO,
          trunc(deli.data_pubblicazione)       AS DATA_PUBBLICAZIONE,
          trunc(deli.data_fine_pubblicazione)  AS DATA_FINE_PUBBLICAZIONE,

          -- dati di conservazione
          jcons_pkg.get_log_conservazione (deli.id_documento_esterno) AS LOG_CONSERVAZIONE,
          deli.stato_conservazione AS STATO_CONSERVAZIONE,

          -- corte dei conti
          CAST (NVL (deli.da_inviare_corte_conti, prop.da_inviare_corte_conti) AS CHAR (1)) AS DA_INVIARE_CORTE_CONTI,
          trunc(deli.data_invio_corte_conti) AS DATA_INVIO_CORTE_CONTI,

          -- dati specifici della ricerca delibera
   		  prop.id_commissione 			AS ID_COMMISSIONE,
          deli.id_oggetto_seduta		AS ID_OGGETTO_SEDUTA,
          pr_alle.id_tipo_allegato      AS ID_TIPO_ALLEGATO_DELIBERA,
          pr_alle_par.id_tipo_allegato  AS ID_TIPO_ALLEGATO_PARERE,

          -- questo campo serve per poi poter fare la ricerca sul testo.
          NVL (deli.id_file_allegato_testo, prop.id_file_allegato_testo) AS ID_TESTO

     FROM proposte_delibera prop,
          delibere deli,
          proposte_delibera_competenze pr_comp,
          tipi_delibera pr_tipo,
          proposte_delibera_soggetti pr_sog,
          proposte_delibera_soggetti pr_uo_prop,
          wkf_engine_iter de_iter,
          wkf_engine_step de_step,
          wkf_engine_step_attori de_attori,
          wkf_cfg_step de_cfg_step,
          wkf_engine_iter pr_iter,
          wkf_engine_step pr_step,
          wkf_engine_step_attori pr_attori,
          wkf_cfg_step pr_cfg_step,
          allegati pr_alle,
          allegati pr_alle_par,
          categorie pr_cat,
          visti_pareri pr_par,
          tipi_visto_parere pr_tipo_par
    WHERE     deli.id_proposta_delibera(+) = prop.id_proposta_delibera
          AND deli.valido(+) = 'Y'
          AND pr_uo_prop.tipo_soggetto(+) = 'UO_PROPONENTE'
          AND pr_uo_prop.id_proposta_delibera(+) = prop.id_proposta_delibera
          AND pr_sog.id_proposta_delibera(+) = prop.id_proposta_delibera
          AND de_iter.id_engine_iter(+) = deli.id_engine_iter
          AND de_step.id_engine_step(+) = de_iter.id_step_corrente
          AND de_attori.id_engine_step(+) = de_step.id_engine_step
          AND de_cfg_step.id_cfg_step(+) = de_step.id_cfg_step
          AND pr_iter.id_engine_iter(+) = prop.id_engine_iter
          AND pr_step.id_engine_step(+) = pr_iter.id_step_corrente
          AND pr_attori.id_engine_step(+) = pr_step.id_engine_step
          AND pr_cfg_step.id_cfg_step(+) = pr_step.id_cfg_step
          AND pr_alle.id_proposta_delibera(+) = prop.id_proposta_delibera
          AND pr_alle_par.id_visto_parere(+) = pr_par.id_visto_parere
          AND pr_cat.id_categoria(+) = prop.id_categoria
          AND pr_par.id_proposta_delibera(+) = prop.id_proposta_delibera
          AND pr_par.valido(+) = 'Y'
          AND pr_tipo_par.id_tipo_visto_parere(+) = pr_par.id_tipologia
          AND pr_comp.id_proposta_delibera = prop.id_proposta_delibera
          AND pr_tipo.id_tipo_delibera = prop.id_tipo_delibera
          AND pr_uo_prop.id_proposta_delibera = prop.id_proposta_delibera
          AND prop.valido = 'Y'
/

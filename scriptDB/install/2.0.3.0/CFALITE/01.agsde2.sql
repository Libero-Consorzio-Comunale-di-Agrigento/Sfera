/* Formatted on 24/04/2015 17.14.19 (QP5 v5.114.809.3010) */
CREATE OR REPLACE FORCE VIEW CF4_DEL
(
   ID_DOCUMENTO,
   ANNO_DEL,
   NUMERO_DEL,
   SEDE_DEL,
   DATA_DEL,
   DESCRIZIONE,
   TIPO_ESEC,
   NUMERO_APP,
   DATA_APP,
   D_DATA_DEL,
   D_DATA_APP,
   ANNO_PRO,
   NUMERO_PRO,
   UNITA_PRO
)
AS
   SELECT   dete.id_determina,
   			dete.anno_determina anno_del,
            dete.numero_determina,
            tr.registro_esterno,
            TO_NUMBER (TO_CHAR (dete.data_numero_determina, 'J')),
            SUBSTR (dete.oggetto, 1, 140),
            CASE
               WHEN dete.stato = 'ANNULLATO' THEN 4               -- ANNULLATO
               WHEN dete.stato = 'NON_ESECUTIVO' THEN 4           -- ANNULLATO
               WHEN dete.data_esecutivita IS NOT NULL THEN 1      -- ESECUTIVO
               ELSE 5                               --IN ATTESA DI ESECUTIVITA
            END
               CASE,
            NULL,
            TO_NUMBER (NULL),
            TRUNC (dete.data_numero_determina),
            TRUNC (data_esecutivita),
            dete.anno_proposta,
            dete.numero_proposta,
            'PRP'
     FROM   determine dete, tipi_registro tr
    WHERE       dete.numero_determina IS NOT NULL
            AND dete.valido = 'Y'
            AND NVL (dete.stato, 'X') NOT IN ('ANNULLATO')
            AND tr.tipo_registro = dete.registro_determina
            AND tr.registro_esterno IS NOT NULL
            AND NOT EXISTS
                  (SELECT   '1'
                     FROM   delibere deli, tipi_registro tr_del
                    WHERE       deli.registro_delibera = tr.tipo_registro
                            AND deli.anno_delibera = dete.anno_determina
                            AND tr_del.registro_esterno = tr.registro_esterno
                            AND deli.numero_delibera = dete.numero_determina)
   UNION ALL
   SELECT   deli.id_delibera,
   			deli.anno_delibera,
            deli.numero_delibera,
            tr.registro_esterno,
            TO_NUMBER (TO_CHAR (deli.data_adozione, 'J')),
            deli.oggetto,
            case -- Il dizionario di questi valori è la tabella CF4.T10
              when deli.eseguibilita_immediata = 'Y' then
                 3 -- immediatamente eseguibile
              when deli.data_esecutivita is not null then
                 2 -- esecutiva per decorrenza termini
              when deli.stato = 'ANNULLATO' then
                 4 -- dete annullata
              else
                 5 -- in attesa di esecutività
           end
              as tipo_esec,
            NULL                                              --del.numero_app
                ,
            TO_NUMBER (NULL)                                    --del.data_app
                            ,
            TRUNC (deli.data_adozione),
            NULL                                             --deli.d_data_app
                ,
            pd.anno_proposta,
            pd.numero_proposta,
            'PRP'                                             --deli.unita_pro
     FROM   proposte_delibera pd, delibere deli, tipi_registro tr
    WHERE   deli.registro_delibera = tr.tipo_registro
            AND tr.registro_esterno IS NOT NULL
            AND deli.id_proposta_delibera = pd.id_proposta_delibera
            AND NOT EXISTS
                  (SELECT   '1'
                     FROM   determine dete, tipi_registro tr_dete
                    WHERE   dete.registro_determina = tr.tipo_registro
                            AND dete.anno_determina = deli.anno_delibera
                            AND tr_dete.registro_esterno =
                                  tr.registro_esterno
                            AND dete.numero_determina = deli.numero_delibera);
/

/* Formatted on 24/04/2015 17.14.42 (QP5 v5.114.809.3010) */
CREATE OR REPLACE FORCE VIEW CF4_PROPOSTE
(
   ID_DOCUMENTO,
   ANNO,
   UNITA_PROPONENTE,
   NUMERO,
   OGGETTO,
   DATA_PROPOSTA,
   IMPEGNO,
   ANNO_CLA,
   ANNULLATA,
   CLASSIFICAZIONE,
   CLASS_DAL,
   COMMISSIONE,
   COMPLETA,
   DATA_MODIFICA,
   DATA_RICEVIMENTO,
   DATA_TRASMISSIONE,
   ESECUTIVITA,
   FILE_PROPOSTA,
   FILE_RELAZIONE,
   FIRMATA,
   ITER,
   NOTE,
   NUMERO_CLA,
   RELATORE,
   SUB,
   TIPO_PROPOSTA,
   TIPO_TRATTAMENTO,
   UNITA_UBICATA,
   UTENTE_MODIFICA,
   WORKFLOW,
   TIPOLOGIA,
   stato_proposta
)
AS
   SELECT   d.id_determina,
   			anno_proposta anno,
            'PRP' unita_proponente,
            numero_proposta numero,
            oggetto,
            data_proposta,
            (SELECT   DECODE (COUNT (1), 0, 'N', 'Y')
               FROM   visti_pareri vp, tipi_visto_parere tvp
              WHERE       d.id_determina = vp.id_determina
                      AND vp.id_tipologia = tvp.id_tipo_visto_parere
                      AND tvp.contabile = 'Y'
                      AND vp.valido = 'Y')
               impegno,
            NULL anno_cla,
            NULL annullata,
            NULL classificazione,
            NULL class_dal,
            NULL commissione,
            NULL completa,
            NULL data_modifica,
            NULL data_ricevimento,
            NULL data_trasmissione,
            NULL esecutivita,
            NULL file_proposta,
            NULL file_relazione,
            NULL firmata,
            NULL iter,
            NULL note,
            NULL numero_cla,
            NULL relatore,
            NULL sub,
            NULL tipo_proposta,
            NULL tipo_trattamento,
            NULL unita_ubicata,
            NULL utente_modifica,
            NULL workflow,
            NULL tipologia,
            NULL stato_proposta
     FROM   determine d
    WHERE       d.valido = 'Y'
            AND d.anno_proposta IS NOT NULL
            AND numero_proposta IS NOT NULL
   UNION
   SELECT   pd.id_proposta_delibera,
   			anno_proposta anno,
            'PRP' unita_proponente,
            numero_proposta numero,
            oggetto,
            data_proposta,
            (SELECT   DECODE (COUNT (1), 0, 'N', 'Y')
               FROM   visti_pareri vp, tipi_visto_parere tvp
              WHERE       pd.id_proposta_delibera = vp.id_proposta_delibera
                      AND vp.id_tipologia = tvp.id_tipo_visto_parere
                      AND tvp.contabile = 'Y'
                      AND vp.valido = 'Y')
               impegno,
            NULL anno_cla,
            NULL annullata,
            NULL classificazione,
            NULL class_dal,
            NULL commissione,
            NULL completa,
            NULL data_modifica,
            NULL data_ricevimento,
            NULL data_trasmissione,
            NULL esecutivita,
            NULL file_proposta,
            NULL file_relazione,
            NULL firmata,
            NULL iter,
            NULL note,
            NULL numero_cla,
            NULL relatore,
            NULL sub,
            NULL tipo_proposta,
            NULL tipo_trattamento,
            NULL unita_ubicata,
            NULL utente_modifica,
            NULL workflow,
            NULL tipologia,
            NULL stato_proposta
     FROM   proposte_delibera pd
    WHERE       pd.valido = 'Y'
            AND pd.anno_proposta IS NOT NULL
            AND pd.numero_proposta IS NOT NULL;
/

GRANT ALL ON CF4_DEL TO ${global.db.cfa.username} with grant option;
/

GRANT ALL ON CF4_PROPOSTE TO ${global.db.cfa.username} with grant option;
/

--liquibase formatted sql
--changeset rdestasio:install_20210221_integrazioni_05 runOnChange:true

CREATE OR REPLACE FORCE VIEW CONS_DETERMINE_COLLEGATE
(
   ID_DETERMINA,
   ID_DOCUMENTO_ESTERNO,
   NUMERO_DETERMINA,
   ANNO_DETERMINA,
   REGISTRO_DETERMINA,
   ID_DETERMINA_COLLEGATA,
   ID_DOCUMENTO_ESTERNO_COLLEGATO,
   NUMERO_DETERMINA_COLLEGATA,
   ANNO_DETERMINA_COLLEGATA,
   REGISTRO_DETERMINA_COLLEGATA,
   OPERAZIONE,
   DATA_OPERAZIONE,
   DESCRIZIONE_COLLEGAMENTO
)
AS
   SELECT d.id_determina,
          d.id_documento_esterno,
          d.numero_determina,
          d.anno_determina,
          d.registro_determina,
          d_collegata.id_determina,
          d_collegata.id_documento_esterno,
          d_collegata.numero_determina,
          d_collegata.anno_determina,
          d_collegata.registro_determina,
          dc.operazione,
          d.data_esecutivita,
             DECODE (dc.operazione,
                     'ANNULLA', 'Atto annullante per ',
                     'INTEGRA', 'Atto integrante per ',
                     '')
          || d_collegata.numero_determina
          || '/'
          || d_collegata.anno_determina
          || ' - '
          || d_collegata.registro_determina
             descrizione_collegamento
     FROM determine d                             --atto che annulla o integra
                     , determine d_collegata      --atto annullato o integrato
                                            , documenti_collegati dc
    WHERE     d.id_determina = dc.id_determina_principale
          AND d_collegata.id_determina = dc.id_determina_collegata
          AND d.valido = 'Y'
          AND d_collegata.valido = 'Y'
          AND d_collegata.numero_determina IS NOT NULL
          AND d.numero_determina IS NOT NULL
          AND dc.operazione IN ('ANNULLA', 'INTEGRA')
   UNION
   SELECT d_collegata.id_determina,
          d_collegata.id_documento_esterno,
          d_collegata.numero_determina,
          d_collegata.anno_determina,
          d_collegata.registro_determina,
          d.id_determina,
          d.id_documento_esterno,
          d.numero_determina,
          d.anno_determina,
          d.registro_determina,
          dc.operazione,
          d.data_esecutivita,
             DECODE (dc.operazione,
                     'ANNULLA', 'Atto annullato da ',
                     'INTEGRA', 'Atto integrato da ',
                     '', '')
          || d.numero_determina
          || '/'
          || d.anno_determina
          || ' - '
          || d.registro_determina
             descrizione_collegamento
     FROM determine d                             --atto che annulla o integra
                     , determine d_collegata      --atto annullato o integrato
                                            , documenti_collegati dc
    WHERE     d.id_determina = dc.id_determina_principale
          AND d_collegata.id_determina = dc.id_determina_collegata
          AND d.valido = 'Y'
          AND d_collegata.valido = 'Y'
          AND d_collegata.numero_determina IS NOT NULL
          AND d.numero_determina IS NOT NULL
          AND dc.operazione IN ('ANNULLA', 'INTEGRA')
/
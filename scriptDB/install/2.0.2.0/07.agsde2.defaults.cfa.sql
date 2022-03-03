--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_07

-- in questo file ci vanno tutti quei package e viste che servono "vuoti" o "finti" in caso non ci sia la relativa integrazione.

-- SENZA integrazione CFA:
CREATE OR REPLACE FORCE VIEW CF_DOCUMENTI
(
   ENTE,
   CODICE_TIPO,
   TITOLO_TIPO,
   DESCRIZIONE_TIPO,
   ID_ESTERNO,
   ANNO_PROP,
   NUMERO_PROP,
   UNITA_PROP,
   ANNO_DEL,
   NUMERO_DEL,
   SEDE_DEL,
   RIF_BIL_PEG,
   ANNO,
   NUMERO,
   DESCRIZIONE,
   IMPORTO,
   CODICE_BENEFICIARIO,
   RAGIONE_SOCIALE_BENEFICIARIO,
   DATA_REG
)
AS
   SELECT NULL,
          NULL,
          NULL,
          NULL,
          TO_NUMBER (NULL),
          TO_NUMBER (NULL),
          TO_NUMBER (NULL),
          NULL,
          TO_NUMBER (NULL),
          TO_NUMBER (NULL),
          NULL,
          NULL,
          TO_NUMBER (NULL),
          TO_NUMBER (NULL),
          NULL,
          TO_NUMBER (NULL),
          TO_NUMBER (NULL),
          NULL,
          TO_DATE (NULL)
     FROM DUAL
/
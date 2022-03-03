--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_06

-- in questo file ci vanno tutti quei package e viste che servono "vuoti" o "finti" in caso non ci sia la relativa integrazione.

-- SENZA integrazione CF4:
CREATE OR REPLACE FORCE VIEW CF4_VISTA_PROP_DEL
(
   TIPO,
   E_S,
   UNITA_PROP,
   NUMERO_PROP,
   ANNO_PROP,
   SEDE_DEL,
   NUMERO_DEL,
   ANNO_DEL,
   RIF_BIL_PEG,
   ANNO,
   NUMERO,
   DESCRIZIONE,
   IMPORTO,
   CODICE_BENEFICIARIO,
   RAGIONE_SOCIALE,
   DATA
)
AS
   SELECT NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL
     FROM DUAL
/

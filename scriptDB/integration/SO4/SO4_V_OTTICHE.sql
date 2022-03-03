--liquibase formatted sql
--changeset rdestasio:install_20200221_11

CREATE OR REPLACE FORCE VIEW SO4_V_OTTICHE
(
   CODICE,
   AMMINISTRAZIONE,
   DESCRIZIONE,
   NOTE,
   ISTITUZIONALE,
   GESTIONE_REVISIONI
)
AS
   SELECT ottica codice,
          amministrazione,
          descrizione,
          nota note,
          DECODE (ottica_istituzionale, 'SI', 1, 0) istituzionale,
          DECODE (gestione_revisioni, 'SI', 1, 0) gestione_revisioni
     FROM SO4_OTTICHE
/

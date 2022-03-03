--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_03 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_PROGETTI
(
   PROGETTO,
   DESCRIZIONE,
   PRIORITA,
   NOTE
)
AS
   SELECT p.progetto,
          p.descrizione,
          p.priorita,
          p.note
     FROM AD4_PROGETTI p
/




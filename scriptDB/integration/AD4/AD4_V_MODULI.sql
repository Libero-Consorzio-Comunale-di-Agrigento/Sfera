--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_02 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_MODULI
(
   MODULO,
   DESCRIZIONE,
   PROGETTO,
   NOTE
)
AS
   SELECT modulo,
          descrizione,
          progetto,
          note
     FROM AD4_MODULI
/

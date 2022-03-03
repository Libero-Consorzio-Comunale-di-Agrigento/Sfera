--liquibase formatted sql
--changeset rdestasio:install_20200221_defaults_06

CREATE OR REPLACE FORCE VIEW GALILEO_SETTORI_LINK
(
   COD_ADS,
   DESCRIZIONE,
   COD_SIAV
)
AS
   SELECT  null, null, null
     FROM dual
/


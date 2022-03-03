--liquibase formatted sql
--changeset rdestasio:install_20200221_13

CREATE OR REPLACE FORCE VIEW SO4_V_RUOLI_COMPONENTE_PUBB
(
   ID_RUOLO_COMPONENTE,
   ID_COMPONENTE,
   RUOLO,
   DAL,
   AL
)
AS
   SELECT id_ruolo_componente,
          id_componente,
          ruolo,
          dal,
          al
     FROM SO4_RUOLI_COMPONENTE_PUBB
/

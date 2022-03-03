--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_06 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_RUOLI
(
   RUOLO,
   DESCRIZIONE,
   MODULO,
   PROGETTO,
   RUOLO_APPLICATIVO
)
AS
   SELECT ruolo,
          descrizione,
          modulo,
          progetto,
          CAST (
             DECODE (gruppo_lavoro,
                     'S', DECODE (gruppo_so, 'S', 'Y', 'N'),
                     'N') AS CHAR (1))
             ruolo_applicativo
     FROM AD4_RUOLI
   UNION
   SELECT da.modulo || '_' || r.ruolo ruolo,
          r.descrizione,
          r.modulo,
          r.progetto,
          'N' ruolo_applicativo
     FROM ad4_diritti_accesso da, AD4_RUOLI r
    WHERE da.ruolo = r.ruolo
/




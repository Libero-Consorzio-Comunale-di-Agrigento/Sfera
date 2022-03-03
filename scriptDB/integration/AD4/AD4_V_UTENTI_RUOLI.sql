--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_09 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_UTENTI_RUOLI
(
   UTENTE,
   RUOLO,
   ISTANZA
)
AS
   SELECT utente, modulo || '_' || ruolo ruolo, istanza
     FROM AD4_DIRITTI_ACCESSO
    WHERE istanza in ('AGSDE2', 'AGSDE2.VIS')
/




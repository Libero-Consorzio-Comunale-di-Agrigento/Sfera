--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_08 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_UTENTI
(
   UTENTE,
   NOMINATIVO,
   PASSWORD,
   ENABLED,
   ACCOUNT_EXPIRED,
   ACCOUNT_LOCKED,
   PASSWORD_EXPIRED,
   TIPO_UTENTE,
   NOMINATIVO_SOGGETTO,
   ESISTE_SOGGETTO
)
AS
   SELECT u.utente,
          u.nominativo,
          u.password,
          CAST (DECODE (u.stato, 'U', 'Y', 'N') AS CHAR (1)) enabled,
          CAST (DECODE (u.stato, 'U', 'N', 'Y') AS CHAR (1)) account_expired,
          CAST (DECODE (u.stato, 'U', 'N', 'Y') AS CHAR (1)) account_locked,
          CAST (DECODE (u.pwd_da_modificare, 'NO', 'N', 'Y') AS CHAR (1))
             password_expired,
          u.tipo_utente,
          AD4_SOGGETTO.GET_DENOMINAZIONE (
             AD4_UTENTE.GET_SOGGETTO (u.utente, 'N', 0))
             nominativo_soggetto,
          CAST (
             DECODE (AD4_UTENTE.GET_SOGGETTO (u.utente, 'N', 0),
                     NULL, 'N',
                     'Y') AS CHAR (1))
             esiste_soggetto
     FROM AD4_UTENTI u
/




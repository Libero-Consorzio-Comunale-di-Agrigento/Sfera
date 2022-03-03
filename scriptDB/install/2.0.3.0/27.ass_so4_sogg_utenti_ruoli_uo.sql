--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_27

/* Formatted on 09/02/2016 10:03:37 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_SO4_SOGG_UTENTI_RUOLI_UO
(
   AMMINISTRAZIONE,
   OTTICA,
   PROGR_UO,
   CODICE_UO,
   DESC_UO,
   RUOLO,
   UTENTE,
   UTENTE_LOGIN,
   SOGGETTO,
   UO_DAL,
   UO_AL,
   COMP_DAL,
   COMP_AL,
   RUOLO_DAL,
   RUOLO_AL
)
AS
   SELECT uo.amministrazione,
          uo.ottica,
          uo.progr progr_uo,
          uo.codice codice_uo,
          uo.descrizione desc_uo,
          rc.ruolo ruolo,
          s.utente utente,
          au.nominativo utente_login,
          s.nome || ' ' || s.cognome soggetto,
          uo.dal uo_dal,
          uo.al uo_al,
          c.dal comp_dal,
          c.al comp_al,
          rc.dal ruolo_dal,
          rc.al ruolo_al
     FROM so4_v_componenti_pubb c,
          so4_v_unita_organizzative_pubb uo,
          as4_v_soggetti_correnti s,
          so4_v_ruoli_componente_pubb rc,
          ad4_utenti au
    WHERE     uo.progr = c.progr_unita
          AND s.ni = c.id_soggetto
          AND rc.id_componente = c.id_componente
          AND s.utente = au.utente(+)
/

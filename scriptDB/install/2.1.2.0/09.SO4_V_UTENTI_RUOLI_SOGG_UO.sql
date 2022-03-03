--liquibase formatted sql
--changeset rdestasio:2.1.2.0_20200221_09

/* Formatted on 09/02/2016 11:09:27 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW SO4_V_UTENTI_RUOLI_SOGG_UO
(
   ID_SOGGETTO,
   UTENTE,
   ID_COMPONENTE,
   COMP_DAL,
   COMP_AL,
   RUOLO,
   RUOLO_DAL,
   RUOLO_AL,
   UO_PROGR,
   UO_DAL,
   UO_AL,
   OTTICA
)
AS
   SELECT sogg.ni id_soggetto,
          sogg.utente,
          comp.id_componente,
          comp.dal comp_dal,
          comp.al comp_al,
          ruoli_comp.ruolo,
          ruoli_comp.dal ruolo_dal,
          ruoli_comp.al ruolo_al,
          uo.progr uo_progr,
          uo.dal uo_dal,
          uo.al uo_al,
          uo.ottica uo_ottica
     FROM so4_v_componenti_pubb comp,
          so4_v_ruoli_componente_pubb ruoli_comp,
          so4_v_unita_organizzative_pubb uo,
          as4_v_soggetti_correnti sogg
    WHERE     ruoli_comp.id_componente = comp.id_componente
          AND ruoli_comp.dal <= SYSDATE
          AND (ruoli_comp.al IS NULL OR ruoli_comp.al >= SYSDATE)
          AND comp.dal <= SYSDATE
          AND (comp.al IS NULL OR comp.al >= SYSDATE)
          AND comp.id_soggetto = sogg.ni
          AND uo.progr = comp.progr_unita
          AND uo.dal <= SYSDATE
          AND (uo.al IS NULL OR uo.al >= SYSDATE)
/

--liquibase formatted sql
--changeset mfrancesconi:2.5_20220930_19.view_so4_v_utenti_ruoli_sogg_uo runOnChange:true

create or replace force view so4_v_utenti_ruoli_sogg_uo
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
as
select sogg.ni id_soggetto,
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
from so4_v_componenti_pubb comp,
     so4_v_ruoli_componente_pubb ruoli_comp,
     so4_v_unita_organizzative_pubb uo,
     as4_v_soggetti_correnti sogg
where ruoli_comp.id_componente = comp.id_componente
  and ruoli_comp.dal <= sysdate
  and (ruoli_comp.al is null or ruoli_comp.al >= trunc (sysdate))
  and comp.dal <= sysdate
  and (comp.al is null or comp.al >= trunc (sysdate))
  and comp.id_soggetto = sogg.ni
  and uo.progr = comp.progr_unita
  and uo.ottica = comp.ottica
  and uo.dal <= sysdate
  and (uo.al is null or uo.al >= trunc (sysdate))
/
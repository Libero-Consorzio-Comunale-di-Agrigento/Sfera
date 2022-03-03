--liquibase formatted sql
--changeset rdestasio:install_20200221_assistenza_13 runOnChange:true

/* Formatted on 09/02/2016 10:51:35 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_WKF_ITER_AZIONI_USATE
(
   ID_CFG_ITER,
   ID_CFG_STEP,
   ITER,
   STEP,
   ATTORE,
   AZIONE_CALCOLO_ATTORE,
   CONDIZIONE_INGRESSO,
   CONDIZIONE_SBLOCCO,
   AZIONE_INGRESSO,
   AZIONE_USCITA,
   PULSANTE,
   CONDIZIONE_VISIBILITA_PULSANTE,
   AZIONE_PULSANTE
)
AS
     SELECT ci.id_cfg_iter,
            cs.id_cfg_step,
            ci.nome iter,
            (cs.sequenza + 1) || '. ' || cs.nome step,
            a.nome attore,
            a.id_azione_calcolo azione_calcolo_attore,
            cs.id_azione_condizione condizione_ingresso,
            cs.id_azione_sblocco condizione_sblocco,
            ai.id_azione_in azione_ingresso,
            ao.id_azione_out azione_uscita,
            p.etichetta pulsante,
            p.id_condizione_visibilita condizione_visibilita_pulsante,
            pa.id_azione azione_pulsante
       FROM wkf_cfg_iter ci,
            wkf_cfg_step cs,
            wkf_cfg_step_azioni_in ai,
            wkf_cfg_step_azioni_out ao,
            wkf_cfg_pulsanti cp,
            wkf_diz_pulsanti p,
            wkf_diz_pulsanti_azioni pa,
            wkf_diz_attori a
      WHERE     ci.id_cfg_iter = cs.id_cfg_iter
            AND ai.id_cfg_step(+) = cs.id_cfg_step
            AND ao.id_cfg_step(+) = cs.id_cfg_step
            AND cp.id_cfg_step(+) = cs.id_cfg_step
            AND p.id_pulsante(+) = cp.id_pulsante
            AND pa.id_pulsante(+) = p.id_pulsante
            AND a.id_attore(+) = cs.id_attore
   ORDER BY ci.id_cfg_iter,
            cs.sequenza,
            ai.azioni_ingresso_idx ASC,
            ao.azioni_uscita_idx ASC,
            pa.azioni_idx
/

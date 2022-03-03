--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_01

-- aggiungo le colonne "note" e "preparazione" per l'iter:
ALTER TABLE WKF_CFG_ITER MODIFY(DESCRIZIONE VARCHAR2(4000 BYTE))
/

-- CAMBIO DI PROGRAMMA: aggiungo la colonna STATO ed elimino "in_uso" e "valido"
alter table wkf_cfg_iter add (stato varchar2(255 byte) default 'FUORI_USO' not null)
/

update wkf_cfg_iter i set stato = 'IN_USO' where valido = 'Y' and verificato = 'Y'
/

update wkf_cfg_iter i set stato = 'IN_PREPARAZIONE' where valido = 'Y' and verificato = 'N'
/

alter table wkf_cfg_iter drop column valido
/

-- aggiungo la colonna per lo step successivo allo sblocco:
ALTER TABLE WKF_CFG_STEP ADD (ID_CFG_STEP_SBLOCCO NUMBER(19))
/

ALTER TABLE WKF_CFG_STEP ADD CONSTRAINT WKFCFGSTE_WKFCFGSTE_FK FOREIGN KEY (ID_CFG_STEP_SBLOCCO) REFERENCES WKF_CFG_STEP (ID_CFG_STEP) ENABLE VALIDATE
/

-- aggiungo la colonna per la condizione di sblocco:
ALTER TABLE WKF_CFG_STEP ADD (id_azione_sblocco NUMBER(19))
/

ALTER TABLE WKF_CFG_STEP ADD CONSTRAINT WKFCFGSTE_WKFDIZAZISB_FK FOREIGN KEY (id_azione_sblocco) REFERENCES WKF_DIZ_AZIONI (ID_AZIONE) ENABLE VALIDATE
/

-- valorizzo le colonne per lo sblocco dello step con lo step cfgSuccessivoSi se il No è null e ho la condizione di avanzamento.
update wkf_cfg_step s
   set s.id_azione_sblocco = s.id_azione_condizione
     , s.id_cfg_step_sblocco = s.id_cfg_step_si
     , s.id_azione_condizione = null
     , s.id_cfg_step_si = null
 where s.id_azione_condizione is not null
   and s.id_cfg_step_si is not null
   and s.id_cfg_step_no is null
/

-- aggiungo la colonna per la sequenza dello step:
ALTER TABLE WKF_CFG_STEP ADD (SEQUENZA NUMBER(11))
/

-- aggiorno il valore di tale colonna:
update wkf_cfg_step us
   set us.sequenza = (select t.nuova_sequenza
  from (
select s.id_cfg_iter, s.id_cfg_step, s.nome, s.sequenza, n.sequenza sequenza_nodo, n.iniziale, (row_number() over (partition by s.id_cfg_iter order by n.sequenza, n.id_nodo asc))-1 nuova_sequenza
  from wkf_cfg_step s
     , wkf_nodi n
 where s.id_nodo = n.id_nodo
 order by s.id_cfg_iter desc, n.sequenza asc) t
where t.id_cfg_step = us.id_cfg_step)
/

-- la imposto come not nullable:
ALTER TABLE WKF_CFG_STEP MODIFY(SEQUENZA  NOT NULL)
/

-- aggiungo la colonna attore per sostituire la cfgSwimLane:
ALTER TABLE WKF_CFG_STEP ADD (ID_ATTORE NUMBER(19))
/

ALTER TABLE WKF_CFG_STEP ADD CONSTRAINT WKFCFGSTE_WKFATT_FK FOREIGN KEY (ID_ATTORE) REFERENCES WKF_DIZ_ATTORI (ID_ATTORE) ENABLE VALIDATE
/

-- faccio update degli attori per gli step:
update wkf_cfg_step s
   set s.id_attore = (select l.id_attore from wkf_cfg_swim_lane l where s.id_cfg_swim_lane = l.id_cfg_swim_lane)
 where s.id_cfg_swim_lane is not null
/

-- azioni in uscita agli step
CREATE TABLE WKF_CFG_STEP_AZIONI_OUT ( ID_CFG_STEP NUMBER(19) NOT NULL, ID_AZIONE_OUT NUMBER(19), AZIONI_USCITA_IDX NUMBER(10) ) LOGGING NOCOMPRESS NOCACHE NOPARALLEL MONITORING
/

ALTER TABLE WKF_CFG_STEP_AZIONI_OUT ADD (  CONSTRAINT WKFCFGSTAZOUT_DIZAZ FOREIGN KEY (ID_AZIONE_OUT) REFERENCES WKF_DIZ_AZIONI (ID_AZIONE) ENABLE VALIDATE)
/

-- elimino la roba non più necessaria (flussi e swim-lane)
ALTER TABLE WKF_CFG_ITER DROP COLUMN ID_FLUSSO
/

ALTER TABLE WKF_CFG_STEP DROP COLUMN ID_NODO
/

ALTER TABLE WKF_CFG_STEP DROP COLUMN ID_CFG_SWIM_LANE
/

ALTER TABLE WKF_FLUSSI DROP PRIMARY KEY CASCADE
/

DROP TABLE WKF_FLUSSI CASCADE CONSTRAINTS PURGE
/

ALTER TABLE WKF_FRECCE DROP PRIMARY KEY CASCADE
/

DROP TABLE WKF_FRECCE CASCADE CONSTRAINTS PURGE
/

ALTER TABLE WKF_NODI DROP PRIMARY KEY CASCADE
/

DROP TABLE WKF_NODI CASCADE CONSTRAINTS PURGE
/

ALTER TABLE WKF_SWIM_LANE DROP PRIMARY KEY CASCADE
/

DROP TABLE WKF_SWIM_LANE CASCADE CONSTRAINTS PURGE
/

ALTER TABLE WKF_CFG_SWIM_LANE DROP PRIMARY KEY CASCADE
/

DROP TABLE WKF_CFG_SWIM_LANE CASCADE CONSTRAINTS PURGE
/

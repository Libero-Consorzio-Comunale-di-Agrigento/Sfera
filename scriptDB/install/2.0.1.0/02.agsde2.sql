--liquibase formatted sql
--changeset rdestasio:2.0.1.0_20200221_02

CREATE OR REPLACE SYNONYM SO4_UTIL FOR ${global.db.so4.username}.SO4_UTIL
/

-- Modifiche dalla versione 2.0.0.0#32
update odg_commissioni_stampe set codice = 'STAMPA' 		where codice is null
/

update ODG_COMMISSIONI_STAMPE set codice = 'CONVOCAZIONE' 	where codice = 'ODG'
/

-- Update per sistemare la sequenza dei partecipanti
begin
	for c in (
	SELECT p.id_seduta,
	        p.id_seduta_partecipante,
	        ROW_NUMBER ()
	        OVER (
	           PARTITION BY p.id_seduta
	           ORDER BY
	              p.id_seduta,
	              c.sequenza ASC,
	              a.denominazione ASC)
	           sequenza
	   FROM odg_sedute_partecipanti p,
	        odg_commissioni_componenti c,
	        as4_v_soggetti_correnti a
	  WHERE     p.id_commissione_componente =
	               c.id_commissione_componente(+)
	        AND p.ni_componente_esterno = a.ni(+)
	ORDER BY p.id_seduta, c.sequenza ASC, a.denominazione ASC)
	loop
	    update odg_sedute_partecipanti p set p.sequenza = c.sequenza where p.id_seduta_partecipante = c.id_seduta_partecipante;
	    commit;
	end loop;
end;
/

-- Update per sistemare le sequenze dei componenti in commissione:
update odg_commissioni_componenti p1 set p1.sequenza = (select t.sequenza_nuova from (select c.id_commissione, c.id_commissione_componente, c.ni_componente, a.denominazione, row_number() over (partition by c.id_commissione order by c.id_commissione, c.sequenza asc, a.denominazione asc) sequenza_nuova, c.sequenza
  from odg_commissioni_componenti c
     , as4_v_soggetti_correnti a
 where c.ni_componente = a.ni(+)
 order by c.id_commissione, c.sequenza asc, a.denominazione asc) t where t.id_commissione_componente = p1.id_commissione_componente)
/

-- stampa unica e pubblicazione per visti e pareri
ALTER TABLE tipi_visto_parere ADD (stampa_unica  CHAR(1 BYTE) DEFAULT 'Y' NOT NULL ,
                                   pubblicazione CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/
             
-- eseguibilità immediata su per la proposta di delibera
ALTER TABLE tipi_delibera ADD (eseguibilita_immediata CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE proposte_delibera ADD (eseguibilita_immediata CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

-- modifica dei pulsanti con competenze in modifica (spostato nel dizionario invece che in configurazione)
begin
	execute immediate 'ALTER TABLE WKF_DIZ_PULSANTI ADD (COMPETENZA_IN_MODIFICA CHAR(1 BYTE) DEFAULT ''Y'' NOT NULL)';
	
	-- prima correggo le competenze sul pulsante presa visione:
	execute immediate 'update wkf_cfg_pulsanti cp set cp.competenza_in_modifica = ''N'' where cp.id_pulsante = (select id_pulsante from wkf_diz_pulsanti p where p.etichetta = ''Presa Visione'')';
	
	execute immediate 'update wkf_diz_pulsanti p set p.competenza_in_modifica = nvl((select min(cp.competenza_in_modifica) from wkf_cfg_pulsanti cp where p.id_pulsante = cp.id_pulsante), ''Y'')';
	
	execute immediate 'ALTER TABLE WKF_CFG_PULSANTI DROP COLUMN COMPETENZA_IN_MODIFICA';
exception when others then
	null;
end;
/

-- flag per gestire l'obbligatorietà del testo tra uno step e l'altro.
ALTER TABLE TIPI_DELIBERA     ADD (TESTO_OBBLIGATORIO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DETERMINA    ADD (TESTO_OBBLIGATORIO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_VISTO_PARERE ADD (TESTO_OBBLIGATORIO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

-- gestione della data di fine seduta.
ALTER TABLE odg_sedute ADD (DATA_FINE_SEDUTA DATE, ORA_FINE_SEDUTA VARCHAR2(5))
/

-- gestione della sequenza dei partecipanti/convocati alla seduta.
ALTER TABLE ODG_SEDUTE_PARTECIPANTI ADD (SEQUENZA_PARTECIPANTE  NUMBER(10) )
/

UPDATE ODG_SEDUTE_PARTECIPANTI SET SEQUENZA_PARTECIPANTE = SEQUENZA
/

COMMENT ON COLUMN ODG_SEDUTE_PARTECIPANTI.SEQUENZA IS 'sequenza convocato seduta'
/

COMMENT ON COLUMN ODG_SEDUTE_PARTECIPANTI.SEQUENZA_PARTECIPANTE IS 'sequenza partecipante seduta'
/

-- pareri delle delibere:
ALTER TABLE VISTI_PARERI ADD (ID_DELIBERA NUMBER(19))
/

ALTER TABLE VISTI_PARERI ADD CONSTRAINT VIS_PAR_DEL_FK FOREIGN KEY (ID_DELIBERA) REFERENCES DELIBERE (ID_DELIBERA) ENABLE VALIDATE
/

ALTER TABLE TIPI_VISTO_PARERE ADD (PROGRESSIVO_CFG_ITER_DELIBERA NUMBER(19))
/

ALTER TABLE DELIBERE ADD (CODICI_VISTI_TRATTATI VARCHAR2(255 BYTE))
/

-- corretto il collegamento al cfgIter nella tabella odg_esiti:
ALTER TABLE ODG_ESITI ADD (PROGRESSIVO_CFG_ITER NUMBER(19))
/

UPDATE ODG_ESITI O SET O.PROGRESSIVO_CFG_ITER = (SELECT C.PROGRESSIVO FROM WKF_CFG_ITER C WHERE C.ID_CFG_ITER = O.ID_CFG_ITER)
/

ALTER TABLE ODG_ESITI DROP COLUMN ID_CFG_ITER
/

-- gestione del frontespizio sulla determina.
ALTER TABLE TIPI_DETERMINA ADD (id_modello_testo_frontespizio NUMBER(19))
/

ALTER TABLE TIPI_DETERMINA ADD CONSTRAINT TIPI_DETE_GTE_MOD_FK FOREIGN KEY (id_modello_testo_frontespizio) REFERENCES GTE_MODELLI (ID_MODELLO) ENABLE VALIDATE
/

-- gestione dei soggetti multipli sui documenti.
ALTER TABLE DETERMINE_SOGGETTI 			ADD (SEQUENZA NUMBER(18) DEFAULT 0 NOT NULL)
/

ALTER TABLE DELIBERE_SOGGETTI 			ADD (SEQUENZA NUMBER(18) DEFAULT 0 NOT NULL)
/

ALTER TABLE PROPOSTE_DELIBERA_SOGGETTI 	ADD (SEQUENZA NUMBER(18) DEFAULT 0 NOT NULL)
/

ALTER TABLE DETERMINE_SOGGETTI             ADD (ATTIVO char(1) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE DELIBERE_SOGGETTI              ADD (ATTIVO char(1) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE PROPOSTE_DELIBERA_SOGGETTI     ADD (ATTIVO char(1) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE CARATTERISTICHE_TIPI_SOGGETTO MODIFY(ID_REGOLA_DEFAULT  NULL)
/

update tipi_soggetti set titolo = 'Uo Destinataria' where codice = 'UO_DESTINATARIA'
/

insert into tipi_soggetti (codice, categoria, titolo) values ('UO_CONTROLLO',  'UNITA', 'Uo di Controllo')
/

insert into tipi_soggetti (codice, categoria, titolo) values ('UO_FIRMATARIO', 'UNITA', 'Uo del Firmatario')
/

-- campi per la notifica di esecutività atto:
update tipi_notifica t 
   set t.help = 'Elenco campi per la sostituzione nell''Oggetto e nel Testo:'||chr(13)||'[ANNO] -> Anno adozione atto'||chr(13)||'[NUMERO] -> Numero dell''atto'||chr(13)||'[TIPO_ATTO] -> tipologia dell''atto'||chr(13)||'[OGGETTO] -> Oggetto dell''atto'||chr(13)||'[UNITA_PROPONENTE] -> unita proponente dell''atto'||chr(13)||'[ANNO_ALBO] -> anno di numerazione dell''albo'||chr(13)||'[NUMERO_ALBO] -> numero dell''albo'||chr(13)||'[DATA_INIZIO_PUBB] -> data di inizio pubblicazione (formato: gg/mm/aaaa)'||chr(13)||'[DATA_FINE_PUBB] -> data di fine pubblicazione (formato: gg/mm/aaaa)'||chr(13)||'[GIORNI_PUBB] -> giorni di pubblicazione (o ''fino a revoca'' se indicato)'
 where t.tipo_notifica = 'ESECUTIVITA_ATTO'
/

-- gestione dei tipi registro unità per caratteristiche.
ALTER TABLE TIPI_REGISTRO_UNITA ADD (ID_CARATTERISTICA NUMBER(19))
/

ALTER TABLE TIPI_REGISTRO_UNITA ADD CONSTRAINT FK_TIPREGUN_CARTIP FOREIGN KEY (ID_CARATTERISTICA) REFERENCES CARATTERISTICHE_TIPOLOGIE (ID_CARATTERISTICA_TIPOLOGIA) ENABLE VALIDATE
/

-- aggiornamento data discussione sugli oggetti seduta.
update odg_oggetti_seduta o set o.data_discussione = (select nvl(S.DATA_INIZIO_SEDUTA, S.DATA_SEDUTA) from odg_sedute s where s.id_seduta = o.id_seduta) where o.data_discussione is null
/

-- aggiorno la data di adozione delle delibere:
update delibere d set d.data_adozione = (select s.data_seduta
  from odg_oggetti_seduta o
     , odg_sedute s 
 where d.data_adozione is null
   and o.id_oggetto_seduta = d.id_oggetto_seduta
   and s.id_seduta = o.id_seduta) where d.data_adozione is null
/

-- aggiorno le caratteristiche-tipologie:
update caratteristiche_tipologie t set t.layout_soggetti = '/atti/documenti/visto/visto_standard.zul' where t.tipo_oggetto = 'VISTO'
/

-- correggo le determine che sono in stato pubblicato e le metto in stato conclusso
update determine de set de.stato = 'CONCLUSO' where de.id_determina in (
select d.id_determina
  from determine d
     , wkf_engine_iter i
 where d.stato = 'PUBBLICATO'
   and d.id_engine_iter = i.id_engine_iter
   and i.data_fine is not null)
/

-- aggiungo l'azione al flusso di pubblicazione:
declare
    id_azione number(19);
    res_count number(19);
begin
    select a.id_azione
      into id_azione
      from wkf_diz_azioni a
     where a.nome_metodo like 'setStatoConcluso'
       and a.tipo_oggetto = 'DETERMINA';

    for c in (select s.*
      from tipi_determina td
         , wkf_cfg_iter i
         , wkf_cfg_step s
     where td.progressivo_cfg_iter_pubb = i.progressivo
       and s.id_cfg_iter = i.id_cfg_iter
       and s.nome = 'Fine') loop
       
        select count(1)
         into res_count
         from wkf_cfg_step_azioni_in a
        where a.id_azione_in = id_azione
          and a.id_cfg_step = c.id_cfg_step;
                
        if (res_count = 0) then
            select count(1)
             into res_count
             from wkf_cfg_step_azioni_in a
            where a.id_cfg_step = c.id_cfg_step;
            
            insert into wkf_cfg_step_azioni_in (id_azione_in, id_cfg_step, AZIONI_INGRESSO_IDX) values (id_azione, c.id_cfg_step, res_count);
        end if;
    end loop;
end;
/

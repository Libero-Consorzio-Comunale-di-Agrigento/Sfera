--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_02

-- caratteristica soggetti certificati:
update caratteristiche_tipologie set layout_soggetti = '/atti/documenti/certificato.zul' where TIPO_OGGETTO = 'CERTIFICATO'
/

-- job per la collezione delle statistiche
DECLARE
  X NUMBER;
BEGIN
  SYS.DBMS_JOB.SUBMIT
  ( job       => X 
   ,what      => 'BEGIN
DBMS_STATS.GATHER_SCHEMA_STATS(''AGSDE2'',DBMS_STATS.AUTO_SAMPLE_SIZE, FALSE, ''FOR ALL COLUMNS SIZE AUTO'', NULL, ''DEFAULT'', TRUE);
END;'
   , next_date => to_date(to_char(sysdate, 'dd/mm/yyyy')||' 23:00:00', 'dd/mm/yyyy hh24:mi:ss')
   , interval  => 'to_date(to_char(sysdate + 1, ''dd/mm/yyyy'')||'' 23:00:00'', ''dd/mm/yyyy hh24:mi:ss'')'
   , no_parse  => FALSE
  );
  SYS.DBMS_OUTPUT.PUT_LINE('Job Number is: ' || to_char(x));
COMMIT;
END;
/

-- trigger logon per usare l'optimizer COST
CREATE OR REPLACE TRIGGER SET_COST_OPTIMIZER
AFTER LOGON
ON SCHEMA
BEGIN
execute immediate 'alter session set optimizer_mode = first_rows_100';
END;
/

-- aggiorno le impostazioni:
delete from impostazioni where codice in ('NUMERA_CON_FIRMA', 'JWORKLIST_CATEGORIA', 'JWORKLIST_DETERMINA_DESK', 'JWORKLIST_DELIBERA_DESK', 'JWORKLIST_PROPOSTA_DELIBERA_DESK', 'JWORKLIST_VISTO_DESK', 'JWORKLIST_CERTIFICATO_DESK')
/

update impostazioni set valore = 'integrazioneContabilitaNessuna' where codice = 'CONTABILITA' and valore = 'N'
/

update impostazioni set valore = 'integrazioneContabilitaCfa' where codice = 'CONTABILITA' and valore = 'contabilitaADS' 
/

update impostazioni set valore = 'integrazioneContabilitaCf4' where codice = 'CONTABILITA' and valore = 'contabilitaCF4'
/

-- aggiorno l'impostazione dell'editor mettendo di default quello senza controllo di chiusura.
update impostazioni set valore = valore||'.NOCHECK' where codice = 'EDITOR_DEFAULT'
/

CREATE OR REPLACE PACKAGE        impostazioni_pkg AS
/******************************************************************************
   NAME:       impostazioni_pkg
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        18/02/2014      esasdelli       1. Created this package.
******************************************************************************/

  procedure add_impostazione (p_codice IN varchar2, p_descrizione in varchar2, p_etichetta in varchar2, p_predefinito in varchar2, p_modificabile in varchar2, p_caratteristiche in varchar2);

  function get_impostazione  (p_codice IN varchar2, p_ente in varchar2) RETURN varchar2;
  
  procedure set_impostazione (p_codice IN varchar2, p_ente in varchar2, p_valore in varchar2);

END impostazioni_pkg;
/

CREATE OR REPLACE PACKAGE BODY        impostazioni_pkg
IS
   PROCEDURE add_impostazione (p_codice            IN VARCHAR2,
                               p_descrizione       IN VARCHAR2,
                               p_etichetta         IN VARCHAR2,
                               p_predefinito       IN VARCHAR2,
                               p_modificabile      IN VARCHAR2,
                               p_caratteristiche   IN VARCHAR2)
   /******************************************************************************
   NOME:        add_impostazione
   DESCRIZIONE: aggiunge una nuova impostazione se non già presente.
   PARAMETRI:
   ******************************************************************************/
   IS
   BEGIN
      FOR c IN (SELECT ente
                  FROM impostazioni
                 WHERE codice = 'OTTICA_SO4')
      LOOP
         MERGE INTO IMPOSTAZIONI A
              USING (SELECT p_codice AS CODICE,
                            c.ente AS ENTE,
                            1 AS VERSION,
                            p_caratteristiche AS CARATTERISTICHE,
                            p_descrizione AS DESCRIZIONE,
                            p_etichetta AS ETICHETTA,
                            p_modificabile AS MODIFICABILE,
                            p_predefinito AS PREDEFINITO,
                            p_predefinito AS VALORE
                       FROM DUAL) B
                 ON (A.CODICE = B.CODICE AND A.ENTE = B.ENTE)
         WHEN NOT MATCHED
         THEN
            INSERT     (CODICE,
                        ENTE,
                        VERSION,
                        CARATTERISTICHE,
                        DESCRIZIONE,
                        ETICHETTA,
                        MODIFICABILE,
                        PREDEFINITO,
                        VALORE)
                VALUES (B.CODICE,
                        B.ENTE,
                        B.VERSION,
                        B.CARATTERISTICHE,
                        B.DESCRIZIONE,
                        B.ETICHETTA,
                        B.MODIFICABILE,
                        B.PREDEFINITO,
                        B.VALORE)
         WHEN MATCHED
         THEN
            UPDATE SET A.VERSION = B.VERSION,
                       A.CARATTERISTICHE = B.CARATTERISTICHE,
                       A.DESCRIZIONE = B.DESCRIZIONE,
                       A.ETICHETTA = B.ETICHETTA,
                       A.MODIFICABILE = B.MODIFICABILE,
                       A.PREDEFINITO = B.PREDEFINITO;
      END LOOP;
   END;

   FUNCTION get_impostazione (p_codice IN VARCHAR2, p_ente IN VARCHAR2)
      RETURN VARCHAR2
   /******************************************************************************
   NOME:        get_impostazione
   DESCRIZIONE: restituisce il valore dell'impostazione per l'ente specificato.
   PARAMETRI:   --
   RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
   NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
   DEL PACKAGE.
   ******************************************************************************/
   IS
      d_valore_impostazione   IMPOSTAZIONI.VALORE%TYPE;
   BEGIN
      BEGIN
         SELECT valore
           INTO d_valore_impostazione
           FROM impostazioni i
          WHERE i.ente = p_ente AND i.codice = p_codice;
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            SELECT valore
              INTO d_valore_impostazione
              FROM impostazioni i
             WHERE i.ente = '*' AND i.codice = p_codice;
      END;

      RETURN d_valore_impostazione;
   END get_impostazione;

   PROCEDURE set_impostazione (p_codice   IN VARCHAR2,
                               p_ente     IN VARCHAR2,
                               p_valore   IN VARCHAR2)
   /******************************************************************************
   NOME:        set_impostazione
   DESCRIZIONE: imposta il valore dell'impostazione per l'ente specificato.
                se l'ente ha valore NULL, allora il valore dell'impostazione viene settato per tutti gli enti.
   PARAMETRI:   --
   RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
   NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
   DEL PACKAGE.
   ******************************************************************************/
   IS
   BEGIN
   
      FOR c IN (SELECT ente
                  FROM impostazioni
                 WHERE codice = 'OTTICA_SO4'
                   and (ente = p_ente or p_ente is null))
      LOOP   
          UPDATE impostazioni
             SET valore = p_valore
           WHERE codice = p_codice AND ente = c.ente;
      end loop;
   END set_impostazione;
END impostazioni_pkg;
/

-- per la gestione dei controlli sull'odg, aggiungo il campo "adottabile" su tipo_delibera:
ALTER TABLE TIPI_DELIBERA ADD (ADOTTABILE CHAR(1 BYTE) DEFAULT 'Y')
/

-- aggiungo nuove impostazioni (PRIMA DEVI AVER AGGIORNATO IMPOSTAZIONI_PKG)
begin
	
	-- PROTOCOLLO_ATTIVO
	impostazioni_pkg.add_impostazione (
		  'PROTOCOLLO_ATTIVO'
		, 'Indica se è abilitata la protocollazione.'
		, 'PROTOCOLLO ATTIVO'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
	-- di default, il protocollo è attivo:
	impostazioni_pkg.set_impostazione ('PROTOCOLLO_ATTIVO', null, 'Y');
	
	-- CASA_DI_VETRO_FILE_PRINCIPALE
	impostazioni_pkg.add_impostazione (
		  'CASA_DI_VETRO_FILE_PRINCIPALE'
		, 'Indica quale file deve essere il principale (se stampa unica, il testo o nessuno)'
		, 'File principale in casa di vetro'
		, 'TESTO'
		, 'Y'
		, '<rowset><row label="Testo" value="TESTO" /><row label="Stampa Unica" value="STAMPA_UNICA" /><row label="Nessuno" value="NESSUNO" /></rowset>'
	);
	
	for c in (select ente, decode(valore, 'Y', 'STAMPA_UNICA', 'TESTO') valore from impostazioni where codice = 'CASA_DI_VETRO_STAMPAUNICA')
	loop
		impostazioni_pkg.set_impostazione ('CASA_DI_VETRO_FILE_PRINCIPALE', c.ente, c.valore);
	end loop;
	
	-- CASA_DI_VETRO_FILE_PRINCIPALE
	impostazioni_pkg.add_impostazione (
		  'CASA_DI_VETRO_PUBBLICA_ALLEGATI'
		, 'Indica se vanno pubblicati o meno gli allegati alla casa di vetro'
		, 'Pubblica gli allegati in casa di vetro'
		, 'Y'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/

-- creo la nuova tabella per gli incarichi:
CREATE TABLE ODG_INCARICHI
(
   ID_INCARICO   NUMBER (19) NOT NULL,
   VERSION       NUMBER (19) NOT NULL,
   DATA_INS      DATE NOT NULL,
   ENTE          VARCHAR2 (255 BYTE) NOT NULL,
   DATA_UPD      DATE NOT NULL,
   TITOLO        VARCHAR2 (255 BYTE) NOT NULL,
   UTENTE_INS    VARCHAR2 (255 BYTE) NOT NULL,
   UTENTE_UPD    VARCHAR2 (255 BYTE) NOT NULL,
   VALIDO        CHAR (1 BYTE) NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING
/

ALTER TABLE ODG_INCARICHI ADD (  PRIMARY KEY (ID_INCARICO) ENABLE VALIDATE)
/

-- aggiungo la colonna incarico ai componenti delle commissioni
ALTER TABLE ODG_COMMISSIONI_COMPONENTI ADD (ID_INCARICO NUMBER(19))
/

ALTER TABLE ODG_COMMISSIONI_COMPONENTI ADD CONSTRAINT COMCOMP_INC_FK FOREIGN KEY (ID_INCARICO) REFERENCES ODG_INCARICHI (ID_INCARICO) ENABLE VALIDATE
/

-- aggiungo la colonna incarico ai convocati della seduta
ALTER TABLE ODG_SEDUTE_PARTECIPANTI ADD (ID_INCARICO NUMBER(19))
/

ALTER TABLE ODG_SEDUTE_PARTECIPANTI ADD CONSTRAINT SEDPAR_INC_FK FOREIGN KEY (ID_INCARICO) REFERENCES ODG_INCARICHI (ID_INCARICO) ENABLE VALIDATE
/

-- 1) creo il dizionario degli incarichi:
begin
    for c in (select distinct upper(rs.titolo) as titolo from odg_ruoli_soggetti rs)
    loop
        INSERT INTO ODG_INCARICHI (VERSION, VALIDO, UTENTE_UPD, UTENTE_INS, TITOLO, ID_INCARICO, ENTE, DATA_UPD, DATA_INS) VALUES (0, 'Y', 'AGSDE2', 'AGSDE2', c.titolo, hibernate_sequence.nextval, IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('ENTI_SO4', '*'), sysdate, sysdate);
    end loop;
    
    commit;
end;
/

-- 2) valorizzo gli incarichi dei componenti delle commissioni:
update odg_commissioni_componenti cc set cc.id_incarico = (select DISTINCT FIRST_VALUE (i.id_incarico) OVER (ORDER BY r.valido_dal DESC)
          from odg_incarichi i
             , odg_ruoli_soggetti r
         where upper(r.titolo) = upper(i.titolo)
           and r.ni_soggetto = cc.ni_componente
           AND ( (   r.valido_dal BETWEEN cc.valido_dal
                                             AND NVL (
                                                    cc.valido_al,
                                                    TO_DATE ('01/01/2100',
                                                             'dd/mm/yyyy'))
                         OR r.valido_al IS NULL)))
/

-- 3) valorizzo gli incarichi dei componenti nelle sedute per i componenti esterni:
update odg_sedute_partecipanti sp
   set sp.id_incarico = (select (select i.id_incarico from odg_incarichi i where upper(i.titolo) = upper(rs.titolo)) id_incarico
  from odg_ruoli_soggetti rs
     , odg_sedute s
 where sp.ni_componente_esterno = rs.ni_soggetto
   and s.id_seduta = sp.id_seduta
   and s.data_seduta between rs.valido_dal and nvl(rs.valido_al, sysdate + 100))
 where sp.ni_componente_esterno is not null
/

-- 4) valorizzo gli incarichi dei componenti nelle sedute per i componenti delle commissioni:
begin
for c in (select SP.ID_SEDUTA_PARTECIPANTE, sp.id_seduta, sp.id_commissione_componente, rs.titolo
  from odg_ruoli_soggetti rs
     , odg_sedute s
     , odg_commissioni_componenti cc
     , odg_sedute_partecipanti sp
 where s.id_seduta = sp.id_seduta
   and sp.id_commissione_componente = cc.id_commissione_componente
   and rs.ni_soggetto = cc.ni_componente
   and s.data_seduta between rs.valido_dal and rs.valido_al
   and sp.ni_componente_esterno is null
 order by sp.id_seduta, sp.id_commissione_componente)
 loop
 update odg_sedute_partecipanti 
    set id_incarico = (select i.id_incarico from odg_incarichi i where upper(i.titolo) = upper(c.titolo) and rownum = 1)
  where ID_SEDUTA_PARTECIPANTE = c.ID_SEDUTA_PARTECIPANTE;
end loop;
end;
/

-- 5) riordino i soggetti delle commissioni:
begin
for c in (select cc.id_commissione_componente, cc.id_commissione, cc.sequenza, cc.valido, row_number() over (partition by cc.id_commissione order by cc.id_commissione, cc.sequenza asc) sequenza_nuova
  from odg_commissioni_componenti cc 
 where cc.valido = 'Y' 
 order by cc.id_commissione asc, cc.sequenza asc, cc.id_commissione_componente asc)
 loop
    update odg_commissioni_componenti cc set cc.sequenza = c.sequenza_nuova where cc.id_commissione_componente = c.id_commissione_componente;
 end loop;
end;
/

-- firma del presidente sulle commissioni e sedute.
ALTER TABLE ODG_COMMISSIONI ADD (VOTO_PRESIDENTE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE ODG_SEDUTE ADD (VOTO_PRESIDENTE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL )
/

-- pubblicazione casa di vetro
ALTER TABLE TIPI_DELIBERA ADD (PUBBLICAZIONE_TRASPARENZA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

ALTER TABLE TIPI_DETERMINA ADD (PUBBLICAZIONE_TRASPARENZA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

-- cambio il tipo delle colonne dei soggetti e dei documenti collegati sulle tabelle di storico della delibera
ALTER TABLE DELIBERE_STORICO ADD (xml_soggetti_clob CLOB)
/

update delibere_storico set xml_soggetti_clob = xml_soggetti
/

ALTER TABLE DELIBERE_STORICO DROP COLUMN XML_SOGGETTI
/

ALTER TABLE DELIBERE_STORICO RENAME COLUMN XML_SOGGETTI_CLOB TO XML_SOGGETTI
/

ALTER TABLE DELIBERE_STORICO MODIFY(XML_SOGGETTI NOT NULL)
/

-- della determina...
ALTER TABLE DETERMINE_STORICO ADD (xml_soggetti_clob CLOB)
/

update DETERMINE_STORICO set xml_soggetti_clob = xml_soggetti
/

ALTER TABLE DETERMINE_STORICO DROP COLUMN XML_SOGGETTI
/

ALTER TABLE DETERMINE_STORICO RENAME COLUMN XML_SOGGETTI_CLOB TO XML_SOGGETTI
/

ALTER TABLE DETERMINE_STORICO MODIFY(XML_SOGGETTI NOT NULL)
/

-- ... della proposta di delibera ...
ALTER TABLE PROPOSTE_DELIBERA_STORICO ADD (xml_soggetti_clob CLOB)
/

update PROPOSTE_DELIBERA_STORICO set xml_soggetti_clob = xml_soggetti
/

ALTER TABLE PROPOSTE_DELIBERA_STORICO DROP COLUMN XML_SOGGETTI
/

ALTER TABLE PROPOSTE_DELIBERA_STORICO RENAME COLUMN XML_SOGGETTI_CLOB TO XML_SOGGETTI
/

ALTER TABLE PROPOSTE_DELIBERA_STORICO MODIFY(XML_SOGGETTI NOT NULL)
/

-- cambio il tipo delle determine collegate:
ALTER TABLE DETERMINE_STORICO ADD (xml_doc_clob CLOB)
/

update DETERMINE_STORICO set xml_doc_clob = xml_determine_collegate
/

ALTER TABLE DETERMINE_STORICO DROP COLUMN xml_determine_collegate
/

ALTER TABLE DETERMINE_STORICO RENAME COLUMN xml_doc_clob TO xml_determine_collegate
/

-- sulle proposte di delibera:
ALTER TABLE PROPOSTE_DELIBERA_STORICO ADD (xml_doc_clob CLOB)
/

update PROPOSTE_DELIBERA_STORICO set xml_doc_clob = xml_delibere_collegate
/

ALTER TABLE PROPOSTE_DELIBERA_STORICO DROP COLUMN xml_delibere_collegate
/

ALTER TABLE PROPOSTE_DELIBERA_STORICO RENAME COLUMN xml_doc_clob TO xml_delibere_collegate
/


-- gestione del CIG (codice identificativo gara)
ALTER TABLE DETERMINE ADD (CIG   VARCHAR2(255))
/

ALTER TABLE DETERMINE_STORICO ADD (CIG   VARCHAR2(255))
/

ALTER TABLE TIPI_DETERMINA ADD (CIG                            CHAR(1 BYTE)   DEFAULT 'N' NOT NULL,
                                CIG_OBBLIGATORIO               CHAR(1 BYTE)   DEFAULT 'N' NOT NULL)
/

-- elimina pulsante 'Presa Visione' e risistema l'ordinamento dei pulsanti.
begin
    for p in (select p.id_pulsante
                  from wkf_diz_pulsanti p
                 where p.etichetta = 'Presa Visione')
    loop
    
        update wkf_cfg_competenze a set a.id_pulsante_provenienza = null where a.id_pulsante_provenienza = p.id_pulsante;
        
        delete from wkf_cfg_competenze a where a.id_pulsante = p.id_pulsante;
        delete from wkf_cfg_pulsanti_attori a where exists(select 1 from wkf_cfg_pulsanti p1 where a.id_cfg_pulsante = p1.id_cfg_pulsante and p1.id_pulsante = p.id_pulsante);
        delete from wkf_cfg_pulsanti a where a.id_pulsante = p.id_pulsante;
        delete from wkf_diz_pulsanti_azioni a where a.id_pulsante = p.id_pulsante;
        delete from wkf_diz_pulsanti a where a.id_pulsante = p.id_pulsante;
        
    end loop;
    
    for c in (select p.id_cfg_pulsante, p.sequenza+1 sequenza, row_number() over (partition by p.id_cfg_step order by p.id_cfg_step, p.sequenza asc) sequenza_nuova
      from wkf_cfg_pulsanti p
     order by p.id_cfg_step asc, p.sequenza asc)
    loop
        update wkf_cfg_pulsanti p set p.sequenza = (c.sequenza_nuova - 1) where p.id_cfg_pulsante = c.id_cfg_pulsante;
    end loop;
    
    commit;
end;
/

update impostazioni set valore = 'N' where codice = 'CONTABILITA' and valore = 'integrazioneContabilitaNessuna'
/

update impostazioni set valore = '2000' where codice = 'LUNGHEZZA_OGGETTO' and valore = '200'
/

begin execute immediate 'create index dete_cf_ik on determine(anno_determina,numero_determina)'; exception when others then null; end;
/

begin execute immediate 'create index deli_cf_ik on delibere(anno_delibera,numero_delibera)'; exception when others then null; end;
/

begin execute immediate 'create index tire_cf_ik on tipi_registro(registro_esterno)'; exception when others then null; end;
/

begin execute immediate 'create index prop_deli_cf_ik on proposte_delibera(anno_proposta, numero_proposta)'; exception when others then null; end;
/

begin execute immediate 'CREATE UNIQUE INDEX DELIBERE_NUMERO_DELI_UK ON DELIBERE (ANNO_DELIBERA, NVL2("NUMERO_DELIBERA","ENTE",NULL), NUMERO_DELIBERA, REGISTRO_DELIBERA)'; exception when others then null; end;
/

begin execute immediate 'CREATE UNIQUE INDEX DETERMINE_NUMERO_DETE_UK ON DETERMINE (ANNO_DETERMINA, NVL2("NUMERO_DETERMINA","ENTE",NULL), NUMERO_DETERMINA, REGISTRO_DETERMINA)'; exception when others then null; end;
/

begin execute immediate 'CREATE UNIQUE INDEX DETERMINE_NUMERO_PROP_UK ON DETERMINE (ANNO_PROPOSTA, NVL2("NUMERO _PROPOSTA","ENTE",NULL), NUMERO_PROPOSTA, REGISTRO_PROPOSTA)'; exception when others then null; end;
/

drop table QRTZ_BLOB_TRIGGERS cascade constraints
/

drop table QRTZ_CALENDARS            cascade constraints
/

drop table QRTZ_CRON_TRIGGERS        cascade constraints
/

drop table QRTZ_FIRED_TRIGGERS       cascade constraints
/

drop table QRTZ_JOB_DETAILS          cascade constraints
/

drop table QRTZ_LOCKS                cascade constraints
/

drop table QRTZ_PAUSED_TRIGGER_GRPS  cascade constraints
/

drop table QRTZ_SCHEDULER_STATE      cascade constraints
/

drop table QRTZ_SIMPLE_TRIGGERS      cascade constraints
/

drop table QRTZ_SIMPROP_TRIGGERS     cascade constraints
/

drop table QRTZ_TRIGGERS             cascade constraints
/

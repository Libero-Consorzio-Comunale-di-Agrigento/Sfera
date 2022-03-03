--liquibase formatted sql
--changeset rdestasio:2.1.0.0_20200221_01

-- caratteristica soggetti certificati:
update caratteristiche_tipologie set layout_soggetti = '/atti/documenti/certificato.zul' where TIPO_OGGETTO = 'CERTIFICATO'
/

-- delibere che non passano da odg:
ALTER TABLE DELIBERE MODIFY(ANNO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE MODIFY(DATA_NUMERO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE MODIFY(NUMERO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE MODIFY(REGISTRO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE MODIFY(RISERVATO  DEFAULT 'N')
/

ALTER TABLE DELIBERE MODIFY(DATA_ADOZIONE  NULL)
/

ALTER TABLE DELIBERE ADD (CAMPI_PROTETTI VARCHAR(255) NULL)
/

ALTER TABLE DELIBERE_STORICO MODIFY(ANNO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE_STORICO MODIFY(DATA_NUMERO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE_STORICO MODIFY(NUMERO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE_STORICO MODIFY(REGISTRO_DELIBERA  NULL)
/

ALTER TABLE DELIBERE_STORICO MODIFY(RISERVATO  DEFAULT 'N')
/

ALTER TABLE DELIBERE_STORICO ADD (DATA_ADOZIONE DATE NULL)
/

ALTER TABLE TIPI_DELIBERA ADD (ID_CARATTERISTICA_DELIBERA  NUMBER(19))
/

CREATE INDEX TIPDEL_CARTIP_DEL_FK ON TIPI_DELIBERA (ID_CARATTERISTICA_DELIBERA) LOGGING NOPARALLEL
/

ALTER TABLE TIPI_DELIBERA ADD (ID_MODELLO_TESTO_FRONTESPIZIO  NUMBER(19))
/

CREATE INDEX TIPDEL_GTEMOD_FRO_FK ON TIPI_DELIBERA (ID_MODELLO_TESTO_FRONTESPIZIO) LOGGING NOPARALLEL
/

ALTER TABLE TIPI_DELIBERA ADD (ID_MODELLO_TESTO_DELIBERA  NUMBER(19))
/

CREATE INDEX TIPDEL_GTEMOD_DEL_FK ON TIPI_DELIBERA (ID_MODELLO_TESTO_DELIBERA) LOGGING NOPARALLEL
/

ALTER TABLE TIPI_DELIBERA ADD (PROGRESSIVO_CFG_ITER_DELIBERA  NUMBER(19))
/

ALTER TABLE TIPI_DELIBERA MODIFY (ID_COMMISSIONE NULL)
/

-- inserisco i nuovi ruoli partecipanti:
declare
    d_count number;
begin
    select count(1) into d_count from odg_ruoli_partecipanti;
    if (d_count > 0) then
        insert into odg_ruoli_partecipanti (ruolo_partecipante, data_ins, descrizione, ente, data_upd, utente_ins, utente_upd, valido, valido_dal) values ('DIRETTORE_AMMINISTRATIVO', sysdate, 'DIRETTORE AMMINISTRATIVO', (select ente from odg_ruoli_partecipanti where rownum = 1), sysdate, (select utente_ins from odg_ruoli_partecipanti where rownum = 1), (select utente_ins from odg_ruoli_partecipanti where rownum = 1), 'Y', sysdate);
        insert into odg_ruoli_partecipanti (ruolo_partecipante, data_ins, descrizione, ente, data_upd, utente_ins, utente_upd, valido, valido_dal) values ('DIRETTORE_GENERALE',       sysdate, 'DIRETTORE GENERALE',       (select ente from odg_ruoli_partecipanti where rownum = 1), sysdate, (select utente_ins from odg_ruoli_partecipanti where rownum = 1), (select utente_ins from odg_ruoli_partecipanti where rownum = 1), 'Y', sysdate);
        insert into odg_ruoli_partecipanti (ruolo_partecipante, data_ins, descrizione, ente, data_upd, utente_ins, utente_upd, valido, valido_dal) values ('DIRETTORE_SANITARIO',      sysdate, 'DIRETTORE SANITARIO',      (select ente from odg_ruoli_partecipanti where rownum = 1), sysdate, (select utente_ins from odg_ruoli_partecipanti where rownum = 1), (select utente_ins from odg_ruoli_partecipanti where rownum = 1), 'Y', sysdate);
    end if;
end;
/

-- inserisco le nuove regole di calcolo:
insert into REGOLE_CALCOLO (ID_REGOLA, CATEGORIA, DESCRIZIONE, NOME_BEAN, NOME_METODO, TIPO, TITOLO) Values (hibernate_sequence.nextval, 'COMPONENTE', NULL, 'regolaCalcoloService', 'getComponenteRelatore', 'DEFAULT', 'Il relatore della Proposta di Delibera')
/
insert into REGOLE_CALCOLO (ID_REGOLA, CATEGORIA, DESCRIZIONE, NOME_BEAN, NOME_METODO, TIPO, TITOLO) Values (hibernate_sequence.nextval, 'COMPONENTE', NULL, 'regolaCalcoloService', 'getComponentePresidente', 'DEFAULT', 'Il Presidente della Seduta o quello dichiarato in Commissione.')
/
insert into REGOLE_CALCOLO (ID_REGOLA, CATEGORIA, DESCRIZIONE, NOME_BEAN, NOME_METODO, TIPO, TITOLO) Values (hibernate_sequence.nextval, 'COMPONENTE', NULL, 'regolaCalcoloService', 'getComponenteSegretario', 'DEFAULT', 'Il Segretario della Seduta o quello dichiarato in Commissione.')
/
insert into REGOLE_CALCOLO (ID_REGOLA, CATEGORIA, DESCRIZIONE, NOME_BEAN, NOME_METODO, TIPO, TITOLO) Values (hibernate_sequence.nextval, 'COMPONENTE', NULL, 'regolaCalcoloService', 'getComponenteDirettoreAmministrativo', 'DEFAULT', 'Il Direttore Amministrativo in Seduta o quello dichiarato in Commissione.')
/
insert into REGOLE_CALCOLO (ID_REGOLA, CATEGORIA, DESCRIZIONE, NOME_BEAN, NOME_METODO, TIPO, TITOLO) Values (hibernate_sequence.nextval, 'COMPONENTE', NULL, 'regolaCalcoloService', 'getComponenteDirettoreSanitario', 'DEFAULT', 'Il Direttore Sanitario in Seduta o quello dichiarato in Commissione.')
/
insert into REGOLE_CALCOLO (ID_REGOLA, CATEGORIA, DESCRIZIONE, NOME_BEAN, NOME_METODO, TIPO, TITOLO) Values (hibernate_sequence.nextval, 'COMPONENTE', NULL, 'regolaCalcoloService', 'getComponenteDirettoreGenerale', 'DEFAULT', 'Il Direttore Generale in Seduta o quello dichiarato in Commissione.')
/

-- inserisco la caratteristica di delibera di default:
declare
    d_count number;
begin
    select count(1) into d_count from CARATTERISTICHE_TIPOLOGIE;
    if (d_count > 0) then
        insert into CARATTERISTICHE_TIPOLOGIE (ID_CARATTERISTICA_TIPOLOGIA, VERSION, DATA_INS, DESCRIZIONE, ENTE, DATA_UPD, LAYOUT_SOGGETTI, TIPO_OGGETTO, TITOLO, UTENTE_INS, UTENTE_UPD, VALIDO, VALIDO_AL, VALIDO_DAL)
         values
           (hibernate_sequence.nextval, 0, sysdate, 'Delibera', (select ente from caratteristiche_tipologie where rownum = 1), sysdate, '/atti/documenti/delibera/delibera_standard.zul', 'DELIBERA', 'Delibera', (select utente_ins from caratteristiche_tipologie where rownum = 1), (select utente_upd from caratteristiche_tipologie where rownum = 1), 'Y', NULL, sysdate);

        insert into CARATTERISTICHE_TIPI_SOGGETTO (ID_CARATTERISTICA_SOGGETTO, VERSION, ID_CARATTERISTICA_TIPOLOGIA, ID_REGOLA_DEFAULT, ID_REGOLA_LISTA, RUOLO, SEQUENZA, TIPO_SOGGETTO, TIPO_SOGGETTO_PARTENZA)
         values (hibernate_sequence.nextval, 0
               , (select id_caratteristica_tipologia from caratteristiche_tipologie where layout_soggetti = '/atti/documenti/delibera/delibera_standard.zul' and rownum = 1)
              , (select id_regola from regole_calcolo where nome_bean = 'regolaCalcoloService' and nome_metodo = 'getComponentePresidente' and rownum = 1)
              , NULL, NULL, 1, 'PRESIDENTE', NULL);

        insert into CARATTERISTICHE_TIPI_SOGGETTO (ID_CARATTERISTICA_SOGGETTO, VERSION, ID_CARATTERISTICA_TIPOLOGIA, ID_REGOLA_DEFAULT, ID_REGOLA_LISTA, RUOLO, SEQUENZA, TIPO_SOGGETTO, TIPO_SOGGETTO_PARTENZA)
         values (hibernate_sequence.nextval, 1
               , (select id_caratteristica_tipologia from caratteristiche_tipologie where layout_soggetti = '/atti/documenti/delibera/delibera_standard.zul' and rownum = 1)
              , (select id_regola from regole_calcolo where nome_bean = 'regolaCalcoloService' and nome_metodo = 'getComponenteSegretario' and rownum = 1)
              , NULL, NULL, 0, 'SEGRETARIO', NULL);

        -- assegno le caratteristiche appena create alle tipologie di delibera esistenti:
        update tipi_delibera td set td.id_caratteristica_delibera = (select id_caratteristica_tipologia from caratteristiche_tipologie where layout_soggetti = '/atti/documenti/delibera/delibera_standard.zul' and rownum = 1) where td.id_caratteristica_delibera is null;
    end if;
end;
/

-- rendo l'iter opzionale in commissione:
ALTER TABLE ODG_COMMISSIONI MODIFY (PROGRESSIVO_CFG_ITER NULL)
/

-- aggiorno l'iter sulle tipologie di delibera:
update tipi_delibera td set td.progressivo_cfg_iter_delibera = (select c.progressivo_cfg_iter from odg_commissioni c where c.id_commissione = td.id_commissione) where td.progressivo_cfg_iter_delibera is null
/

-- aggiungo la colonna per la copia del testo della proposta
ALTER TABLE TIPI_DELIBERA ADD (COPIA_TESTO_PROPOSTA CHAR(1 BYTE) DEFAULT 'N')
/

-- aggiungo la colonna per la data scadenza
ALTER TABLE DETERMINE ADD (DATA_SCADENZA date)
/

-- aggiungo la colonna per la motivazione scadenza 
ALTER TABLE DETERMINE ADD (MOTIVAZIONE_SCADENZA VARCHAR(4000))
/

-- aggiungo la colonna per la data scadenza
ALTER TABLE DETERMINE_STORICO ADD (DATA_SCADENZA date)
/

-- aggiungo la colonna per la motivazione scadenza 
ALTER TABLE DETERMINE_STORICO ADD (MOTIVAZIONE_SCADENZA VARCHAR(4000))
/

ALTER TABLE TIPI_DETERMINA ADD (ESECUTIVITA_MOVIMENTI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

-- ricreo l'indice univoco sulla delibera siccome può avere anno/numero/registro nulli:
begin
    execute immediate 'DROP INDEX DELIBERE_NUMERO_DELI_UK';
exception when others then
    null;
end;
/

CREATE UNIQUE INDEX DELIBERE_NUMERO_DELI_UK ON DELIBERE (nvl2(numero_delibera, ANNO_DELIBERA, null), NVL2(NUMERO_DELIBERA,ENTE,NULL), NUMERO_DELIBERA, nvl2(numero_delibera, REGISTRO_DELIBERA, null)) LOGGING NOPARALLEL
/
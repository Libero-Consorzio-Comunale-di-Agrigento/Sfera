--liquibase formatted sql
--changeset rdestasio:2.4.0.0_20200221_01

-- #22440 Slittamento data esecutività in caso di giorno festivo
CREATE TABLE CALENDARIO_FESTIVITA
(
   ID_CALENDARIO_FESTIVITA   NUMBER (19) NOT NULL,
   VERSION                   NUMBER (19) NOT NULL,
   DESCRIZIONE               VARCHAR2 (255 BYTE) NOT NULL,
   GIORNO                    NUMBER (2) NOT NULL,
   MESE                      NUMBER (2) NOT NULL,
   ANNO                      NUMBER (4),
   ENTE                      VARCHAR2 (255 BYTE) NOT NULL,
   DATA_INS                  DATE NOT NULL,
   DATA_UPD                  DATE NOT NULL,
   UTENTE_INS                VARCHAR2 (255 BYTE) NOT NULL,
   UTENTE_UPD                VARCHAR2 (255 BYTE) NOT NULL,
   VALIDO                    CHAR (1 BYTE) NOT NULL,
   VALIDO_AL                 DATE,
   VALIDO_DAL                DATE NOT NULL
)
/

ALTER TABLE CALENDARIO_FESTIVITA ADD (PRIMARY KEY (ID_CALENDARIO_FESTIVITA) ENABLE VALIDATE)
/

-- gestione degli oggetti file ricorrenti legati alla tipologia http://svi-redmine/issues/22462
CREATE TABLE TIPI_DELIBERA_OGG_RIC (ID_OGGETTO_RICORRENTE NUMBER(19) NOT NULL,
											              ID_TIPO_DELIBERA      NUMBER(19) NOT NULL)
/

CREATE TABLE TIPI_DETERMINA_OGG_RIC (ID_OGGETTO_RICORRENTE NUMBER(19) NOT NULL,
											               ID_TIPO_DETERMINA     NUMBER(19) NOT NULL)
/

ALTER TABLE DETERMINE ADD ID_OGGETTO_RICORRENTE NUMBER (19)
/

ALTER TABLE PROPOSTE_DELIBERA ADD ID_OGGETTO_RICORRENTE NUMBER (19)
/

ALTER TABLE OGGETTI_RICORRENTI ADD CODICE varchar2 (255 byte) null
/
ALTER TABLE OGGETTI_RICORRENTI MODIFY (OGGETTO varchar2 (4000 byte))
/

-- #22613 Pubblicazione nel futuro
alter table proposte_delibera add data_min_pubblicazione DATE NULL
/

alter table determine add data_min_pubblicazione DATE NULL
/

alter table delibere add da_pubblicare CHAR(1 byte) default 'Y'
/

alter table determine add da_pubblicare CHAR(1 byte) default 'Y'
/

alter table tipi_determina add pubblicazione_futura char (1 byte) default 'N'
/

alter table tipi_delibera add pubblicazione_futura char (1 byte) default 'N'
/

-- #22294	Inserimento del campo "data Seduta prevista il"
ALTER TABLE PROPOSTE_DELIBERA ADD DATA_SCADENZA DATE
/

-- #22821 Delibere: campo motivazioni per immediata eseguibilità
alter table delibere_storico add motivazioni_eseguibilita varchar2(255 byte) NULL
/

alter table delibere add motivazioni_eseguibilita varchar2(255 byte) NULL
/

alter table proposte_delibera add motivazioni_eseguibilita varchar2(255 byte) NULL
/

alter table odg_oggetti_seduta add motivazioni_eseguibilita varchar2(255 byte) NULL
/

-- #22460 Giorni di pubblicazione non modificabili per tipologia
alter table tipi_determina add giorni_pubb_modificabile char(1 byte) default 'Y'
/

alter table tipi_delibera add giorni_pubb_modificabile char(1 byte) default 'Y'
/

-- gestione dei movimenti contabili sulle delibere
ALTER TABLE TIPI_DELIBERA ADD (ESECUTIVITA_MOVIMENTI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

-- aggiornamento a gestione-iter 1.4: aggiunta gestione delle istruzioni_sql, refactor delle azioni client
alter table wkf_diz_azioni add (istruzione_sql varchar2(255 byte))
/

update wkf_diz_azioni set nome_metodo = nome_metodo_client where nome_metodo_client is not NULL
/

declare
 d_constraint_name varchar2(255);
begin
select constraint_name
  into d_constraint_name
  from user_cons_columns
 where table_name = 'WKF_DIZ_AZIONI'
   and column_name = 'NOME_METODO_CLIENT';

 execute immediate 'alter table wkf_diz_azioni drop constraint '||d_constraint_name;
end;
/

alter table wkf_diz_azioni drop column nome_metodo_client
/

ALTER TABLE WKF_DIZ_AZIONI ADD CONSTRAINT WKFDIZAZ_BNMTSQL_UK UNIQUE (ISTRUZIONE_SQL, TIPO_OGGETTO, NOME_METODO, NOME_BEAN) ENABLE VALIDATE
/

-- indici su valido ed ente
CREATE INDEX DET_VALIDO_IK ON DETERMINE (VALIDO) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX DEL_VALIDO_IK ON DELIBERE (VALIDO) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX VP_VALIDO_IK ON VISTI_PARERI (VALIDO) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX CER_VALIDO_IK ON CERTIFICATI (VALIDO) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX PDE_VALIDO_IK ON PROPOSTE_DELIBERA (VALIDO) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX DET_ENTE_IK ON DETERMINE (ENTE) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX DEL_ENTE_IK ON DELIBERE (ENTE) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX VP_ENTE_IK ON VISTI_PARERI (ENTE) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX CER_ENTE_IK ON CERTIFICATI (ENTE) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

CREATE INDEX PDE_ENTE_IK ON PROPOSTE_DELIBERA (ENTE) NOLOGGING STORAGE (BUFFER_POOL DEFAULT) NOPARALLEL
/

-- conversione dell'id documento esterno da long a string
ALTER TABLE AG_ACQUISIZIONE_DETERMINE ADD (IDDOCEST VARCHAR (255))
/

UPDATE AG_ACQUISIZIONE_DETERMINE set IDDOCEST = ID_DOC_ESTERNO
/

ALTER TABLE AG_ACQUISIZIONE_DETERMINE DROP PRIMARY KEY
/

ALTER TABLE AG_ACQUISIZIONE_DETERMINE DROP COLUMN ID_DOC_ESTERNO
/

ALTER TABLE AG_ACQUISIZIONE_DETERMINE RENAME COLUMN IDDOCEST TO ID_DOC_ESTERNO
/

ALTER TABLE AG_ACQUISIZIONE_DETERMINE ADD (PRIMARY KEY (APPLICATIVO_ESTERNO, ID_DOC_ESTERNO) ENABLE VALIDATE)
/

CREATE TABLE AG_ACQUISIZIONE_DELIBERE (
  APPLICATIVO_ESTERNO   VARCHAR2(255 BYTE),
  ID_DOC_ESTERNO        VARCHAR2(255 BYTE),
  TIPO_REGISTRO         VARCHAR2(10 BYTE)       DEFAULT 'PAR'                 NOT NULL,
  MOVIMENTO             VARCHAR2(10 BYTE)       DEFAULT 'PAR'                 NOT NULL,
  TIPOLOGIA             VARCHAR2(100 BYTE),
  CODICE_MODELLO        VARCHAR2(100 BYTE)      NOT NULL,
  OGGETTO               VARCHAR2(1000 BYTE)     NOT NULL,
  CLASSIFICAZIONE       VARCHAR2(40 BYTE),
  ANNO_CLA              NUMBER(10),
  NUMERO_CLA            VARCHAR2(30 BYTE),
  UNITA_ESIBENTE        VARCHAR2(50 BYTE),
  UNITA_PROTOCOLLANTE   VARCHAR2(50 BYTE)       NOT NULL,
  UTENTE_PROTOCOLLANTE  VARCHAR2(40 BYTE)       NOT NULL,
  NOTE                  VARCHAR2(255 BYTE),
  IMPEGNO               CHAR(1 BYTE),
  APRI_REGISTRO         CHAR(1 BYTE) DEFAULT 'N',
  UTENTE_DIRIGENTE      VARCHAR2(255 BYTE)        NOT NULL,
  PROGR_ASSESSORATO     NUMBER(8),
  FILE_DOCUMENTO        BLOB,
  STATO_ACQUISIZIONE    VARCHAR2(100 BYTE)      DEFAULT 'ELABORARE'           NOT NULL,
  NOMEFILE              VARCHAR2(255 BYTE),
  DOCUMENTI_FASCICOLO   VARCHAR2(4000 BYTE),
  DATA_INS              DATE NOT NULL,
  SPESA                 VARCHAR2(255 BYTE),
  CIG                   VARCHAR2(500 BYTE),
  TIPO_ATTO             VARCHAR2(255 BYTE),
  UTENTE_INSERIMENTO    VARCHAR2(255 BYTE),
  FORMATO_FILE          VARCHAR2(255 BYTE),
  UNITA_CONTROLLO       VARCHAR2(255 BYTE),
  ID_FATTURE            VARCHAR2(1000 BYTE),
  FIRMATARI             VARCHAR2(1000 BYTE),
  PROPOSTA_DELIBERA_ID  NUMBER(19),
  ENTE                  VARCHAR2(255 BYTE)
)
/

CREATE UNIQUE INDEX IX_ACQ_DELI ON AG_ACQUISIZIONE_DELIBERE
(APPLICATIVO_ESTERNO, ID_DOC_ESTERNO)
/

CREATE OR REPLACE TRIGGER AG_ACQUISIZIONE_DELIBERE_TIU
BEFORE INSERT OR UPDATE
ON AG_ACQUISIZIONE_DELIBERE REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
BEGIN

    IF INSERTING  and :new.oggetto IS NOT NULL then
   :new.oggetto := upper (:new.oggetto);
   end if;
   IF :new.oggetto IS NOT NULL
   THEN
      IF (:new.oggetto <> NVL (:old.oggetto, ' '))
      --IF UPDATING ('oggetto') OR INSERTING ('oggetto')
      THEN
         :new.oggetto := REPLACE (:new.oggetto, ' ¿ ', ' - ');
         :new.oggetto := REPLACE (:new.oggetto, 'A¿ ', 'A'' ');
         :new.oggetto := REPLACE (:new.oggetto, 'À', 'A''');
         :new.oggetto := REPLACE (:new.oggetto, 'L¿', 'L''');
         :new.oggetto := REPLACE (:new.oggetto, '¿', '''');
         :new.oggetto := REPLACE (:new.oggetto, CHR (13) || CHR (10), ' ');
         :new.oggetto := REPLACE (:new.oggetto, '°', '.');
         :new.oggetto := REPLACE (:new.oggetto, ' ? ',' - ');
         :new.oggetto := REPLACE (:new.oggetto, 'L?','L''');
         :new.oggetto := REPLACE (:new.oggetto, ' ? ',' - ');
         :new.oggetto := REPLACE (:new.oggetto, '?', '''');
         :new.oggetto := REPLACE (:new.oggetto,'É','E''');
         :new.oggetto := REPLACE (:new.oggetto,'Ò','O''');
         :new.oggetto := REPLACE (:new.oggetto,'·','');
      END IF;
   END IF;
END AG_ACQUISIZIONE_DELIBERE_TIU;
/


ALTER TABLE AG_ACQUISIZIONE_DELIBERE ADD (
  CONSTRAINT IX_ACQ_DELI
  PRIMARY KEY
  (APPLICATIVO_ESTERNO, ID_DOC_ESTERNO)
  USING INDEX IX_ACQ_DELI
  ENABLE VALIDATE)
/

-- gestione dei riferimenti a documenti esterni.
CREATE TABLE RIFERIMENTI_ESTERNI (ID_RIFERIMENTO_ESTERNO NUMBER(19) NOT NULL, ID_DOCUMENTO_ESTERNO NUMBER(19) NOT NULL, CODICE_DOCUMENTALE_ESTERNO VARCHAR2(255 byte) NOT NULL, TITOLO VARCHAR2(4000 byte) NOT NULL, TIPO_DOCUMENTO VARCHAR2(255 byte) NOT NULL,
  DATA_INS                      DATE                NOT NULL,
  DATA_UPD                      DATE                NOT NULL,
  ENTE                          VARCHAR2(255 BYTE)     NOT NULL,
  UTENTE_INS                    VARCHAR2(255 BYTE)     NOT NULL,
  UTENTE_UPD                    VARCHAR2(255 BYTE)     NOT NULL,
  VALIDO                        CHAR(1 BYTE) DEFAULT 'Y' NOT NULL,
  VERSION                       NUMBER(19) NOT NULL)
/

ALTER TABLE RIFERIMENTI_ESTERNI ADD (PRIMARY KEY (ID_RIFERIMENTO_ESTERNO) ENABLE VALIDATE)
/

ALTER TABLE documenti_collegati add (id_riferimento_est_coll NUMBER(19) NULL)
/

ALTER TABLE documenti_collegati add (FOREIGN KEY (id_riferimento_est_coll) REFERENCES RIFERIMENTI_ESTERNI (ID_RIFERIMENTO_ESTERNO) ENABLE VALIDATE)
/

CREATE INDEX rifestcol_rifest_fk ON DOCUMENTI_COLLEGATI (id_riferimento_est_coll) LOGGING NOPARALLEL
/

ALTER TABLE DELEGHE ADD (ID_DELEGA_STORICO NUMBER(19))
/

-- gestione dei tipi allegato:
insert into TIPI_ALLEGATO (
  CODICE, DATA_INS, DATA_UPD,
  DESCRIZIONE, ENTE,
  ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
  MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
  STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
  TITOLO, UTENTE_INS, UTENTE_UPD,
  VALIDO, VALIDO_AL, VALIDO_DAL,
  VERSION)
select 'SCHEDA_CONTABILE',
 sysdate,
 sysdate,
 'Scheda Contabile',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_STAMPAUNICA', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'N'),
 decode(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_DAFIRMARE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y', 'DA_FIRMARE', 'DA_NON_FIRMARE'),
 'DETERMINA',
 'Scheda Contabile',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                from TIPI_ALLEGATO
                where (codice = 'SCHEDA_CONTABILE' and tipologia = 'DETERMINA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'SCHEDA_CONTABILE',
 sysdate,
 sysdate,
 'Scheda Contabile',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_STAMPAUNICA', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'N'),
 decode(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_DAFIRMARE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y', 'DA_FIRMARE', 'DA_NON_FIRMARE'),
 'DELIBERA',
 'Scheda Contabile',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'SCHEDA_CONTABILE' and tipologia = 'DELIBERA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'SCHEDA_CONTABILE',
 sysdate,
 sysdate,
 'Scheda Contabile',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_STAMPAUNICA', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'N'),
 decode(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_DAFIRMARE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y', 'DA_FIRMARE', 'DA_NON_FIRMARE'),
 'VISTO',
 'Scheda Contabile',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'SCHEDA_CONTABILE' and tipologia = 'VISTO')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'SCHEDA_CONTABILE',
 sysdate,
 sysdate,
 'Scheda Contabile',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_STAMPAUNICA', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'N'),
 decode(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_DAFIRMARE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y', 'DA_FIRMARE', 'DA_NON_FIRMARE'),
 'PARERE',
 'Scheda Contabile',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'SCHEDA_CONTABILE' and tipologia = 'PARERE')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'SCHEDA_CONTABILE',
 sysdate,
 sysdate,
 'Scheda Contabile',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('PUBBLICAZIONE_SCHEDA_CONTABILE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y'),
 nvl(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_STAMPAUNICA', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'N'),
 decode(impostazioni_pkg.get_impostazione('CONTABILITA_SCHEDA_DAFIRMARE', (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*')), 'Y', 'DA_FIRMARE', 'DA_NON_FIRMARE'),
 'PROPOSTA_DELIBERA',
 'Scheda Contabile',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'SCHEDA_CONTABILE' and tipologia = 'PROPOSTA_DELIBERA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'OMISSIS',
 sysdate,
 sysdate,
 'Omissis',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'PROPOSTA_DELIBERA',
 'Omissis',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'OMISSIS' and tipologia = 'PROPOSTA_DELIBERA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'OMISSIS',
 sysdate,
 sysdate,
 'Omissis',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'DETERMINA',
 'Omissis',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'OMISSIS' and tipologia = 'DETERMINA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'OMISSIS',
 sysdate,
 sysdate,
 'Omissis',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'DELIBERA',
 'Omissis',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'OMISSIS' and tipologia = 'DELIBERA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'OMISSIS',
 sysdate,
 sysdate,
 'Omissis',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'VISTO',
 'Omissis',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'OMISSIS' and tipologia = 'VISTO')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'OMISSIS',
 sysdate,
 sysdate,
 'Omissis',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'PARERE',
 'Omissis',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'OMISSIS' and tipologia = 'PARERE')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'FRONTESPIZIO',
 sysdate,
 sysdate,
 'Frontespizio',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_NON_FIRMARE',
 'DETERMINA',
 'Frontespizio',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'FRONTESPIZIO' and tipologia = 'DETERMINA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'FRONTESPIZIO',
 sysdate,
 sysdate,
 'Frontespizio',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_NON_FIRMARE',
 'DELIBERA',
 'Frontespizio',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'FRONTESPIZIO' and tipologia = 'DELIBERA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'FRONTESPIZIO',
 sysdate,
 sysdate,
 'Frontespizio',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_NON_FIRMARE',
 'PROPOSTA_DELIBERA',
 'Frontespizio',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'FRONTESPIZIO' and tipologia = 'PROPOSTA_DELIBERA')) and exists (select 1 from tipi_allegato)
/

-- update di tutti gli allegati che non hanno già un tipo_allegato
update allegati a set a.id_tipo_allegato = (select t.id_tipo_allegato from tipi_allegato t where t.codice = a.codice
                                and t.tipologia = (select case when a1.id_determina is not null then 'DETERMINA'
                                when a1.id_delibera is not null then 'DELIBERA'
                                when a1.id_proposta_delibera is not null then 'PROPOSTA_DELIBERA'
                                else (select case when vp.id_determina is not null then 'VISTO' else 'PARERE' end case from visti_pareri vp where vp.id_visto_parere = a1.id_visto_parere)
                           end case from allegati a1 where a1.id_allegato = a.id_allegato)) where a.id_tipo_allegato is null
/


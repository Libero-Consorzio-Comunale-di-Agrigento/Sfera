--liquibase formatted sql
--changeset rdestasio:2.1.7.0_20200221_01

ALTER TABLE DELIBERE ADD (DATA_ESECUTIVITA_MANUALE DATE)
/

ALTER TABLE ODG_COMMISSIONI ADD (SEDUTA_PUBBLICA CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE ODG_SEDUTE ADD (PUBBLICA CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

-- gestione del controllo di regolarità
CREATE TABLE CONTROLLO_REGOLARITA
(
  ID_CONTROLLO_REGOLARITA		NUMBER(19)      	NOT NULL,
  VERSION                       NUMBER(19)      	NOT NULL,
  AMBITO                        VARCHAR2(255 BYTE) 	NOT NULL,
  DATA_ESECUTIVITA_AL           DATE NOT NULL,
  DATA_ESECUTIVITA_DAL          DATE NOT NULL,
  DATA_INS                      DATE            	NOT NULL,
  DATA_UPD                      DATE            	NOT NULL,
  ENTE                          VARCHAR2(255 BYTE) 	NOT NULL,
  TIPO_CONTROLLO_REGOLARITA	  	NUMBER(19) 			NOT NULL,
  TIPO_REGISTRO                 VARCHAR2(255 BYTE),
  UTENTE_INS                    VARCHAR2(255 BYTE) 	NOT NULL,
  UTENTE_UPD                    VARCHAR2(255 BYTE) 	NOT NULL,
  VALIDO                        CHAR(1 BYTE)    	NOT NULL,
  VALIDO_AL                     DATE,
  VALIDO_DAL                    DATE            	NOT NULL,
  ID_MODELLO_TESTO              NUMBER(19),
  STATO                         VARCHAR2(255 BYTE),
  NUMERO_PROTOCOLLO             NUMBER(10),
  ANNO_PROTOCOLLO               NUMBER(10),
  ATTI_DA_ESTRARRE              NUMBER(10) 			NOT NULL,
  TOTALE_ATTI	                NUMBER(10),
  PERCENTUALE	                CHAR(1 BYTE)    	NOT NULL,
  CRITERI_RICERCA			 	CLOB
)
/

CREATE TABLE CONTROLLO_REGOLARITA_DOC
(
  ID_CONTROLLO_REGOLARITA_DOC NUMBER(19)      NOT NULL,
  VERSION                     NUMBER(19)      NOT NULL,
  ID_DELIBERA                 NUMBER(19),
  ID_DETERMINA                NUMBER(19),
  ID_CONTROLLO_REGOLARITA  		NUMBER(19)      NOT NULL,
  STATO 						          VARCHAR2(255 BYTE),
  NOTE 							          VARCHAR2(255 BYTE),
  ID_ESITO_CONTROLLO_REG		  NUMBER(19),
  NOTIFICATA	                CHAR(1 BYTE) NOT NULL
)
/

CREATE TABLE TIPI_CONTROLLO_REG
(
  ID_TIPO_CONTROLLO_REGOLARITA NUMBER(19) PRIMARY KEY,
  SEQUENZA                  NUMBER(10),
  ENTE                      VARCHAR2(255 BYTE) NOT NULL,
  TITOLO                    VARCHAR2(255 BYTE) NOT NULL,
  AMBITO                    VARCHAR2(255 BYTE) NOT NULL,
  VALIDO                    CHAR(1 BYTE)       NOT NULL
)
/

CREATE TABLE TIPI_ESITI_CONTROLLO_REG
(
  ID_ESITO_CONTROLLO_REG    NUMBER(19)          NOT NULL,
  VERSION                   NUMBER(19)          NOT NULL,
  DATA_INS                  DATE                NOT NULL,
  DESCRIZIONE               VARCHAR2(4000 BYTE),
  ENTE                      VARCHAR2(255 BYTE)  NOT NULL,
  DATA_UPD                  DATE                NOT NULL,
  SEQUENZA                  NUMBER(10)          NOT NULL,
  TITOLO                    VARCHAR2(255 BYTE)  NOT NULL,
  UTENTE_INS                VARCHAR2(255 BYTE)  NOT NULL,
  UTENTE_UPD                VARCHAR2(255 BYTE)  NOT NULL,
  VALIDO                    CHAR(1 BYTE)        NOT NULL,
  AMBITO					          VARCHAR2(255 BYTE)  NOT NULL
)
/

-- indici per cfa
begin execute immediate 'create index dete_cf_prop_ik on determine(anno_proposta,numero_proposta)'; exception when others then null; end;
/
--liquibase formatted sql
--changeset rdestasio:2.4.7.0_20200221_01

CREATE TABLE OPERAZIONI_LOG
(
  ID_LOG                  NUMBER(19) PRIMARY KEY,
  ID_DOCUMENTO            NUMBER(19) NOT NULL,
  TIPO_OGGETTO            VARCHAR2(255 BYTE) NOT NULL,
  VERSION                 NUMBER(19) NOT NULL,
  DATA_OPERAZIONE         DATE NOT NULL,
  PAGINA                  VARCHAR2(255 BYTE) NOT NULL,
  OPERAZIONE              VARCHAR2(255 BYTE) NOT NULL,
  DESCRIZIONE             VARCHAR2(4000 BYTE),
  UTENTE                  VARCHAR2(255 BYTE) NOT NULL
)
/


CREATE TABLE PREFERENZE
(
  ID                      NUMBER(19) PRIMARY KEY,
  CODICE                  VARCHAR2(255 BYTE) NOT NULL,
  ETICHETTA               VARCHAR2(255 BYTE) NOT NULL,
  DESCRIZIONE             VARCHAR2(4000 BYTE),
  NOME_METODO             VARCHAR2(255 BYTE) NOT NULL,
  ENTE                    VARCHAR2(255 BYTE) NOT NULL,
  VALORE_DEFAULT          VARCHAR2(255 BYTE)
)
/

CREATE TABLE PREFERENZE_UTENTE
(
  ID                      NUMBER(19) PRIMARY KEY,
  ID_PREFERENZA           NUMBER(19) NOT NULL,
  UTENTE                  VARCHAR2(255 BYTE) NOT NULL,
  VALORE                  VARCHAR2(255 BYTE) NOT NULL
)
/

DECLARE
  d_ente              varchar2 (100);
  A_SEPARATOR         VARCHAR2(1) :='#';
BEGIN

  begin
      d_ente := impostazioni_pkg.get_impostazione ('ENTI_SO4', '*');
      insert into PREFERENZE (ID, CODICE, ETICHETTA, DESCRIZIONE, NOME_METODO, ENTE, VALORE_DEFAULT) VALUES (hibernate_sequence.nextval, 'UNITA_DEFAULT', 'Unità preferita', 'Unità scelta dall''utente come preferita',  'it.finmatica.atti.integrazioni.lookup.LookupUfficioUtente', d_ente, '');
  exception
         when no_data_found then
            null;
  end;
END;
/

ALTER TABLE DOCUMENTI_COLLEGATI ADD (ID_DELIBERA_PRINCIPALE NUMBER(19))
/

CREATE INDEX DELPRINC_DET_FK ON DOCUMENTI_COLLEGATI (ID_DELIBERA_PRINCIPALE) LOGGING NOPARALLEL
/

ALTER TABLE DOCUMENTI_COLLEGATI ADD CONSTRAINT DOCCOL_DEL_PRINC_FK FOREIGN KEY (ID_DELIBERA_PRINCIPALE) REFERENCES DELIBERE (ID_DELIBERA) ENABLE VALIDATE
/

ALTER TABLE TIPI_ALLEGATO ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE ALLEGATI ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DELIBERA ADD (PUBBLICA_ALLEGATI_DEFAULT CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DETERMINA ADD (PUBBLICA_ALLEGATI_DEFAULT CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_VISTO_PARERE ADD (PUBBLICA_ALLEGATI_DEFAULT CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

UPDATE TIPI_DELIBERA SET PUBBLICA_ALLEGATI_DEFAULT = 'N' WHERE PUBBLICA_ALLEGATI = 'N'
/

UPDATE TIPI_DETERMINA SET PUBBLICA_ALLEGATI_DEFAULT = 'N' WHERE PUBBLICA_ALLEGATI = 'N'
/

UPDATE TIPI_VISTO_PARERE SET PUBBLICA_ALLEGATI_DEFAULT = 'N' WHERE PUBBLICA_ALLEGATI = 'N'
/
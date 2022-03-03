--liquibase formatted sql
--changeset rdestasio:2.3.1.0_20200221_01

-- rinomino la colonna per la motivazione scadenza in motivazione
ALTER TABLE DETERMINE RENAME COLUMN MOTIVAZIONE_SCADENZA TO MOTIVAZIONE
/

ALTER TABLE DETERMINE_STORICO RENAME COLUMN MOTIVAZIONE_SCADENZA TO MOTIVAZIONE
/

ALTER TABLE PROPOSTE_DELIBERA ADD (MOTIVAZIONE VARCHAR(4000), PRIORITA NUMBER(1))
/

ALTER TABLE PROPOSTE_DELIBERA_STORICO ADD (MOTIVAZIONE VARCHAR(4000), PRIORITA NUMBER(1))
/

-- aggiungo la colonna per la priorità
ALTER TABLE DETERMINE ADD (PRIORITA NUMBER(1))
/

ALTER TABLE DETERMINE_STORICO ADD (PRIORITA NUMBER(1))
/

-- gestione dei dati aggiuntivi
alter table dati_aggiuntivi rename to tipi_dati_aggiuntivi_valori
/

alter table tipi_dati_aggiuntivi_valori rename column id_dato_aggiuntivo to id_tipo_dato_aggiuntivo_valore
/

alter index dati_aggiuntivi_pk rename to tipi_dati_aggiuntivi_valori_pk
/

alter table tipi_dati_aggiuntivi_valori rename constraint dati_aggiuntivi_pk to TIPI_DATI_AGGIUNTIVI_VALORI_PK
/

CREATE TABLE DATI_AGGIUNTIVI (ID_DATO_AGGIUNTIVO NUMBER(19)
, VERSION NUMBER(19)
, CODICE VARCHAR2(255 BYTE)
, VALORE VARCHAR2(255 BYTE)
, ID_TIPO_DATO_AGGIUNTIVO_VALORE NUMBER (19)
, ID_DELIBERA NUMBER (19)
, ID_PROPOSTA_DELIBERA NUMBER (19)
, ID_DETERMINA NUMBER (19)
, DATA_INS DATE
, DATA_UPD DATE
, UTENTE_INS VARCHAR2(255 BYTE)
, UTENTE_UPD VARCHAR2(255 BYTE)) LOGGING NOCOMPRESS NOCACHE NOPARALLEL MONITORING
/

CREATE UNIQUE INDEX DATI_AGGIUNTIVI_PK ON DATI_AGGIUNTIVI (ID_DATO_AGGIUNTIVO) LOGGING NOPARALLEL
/

ALTER TABLE DATI_AGGIUNTIVI ADD CONSTRAINT DTAGG_TPDTAGGVAL_FK FOREIGN KEY (ID_TIPO_DATO_AGGIUNTIVO_VALORE) REFERENCES TIPI_DATI_AGGIUNTIVI_VALORI (ID_TIPO_DATO_AGGIUNTIVO_VALORE)
/

ALTER TABLE DATI_AGGIUNTIVI ADD CONSTRAINT DTAGG_DELI_FK FOREIGN KEY (ID_DELIBERA) REFERENCES DELIBERE (ID_DELIBERA)
/

ALTER TABLE DATI_AGGIUNTIVI ADD CONSTRAINT DTAGG_DETE_FK FOREIGN KEY (ID_DETERMINA) REFERENCES DETERMINE (ID_DETERMINA)
/

ALTER TABLE DATI_AGGIUNTIVI ADD CONSTRAINT DTAGG_PRDELI_FK FOREIGN KEY (ID_PROPOSTA_DELIBERA) REFERENCES PROPOSTE_DELIBERA (ID_PROPOSTA_DELIBERA)
/

update impostazioni set codice = 'DATI_AGGIUNTIVI' where codice = 'DATO_AGGIUNTIVO'
/

update impostazioni set valore = decode(valore, 'Y', 'RIFLESSI_CONTABILI', 'N') where codice = 'DATI_AGGIUNTIVI'
/

update tipi_dati_aggiuntivi_valori set codice = 'RIFLESSI_CONTABILI'
/

BEGIN
   FOR c IN (SELECT d.id_determina,
                    d.id_dato_aggiuntivo,
                    d.ente,
                    d.data_upd,
                    d.utente_upd
               FROM determine d
              WHERE d.id_dato_aggiuntivo IS NOT NULL)
   LOOP
      BEGIN
         INSERT INTO DATI_AGGIUNTIVI (CODICE,
                                      DATA_INS,
                                      DATA_UPD,
                                      ID_DATO_AGGIUNTIVO,
                                      ID_DELIBERA,
                                      ID_DETERMINA,
                                      ID_PROPOSTA_DELIBERA,
                                      ID_TIPO_DATO_AGGIUNTIVO_VALORE,
                                      UTENTE_INS,
                                      UTENTE_UPD,
                                      VALORE,
                                      VERSION)
              VALUES ('RIFLESSI_CONTABILI',
                      c.data_upd,
                      c.data_upd,
                      hibernate_sequence.NEXTVAL,
                      NULL,
                      c.id_determina,
                      NULL,
                      c.id_dato_aggiuntivo,
                      c.utente_upd,
                      c.utente_upd,
                      NULL,
                      0);
      END;
   END LOOP;
END;
/

alter table determine drop column id_dato_aggiuntivo
/

update wkf_diz_azioni set nome_bean = 'datiAggiuntiviAction', nome_metodo = 'verificaRiflessiContabili' where nome_bean = 'determinaAction' and nome_metodo = 'verificaDatiAggiuntivi'
/

-- elimino i dati aggiuntivi da determine_storico, aggiungo la colonna dati_aggiuntivi che conterrà l'xml dei dati.
alter table determine_storico add (xml_dati_aggiuntivi CLOB)
/

alter table delibere_storico add (xml_dati_aggiuntivi CLOB)
/

alter table proposte_delibera_storico add (xml_dati_aggiuntivi CLOB)
/

update determine_storico ds set ds.xml_dati_aggiuntivi = (SELECT '<datiAggiuntivi><dato id='''||ds.id_dato_aggiuntivo||''' codice='''||da.codice||''' valore='''' idTipoValore='''||da.id_tipo_dato_aggiuntivo_valore||''' tipoValore='''||DBMS_XMLGEN.CONVERT(da.descrizione)||''' sequenzaTipoValore='''||da.sequenza||'''/></datiAggiuntivi>'
  FROM tipi_dati_aggiuntivi_valori da
 WHERE da.id_tipo_dato_aggiuntivo_valore = ds.id_dato_aggiuntivo
   AND ds.id_dato_Aggiuntivo IS NOT NULL) where id_dato_aggiuntivo is not null
/

alter table determine_storico drop column id_dato_aggiuntivo
/

-- tabella per gestire la visibilità dei campi/sezione (per ora note e storico)
CREATE TABLE REGOLE_CAMPI (ID_REGOLA_CAMPO NUMBER(19) NOT NULL, DATA_INS DATE NOT NULL, ENTE VARCHAR2(255 BYTE) NOT NULL, ID_ATTORE NUMBER(19), DATA_UPD DATE, UTENTE_INS VARCHAR2(255 BYTE) NOT NULL, UTENTE_UPD VARCHAR2(255 BYTE), VALIDO CHAR(1 BYTE) NOT NULL, VISIBILE CHAR(1 BYTE) NOT NULL, MODIFICABILE CHAR(1 BYTE) NOT NULL, INVERTI_REGOLA CHAR(1 BYTE) NOT NULL, TIPO_OGGETTO VARCHAR2(255 BYTE), BLOCCO VARCHAR2(255 BYTE) NOT NULL, CAMPO VARCHAR2(255 BYTE), VERSION NUMBER(19))
/

CREATE UNIQUE INDEX REGOLE_CAMPI_PK ON REGOLE_CAMPI (ID_REGOLA_CAMPO)
/

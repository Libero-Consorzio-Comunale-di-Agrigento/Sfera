--liquibase formatted sql
--changeset rdestasio:2.4.8.0_20200221_01

ALTER TABLE MOVIMENTI_CONTABILI ADD(ESERCIZIO_ESTERNO NUMBER(19), PROGRESSIVO_ESTERNO NUMBER(19), ANNO_ESERCIZIO_ESTERNO NUMBER(19), NUMERO_MOVIMENTO NUMBER(19), DATA_MOVIMENTO DATE, CODICE_PDCF VARCHAR2(255 BYTE), DESCRIZIONE_PDCF VARCHAR2(4000 BYTE), ESECUTIVITA VARCHAR2(255 BYTE))
/

-- aggiorno i parametri per treviso:
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO' and valore = 'protocolloTrevisoTest') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'TIPO_DOCUMENTO_DETERMINA', '*', (select valore_esterno from mapping_integrazioni where categoria = 'PROTOCOLLO_DOCAREA' AND CODICE = 'TIPO_DOCUMENTO_DETERMINA'), c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'TIPO_DOCUMENTO_DELIBERA', '*', (select valore_esterno from mapping_integrazioni where categoria = 'PROTOCOLLO_DOCAREA' AND CODICE = 'TIPO_DOCUMENTO_DELIBERA'), c.ente, 0);

  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO' and valore = 'protocolloTreviso') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'TIPO_DOCUMENTO_DETERMINA', '*', (select valore_esterno from mapping_integrazioni where categoria = 'PROTOCOLLO_DOCAREA' AND CODICE = 'TIPO_DOCUMENTO_DETERMINA'), c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'TIPO_DOCUMENTO_DELIBERA', '*', (select valore_esterno from mapping_integrazioni where categoria = 'PROTOCOLLO_DOCAREA' AND CODICE = 'TIPO_DOCUMENTO_DELIBERA'), c.ente, 0);

  end loop;
end;
/

ALTER TABLE TIPI_DETERMINA ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

UPDATE TIPI_DETERMINA SET PUBBLICA_VISUALIZZATORE = PUBBLICAZIONE
/

ALTER TABLE TIPI_DELIBERA ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

UPDATE TIPI_DELIBERA SET PUBBLICA_VISUALIZZATORE = PUBBLICAZIONE
/

ALTER TABLE ODG_SEDUTE_STAMPE ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

UPDATE ODG_SEDUTE_STAMPE SET PUBBLICA_VISUALIZZATORE = PUBBLICA_NEL_VISUALIZZATORE
/

ALTER TABLE ODG_SEDUTE_STAMPE DROP COLUMN PUBBLICA_NEL_VISUALIZZATORE
/

ALTER TABLE ODG_SEDUTE_STAMPE_STORICO ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

UPDATE ODG_SEDUTE_STAMPE_STORICO SET PUBBLICA_VISUALIZZATORE = PUBBLICA_NEL_VISUALIZZATORE
/

ALTER TABLE ODG_SEDUTE_STAMPE_STORICO DROP COLUMN PUBBLICA_NEL_VISUALIZZATORE
/

ALTER TABLE DETERMINE ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

UPDATE DETERMINE SET PUBBLICA_VISUALIZZATORE = 'Y' WHERE DATA_PUBBLICAZIONE IS NOT NULL
/

ALTER TABLE DELIBERE ADD (PUBBLICA_VISUALIZZATORE CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

UPDATE DELIBERE SET PUBBLICA_VISUALIZZATORE = 'Y' WHERE DATA_PUBBLICAZIONE IS NOT NULL
/

--liquibase formatted sql
--changeset rdestasio:2.1.4.0_20200221_01

update impostazioni set valore = 'Y' where codice = 'FIRMA_CON_TIMESTAMP'
/

-- classifica e fascicoli in Delibera
ALTER TABLE DELIBERE ADD (CLASSIFICA_CODICE  VARCHAR2(255 BYTE))
/

ALTER TABLE DELIBERE ADD (CLASSIFICA_DESCRIZIONE  VARCHAR2(255 BYTE))
/

ALTER TABLE DELIBERE ADD (CLASSIFICA_DAL  DATE)
/

ALTER TABLE DELIBERE ADD (FASCICOLO_NUMERO  VARCHAR2(255 BYTE))
/

ALTER TABLE DELIBERE ADD (FASCICOLO_ANNO  NUMBER(10))
/

ALTER TABLE DELIBERE ADD (FASCICOLO_OGGETTO  VARCHAR2(4000 BYTE))
/

-- aggiungo la colonna per definire quali determine e delibere debbano essere rese esecutive
ALTER TABLE TIPI_DETERMINA ADD (DIVENTA_ESECUTIVA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

ALTER TABLE TIPI_DELIBERA ADD (DIVENTA_ESECUTIVA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

ALTER TABLE DELIBERE ADD (DIVENTA_ESECUTIVA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

ALTER TABLE DETERMINE ADD (DIVENTA_ESECUTIVA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

update delibere d 
   set  (d.classifica_codice,  d.classifica_dal,  d.classifica_descrizione,  d.fascicolo_anno,  d.fascicolo_numero,  d.fascicolo_oggetto) = 
(select pd.classifica_codice, pd.classifica_dal, pd.classifica_descrizione, pd.fascicolo_anno, pd.fascicolo_numero, pd.fascicolo_oggetto 
   from proposte_delibera pd 
  where pd.id_proposta_delibera = d.id_proposta_delibera)
/

UPDATE (SELECT *
          FROM TIPI_DELIBERA TIPI
         WHERE TIPI.ID_TIPO_CERT_ESEC IS NOT NULL) T
   SET T.DIVENTA_ESECUTIVA = 'Y'
/

UPDATE (SELECT *
          FROM TIPI_DETERMINA TIPI
         WHERE TIPI.ID_TIPO_CERT_ESEC IS NOT NULL) T
   SET T.DIVENTA_ESECUTIVA = 'Y'
/

UPDATE (SELECT DETE.*
          FROM DETERMINE DETE, TIPI_DETERMINA TIPI
         WHERE     DETE.ID_TIPO_DETERMINA = TIPI.ID_TIPO_DETERMINA
               AND TIPI.ID_TIPO_CERT_ESEC IS NOT NULL) D
   SET D.DIVENTA_ESECUTIVA = 'Y'
/

UPDATE (SELECT DELI.*
          FROM DELIBERE DELI, TIPI_DELIBERA TIPI, PROPOSTE_DELIBERA PROP
         WHERE     DELI.ID_PROPOSTA_DELIBERA = PROP.ID_PROPOSTA_DELIBERA
               AND PROP.ID_TIPO_DELIBERA = TIPI.ID_TIPO_DELIBERA
               AND TIPI.ID_TIPO_CERT_ESEC IS NOT NULL) D
   SET D.DIVENTA_ESECUTIVA = 'Y'
/

ALTER TABLE TIPI_DETERMINA ADD (SCRITTURA_MOVIMENTI_CONTABILI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DELIBERA ADD (SCRITTURA_MOVIMENTI_CONTABILI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

-- aggiorno le dimensioni dei file (non presenti su gdm)
merge into file_allegati fa
using (SELECT f.id_file_allegato,
       F.NOME,
       nvl(length(f.allegato), 0) dimensione_file
  FROM file_allegati f
 WHERE f.id_file_esterno is null
   and f.dimensione < 0) n
   on (fa.id_file_allegato = n.id_file_allegato)
when matched then update set fa.dimensione = n.dimensione_file
/
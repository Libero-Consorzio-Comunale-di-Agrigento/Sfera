--liquibase formatted sql
--changeset rdestasio:2.1.1.0_20200221_00

-- alcuni enti sono stati clonati da una versione 2.1.0.0 in cui non sono presenti queste modifiche.
-- le ripetiamo qui, in modo da riallineare la situazione.

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
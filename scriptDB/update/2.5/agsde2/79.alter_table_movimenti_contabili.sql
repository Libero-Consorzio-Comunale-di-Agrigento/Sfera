--liquibase formatted sql
--changeset mfrancesconi:2.5_20241008_79.alter_table_movimenti_contabili runOnChange:true

begin
    execute immediate 'alter table MOVIMENTI_CONTABILI add (CODICE_GLOBALE VARCHAR2(255))';
    exception when others then null;
end;
/

begin
    execute immediate 'alter table MOVIMENTI_CONTABILI add (ANNO_COMPETENZA_VARIAZIONE NUMBER(19))';
    exception when others then null;
end;
/

begin
    execute immediate 'alter table MOVIMENTI_CONTABILI add (CODICE_CENTRO_GESTORE VARCHAR2(255))';
    exception when others then null;
end;
/

begin
    execute immediate 'alter table MOVIMENTI_CONTABILI add (DESCRIZIONE_CENTRO_GESTORE VARCHAR2(4000))';
    exception when others then null;
end;
/
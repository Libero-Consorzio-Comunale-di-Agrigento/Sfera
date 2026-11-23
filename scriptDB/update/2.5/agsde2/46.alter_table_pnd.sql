--liquibase formatted sql
--changeset mfrancesconi:2.5_20230828_46.alter_table_pnd runOnChange:true

begin
    execute immediate 'alter table TIPI_DETERMINA add (PND_NOTIFICA CHAR(1) DEFAULT ''N'' NOT NULL)';
    exception when others then null;
end;
/

begin
    execute immediate 'alter table DESTINATARI_NOTIFICHE add (NI_SOGGETTO NUMBER(19), IMPORTO NUMBER(19, 2), GIORNI_SCADENZA NUMBER(19), DATA_SCADENZA DATE, CODICE_ESTERNO VARCHAR2(255))';
    exception when others then null;
end;
/
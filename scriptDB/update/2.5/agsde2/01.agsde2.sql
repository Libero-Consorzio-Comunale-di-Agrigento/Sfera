--liquibase formatted sql
--changeset rdestasio:2.5.6.0_20210928_01

begin
    execute immediate 'CREATE INDEX VISPARST_IDVENT_IK ON VISTI_PARERI_STORICO("ID_VISTO_PARERE","ENTE")';
exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE CATEGORIE ADD (controllo_cdv CHAR(1 BYTE) DEFAULT ''Y'' NOT NULL)';
exception when others then null;
end;
/

--liquibase formatted sql
--changeset mfrancesconi:2.5_20220825_12.table_odg_esiti_voti_codice runOnChange:true

begin
    execute immediate 'alter table odg_esiti add (codice_esterno varchar2 (255 byte))';
exception when others then null;
end;
/
begin
    execute immediate 'alter table odg_voti add (codice_esterno varchar2 (255 byte))';
exception when others then null;
end;
/
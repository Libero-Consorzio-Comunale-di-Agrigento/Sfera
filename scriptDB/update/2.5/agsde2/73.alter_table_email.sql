--liquibase formatted sql
--changeset mfrancesconi:2.5_20240830_73.alter_table_email runOnChange:true

begin
    execute immediate 'alter table EMAIL modify(INDIRIZZO_EMAIL varchar2(4000 byte))';
    exception when others then null;
end;
/
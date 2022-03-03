--liquibase formatted sql
--changeset mfrancesconi:2.5.7.0_20211206_07_table_delibera_classifica_descr runOnChange:true

begin
    execute immediate 'alter table delibere modify (classifica_descrizione varchar2 (4000 byte))';
exception when others then null;
end;
/
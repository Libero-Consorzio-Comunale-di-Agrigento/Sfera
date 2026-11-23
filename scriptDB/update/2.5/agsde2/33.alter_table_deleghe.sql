--liquibase formatted sql
--changeset mfrancesconi:2.5_20230331_33.alter_table_deleghe runOnChange:true

begin
    execute immediate 'alter table DELEGHE modify (DESCRIZIONE_ASSESSORATO varchar2 (4000))';
exception when others then null;
end;
/
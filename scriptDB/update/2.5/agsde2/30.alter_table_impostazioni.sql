--liquibase formatted sql
--changeset mfrancesconi:2.5_20230329_30.alter_table_impostazioni runOnChange:true

begin
    execute immediate 'alter table IMPOSTAZIONI modify (PREDEFINITO varchar2 (4000), VALORE VARCHAR2(4000))';
exception when others then null;
end;
/
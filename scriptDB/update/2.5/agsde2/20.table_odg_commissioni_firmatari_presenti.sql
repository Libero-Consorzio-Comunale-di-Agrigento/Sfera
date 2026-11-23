--liquibase formatted sql
--changeset mfrancesconi:2.5_20221019_20.table_odg_commissioni_firmatari_presenti runOnChange:true

begin
    execute immediate 'alter table odg_commissioni add (firmatari_presenti char (1) default ''Y'')';
exception when others then null;
end;
/
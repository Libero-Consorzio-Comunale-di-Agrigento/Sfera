--liquibase formatted sql
--changeset mfrancesconi:2.5_20220914_13.table_movimenti_anno_esigibilita runOnChange:true

begin
    execute immediate 'alter table movimenti_contabili add (anno_esigibilita number(19))';
exception when others then null;
end;
/
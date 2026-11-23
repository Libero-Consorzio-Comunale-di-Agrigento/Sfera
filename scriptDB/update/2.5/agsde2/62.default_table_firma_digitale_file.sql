--liquibase formatted sql
--changeset mfrancesconi:2.5_20240408_62.default_table_firma_digitale_file runOnChange:true stripComments:false failOnError:false

begin
   execute immediate 'alter table firma_digitale_file modify(posizione_firma default 0)';
exception
   when others then
      null;
end;
/
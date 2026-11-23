--liquibase formatted sql
--changeset mfrancesconi:2.5_20230123_26.indice_stato_conservazione runOnChange:true

begin
execute immediate 'create index det_stacon_ik on determine (stato_conservazione)';
exception
   when others then
      null;
end;
/

begin
execute immediate 'create index del_stacon_ik on delibere (stato_conservazione)';
exception
   when others then
      null;
end;
/
--liquibase formatted sql
--changeset mfrancesconi:2.5_20230421_38_aggiunta_indici runOnChange:true stripComments:false failOnError:false

begin
execute immediate 'create index desnotatt_idrif_ik on destinatari_notifiche_attivita (id_riferimento)';
exception when others then null;
end;
/

begin
execute immediate 'create unique index budget_pk on budget (id_budget)';
exception when others then null;
end;
/

begin
execute immediate 'alter table budget add (primary key(id_budget) using index budget_pk enable validate)';
exception when others then null;
end;
/
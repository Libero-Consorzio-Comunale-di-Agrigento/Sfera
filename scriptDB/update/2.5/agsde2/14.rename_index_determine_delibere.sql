--liquibase formatted sql
--changeset mfrancesconi:2.5_20220914_14.rename_index_determine_delibere runOnChange:true

declare
    d_nome_indice   varchar2 (1000);
begin
    select index_name
    into d_nome_indice
    from USER_IND_COLUMNS
    where COLUMN_NAME = 'ID_DETERMINA' and table_name = 'DETERMINE';

    execute immediate 'alter INDEX ' || d_nome_indice || ' rename to DETERMINE_PK';

    select index_name
    into d_nome_indice
    from USER_IND_COLUMNS
    where COLUMN_NAME = 'ID_DELIBERA' and table_name = 'DELIBERE';

    execute immediate 'alter INDEX ' || d_nome_indice || ' rename to DELIBERE_PK';
exception
   when others then
      null;
end;
/
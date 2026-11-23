--liquibase formatted sql
--changeset mfrancesconi:2.6_20260831_28.alter_table_determine_add_importo_totale runOnChange:true stripComments:false failOnError:false

begin
    execute immediate 'ALTER TABLE DETERMINE ADD(IMPORTO_TOTALE NUMBER(19,2))';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE DETERMINE_STORICO ADD(IMPORTO_TOTALE NUMBER(19,2))';
    exception when others then null;
end;
/
begin
    execute immediate 'ALTER TABLE PROPOSTE_DELIBERA ADD(IMPORTO_TOTALE NUMBER(19,2))';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE PROPOSTE_DELIBERA_STORICO ADD(IMPORTO_TOTALE NUMBER(19,2))';
    exception when others then null;
end;
/
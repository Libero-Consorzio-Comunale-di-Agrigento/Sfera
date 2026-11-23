--liquibase formatted sql
--changeset mfrancesconi:2.6_20260831_27.alter_table_tipologie_add_importo_totale runOnChange:true stripComments:false failOnError:false

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD(VIS_IMPORTO_TOTALE CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD(IMPORTO_TOTALE_OBBLIGATORIO CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD(VIS_IMPORTO_TOTALE CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD(IMPORTO_TOTALE_OBBLIGATORIO CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/

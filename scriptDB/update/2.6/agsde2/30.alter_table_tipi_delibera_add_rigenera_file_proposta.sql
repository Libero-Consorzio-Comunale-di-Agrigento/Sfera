--liquibase formatted sql
--changeset mfrancesconi:2.6_20261019_30.alter_table_tipi_delibera_add_rigenera_file_proposta runOnChange:true stripComments:false failOnError:false

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD(RIGENERA_FILE_PROPOSTA CHAR(1) DEFAULT ''N'' NOT NULL)';
    exception when others then null;
end;
/

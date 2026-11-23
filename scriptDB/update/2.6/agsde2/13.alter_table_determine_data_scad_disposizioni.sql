--liquibase formatted sql
--changeset mfrancesconi:2.6_20260402_13.alter_table_determine_data_scad_disposizioni runOnChange:true stripComments:false failOnError:false

begin
    execute immediate 'ALTER TABLE DETERMINE ADD(DATA_SCAD_DISPOSIZIONI DATE)';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE DETERMINE_STORICO ADD(DATA_SCAD_DISPOSIZIONI DATE)';
    exception when others then null;
end;
/
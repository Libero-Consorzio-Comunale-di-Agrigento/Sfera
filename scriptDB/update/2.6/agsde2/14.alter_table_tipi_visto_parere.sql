--liquibase formatted sql
--changeset mfrancesconi:2.6_20260417_14.alter_table_tipi_visto_parere runOnChange:true stripComments:false failOnError:false


begin
    execute immediate 'ALTER TABLE TIPI_VISTO_PARERE ADD (RICALCOLO_FIRMATARIO CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/
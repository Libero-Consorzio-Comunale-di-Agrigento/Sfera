--liquibase formatted sql
--changeset mfrancesconi:2.6_20260624_21.alter_table_tipi_certificato_add_edita_testo runOnChange:true stripComments:false failOnError:false

begin
    execute immediate 'ALTER TABLE TIPI_CERTIFICATO ADD(EDITA_TESTO CHAR(1) DEFAULT ''N'' NOT NULL)';
    exception when others then null;
end;
/

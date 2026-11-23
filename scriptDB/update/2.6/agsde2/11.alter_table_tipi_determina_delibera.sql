--liquibase formatted sql
--changeset mfrancesconi:2.6_20260327_11.alter_table_tipi_determina_delibera runOnChange:true stripComments:false failOnError:false


begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD (CONTABILITA_CHECK_RISERVATO CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD (CONTABILITA_CHECK_RISERVATO CHAR(1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD(VIS_DATA_SCAD_DISPOSIZIONE CHAR (1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD(GESTIONE_RISERVATO CHAR (1) DEFAULT ''Y'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD(GESTIONE_RISERVATO_ALLEGATI CHAR (1) DEFAULT ''Y'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD(GESTIONE_RISERVATO CHAR (1) DEFAULT ''Y'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD(GESTIONE_RISERVATO_ALLEGATI CHAR (1) DEFAULT ''Y'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD(IMPEGNO_OBBLIGATORIO CHAR (1) DEFAULT ''N'')';
    exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE TIPI_DELIBERA ADD(IMPEGNO_OBBLIGATORIO CHAR (1) DEFAULT ''N'')';
    exception when others then null;
end;
/
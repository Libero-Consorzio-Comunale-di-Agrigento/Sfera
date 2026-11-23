--liquibase formatted sql
--changeset mfrancesconi:2.5_20240607_67.alter_table_tipologie_add_tipo_firma runOnChange:true stripComments:false failOnError:false

alter table tipi_determina add (tipo_firma varchar2(255))
/

alter table tipi_delibera add (tipo_firma varchar2(255))
/

alter table tipi_visto_parere add (tipo_firma varchar2(255))
/
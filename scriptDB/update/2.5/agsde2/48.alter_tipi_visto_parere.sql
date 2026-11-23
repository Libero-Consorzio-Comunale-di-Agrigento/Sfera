--liquibase formatted sql
--changeset mfrancesconi:2.5_20231017_48.alter_table_tipi_visto_parere runOnChange:true stripComments:false failOnError:false

alter table tipi_visto_parere add ( testo_modificabile_no_iter char(1) default 'Y', multi_occorrenze char(1) default 'Y')
/
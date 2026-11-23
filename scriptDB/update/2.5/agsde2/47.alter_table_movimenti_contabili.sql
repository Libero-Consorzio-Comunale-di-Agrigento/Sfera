--liquibase formatted sql
--changeset mfrancesconi:2.5_20230928_47.alter_table_movimenti_contabili runOnChange:true stripComments:false failOnError:false

alter table movimenti_contabili add ( numero_programmazione varchar2(255), numero_impegno_padre varchar2(255))
/
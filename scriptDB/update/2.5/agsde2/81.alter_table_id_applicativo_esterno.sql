--liquibase formatted sql
--changeset amarinelli:2.5_20241125_81.alter_table_id_applicativo_esterno runOnChange:true stripComments:false failOnError:true

alter table DETERMINE add (ID_APPLICATIVO_ESTERNO varchar2(255))
/

alter table DELIBERE add (ID_APPLICATIVO_ESTERNO varchar2(255))
/


--liquibase formatted sql
--changeset amarinelli:2.5_20240717_69.alter_table_id_fascicolo_esterno runOnChange:true stripComments:false failOnError:true

alter table DETERMINE add (ID_FASCICOLO_ESTERNO varchar2(255))
/

alter table DELIBERE add (ID_FASCICOLO_ESTERNO varchar2(255))
/

alter table PROPOSTE_DELIBERA add (ID_FASCICOLO_ESTERNO varchar2(255))
/

alter table ODG_SEDUTE_STAMPE add (ID_FASCICOLO_ESTERNO varchar2(255))
/
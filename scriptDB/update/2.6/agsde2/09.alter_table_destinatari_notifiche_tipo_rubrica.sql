--liquibase formatted sql
--changeset amarinelli:2.6_20260217_09.09.alter_table_destinatari_notifiche_tipo_rubrica runOnChange:true stripComments:false failOnError:false

ALTER TABLE DESTINATARI_NOTIFICHE ADD (TIPO_RUBRICA_ESTERNA VARCHAR2(255))
/
--liquibase formatted sql
--changeset amarinelli:2.5_20241202_82.alter_table_pitre_template runOnChange:true stripComments:false failOnError:true

alter table PROTOCOLLO_PITRE_TEMPLATE add (PROTOCOLLA CHAR(1 BYTE) DEFAULT 'N')
/



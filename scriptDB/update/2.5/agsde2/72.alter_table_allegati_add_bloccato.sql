--liquibase formatted sql
--changeset amarinelli:2.5_20240828_72.alter_table_alelgati_add_bloccato runOnChange:true stripComments:false failOnError:true

ALTER TABLE ALLEGATI
 ADD (BLOCCATO  CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

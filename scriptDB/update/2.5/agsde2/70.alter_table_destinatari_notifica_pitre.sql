--liquibase formatted sql
--changeset amarinelli:2.5_20240809_70.70.alter_table_destinatari_notifica_pitre runOnChange:true stripComments:false failOnError:true

-- campi aggiunti per integrazione pitre:
ALTER TABLE DESTINATARI_NOTIFICHE ADD (ID_SOGGETTO_ESTERNO VARCHAR2(255))
/
ALTER TABLE DESTINATARI_NOTIFICHE ADD (TIPO_SOGGETTO_ESTERNO VARCHAR2(255))
/
ALTER TABLE DESTINATARI_NOTIFICHE ADD (TIPO_CORRISPONDENTE_ESTERNO VARCHAR2(255))
/

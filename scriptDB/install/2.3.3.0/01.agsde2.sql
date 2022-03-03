--liquibase formatted sql
--changeset rdestasio:2.3.3.0_20200221_01

ALTER TABLE FIRMA_DIGITALE_FILE ADD (ID_DOCUMENTO_GDM NUMBER)
/
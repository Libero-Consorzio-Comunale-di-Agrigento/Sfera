--liquibase formatted sql
--changeset rdestasio:2.1.5.0_20200221_01

ALTER TABLE TIPI_ALLEGATO ADD (PUBBLICA_ALBO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_ALLEGATO ADD (PUBBLICA_CASA_DI_VETRO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE ALLEGATI ADD (PUBBLICA_ALBO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE ALLEGATI ADD (PUBBLICA_CASA_DI_VETRO CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

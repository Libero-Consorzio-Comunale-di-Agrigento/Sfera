--liquibase formatted sql
--changeset rdestasio:2.5.0.0_20200221_01

ALTER TABLE CONTROLLO_REGOLARITA ADD (DATA_ESTRAZIONE DATE)
/


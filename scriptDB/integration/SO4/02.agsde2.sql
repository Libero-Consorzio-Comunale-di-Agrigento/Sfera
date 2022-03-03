--liquibase formatted sql
--changeset rdestasio:install_20200221_02

CREATE OR REPLACE SYNONYM SO4_INDIRIZZI_TELEMATICI FOR ${global.db.so4.username}.INDIRIZZI_TELEMATICI
/

CREATE OR REPLACE SYNONYM SO4_AOO_VIEW FOR ${global.db.so4.username}.AOO_VIEW
/

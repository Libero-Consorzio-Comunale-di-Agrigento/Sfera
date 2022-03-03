--liquibase formatted sql
--changeset rdestasio:install_20200221_senza_deleghe_01

create or replace synonym SO4_applicativi for so4_applicativi_fake
/
create or replace synonym SO4_COMPETENZE_DELEGA for so4_competenze_delega_fake
/
CREATE or replace SYNONYM so4_competenze_delega_tpk FOR so4_competenze_delega_tpk_fake
/

--liquibase formatted sql
--changeset rdestasio:install_20200221_deleghe_02

create or replace synonym SO4_DELEGHE for ${global.db.so4.username}.deleghe
/

create or replace synonym SO4_applicativi for ${global.db.so4.username}.applicativi
/

create or replace synonym SO4_COMPETENZE_DELEGA for ${global.db.so4.username}.COMPETENZE_DELEGA
/

CREATE or replace SYNONYM so4_competenze_delega_tpk FOR ${global.db.so4.username}.competenze_delega_tpk
/
